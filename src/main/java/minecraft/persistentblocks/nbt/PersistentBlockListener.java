package minecraft.persistentblocks.nbt;

import minecraft.persistentblocks.nbt.events.PersistentBlockBreakEvent;
import minecraft.persistentblocks.nbt.events.PersistentBlockMoveEvent;
import minecraft.persistentblocks.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class PersistentBlockListener implements Listener {

    private final PersistentBlockManager persistentBlockManager;
    private final Predicate<Block> persistentBlockFilter;

    public PersistentBlockListener(final @NotNull PersistentBlockManager persistentBlockManager) {
        this.persistentBlockManager = persistentBlockManager;
        this.persistentBlockFilter = persistentBlockManager::hasPersistentBlockData;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!persistentBlockManager.isDirty(block)) {
            callAndRemove(block, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.PISTON_HEAD) {
            PistonHead pistonHead = (PistonHead) event.getBlock().getBlockData();
            callAndRemove(Util.getPistonBase(event.getBlock(), pistonHead.getFacing()), event);
        } else {
            callAndRemove(event.getBlock(), event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        if (event.getTo() != event.getBlock().getType()) {
            callAndRemove(event.getBlock(), event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(final BlockExplodeEvent event) {
        callAndRemoveBlockList(event.blockList(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent event) {
        callAndRemoveBlockList(event.blockList(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBurn(final BlockBurnEvent event) {
        callAndRemove(event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFade(final BlockFadeEvent event) {
        if (event.getNewState().getType() != event.getBlock().getType()) {
            callAndRemove(event.getBlock(), event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructure(final StructureGrowEvent event) {
        callAndRemoveBlockStateList(event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFertilize(final BlockFertilizeEvent event) {
        callAndRemoveBlockStateList(event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTnt(final TNTPrimeEvent event) {
        callAndRemove(event.getBlock(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPiston(final BlockPistonExtendEvent event) {
        onPiston(event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSculkSpread(final BlockSpreadEvent event) {
        if(event.getNewState().getType() == Material.SCULK) {
            callAndRemove(event.getBlock(), event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPiston(final BlockPistonRetractEvent event) {
        onPiston(event.getBlocks(), event);
    }

    private void onPiston(final List<Block> blocks, final BlockPistonEvent bukkitEvent) {
        Map<Block, Block> map = new LinkedHashMap<>();
        BlockFace direction = bukkitEvent.getDirection();
        blocks.stream().filter(persistentBlockFilter).forEach(block -> {
            if (persistentBlockManager.isEmpty(block))
                return;
            Block destinationBlock = block.getRelative(direction);
            PersistentBlockMoveEvent moveEvent = new PersistentBlockMoveEvent(block, destinationBlock, bukkitEvent);
            Bukkit.getPluginManager().callEvent(moveEvent);
            if (moveEvent.isCancelled())
                return;
            map.put(destinationBlock, block);
        });

        Utils.reverse(map).forEach((destinationBlock, block) -> {
            persistentBlockManager.copyTo(destinationBlock, block);
            persistentBlockManager.clear(block);
        });
    }

    //todo: gravitiy blocks - BlockPhysicsEvent (Not very performance friendly)
    //todo: getPistonMoveReaktion, when moved by an piston, when the block break then the persistent data gets deleted
    //event.getBlock().getPistonMoveReaction()

    private void callAndRemove(final @NotNull Block block, final @NotNull Event bukkitEvent) {
        if (callEvent(block, bukkitEvent)) {
            persistentBlockManager.clear(block);
        }
    }

    private void callAndRemoveBlockList(final @NotNull List<Block> blocks, final @NotNull Event bukkitEvent) {
        blocks.stream()
                .filter(persistentBlockFilter)
                .forEach(block -> callAndRemove(block, bukkitEvent));
    }

    private void callAndRemoveBlockStateList(final List<BlockState> blockStates, final Event bukkitEvent) {
        blockStates.stream()
                .map(BlockState::getBlock)
                .filter(persistentBlockFilter)
                .forEach(block -> callAndRemove(block, bukkitEvent));
    }

    private boolean callEvent(final @NotNull Block block, final @NotNull Event bukkitEvent) {
        if (!persistentBlockFilter.test(block))
            return false;

        PersistentBlockBreakEvent persistentBlockBreakEvent = new PersistentBlockBreakEvent(block, bukkitEvent);
        Bukkit.getPluginManager().callEvent(persistentBlockBreakEvent);
        return !persistentBlockBreakEvent.isCancelled();
    }

    private static final class Utils {

        private static <K, V> Map<K, V> reverse(Map<K, V> map) {
            LinkedHashMap<K, V> reversed = new LinkedHashMap<>();
            List<K> keys = new ArrayList<>(map.keySet());
            Collections.reverse(keys);
            keys.forEach((key) -> reversed.put(key, map.get(key)));
            return reversed;
        }
    }
}

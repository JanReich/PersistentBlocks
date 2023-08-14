package minecraft.persistentblocks.listener;

import minecraft.persistentblocks.nbt.PersistentBlockManager;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class BlockListener implements Listener {

    private final NamespacedKey namespacedKey;
    private final PersistentBlockManager persistentBlockManager;

    public BlockListener(final @NotNull PersistentBlockManager persistentBlockManager,
                         final @NotNull NamespacedKey namespacedKey) {
        this.namespacedKey = namespacedKey;
        this.persistentBlockManager = persistentBlockManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent event) {
        persistentBlockManager.set(event.getBlock(), namespacedKey, PersistentDataType.STRING, "placed");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!persistentBlockManager.hasPersistentBlockData(block))
            return;
        if (persistentBlockManager.get(block, namespacedKey, PersistentDataType.STRING)
                .equalsIgnoreCase("placed")) {
            //Todo: only do something, when the block was placed by the Player
            event.getPlayer().sendMessage("Server: Warum baust du deine eigene Kreation ab?");
        }
    }
}

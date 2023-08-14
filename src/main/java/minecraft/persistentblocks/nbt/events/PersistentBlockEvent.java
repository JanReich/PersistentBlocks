package minecraft.persistentblocks.nbt.events;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Getter
public class PersistentBlockEvent extends Event implements Cancellable {

    private boolean isCancelled = false;

    private final Block block;
    private final Event bukkitEvent;
    private final static HandlerList HANDLERS = new HandlerList();

    public PersistentBlockEvent(final @NotNull Block block, final @NotNull Event bukkitEvent) {
        this.block = block;
        this.bukkitEvent = bukkitEvent;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @NotNull
    public Reason getReason() {
        for (Reason reason : Reason.values()) {
            if (reason == Reason.UNKNOWN) continue;
            if (reason.eventClasses.stream().anyMatch(clazz -> clazz.equals(bukkitEvent.getClass())))
                return reason;
        }
        return Reason.UNKNOWN;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public  enum Reason {
        BLOCK_BREAK(BlockBreakEvent.class),
        BLOCK_PLACE(BlockPlaceEvent.class, BlockMultiPlaceEvent.class),
        EXPLOSION(EntityExplodeEvent.class, BlockExplodeEvent.class),
        PISTON(BlockPistonExtendEvent.class, BlockPistonRetractEvent.class),
        BURN(BlockBurnEvent.class),
        ENTITY_CHANGE_BLOCK(EntityChangeBlockEvent.class),
        FADE(BlockFadeEvent.class),
        STRUCTURE_GROW(StructureGrowEvent.class),
        FERTILIZE(BlockFertilizeEvent.class),
        UNKNOWN((Class<? extends Event>) null);

        @NotNull
        private final List<Class<? extends Event>> eventClasses;

        @SafeVarargs
        Reason(Class<? extends Event>... eventClasses) {
            this.eventClasses = Arrays.asList(eventClasses);
        }

        @NotNull
        public List<Class<? extends Event>> getApplicableEvents() {
            return this.eventClasses;
        }
    }
}

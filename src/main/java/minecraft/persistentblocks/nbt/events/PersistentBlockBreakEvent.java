package minecraft.persistentblocks.nbt.events;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class PersistentBlockBreakEvent extends PersistentBlockEvent {

    public PersistentBlockBreakEvent(final @NotNull Block block, final @NotNull Event bukkitEvent) {
        super(block, bukkitEvent);
    }
}

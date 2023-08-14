package minecraft.persistentblocks.nbt.events;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Getter
public class PersistentBlockMoveEvent extends PersistentBlockEvent {

    private final @NotNull Block blockTo;

    public PersistentBlockMoveEvent(final @NotNull Block blockFrom, final Block blockTo, final @NotNull Event bukkitEvent) {
        super(blockFrom, bukkitEvent);

        this.blockTo = blockTo;
    }
}

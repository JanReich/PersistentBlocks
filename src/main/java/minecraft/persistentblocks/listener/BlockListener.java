package minecraft.persistentblocks.listener;

import minecraft.persistentblocks.nbt.PersistentBlockManager;
import minecraft.persistentblocks.nbt.events.PersistentBlockBreakEvent;
import org.bukkit.NamespacedKey;
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
    public void onPersistentBreak(final PersistentBlockBreakEvent event) {
        if (!(event.getBukkitEvent() instanceof BlockBreakEvent)) {
            return;
        }
        BlockBreakEvent causedBy = (BlockBreakEvent) event.getBukkitEvent();
        causedBy.getPlayer().sendMessage("Server: Warum baust du deine eigene Kreation ab?");
    }
}

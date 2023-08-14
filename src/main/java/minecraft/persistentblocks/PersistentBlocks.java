package minecraft.persistentblocks;

import minecraft.persistentblocks.commands.PersistentCommand;
import minecraft.persistentblocks.listener.BlockListener;
import minecraft.persistentblocks.nbt.PersistentBlockManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PersistentBlocks extends JavaPlugin {

    private NamespacedKey namespacedKey;
    private PersistentBlockManager persistentBlockManager;

    @Override
    public void onEnable() {
        Bukkit.getServer().getConsoleSender().sendMessage("§7[§6Persistent Blocks§7] Plugin wurde gestartet");

        init();
    }

    private void init() {
        namespacedKey = new NamespacedKey(this, "placed-block");
        persistentBlockManager = new PersistentBlockManager(this);

        BlockListener blockListener = new BlockListener(persistentBlockManager, namespacedKey);
        Bukkit.getPluginManager().registerEvents(blockListener, this);

        PersistentCommand persistentCommand = new PersistentCommand(persistentBlockManager);
        Objects.requireNonNull(getCommand("persistent")).setExecutor(persistentCommand);
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().getConsoleSender().sendMessage("§7[§6Persistent Blocks§7] wurde gestoppt");
    }
}

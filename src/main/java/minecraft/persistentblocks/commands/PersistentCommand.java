package minecraft.persistentblocks.commands;

import minecraft.persistentblocks.nbt.PersistentBlockManager;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PersistentCommand implements CommandExecutor {

    private final PersistentBlockManager persistentBlockManager;

    public PersistentCommand(final @NotNull PersistentBlockManager persistentBlockManager) {
        this.persistentBlockManager = persistentBlockManager;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
                             final @NotNull String label, final @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly player can perform this command!");
            return false;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("persistent.admin")) {
            player.sendMessage("§cYou dont have the permission to perform this action!");
        }
        if (args.length != 0) {
            sender.sendMessage("§cUsage: /persistent");
            return false;
        }
        player.sendMessage("§aIn your chunk are " +
                persistentBlockManager.getPersistentBlockInChunk(player.getChunk()) + " persistent blocks placed");
        return true;
    }
}

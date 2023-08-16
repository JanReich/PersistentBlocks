package minecraft.persistentblocks.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

public class Util {

    @NotNull
    public static Block getPistonBase(final Block pistonHead, final BlockFace blockFace) {
        if (pistonHead.getType() != Material.PISTON_HEAD) {
            return pistonHead;
        }

        switch (blockFace) {
            case UP:
                return pistonHead.getWorld().getBlockAt(pistonHead.getX(), pistonHead.getY() - 1, pistonHead.getZ());
            case DOWN:
                return pistonHead.getWorld().getBlockAt(pistonHead.getX(), pistonHead.getY() + 1, pistonHead.getZ());
            case NORTH:
                return pistonHead.getWorld().getBlockAt(pistonHead.getX(), pistonHead.getY(), pistonHead.getZ() + 1);
            case EAST:
                return pistonHead.getWorld().getBlockAt(pistonHead.getX() - 1, pistonHead.getY(), pistonHead.getZ());
            case SOUTH:
                return pistonHead.getWorld().getBlockAt(pistonHead.getX(), pistonHead.getY(), pistonHead.getZ() - 1);
            case WEST:
                return pistonHead.getWorld().getBlockAt(pistonHead.getX() + 1, pistonHead.getY(), pistonHead.getZ());
            default:
                return pistonHead;
        }
    }
}

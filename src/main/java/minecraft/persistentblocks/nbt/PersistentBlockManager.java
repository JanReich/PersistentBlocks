package minecraft.persistentblocks.nbt;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PersistentBlockManager {

    private final int CHUNK_MIN_XZ = 0;
    private final int CHUNK_MAX_XZ = 15;
    private final Pattern KEY_REGEX = Pattern.compile("^x(\\d+)y(-?\\d+)z(\\d+)$");

    private final Plugin plugin;
    private final Set<Map.Entry<UUID, BlockVector>> dirtyBlocks;
    private static final PersistentDataType<?, ?>[] PRIMITIVE_DATA_TYPES = new PersistentDataType<?, ?>[] {
            PersistentDataType.BYTE,
            PersistentDataType.SHORT,
            PersistentDataType.INTEGER,
            PersistentDataType.LONG,
            PersistentDataType.FLOAT,
            PersistentDataType.DOUBLE,
            PersistentDataType.STRING,
            PersistentDataType.BYTE_ARRAY,
            PersistentDataType.INTEGER_ARRAY,
            PersistentDataType.LONG_ARRAY,
            PersistentDataType.TAG_CONTAINER_ARRAY,
            PersistentDataType.TAG_CONTAINER
    };

    public PersistentBlockManager(final Plugin plugin) {
        this.plugin = plugin;
        dirtyBlocks = new HashSet<>();

        PersistentBlockListener persistentBlockListener = new PersistentBlockListener(this);
        Bukkit.getPluginManager().registerEvents(persistentBlockListener, plugin);
    }

    public <T, Z> void set(final @NotNull Block block, final @NotNull NamespacedKey namespacedKey,
                           final @NotNull PersistentDataType<T, Z> persistentDataType, final @NotNull Z z) {
        PersistentDataContainer persistentBlockContainer = getPersistentBlockContainer(block);
        persistentBlockContainer.set(namespacedKey, persistentDataType, z);
        save(block, persistentBlockContainer);
    }

    public <T, Z> boolean has(final @NotNull Block block, final @NotNull NamespacedKey namespacedKey,
                              final @NotNull PersistentDataType<T, Z> persistentDataType) {
        return getPersistentBlockContainer(block).has(namespacedKey, persistentDataType);
    }

    public <T, Z> Z get(final @NotNull Block block, final @NotNull NamespacedKey namespacedKey,
                        final @NotNull PersistentDataType<T, Z> persistentDataType) {
        return getPersistentBlockContainer(block).get(namespacedKey, persistentDataType);
    }

    public void clear(final @NotNull Block block) {
        PersistentDataContainer persistentBlockContainer = getPersistentBlockContainer(block);
        persistentBlockContainer.getKeys().forEach(persistentBlockContainer::remove);
        save(block, persistentBlockContainer);
    }

    /**
     * This method saves the PersistentDataContainer for the Block inside of the PersistentDataContainer from the Chunk
     * @param block The Block the data will be stored for is needed for getting the chunk of the Block
     * @param persistentBlockContainer The PersistentDataContainer of the Block
     */
    private void save(final @NotNull Block block, final @NotNull PersistentDataContainer persistentBlockContainer) {
        setDirty(getBlockEntry(block)); //todo: überarbeiten
        if (persistentBlockContainer.isEmpty()) {
            block.getChunk().getPersistentDataContainer().remove(getBlockKey(block));
        } else {
            block.getChunk().getPersistentDataContainer().set(getBlockKey(block),
                    PersistentDataType.TAG_CONTAINER, persistentBlockContainer);
        }
    }

    /**
     * This Method checks if the Chunk has an PersistentContainer for the @param block
     * @param block the Block that will be handeled
     * @return  true - if the Chunk has an PersistentContainer for the @param block
     *          false - if the Chunk has no PersistentContainer for the @param block
     */
    public boolean hasPersistentBlockData(final @NotNull Block block) {
        return block.getChunk().getPersistentDataContainer().has(getBlockKey(block), PersistentDataType.TAG_CONTAINER);
    }

    public boolean isEmpty(final @NotNull Block block) {
        return getPersistentBlockContainer(block).isEmpty();
    }

    public void copyTo(final Block destinationBlock, final Block block) {
        getPersistentBlockContainer(block).getKeys().forEach(namespacedKey -> {
            PersistentDataType dataType = getDataType(block, namespacedKey);
            if (dataType == null)
                return;
            set(destinationBlock, namespacedKey, dataType, get(block, namespacedKey, dataType));
        });
    }

    @Nullable
    public PersistentDataType<?, ?> getDataType(final @NotNull Block block, final @NotNull NamespacedKey namespacedKey) {
        for (PersistentDataType<?, ?> dataType : PRIMITIVE_DATA_TYPES) {
            if (getPersistentBlockContainer(block).has(namespacedKey, dataType)) {
                return dataType;
            }
        }
        return null;
    }

    public int getPersistentBlockInChunk(final @NotNull Chunk chunk) {
        return chunk.getPersistentDataContainer().getKeys().size();
    }

    @NotNull
    public Set<Block> getPersistentBlocks(final @NotNull Chunk chunk, @NotNull final NamespacedKey namespacedKey) {
        final PersistentDataContainer chunkContainer = chunk.getPersistentDataContainer();
        return chunkContainer.getKeys().stream().filter(key -> key.getNamespace()
                        .equals(namespacedKey.getNamespace())).map(key -> getBlockFromKey(chunk, key))
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Nullable
    public Block getBlockFromKey(final Chunk chunk, final NamespacedKey key) {
        final Matcher matcher = KEY_REGEX.matcher(key.getKey());
        if (!matcher.matches())
            return null;
        final int x = Integer.parseInt(matcher.group(1));
        final int y = Integer.parseInt(matcher.group(2));
        final int z = Integer.parseInt(matcher.group(3));
        if ((x < CHUNK_MAX_XZ || x > CHUNK_MAX_XZ) || (z < CHUNK_MIN_XZ || z > CHUNK_MAX_XZ) || (y < chunk.getWorld().getMinHeight() || y > chunk.getWorld().getMaxHeight() - 1))
            return null;
        return chunk.getBlock(x, y, z);
    }

    /**
     * This method will create an NamespacedKey for the @param block with the relative chunk coordinates
     * @param block the block there will created a NameSpacedKey for
     * @return NameSpacedKey withe the relative Chunk coordinates as String
     */
    @NotNull
    private NamespacedKey getBlockKey(final @NotNull Block block) {
        final int x = block.getX() & 0x000F;
        final int y = block.getY();
        final int z = block.getZ() & 0x000F;
        String relativeCoordinates = "x" + x + "y" + y + "z" + z;
        return new NamespacedKey(plugin, relativeCoordinates);
    }

    /**
     * This method gets an existing PersistentDataContainer out of the chunk or creates a new
     * one if there is no PersistentDataContainer existing for the @param block.
     * @param block - The block for that this method will get a PersistentDataContainer
     * @return PersistentDataContainer, if there is already a container for the block the existing
     * one will be returned
     */
    @NotNull
    private PersistentDataContainer getPersistentBlockContainer(final @NotNull Block block) {
        final PersistentDataContainer chunkContainer = block.getChunk().getPersistentDataContainer();
        final PersistentDataContainer blockContainer;
        NamespacedKey blockKey = getBlockKey(block);
        if (chunkContainer.has(blockKey, PersistentDataType.TAG_CONTAINER)) {
            blockContainer = chunkContainer.get(blockKey, PersistentDataType.TAG_CONTAINER);
            assert blockContainer != null;
            return blockContainer;
        }
        blockContainer = chunkContainer.getAdapterContext().newPersistentDataContainer();
        return blockContainer;
    }

    private void setDirty(final Map.Entry<UUID, BlockVector> blockEntry) {
        dirtyBlocks.add(blockEntry);
        Bukkit.getScheduler().runTask(plugin, () -> dirtyBlocks.remove(blockEntry));
    }

    public boolean isDirty(final @NotNull Block block) {
        return dirtyBlocks.contains(getBlockEntry(block));
    }

    @NotNull
    private Map.Entry<UUID, BlockVector> getBlockEntry(final @NotNull Block block) {
        final UUID uuid = block.getWorld().getUID();
        final BlockVector blockVector = new BlockVector(block.getX(), block.getY(), block.getZ());
        return new AbstractMap.SimpleEntry<>(uuid, blockVector);
    }
}

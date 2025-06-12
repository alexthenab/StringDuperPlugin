package alexthenab.stringDuperPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.persistence.PersistentDataType;

public class StringDuperPlugin extends JavaPlugin implements Listener {

    private final NamespacedKey duperKey = new NamespacedKey(this, "string_duper");
    private final Component DISPLAY_NAME = Component.text("String Duper");
    private final String DISPLAY_NAME_PLAIN = PlainTextComponentSerializer.plainText().serialize(DISPLAY_NAME);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        registerRecipe();
        startDuperTask();
    }

    private void registerRecipe() {
        ItemStack duper = new ItemStack(Material.DAYLIGHT_DETECTOR);
        ItemMeta meta = duper.getItemMeta();
        meta.displayName(DISPLAY_NAME);
        duper.setItemMeta(meta);

        ShapelessRecipe recipe = new ShapelessRecipe(duperKey, duper);
        recipe.addIngredient(Material.STRING);
        recipe.addIngredient(Material.STRING);
        recipe.addIngredient(Material.WATER_BUCKET);
        recipe.addIngredient(Material.WATER_BUCKET);
        recipe.addIngredient(Material.TRIPWIRE_HOOK);
        recipe.addIngredient(Material.TRIPWIRE_HOOK);
        recipe.addIngredient(Material.OAK_TRAPDOOR);
        recipe.addIngredient(Material.OAK_TRAPDOOR);
        recipe.addIngredient(Material.SHEARS);

        getServer().addRecipe(recipe);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.DAYLIGHT_DETECTOR && item.hasItemMeta()) {
            Component nameComponent = item.getItemMeta().displayName();
            String name = (nameComponent != null) ? PlainTextComponentSerializer.plainText().serialize(nameComponent) : "";
            if (name.equals(DISPLAY_NAME_PLAIN)) {
                Block block = event.getBlockPlaced();
                BlockState state = block.getState();
                if (state instanceof TileState tile) {
                    tile.getPersistentDataContainer().set(duperKey, PersistentDataType.BYTE, (byte) 1);
                    tile.update();
                }
            }
        }
    }

    private void startDuperTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        for (BlockState state : chunk.getTileEntities()) {
                            if (state instanceof TileState tile &&
                                    tile.getType() == Material.DAYLIGHT_DETECTOR &&
                                    tile.getPersistentDataContainer().has(duperKey, PersistentDataType.BYTE)) {
                                Block block = tile.getBlock();
                                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 1, 0.5),
                                        new ItemStack(Material.STRING, 64));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20 * 15, 20 * 15); // every 20 seconds
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        if (state instanceof TileState tile &&
                tile.getPersistentDataContainer().has(duperKey, PersistentDataType.BYTE)) {
            tile.getPersistentDataContainer().remove(duperKey);
        }
    }
}

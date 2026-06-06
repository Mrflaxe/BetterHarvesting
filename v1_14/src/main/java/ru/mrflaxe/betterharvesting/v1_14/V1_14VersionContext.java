package ru.mrflaxe.betterharvesting.v1_14;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;

import ru.mrflaxe.betterharvesting.version.VersionContext;

public class V1_14VersionContext implements VersionContext {

    public static final int[] MIN_VERSION = {1, 14, 0};

    @Override
    @SuppressWarnings("deprecation")
    public Enchantment getUnbreakingEnchantment() {
        return Enchantment.DURABILITY;
    }

    @Override
    public void playPlantSound(Location location, Material cropType) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        if (cropType == Material.NETHER_WART) {
            world.playSound(location, Sound.ITEM_NETHER_WART_PLANT, 1, 1);
        } else {
            world.playSound(location, Sound.ITEM_CROP_PLANT, 1, 1);
        }
    }
}

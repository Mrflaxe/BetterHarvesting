package ru.mrflaxe.betterharvesting.version;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

/**
 * Version-specific facade. Each supported Minecraft version range provides
 * its own implementation; the entry-point module picks the right one at runtime.
 */
public interface VersionContext {

    Enchantment getUnbreakingEnchantment();

    void playPlantSound(Location location, Material cropType);
}

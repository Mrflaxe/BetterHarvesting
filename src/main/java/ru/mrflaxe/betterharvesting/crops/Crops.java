package ru.mrflaxe.betterharvesting.crops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;

public class Crops {
    
    private final List<Material> cropsTypes;
    private final Map<Material, Material> cropsSeeds;
    
    public Crops() {
        this.cropsTypes = getCropsTypes();
        this.cropsSeeds = getCropsSeeds();
    }
    
    public boolean contains(Material type) {
        return cropsTypes.contains(type);
    }
    
    public void playPlantSound(Location location, Material cropType) {
        World world = location.getWorld();
        
        if(cropType == Material.NETHER_WART) world.playSound(location, Sound.ITEM_NETHER_WART_PLANT, 1, 1);
        else world.playSound(location, Sound.ITEM_CROP_PLANT, 1, 1);
    }
    
    /**
     * Gets seed material for crops material
     * @param cropsType - crops to get seeds type
     * @return seeds type for given crops
     * @throws IllegalArgumentException - if the given parameter is not valid or null
     */
    public Material getCropsSeedType(Material cropsType) throws IllegalArgumentException {
        try{
            return cropsSeeds.get(cropsType);
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException("Can't match value for param key 'cropsType'.");
        }
    }
    
    private List<Material> getCropsTypes() {
        List<Material> list = new ArrayList<>();
        
        list.add(Material.WHEAT);
        list.add(Material.CARROTS);
        list.add(Material.POTATOES);
        list.add(Material.NETHER_WART);
        list.add(Material.BEETROOTS);
        
        return list;
    }
    
    private Map<Material, Material> getCropsSeeds() {
        Map<Material, Material> map = new HashMap<>();
        
        map.put(Material.WHEAT, Material.WHEAT_SEEDS);
        map.put(Material.CARROTS, Material.CARROT);
        map.put(Material.POTATOES, Material.POTATO);
        map.put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
        map.put(Material.NETHER_WART, Material.NETHER_WART);
        
        return map;
    }
}

package ru.mrflaxe.betterharvesting;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Listener implements org.bukkit.event.Listener {
    
    private List<Material> cultures;
    
    public Listener() {
        cultures = getcultures();
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null) return;
        if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if(e.getMaterial() != Material.AIR) return;
        if(e.getHand() == EquipmentSlot.OFF_HAND) return;
        
        Material type = e.getClickedBlock().getType();
        if(!cultures.contains(type)) return;
        
        Block culture = e.getClickedBlock();
        Ageable data = (Ageable) culture.getBlockData();
        
        int age = data.getAge();
        if(age != data.getMaximumAge()) return;
        
        List<ItemStack> drops = (List<ItemStack>) culture.getDrops();
        
        int size = drops.size();
        ItemStack seeds = drops.get(size - 1);
        seeds.setAmount(seeds.getAmount() - 1);
        drops.set(size - 1, seeds);
        
        data.setAge(0);
        culture.setBlockData(data);
        
        playSound(culture, e.getPlayer());
        
        if(size != 0) {
            Location location = culture.getLocation();
            drops.forEach(item -> {
                System.out.println(item.toString());
                location.getWorld().dropItemNaturally(location, item);
            });
        }
    }
    
    private void playSound(Block culture, Player player) {
        Location loc = culture.getLocation();
        Material type = culture.getType();
        
        if(type == Material.NETHER_WART) player.playSound(loc, Sound.BLOCK_ROOTS_BREAK, 1, 1);
        else player.playSound(loc, Sound.BLOCK_CROP_BREAK, 1, 1);
    }
    
    private List<Material> getcultures() {
        List<Material> list = new ArrayList<>();
        
        list.add(Material.WHEAT);
        list.add(Material.CARROTS);
        list.add(Material.POTATOES);
        list.add(Material.NETHER_WART);
        list.add(Material.BEETROOTS);
        
        return list;
    }
    
    public void register(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

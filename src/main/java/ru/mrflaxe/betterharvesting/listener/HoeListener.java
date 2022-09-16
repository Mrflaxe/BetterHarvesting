package ru.mrflaxe.betterharvesting.listener;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

import ru.mrflaxe.betterharvesting.crops.Crops;

public class HoeListener implements Listener {
    
    private final Crops crops;
    
    public HoeListener() {
        this.crops = new Crops();
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null) {
            return;
        }
        
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        
        Material itemType = event.getMaterial();
        
        if(!itemType.toString().endsWith("_HOE")) {
            return;
        }
        
        Material blockType = event.getClickedBlock().getType();
        
        // Checks if block is a crop
        if(!crops.contains(blockType)) {
            return;
        }
        
        Block crop = event.getClickedBlock();
        Ageable data = (Ageable) crop.getBlockData();
        
        int age = data.getAge();
        
        // Check if a crop is grown
        if(age != data.getMaximumAge()) {
            return;
        }
        
        // Damaging the tool
        ItemStack hoe = event.getItem();
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        
        reduceDurability(player, hoe, hand);
        
        
        // Changes a drop
        List<ItemStack> drops = (List<ItemStack>) crop.getDrops(hoe);
        
        int size = drops.size();
        // A seeds always goes first in a collection
        int seedsIndex = getSeedsIndex(drops, blockType);
        
        ItemStack seeds = drops.get(seedsIndex);
        seeds.setAmount(seeds.getAmount() - 1);
        drops.set(seedsIndex, seeds);
        
        // Now just set age of crop to beginning
        data.setAge(0);
        crop.setBlockData(data);
        
        Location cropLocation = crop.getLocation();
        
        crops.playPlantSound(cropLocation, blockType);
        cropLocation.getWorld().playSound(cropLocation, Sound.ITEM_HOE_TILL, 1, 1);
        
        updatePlayerStatistic(player, hoe, blockType);
        addAdvancementIfHasNot(player);
        
        // And finally drops a harvested crops
        if(size != 0) {
            Location location = crop.getLocation();
            drops.forEach(item -> {
                if(item == null || item.getAmount() == 0 || item.getType().equals(Material.AIR)) {
                    return;
                }
                
                location.getWorld().dropItemNaturally(location, item);
            });
        }
    }
    
    private int getSeedsIndex(List<ItemStack> drops, Material cropType) {
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            Material seedTypeForGivenCrop = crops.getCropsSeedType(cropType);
            
            if(drop.getType().equals(seedTypeForGivenCrop)) {
                return i;
            }
        }
        
        return -1;
    }
    
    private void reduceDurability(Player player, ItemStack hoe, EquipmentSlot hand) {
        Damageable hoeMeta = (Damageable) hoe.getItemMeta();
        
        // If the hoe have unbreaking enchantment should to handle it
        // The chance not to reduce is range of int values. If random 'dice' gets in
        // this range durability will be decreased.
        int unbreakingLevel = hoe.getItemMeta().getEnchantLevel(Enchantment.DURABILITY);
        int chanceNotToReduce = 100 / (1 + unbreakingLevel);
        
        Random random = new Random();
        int dice = random.nextInt(100);
        
        // For each level of unbreaking chance not to reduce will gets lower and lower
        // When unbreaking level is 0 chance is 100%
        // When unbreaking level is 1 chance become 50% and etc.
        if(dice <= chanceNotToReduce) {
            int damage = hoeMeta.getDamage();
            int durability = hoe.getType().getMaxDurability() - damage;
            
            if(durability < 1) {
                player.getInventory().setItem(hand, new ItemStack(Material.AIR));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                player.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
                return;
            }
            
            hoeMeta.setDamage(damage + 1);
            hoe.setItemMeta(hoeMeta);
            
            player.getInventory().setItem(hand, hoe);
        }
    }
    
    private void updatePlayerStatistic(Player player, ItemStack hoe, Material cropsType) {
        int minedCount = player.getStatistic(Statistic.MINE_BLOCK, cropsType);
        int itemUseCount = player.getStatistic(Statistic.USE_ITEM, hoe.getType());
        
        minedCount++;
        itemUseCount++;
        
        player.setStatistic(Statistic.MINE_BLOCK, cropsType, minedCount);
        player.setStatistic(Statistic.USE_ITEM, hoe.getType(), itemUseCount);
    }
    
    private void addAdvancementIfHasNot(Player player) {
        NamespacedKey advancementKey = NamespacedKey.minecraft("husbandry/plant_seed");
        Advancement plantSeed = Bukkit.getAdvancement(advancementKey);
        AdvancementProgress progress = player.getAdvancementProgress(plantSeed);
        
        if(progress.isDone()) {
            return;
        }
        
        Collection<String> remainingCriteria = progress.getRemainingCriteria();
        for (String criterion : remainingCriteria) {
            progress.awardCriteria(criterion);
        }
    }
    
    public void register(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

package ru.mrflaxe.betterharvesting.listener;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.*;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import ru.mrflaxe.betterharvesting.crops.Crops;
import ru.mrflaxe.betterharvesting.version.VersionContext;

public class HoeListener implements Listener {

    private final Crops crops;
    private final VersionContext context;
    private final Random random = new Random();

    public HoeListener(VersionContext context) {
        this.context = context;
        this.crops = new Crops();
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Material itemType = event.getMaterial();

        if (!itemType.toString().endsWith("_HOE")) {
            return;
        }

        Material blockType = event.getClickedBlock().getType();

        if (!crops.contains(blockType)) {
            return;
        }

        Block crop = event.getClickedBlock();
        Ageable data = (Ageable) crop.getBlockData();

        int age = data.getAge();

        if (age != data.getMaximumAge()) {
            return;
        }

        ItemStack hoe = event.getItem();
        Player player = event.getPlayer();
        GameMode gameMode = player.getGameMode();
        EquipmentSlot hand = event.getHand();

        if (gameMode != GameMode.CREATIVE) {
            reduceDurability(player, hoe, hand);
        }

        @SuppressWarnings("unchecked")
        List<ItemStack> drops = (List<ItemStack>) crop.getDrops(hoe);

        int size = drops.size();
        int seedsIndex = getSeedsIndex(drops, blockType);

        if (seedsIndex >= 0) {
            ItemStack seeds = drops.get(seedsIndex);
            seeds.setAmount(seeds.getAmount() - 1);
            drops.set(seedsIndex, seeds);
        }

        data.setAge(0);
        crop.setBlockData(data);

        Location cropLocation = crop.getLocation();

        context.playPlantSound(cropLocation, blockType);
        cropLocation.getWorld().playSound(cropLocation, Sound.ITEM_HOE_TILL, 1, 1);

        updatePlayerStatistic(player, hoe, blockType);
        if (gameMode != GameMode.CREATIVE) {
            addAdvancementIfHasNot(player);
        }

        if (size != 0) {
            Location location = crop.getLocation();
            drops.forEach(item -> {
                if (item == null || item.getAmount() == 0 || item.getType().equals(Material.AIR)) {
                    return;
                }
                location.getWorld().dropItemNaturally(location, item);
            });
        }
    }

    private int getSeedsIndex(List<ItemStack> drops, Material cropType) {
        Material seedTypeForGivenCrop = crops.getCropsSeedType(cropType);
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (drop.getType().equals(seedTypeForGivenCrop)) {
                return i;
            }
        }
        return -1;
    }

    private void reduceDurability(Player player, ItemStack hoe, EquipmentSlot hand) {
        ItemMeta meta = hoe.getItemMeta();
        Damageable hoeMeta = (Damageable) meta;

        Enchantment unbreaking = context.getUnbreakingEnchantment();
        int unbreakingLevel = unbreaking == null ? 0 : meta.getEnchantLevel(unbreaking);
        int chanceNotToReduce = 100 / (1 + unbreakingLevel);

        int dice = random.nextInt(100);

        if (dice <= chanceNotToReduce) {
            int damage = hoeMeta.getDamage();
            int durability = hoe.getType().getMaxDurability() - damage;

            if (durability < 1) {
                replaceHeldItem(player, hand, new ItemStack(Material.AIR));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                return;
            }

            hoeMeta.setDamage(damage + 1);
            hoe.setItemMeta(meta);

            replaceHeldItem(player, hand, hoe);
        }
    }

    private void replaceHeldItem(Player player, EquipmentSlot hand, ItemStack item) {
        if (hand == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(item);
        } else {
            player.getInventory().setItemInMainHand(item);
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
        if (plantSeed == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancementProgress(plantSeed);

        if (progress.isDone()) {
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

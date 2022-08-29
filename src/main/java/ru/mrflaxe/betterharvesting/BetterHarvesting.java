package ru.mrflaxe.betterharvesting;

import org.bukkit.plugin.java.JavaPlugin;

import ru.mrflaxe.betterharvesting.listener.HoeListener;

public class BetterHarvesting extends JavaPlugin {
    
    
    @Override
    public void onEnable() {
        new HoeListener().register(this);
    }
    
}

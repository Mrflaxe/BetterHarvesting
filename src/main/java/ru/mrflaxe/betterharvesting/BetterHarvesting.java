package ru.mrflaxe.betterharvesting;

import org.bukkit.plugin.java.JavaPlugin;

public class BetterHarvesting extends JavaPlugin {
    
    
    @Override
    public void onEnable() {
        new Listener().register(this);
    }
    
}

package ru.mrflaxe.betterharvesting;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import ru.mrflaxe.betterharvesting.bootstrap.VersionResolver;
import ru.mrflaxe.betterharvesting.listener.HoeListener;
import ru.mrflaxe.betterharvesting.version.VersionContext;

public class BetterHarvesting extends JavaPlugin {

    @Override
    public void onEnable() {
        VersionContext context;
        try {
            context = VersionResolver.resolve(getLogger());
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Failed to resolve a version context. Plugin will be disabled.", t);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new HoeListener(context).register(this);
    }
}

package com.codisimus.plugins.phatloots.commands;

import com.codisimus.plugins.phatloots.PhatLoots;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

class CommandInjector {

    public static void inject(String commandName, String description, Consumer<PluginCommand> commandConsumer) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand pluginCommand = constructor.newInstance(commandName, PhatLoots.plugin);
            pluginCommand.setDescription(description);

            commandConsumer.accept(pluginCommand);

            Bukkit.getCommandMap().register(commandName, "phatloots", pluginCommand);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to construct PluginCommand " + commandName, e);
        }
    }
}

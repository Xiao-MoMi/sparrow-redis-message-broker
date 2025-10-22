package net.momirealms.sparrow.redis.messagebroker.plugin;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class SparrowRedisMessageBrokerBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(
                    Commands.literal("srmb")
                    .then(Commands.literal("benchmark").executes(commandContext -> {
                        Thread.ofVirtual().start(() -> SparrowRedisMessageBrokerPlugin.instance().startBenchMark());
                        return Command.SINGLE_SUCCESS;
                    }))
                    .build());
        });
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new SparrowRedisMessageBrokerPlugin(this);
    }
}

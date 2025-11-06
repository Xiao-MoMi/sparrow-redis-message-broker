package net.momirealms.sparrow.redis.messagebroker.plugin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.momirealms.sparrow.redis.messagebroker.plugin.example.PlayerCountRequestMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class SparrowRedisMessageBrokerBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(
                        Commands.literal("srmb")
                                .then(Commands.literal("benchmark").executes(commandContext -> {
                                    Thread.ofVirtual().start(() -> SparrowRedisMessageBrokerPlugin.instance().startBenchMark());
                                    return Command.SINGLE_SUCCESS;
                                }))
                                .then(Commands.literal("get-online-player-count").then(Commands.argument("server", StringArgumentType.greedyString()).executes(commandContext -> {
                                    String server = commandContext.getArgument("server", String.class);
                                    SparrowRedisMessageBrokerPlugin.instance().messageBroker().publishTwoWay(new PlayerCountRequestMessage(), server).thenAccept(response -> {
                                        commandContext.getSource().getSender().sendMessage(Component.text(response.playerCount()));
                                    });
                                    return Command.SINGLE_SUCCESS;
                                })))
                                .build()
                )
        );
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new SparrowRedisMessageBrokerPlugin(this);
    }
}

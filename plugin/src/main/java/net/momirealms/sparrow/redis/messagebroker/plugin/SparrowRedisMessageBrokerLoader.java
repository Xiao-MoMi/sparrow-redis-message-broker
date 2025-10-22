package net.momirealms.sparrow.redis.messagebroker.plugin;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class SparrowRedisMessageBrokerLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("io.lettuce:lettuce-core:6.8.1.RELEASE"), null));
        resolver.addRepository(new RemoteRepository.Builder("aliyun", "default", "https://maven.aliyun.com/repository/public/").build());
        classpathBuilder.addLibrary(resolver);
    }
}

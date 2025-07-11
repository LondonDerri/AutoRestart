package net.londonderri.autorestart;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.londonderri.autorestart.config.ConfigManager;
import net.londonderri.autorestart.holder.RestartDataHolder;
import net.londonderri.autorestart.init.Commands;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AutoRestart implements DedicatedServerModInitializer {
	public static final String MOD_ID = "autorestart";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static RestartDataHolder dataHolder;
	public static boolean isDisableShutdown = false;
	public static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
			.setNameFormat("AutoRestart-Executor-Thread-%d")
			.setDaemon(true)
			.build());

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
			try {
				setReadConfiguration(minecraftServer);
			} catch (Exception e) {
				LOGGER.error("Failed to read the mod configuration file: " + e.getMessage());
				dataHolder = null;
			}

			if (dataHolder != null) {
				if (dataHolder.isScriptEnabled()) {
					getShutdownHook();
				}
			}
		});

		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((minecraftServer, lifecycledResourceManager) -> {
			try {
				ConfigManager configManager = new ConfigManager();
				configManager.loadConfig().setup(minecraftServer);
				MutableText loaded = Text.literal("[").append(Text.literal("AutoRestart").formatted(Formatting.LIGHT_PURPLE).append(Text.literal("]").formatted(Formatting.WHITE)).append(Text.literal(" Successfully reloaded the configuration!").formatted(Formatting.GREEN)));
				minecraftServer.getPlayerManager().broadcast(loaded, MessageType.CHAT);
			} catch (Exception e) {
				MutableText failed = Text.literal("[").append(Text.literal("AutoRestart").formatted(Formatting.LIGHT_PURPLE).append(Text.literal("]").formatted(Formatting.WHITE)).append(Text.literal(" Failed to reboot the configuration!").formatted(Formatting.RED)));
				minecraftServer.getPlayerManager().broadcast(failed, MessageType.CHAT);
				LOGGER.error("It is not possible to restart the configuration file: " + e.getMessage());
			}
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer ->
				executorService.shutdown());

		ServerTickEvents.END_SERVER_TICK.register(Commands::onServerTick);

		CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) ->
				Commands.register(commandDispatcher)));
	}

	private void setReadConfiguration(MinecraftServer minecraftServer) throws Exception {
		ConfigManager configManager = new ConfigManager();
		if (!Files.isDirectory(Paths.get("./config"))) {
			Files.createDirectory(Paths.get("./config"));
		}

		if (!Files.exists(Paths.get("./config/autorestart.yml"))) {
			configManager.createConfig();
		}

		configManager.loadConfig().setup(minecraftServer);
	}

	public static void getShutdownHook() {
		Thread thread = new Thread(() -> {
			if (!isDisableShutdown) {
				String os = System.getProperty("os.name").toLowerCase();
				try {
					if (os.startsWith("windows")) {
						new ProcessBuilder("cmd.exe", "/c", "start " + dataHolder.getPathToScript()).start();
					} else new ProcessBuilder(dataHolder.getPathToScript()).start();
				} catch (IOException e) {
					LOGGER.error("Unable to restart the server: " + e.getMessage());
				}
			}
		});

		Runtime.getRuntime().addShutdownHook(thread);
	}
}
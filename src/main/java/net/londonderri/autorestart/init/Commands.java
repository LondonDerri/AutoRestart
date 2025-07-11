package net.londonderri.autorestart.init;

import com.mojang.brigadier.CommandDispatcher;
import net.londonderri.autorestart.AutoRestart;
import net.londonderri.autorestart.config.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Commands {
    private static final int SIZE_MSPT = 100;
    private static final long[] TICK_TIMES = new long[SIZE_MSPT];
    private static boolean isFirstTick = true;
    private static int tickIndex = 0;
    private static long lastTickTime = System.nanoTime();

    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(CommandManager.literal("restart").requires(serverCommandSource ->
                serverCommandSource.hasPermissionLevel(4)).executes(context ->
                restart(context.getSource())));
        commandDispatcher.register(CommandManager.literal("restart-scheduler").executes(context ->
                getTimeUntilRestart(context.getSource())));
        commandDispatcher.register(CommandManager.literal("tps").requires(serverCommandSource ->
                serverCommandSource.hasPermissionLevel(4)).executes(context ->
                displayTPS(context.getSource())));
    }

    private static int displayTPS(ServerCommandSource commandSource) {
        Config config = new Config();
        double mspt = calculationMSPT();
        double tps = Math.min(1000.0 / mspt, 20.0);

        MemoryUsage memoryUsageHeap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long memoryUsed = memoryUsageHeap.getUsed() / (1024 * 1024);
        long memoryMax = memoryUsageHeap.getMax() / (1024 * 1024);
        double percent = (double) memoryUsed / memoryMax * 100;
        int memoryUsedPercent = Math.toIntExact(Math.round(percent));

        commandSource.getServer().getPlayerManager().broadcast(Text.literal(String.format(config.tpsInfo, tps, mspt)), false);
        commandSource.getServer().getPlayerManager().broadcast(Text.literal(String.format(config.memoryInfo, memoryUsed, memoryMax, memoryUsedPercent)), false);
        return 0;
    }

    private static double calculationMSPT() {
        int mspt = Math.min(SIZE_MSPT, tickIndex);
        if (mspt == 0) {
            return 0.0;
        }

        long totalTime = 0;
        for (int i = 0; i < mspt; i++) {
            totalTime += TICK_TIMES[i];
        }

        return (totalTime / mspt) / 1_000_000.0;
    }

    private static int restart(ServerCommandSource commandSource) {
        if (AutoRestart.dataHolder == null || !AutoRestart.dataHolder.isScriptEnabled()) {
            AutoRestart.getShutdownHook();
        }

        commandSource.getServer().stop(false);
        return 0;
    }

    private static int getTimeUntilRestart(ServerCommandSource commandSource) {
        if (AutoRestart.dataHolder != null) {
            commandSource.getServer().getPlayerManager().broadcast(Text.literal("The next server reboot will be at " + LocalDateTime.ofEpochSecond(AutoRestart.dataHolder.getRestartTime() / 1000, 0, OffsetDateTime.now().getOffset()).format(DateTimeFormatter.ofPattern("HH:mm"))), false);
        } else {
            commandSource.getServer().getPlayerManager().broadcast(Text.literal("Server autoreboot disabled"), false);
        }

        return 0;
    }

    public static void onServerTick(MinecraftServer minecraftServer) {
        long currentTime = System.nanoTime();
        if (!isFirstTick) {
            long tickTime = currentTime - lastTickTime;
            TICK_TIMES[tickIndex % SIZE_MSPT] = tickTime;
            tickIndex++;
        } else {
            isFirstTick = false;
        }

        lastTickTime = currentTime;
    }
}

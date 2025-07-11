package net.londonderri.autorestart.holder;

import net.londonderri.autorestart.AutoRestart;
import net.londonderri.autorestart.config.Config;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;

public class RestartDataHolder {
    private long restartTime;
    private final long countdown;
    private MessagesDataHolder messagesDataHolder;
    private final String countdownMessage;
    private final boolean isEnableScript;
    private final String pathToScript;
    private final String disconnectMessage;
    private boolean triggered;
    private long lastCountdownBroadcast = 0;

    public RestartDataHolder(long restartTime, long countdown, HashMap<Long, String> messages, List<Long> timeList, Config config) {
        this.restartTime = restartTime;
        this.countdown = countdown;
        this.messagesDataHolder = new MessagesDataHolder(timeList, messages);
        this.countdownMessage = config.countdownMessage;
        this.isEnableScript = config.isEnableScript;
        this.pathToScript = config.pathToScript;
        this.disconnectMessage = config.disconnectMessage;
    }

    public void update(MinecraftServer server, long ms) {
        if (AutoRestart.dataHolder.isScriptEnabled()) {
            sendTimeMessage(server, ms);
            tryRestart(server, ms);
            tryCountdown(server, ms);
        }
    }

    private void tryCountdown(MinecraftServer server, long ms) {
        if (ms > countdown) {
            if (ms - lastCountdownBroadcast >= 1000) {
                server.getPlayerManager().broadcast(Text.literal(String.format(countdownMessage, (restartTime - ms) / 1000).replace(String.format(countdownMessage, 0), disconnectMessage)).formatted(Formatting.YELLOW), MessageType.CHAT);
                lastCountdownBroadcast = ms;
            }
        }
    }

    private void tryRestart(MinecraftServer server, long ms) {
        if (ms > restartTime) {
            shutdown(server);
        }
    }

    private void sendTimeMessage(MinecraftServer server, long ms) {
        messagesDataHolder.setNextMessage(server, ms);
    }

    private void shutdown(MinecraftServer server) {
        if (!triggered) {
            triggered = true;
            server.stop(false);
        }
    }

    public boolean isScriptEnabled() {
        return isEnableScript;
    }

    public String getPathToScript() {
        return pathToScript;
    }

    public long getRestartTime() {
        return restartTime;
    }
}

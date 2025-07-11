package net.londonderri.autorestart.config;

import net.londonderri.autorestart.AutoRestart;
import net.londonderri.autorestart.holder.RestartDataHolder;
import net.minecraft.server.MinecraftServer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

public class Config {
    public boolean isEnableScript;
    public String pathToScript;
    public List<String> timeRestart = new ArrayList<>();
    public int countdown;
    public String countdownMessage;
    public String restartScheduler;
    public String disconnectMessage;
    public String tpsInfo;
    public String memoryInfo;
    public HashMap<Long, String> messages = new HashMap<>();

    public Config() {
        this.countdown = 15;
        timeRestart.add("00:00");
        timeRestart.add("12:00");
        messages.put(600L, "The server will reboot via 10 minutes");
        messages.put(300L, "The server will reboot via 5 minutes");
        messages.put(180L, "The server will reboot via 3 minutes");
        messages.put(60L, "The server will reboot via 1 minute");
        this.pathToScript = "restart.bat";
        this.countdownMessage = "The server will restart in %s seconds";
        this.restartScheduler = "The next server reboot will be at";
        this.disconnectMessage = "The server is rebooting, please wait a couple of minutes...";
        this.isEnableScript = true;
        this.tpsInfo = "§eTPS: §a%.1f§f | §eMSPT: §a%.3f§e ms";
        this.memoryInfo = "§eRAM: §a%dMB §f/ §a%dMB §7(%d%%)";
    }

    public void setup(MinecraftServer server) throws Exception {
        ArrayList<Long> timeList = new ArrayList<>();
        LocalDateTime localDateTime = LocalDateTime.now();
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        long ms = localDateTime.toEpochSecond(offsetDateTime.getOffset()) * 1000;
        for (String times : this.timeRestart) {
            int index = times.indexOf(':');
            int hour = Integer.parseInt(times.substring(0, index));
            int minutes = Integer.parseInt(times.substring(index + 1));
            timeList.add(localDateTime.withHour(hour).withMinute(minutes).toEpochSecond(offsetDateTime.getOffset()) * 1000);
        }

        Collections.sort(timeList);
        boolean isDisableRestart = false;
        if (!timeList.isEmpty()) {
            timeList.add(timeList.get(0) + 86_400_000);
            Collections.sort(timeList);
        } else {
            isDisableRestart = true;
        }

        if (!isDisableRestart) {
            long restart = 0;
            for (long list : timeList) {
                if (ms < list) {
                    restart = list;
                    break;
                }
            }

            if (restart <= 0) {
                AutoRestart.LOGGER.error("The reboot time in the mod configuration cannot be less than or equal to 0!");
            }

            List<Long> messageTime = new ArrayList<>();
            for (long copyList : messages.keySet()) {
                messageTime.add(restart - (copyList * 1000));
            }

            Collections.sort(messageTime);
            HashMap<Long, String> messages = new HashMap<>();
            for (Map.Entry<Long, String> entry : messages.entrySet()) {
                messages.put(restart - (entry.getKey() * 1000), entry.getValue());
            }

            AutoRestart.dataHolder = new RestartDataHolder(restart, restart - (countdown + 1) * 1000L, messages, messageTime, this);
        }
    }
}

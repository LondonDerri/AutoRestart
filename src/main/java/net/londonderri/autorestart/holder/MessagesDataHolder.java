package net.londonderri.autorestart.holder;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.List;

public class MessagesDataHolder {
    private final List<Long> timeRestart;
    private long nextMessage;
    private int position;
    private boolean isDisableMessages;
    private final HashMap<Long, String> messages;

    public MessagesDataHolder(List<Long> timeRestart, HashMap<Long, String> messages) {
        this.timeRestart = timeRestart;
        this.messages = messages;
        setup();
    }

    public void setNextMessage(MinecraftServer server, long currentMillis) {
        if (!isDisableMessages && currentMillis > nextMessage) {
            server.getPlayerManager().broadcast(new LiteralText(messages.get(nextMessage)), MessageType.CHAT, Util.NIL_UUID);
            int pos = position + 1;
            if (pos < messages.size()) {
                nextMessage = timeRestart.get(pos);
                position = pos;
            } else {
                this.isDisableMessages = true;
            }
        }
    }

    private void setup() {
        long ms = System.currentTimeMillis();
        for (int res = 0; res < timeRestart.size(); res++) {
            long time = timeRestart.get(res);
            if (ms < time) {
                nextMessage = time;
                position = res;
                return;
            }
        }

        this.isDisableMessages = true;
    }
}

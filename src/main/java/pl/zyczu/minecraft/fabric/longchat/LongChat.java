package pl.zyczu.minecraft.fabric.longchat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class LongChat implements ModInitializer {

    public static final String MOD_ID = "longchat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing LongChat");

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ChatScreen chat) {
                for (var c : chat.children()) {
                    if (c instanceof EditBox box) {
                        box.setMaxLength(1000000);
                    }
                }
            }
        });
    }

    private static final ResourceLocation COMMAND_PART_PACKET = new ResourceLocation("longchat", "cmdpart");

    public static void sendLongCommand(String string) {
        var bytes = string.getBytes(StandardCharsets.UTF_8);
        int i = 0;
        while (i < bytes.length) {
            int j = Math.min(i+30000, bytes.length);
            int len = j-i;
            var buf = PacketByteBufs.create();
            buf.writeShort(len);
            buf.writeBytes(bytes, i, len);
            ClientPlayNetworking.send(COMMAND_PART_PACKET, buf);
            i = j;
        }
        var buf = PacketByteBufs.create();
        buf.writeShort(0);
        ClientPlayNetworking.send(COMMAND_PART_PACKET, buf); // 0 length = commit
    }

}

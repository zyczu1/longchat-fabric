package pl.zyczu.minecraft.fabric.longchat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.zyczu.minecraft.fabric.longchat.LongChat;

@Mixin(ChatScreen.class)
public class MixinChatScreen {

    @Inject(
            method = "handleChatInput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendCommand(Ljava/lang/String;)V"),
            require = 1,
            cancellable = true
    )
    private void onSendCommand(String string, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (string.length() > 256) {
            LongChat.sendLongCommand(string.substring(1));
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "handleChatInput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"),
            require = 1,
            cancellable = true
    )
    private void onSendChat(String string, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (string.length() > 256) {
            int i = 0;
            while (i < string.length()) {
                int j = Math.min(i+256, string.length());
                Minecraft.getInstance().player.connection.sendChat(string.substring(i, j));
                i = j;
            }
            cir.setReturnValue(true);
        }
    }

    /**
     * @author zyczu
     * @reason Disable chat message trimming
     */
    @Overwrite
    public String normalizeChatMessage(String string) {
        return StringUtils.normalizeSpace(string.trim());
    }

}

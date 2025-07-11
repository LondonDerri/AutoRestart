package net.londonderri.autorestart.mixin;

import net.londonderri.autorestart.AutoRestart;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixins {
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tick(Ljava/util/function/BooleanSupplier;)V", shift = At.Shift.AFTER))
    private void updateTrackers(CallbackInfo callbackInfo) {
        if (AutoRestart.dataHolder != null) {
            AutoRestart.dataHolder.update((MinecraftServer) (Object) this, System.currentTimeMillis());
        }
    }
}

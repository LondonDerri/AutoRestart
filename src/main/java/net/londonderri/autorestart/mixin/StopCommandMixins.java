package net.londonderri.autorestart.mixin;

import com.mojang.brigadier.context.CommandContext;
import net.londonderri.autorestart.AutoRestart;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.StopCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StopCommand.class)
public class StopCommandMixins {
    @Inject(method = "method_13676(Lcom/mojang/brigadier/context/CommandContext;)I", at = @At("HEAD"))
    private static void disableShutdownHook(CommandContext<ServerCommandSource> commandContext, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        AutoRestart.isDisableShutdown = true;
    }
}

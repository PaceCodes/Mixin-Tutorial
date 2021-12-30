package me.pace.client.mixin;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "createDisplay", at = @At("RETURN"))
    public void createDisplay(CallbackInfo callbackInfo) {
        Display.setTitle("Test Client 1.8.9: Spongepowered Mixins");
    }
}
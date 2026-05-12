package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {

    private static KeyMapping keyBinding;

    @Override
    public void onInitializeClient() {
        // Регистрация кнопки R (можно поменять GLFW_KEY_R на любую другую)
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Включить Автобай",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "FunTime Helper"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Проверка нажатия кнопки
            while (keyBinding.consumeClick()) {
                AutoBuy.enabled = !AutoBuy.enabled;
                
                if (client.player != null) {
                    String status = AutoBuy.enabled ? "§aВКЛ" : "§cВЫКЛ";
                    client.player.displayClientMessage(
                        Component.literal("§6[AutoBuy] §fСтатус: " + status), 
                        true
                    );
                }
            }

            // Запуск логики
            AutoBuy.onTick();
        });
    }
}

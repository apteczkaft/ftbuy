package com.example;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;

public class AutoBuy {
    private static final Minecraft mc = Minecraft.getInstance();
    
    // НАСТРОЙКИ
    public static boolean enabled = false;
    public static String targetName = "Талисман"; // Что ищем
    public static long maxPrice = 500000;         // Макс цена
    private static long lastClickTime = 0;

    public static void onTick() {
        if (!enabled || mc.screen == null || mc.player == null) return;

        if (mc.screen instanceof ContainerScreen<?> screen) {
            String title = screen.getTitle().getString();

            // 1. Поиск предмета на аукционе
            if (title.contains("Аукцион") || title.contains("Поиск")) {
                for (Slot slot : screen.getMenu().slots) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty() && stack.getHoverName().getString().contains(targetName)) {
                        if (getPriceFromLore(stack) <= maxPrice) {
                            executeClick(screen.getMenu().containerId, slot.index);
                        }
                    }
                }
            }

            // 2. Авто-подтверждение покупки (зеленая шерсть)
            if (title.contains("Подтверждение") || title.contains("Покупка")) {
                for (Slot slot : screen.getMenu().slots) {
                    String slotName = slot.getItem().getHoverName().getString();
                    if (slotName.contains("Подтвердить") || slotName.contains("Купить")) {
                        executeClick(screen.getMenu().containerId, slot.index);
                    }
                }
            }
        }
    }

    private static void executeClick(int containerId, int slotId) {
        // Задержка 200мс, чтобы античит не кикнул
        if (System.currentTimeMillis() - lastClickTime < 200) return;

        mc.getConnection().send(new ServerboundContainerClickPacket(
                containerId, 0, slotId, 0, ClickType.PICKUP,
                mc.screen.getMenu().getSlot(slotId).getItem(),
                new Int2ObjectOpenHashMap<>()
        ));
        lastClickTime = System.currentTimeMillis();
    }

    private static long getPriceFromLore(ItemStack stack) {
        try {
            // Берем описание предмета и вытаскиваем только цифры
            List<Component> lines = stack.getTooltipLines(null, mc.player, net.minecraft.world.item.TooltipFlag.Default.NORMAL);
            for (Component line : lines) {
                String text = line.getString().replaceAll("[^0-9]", "");
                if (!text.isEmpty()) {
                    long price = Long.parseLong(text);
                    // Обычно цена — это самое большое число в описании
                    if (price > 100) return price; 
                }
            }
        } catch (Exception ignored) {}
        return Long.MAX_VALUE;
    }
}

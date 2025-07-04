package su.nightexpress.economybridge.item.handler.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.item.ItemPlugins;
import su.nightexpress.economybridge.item.handler.AbstractItemHandler;

import java.lang.reflect.Method;

/**
 * Item handler for CraftEngine custom items. Uses reflection to avoid
 * mandatory compile-time dependency on CraftEngine API.
 */
public class CraftEngineHandler extends AbstractItemHandler {

    /* Cached reflection handles to avoid lookup cost each call */
    private static Class<?> ceItemsClass;
    private static Method   isCustomItemMethod;
    private static Method   getCustomItemIdMethod;
    private static Method   byIdMethod;
    private static Class<?> keyClass;
    private static Method   keyFromMethod;

    static {
        try {
            ceItemsClass = Class.forName("net.momirealms.craftengine.bukkit.api.CraftEngineItems");
            keyClass = Class.forName("net.momirealms.craftengine.core.util.Key");
            isCustomItemMethod = ceItemsClass.getMethod("isCustomItem", ItemStack.class);
            getCustomItemIdMethod = ceItemsClass.getMethod("getCustomItemId", ItemStack.class);
            byIdMethod = ceItemsClass.getMethod("byId", keyClass);
            keyFromMethod = keyClass.getMethod("from", String.class);
        } catch (Throwable ignored) {
            // CraftEngine not present – handler will act as unavailable
        }
    }

    private static boolean available() {
        return ceItemsClass != null;
    }

    @Override
    @NotNull
    public String getName() {
        return ItemPlugins.CRAFT_ENGINE;
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        if (!available()) return false;
        try {
            return (boolean) isCustomItemMethod.invoke(null, item);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isValidId(@NotNull String itemId) {
        if (!available()) return false;
        try {
            Object key = keyFromMethod.invoke(null, itemId);
            Object customItem = byIdMethod.invoke(null, key);
            return customItem != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Nullable
    public ItemStack createItem(@NotNull String itemId) {
        if (!available()) return null;
        try {
            Object key = keyFromMethod.invoke(null, itemId);
            Object customItem = byIdMethod.invoke(null, key);
            if (customItem == null) return null;
            // CustomItem extends BuildableItem and has buildItemStack()
            Method buildMethod = customItem.getClass().getMethod("buildItemStack");
            return (ItemStack) buildMethod.invoke(customItem);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Nullable
    public String getItemId(@NotNull ItemStack item) {
        if (!available()) return null;
        try {
            Object key = getCustomItemIdMethod.invoke(null, item);
            if (key == null) return null;
            return (String) keyClass.getMethod("asString").invoke(key);
        } catch (Exception e) {
            return null;
        }
    }
} 
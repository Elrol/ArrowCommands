package dev.elrol.arrow.commands.menus;

import dev.elrol.arrow.commands.registries.CommandsMenuItems;
import dev.elrol.arrow.libs.MenuUtils;
import dev.elrol.arrow.libs.ModTranslations;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ConfirmMenu extends _CommandMenuBase {
    String message = "confirm_message";
    String confirm = "confirm_accept";
    String cancel = "confirm_cancel";
    Runnable confirmCallback = ()->{};
    Runnable cancelCallback = ()->{};

    public ConfirmMenu(ServerPlayerEntity player) {
        super(player, ScreenHandlerType.GENERIC_9X5);
    }

    @Override
    protected void drawMenu() {
        super.drawMenu();

        Text cancelText = ModTranslations.err(cancel);
        Text confirmText = ModTranslations.msg(confirm);
        Text messageText = ModTranslations.info(message);

        setSlot(19, MenuUtils.item(CommandsMenuItems.RED_BUTTON_LEFT, 1, cancelText).setCallback(this::cancel));
        setSlot(20, MenuUtils.item(CommandsMenuItems.RED_BUTTON_MIDDLE, 1, cancelText).setCallback(this::cancel));
        setSlot(21, MenuUtils.item(CommandsMenuItems.RED_BUTTON_RIGHT, 1, cancelText).setCallback(this::cancel));

        setSlot(23, MenuUtils.item(CommandsMenuItems.LIME_BUTTON_LEFT, 1, confirmText).setCallback(this::confirm));
        setSlot(24, MenuUtils.item(CommandsMenuItems.LIME_BUTTON_MIDDLE, 1, confirmText).setCallback(this::confirm));
        setSlot(25, MenuUtils.item(CommandsMenuItems.LIME_BUTTON_RIGHT, 1, confirmText).setCallback(this::confirm));

        setSlot(40, MenuUtils.item(CommandsMenuItems.LIGHT_GRAY_BUTTON, 1, messageText));
    }

    @Override
    public int getMenuID() {
        return 21;
    }

    @Override
    public @NotNull String getMenuName() {
        return "confirm";
    }

    @Override
    public char getMenuUnicode() {
        return '≕';
    }

    private void confirm() {
        if(this.confirmCallback != null) {
            click();
            confirmCallback.run();
        }
    }

    private void cancel() {
        if(this.cancelCallback != null) {
            click();
            cancelCallback.run();
        }
    }

    public void setRunnable(Runnable confirmCallback, Runnable cancelCallback) {
        if(confirmCallback != null) this.confirmCallback = confirmCallback;
        if(cancelCallback != null) this.cancelCallback = cancelCallback;
    }

    public void init(String message, Runnable confirmCallback, Runnable cancelCallback) {
        this.message = message;
        setRunnable(confirmCallback,cancelCallback);
    }

    public void init(String message, String confirm, String cancel, Runnable confirmCallback, Runnable cancelCallback) {
        this.confirm = confirm;
        this.cancel = cancel;
        init(message, confirmCallback, cancelCallback);
    }
}

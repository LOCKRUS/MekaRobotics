package com.io.mekarobotics.common.capabilities;

import com.io.mekarobotics.common.helpers.util.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

public class CommandApplyException extends RuntimeException {

    private final String unlocalized_message;

    public CommandApplyException(String unlocalized_msg, Object... args) {
        super(Lang.localise(unlocalized_msg, args).toString());
        this.unlocalized_message = unlocalized_msg;
    }

    public MutableComponent getErrorMessage() {
        MutableComponent msg = Component.literal("CommandApplyException: ");
        msg.append(Lang.localise(unlocalized_message));
        return msg.setStyle(msg.getStyle().withColor(TextColor.fromLegacyFormat(ChatFormatting.RED)));
    }
}

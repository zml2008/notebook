package xyz.aoeu.notebook;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.command.CommandSource;

import java.util.function.Consumer;

public class Messages {
    public static final Text ID_SPACER = Texts.of(") ");
    private Messages() {}

    public static TextBuilder normal(TextBuilder input) {
        return input.color(TextColors.DARK_AQUA);
    }

    public static TextBuilder hl(TextBuilder input) {
        return input.color(TextColors.AQUA);
    }

    public static TextBuilder error(TextBuilder input) {
        return input.color(TextColors.RED);
    }

    public static TextBuilder callback(TextBuilder input, Consumer<CommandSource> callback) {
        return input.onClick(TextActions.executeCallback(callback))
                .style(TextStyles.UNDERLINE);
    }
}

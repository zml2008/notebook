package xyz.aoeu.notebook;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.function.Consumer;

public class Messages {
    public static final Text ID_SPACER = Text.of(") ");
    private Messages() {}

    public static Text.Builder normal(Text.Builder input) {
        return input.color(TextColors.DARK_AQUA);
    }

    public static Text.Builder hl(Text.Builder input) {
        return input.color(TextColors.AQUA);
    }

    public static Text.Builder error(Text.Builder input) {
        return input.color(TextColors.RED);
    }

    public static Text.Builder callback(Text.Builder input, Consumer<CommandSource> callback) {
        return input.onClick(TextActions.executeCallback(callback))
                .style(TextStyles.UNDERLINE);
    }
}

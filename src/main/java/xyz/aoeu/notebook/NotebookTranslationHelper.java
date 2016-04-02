package xyz.aoeu.notebook;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.ResourceBundleTranslation;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;

class NotebookTranslationHelper {
    private static final Function<Locale, ResourceBundle> LOOKUP_FUNC = new Function<Locale, ResourceBundle>() {
        @Nullable
        @Override
        public ResourceBundle apply(Locale input) {
            return ResourceBundle.getBundle("xyz.aoeu.notebook.Translations", input);
        }
    };

    private NotebookTranslationHelper() {} // Prevent instance creation

    public static Text.Builder t(String key, Object... args) {
        return Text.builder(new ResourceBundleTranslation(key, LOOKUP_FUNC), args);
    }
}

package xyz.aoeu.notebook;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.command.CommandPermissionException;

public class Permissions {
    public static final String NOTEBOOK_VIEW = "notebook.view";
    public static final String NOTEBOOK_CREATE = "notebook.create";
    public static final String NOTEBOOK_CREATE_BOOK = "notebook.create.book";
    public static final String NOTEBOOK_EDIT = "notebook.edit";

    private Permissions() {}

    public static String owned(String root, Subject subject, User target) {
        if (subject == target) {
            return root + ".own";
        } else {
            return root + ".other." + target.getUniqueId();
        }
    }

    public static void check(Subject subj, String permission) throws CommandPermissionException {
        if (!subj.hasPermission(permission)) {
            throw new CommandPermissionException();
        }

    }
}

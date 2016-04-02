package xyz.aoeu.notebook;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.PEBKACException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.spongepowered.api.command.args.GenericArguments.*;
import static xyz.aoeu.notebook.Messages.*;
import static xyz.aoeu.notebook.NotebookTranslationHelper.t;

/**
 * A simple sponge plugin
 */
@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION)
public class NotebookPlugin {

    // These are all injected on plugin load for users to work from
    @Inject private Logger logger;
    // Give us a configuration to work from
    @Inject @DefaultConfig(sharedRoot = true) private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private NotebookConfiguration config;
    @Inject private Game game;
    private SqlService sql;
    private PaginationService pagination;
    private Executor asyncExecutor;

    private NotebookStorage storage;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        try {
            ConfigurationNode node = configLoader.load();
            this.config = node.getValue(NotebookConfiguration.TYPE);
            if (this.config == null) {
                this.config = new NotebookConfiguration();
                node.setValue(NotebookConfiguration.TYPE, this.config);
            }
            configLoader.save(node);
        } catch (IOException e) {
            throw new PEBKACException("Unable to load configuration", e);
        } catch (ObjectMappingException e) {
            throw new PEBKACException("Error interpreting configuration data", e);
        }
        this.sql = game.getServiceManager().provideUnchecked(SqlService.class);
        this.pagination = game.getServiceManager().provideUnchecked(PaginationService.class);
        this.asyncExecutor = task -> game.getScheduler().createTaskBuilder().async().execute(task).submit(this);
        try {
            this.storage = new SqlNotebookStorage(this.sql.getDataSource(this.config.getConnectionUrl()), asyncExecutor);
        } catch (SQLException e) {
            throw new PEBKACException("Error connecting to SQL database", e);
        }


        game.getCommandManager().register(this, CommandSpec.builder()
                .arguments(onlyOne(playerOrSource(t("command.notes.arg.source").build())))
                .executor((src, args) -> {
                    User target = args.<User>getOne("command.notes.arg.source").orElseThrow(IllegalStateException::new);
                    Permissions.check(src, Permissions.owned(Permissions.NOTEBOOK_VIEW, src, target));
                    src.sendMessage(normal(t("command.notes.result.fetching")).build());
                    game.getScheduler().createTaskBuilder()
                            .async()
                            .execute(() -> {
                                Iterable<Note> notes = getStorage().getNotes(target.getUniqueId());
                                this.pagination.builder()
                                        .contents(Iterables.transform(notes, note -> {
                                                    return normal(Text.builder())
                                                            .append(hl(Text.builder(String.valueOf(note.getId())).append(ID_SPACER)).build(), // ID
                                                                    Text.of(note.getContents())).build(); // Contents
                                                }))
                                        .title(normal(t("command.notes.result.header", hl(Text.builder(target.getName())).build())).build())
                                        .sendTo(src);

                            }).submit(this);
                    return CommandResult.empty(); // Unknown success value

                })
                .build(), "notes");
        game.getCommandManager().register(this, CommandSpec.builder()
                .arguments(flags()
                        .flag("b") // Add from book
                        .valueFlag(integer(t("command.note.arg.id").build()), "e")
                        .valueFlag(onlyOne(playerOrSource(t("command.note.arg.owner").build())), "o")
                        .buildWith(remainingJoinedStrings(t("command.note.arg.contents").build()))
                )
                .executor((src, args) -> {
                    String message = args.<String>getOne("command.note.arg.contents").get();
                    final Optional<User> ownerOpt = args.<User>getOne("command.note.arg.owner");
                    final User owner;
                    if (ownerOpt.isPresent()) {
                        owner = ownerOpt.get();
                    } else {
                        owner = checkPlayer(src);
                    }
                    final boolean getFromBook = args.<Boolean>getOne("b").orElse(false);
                    final Optional<Integer> editId = args.<Integer>getOne("e");
                    if (getFromBook) {
                        throw new CommandException(t("command.note.error.book_nyi").build());
                        // TODO: Update the message with the currently held book contents
                    }

                    if (editId.isPresent()) {
                        throw new UnsupportedOperationException(); // TODO
                    } else {
                        Permissions.check(src, Permissions.owned(getFromBook ? Permissions.NOTEBOOK_CREATE_BOOK : Permissions.NOTEBOOK_CREATE, src, owner));
                    }

                    getStorage().insertNote(new Note(owner.getUniqueId(), message)).handle((note, err) -> {
                        if (err != null) {
                            src.sendMessage(error(t("command.note.error.exception")).build());
                            logger.error("Error creating note for user " + src.getName(), err);
                        } else {
                            src.sendMessage(normal(t("command.note.success.added")).build());
                        }
                        return note;
                    });

                    return CommandResult.success();
                })
                .build(), "note");
        // Perform initialization tasks here
    }

    private Player checkPlayer(CommandSource src) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(t("command.generic.error.source_not_player").build());
        }
        return ((Player) src);
    }


    @Listener
    public void disable(GameStoppingServerEvent event) {
        // Perform shutdown tasks here
    }

    public NotebookStorage getStorage() {
        return this.storage;
    }
}

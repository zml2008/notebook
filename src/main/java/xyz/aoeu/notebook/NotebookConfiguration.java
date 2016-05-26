package xyz.aoeu.notebook;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class NotebookConfiguration {
    public static final TypeToken<NotebookConfiguration> TYPE = TypeToken.of(NotebookConfiguration.class);

    @Setting("connection-url") private String connectionUrl = "h2:./config/notebook/notes.db";

    public String getConnectionUrl() {
        return connectionUrl;
    }
}

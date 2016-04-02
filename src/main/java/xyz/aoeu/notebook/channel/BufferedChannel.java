package xyz.aoeu.notebook.channel;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A channel with a fixed-size buffer
 */
class BufferedChannel<T> implements Channel<T> {
    private static final int DEFAULT_BUFFER = 16;
    private Object[] buffer;


    BufferedChannel(int bufferSize) {
        this.buffer = new Object[bufferSize + 1];
    }

    BufferedChannel() {
        this(DEFAULT_BUFFER);
    }

    @Override
    public void provide(T object) throws ChannelClosedException {

    }

    @Override
    public boolean offer(T object) {
        return false;
    }

    @Override
    public Stream<T> stream() {
        return null;
    }

    @Override
    public void close() {

    }

    //TODO: Block on hasNext to determine if channel is closed?
    @Override
    public Iterator<T> iterator() {
        return null;
    }
}

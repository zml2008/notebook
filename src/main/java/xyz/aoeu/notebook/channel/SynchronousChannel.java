package xyz.aoeu.notebook.channel;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by zml on 28.11.15.
 */
public class SynchronousChannel<T> implements Channel<T> {
    private static final Object POISON = new Object();
    private Object buffer;


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

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super T> action) {

    }

    @Override
    public Spliterator<T> spliterator() {
        return null;
    }
}

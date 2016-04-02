package xyz.aoeu.notebook.channel;

import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Empty channel that starts out closed and holds no data.
 */
class EmptyChannel<T> implements Channel<T> {
    static EmptyChannel INSTANCE = new EmptyChannel();

    @Override
    public void provide(T object) throws ChannelClosedException {
        throw new ChannelClosedException();
    }

    @Override
    public boolean offer(T object) {
        return false;
    }

    @Override
    public Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.emptyIterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.emptySpliterator();
    }
}

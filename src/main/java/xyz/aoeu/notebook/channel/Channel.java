package xyz.aoeu.notebook.channel;

import java.util.concurrent.SynchronousQueue;
import java.util.stream.Stream;

/**
 * A channel is a data structure that allows message passing between threads.
 */
public interface Channel<T> extends Iterable<T>, AutoCloseable {

    /**
     * Create a new channel with a fixed-size buffer
     *
     * @param bufferSize The buffer size to use
     * @return A new channel
     */
    static <T> Channel<T> buffered(int bufferSize) {
        return new BufferedChannel<>(bufferSize);
    }

    /**
     * Create a new channel with a fixed-sized buffer. This size is unknown, but greater than 1.
     *
     * @return A new channel
     */
    static <T> Channel<T> buffered() {
        return new BufferedChannel<>();
    }

    /**
     * Create a channel that will directly pass an object. This behaves similar to a {@link SynchronousQueue}
     *
     * @return A new channel
     */
    static <T> Channel<T> synchronous() {
        return null;
    }

    /**
     * Create a ne channel that is already closed, so no elements may be passed to it.
     * @return A new channel
     */
    @SuppressWarnings("unchecked")
    static <T> Channel<T> empty() {
        return EmptyChannel.INSTANCE;
    }

    /**
     * Provide an object to this channel. This method will block if the queue is full
     * @param object The object to provide
     */
    void provide(T object) throws ChannelClosedException;

    /**
     * Offer an object to this channel. This method will return false if the queue is full
     * @param object The object to offer
     * @return Whether the offer was successful
     */
    boolean offer(T object);

    Stream<T> stream();

    /**
     * Close this channel. After this occurs, no new elements will be accepted and an end will be attached to iterators.
     */
    void close();
}

package me.itzg.utils.io;

import java.nio.ByteBuffer;

/**
 * Since ByteBuffers themselves derive their hashCode based on content, this wrapper instead uses the
 * buffer object's instance as the hash/equality for {@link java.util.Map#put(Object, Object)} usage.
 */
class ByteBufferInstance {
    final ByteBuffer buffer;

    ByteBufferInstance(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(buffer);
    }

    @Override
    public boolean equals(Object obj) {
        return this.buffer == obj;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}

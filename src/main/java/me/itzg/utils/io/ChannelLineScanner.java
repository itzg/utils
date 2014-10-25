package me.itzg.utils.io;

import me.itzg.utils.UsedExternally;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Provides an efficient traversal of a file line-by-line with the extra benefit that will tell the
 * observer the exact file offset of the line's starting position.
 *
 * @author Geoff Bourne
 * @since  10/19/2014
 */
public class ChannelLineScanner {

    private int maxLineSize = 500;

    private int bufferSize = 1024;

    private Charset charset = Charset.forName("UTF-8");

    private char[] delimiter = new char[]{'\n'};

    @UsedExternally
    public int getMaxLineSize() {
        return maxLineSize;
    }

    public void setMaxLineSize(int maxLineSize) {
        this.maxLineSize = maxLineSize;
    }

    @UsedExternally
    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setDelimiter(String delimiterSequence) {
        delimiter = delimiterSequence.toCharArray();
    }

    @UsedExternally
    public String getDelimiter() {
        return new String(delimiter);
    }

    public void scan(ReadableByteChannel channel, Observer observer) throws IOException {

        LinkedList<ByteBuffer> buffers = new LinkedList<>(
                Arrays.asList(ByteBuffer.allocate(bufferSize), ByteBuffer.allocate(bufferSize)));

        int amountRead;
        long totalAmountRead = 0;
        ByteBuffer remainder = null;

        while ((amountRead = channel.read(buffers.getFirst())) != -1) {
            if (amountRead > 0) {
                long bufferPositionInFile = totalAmountRead;
                totalAmountRead += amountRead;
                int previousNewLinePos = 0;

                ByteBuffer activeBuffer = buffers.getFirst();
                activeBuffer.flip();
                CharBuffer charBuffer = charset.newDecoder().decode(activeBuffer);

                while (charBuffer.hasRemaining()) {
                    int newLinePos = scanForNewLine(charBuffer);

                    if (newLinePos != -1) {
                        CharBuffer line = charBuffer.duplicate();

                        line.position(previousNewLinePos)
                            .limit(newLinePos);

                        observer.observeLine(line, bufferPositionInFile+previousNewLinePos);

                        // grab position AFTER the delimiter
                        previousNewLinePos = charBuffer.position();
                    }
                }

                remainder = activeBuffer.duplicate();
                remainder.position(previousNewLinePos);

                // copy over the remainder to the carry-over buffer
                buffers.getLast().put(activeBuffer);
                // prep active to become carry-over
                activeBuffer.clear();
                // swap carry-over for active
                buffers.addFirst(buffers.removeLast());
            }

        }

        // and check for a remaining line if file didn't end with line delimiter
        if (remainder != null && remainder.hasRemaining()) {
            int sizeOfRemainder = remainder.remaining();
            CharBuffer charBuffer = charset.newDecoder().decode(remainder);
            observer.observeLine(charBuffer,
                    totalAmountRead - sizeOfRemainder);
        }
    }

    /**
     *
     * @param buffer the current file chunk positioned at the start or just past the last
     *               delimiter
     * @return the position before the delimiter
     * @throws IOException
     */
    private int scanForNewLine(CharBuffer buffer) throws IOException {
        int lookingAt = 0;
        int prePosition = buffer.position();
        while (buffer.remaining() > 0) {
            char c = buffer.get();
            if (c == delimiter[lookingAt++]) {
                if (lookingAt >= delimiter.length) {
                    return prePosition;
                }
            }
            else {
                prePosition = buffer.position();
                lookingAt = 0;
            }
        }
        return -1;
    }

    public interface Observer {

        void observeLine(CharSequence line, long position);
    }
}

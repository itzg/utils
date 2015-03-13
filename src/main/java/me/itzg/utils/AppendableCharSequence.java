package me.itzg.utils;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * This performs zero-copy appending of {@link java.lang.CharSequence}s into a unified instance
 * that supports all the usual operations such as {@link java.lang.CharSequence#charAt(int)} and
 * {@link java.lang.CharSequence#subSequence(int, int)} arbitrarily across the appended content
 * also in a zero-copy manner.
 *
 * @author Geoff Bourne
 * @since 12/6/2014
 */
public class AppendableCharSequence implements CharSequence, Appendable {
    private TreeMap<Integer, CharSequence> sequences = new TreeMap<>();

    // total running length
    private int length;

    // used to optimize repeated, sequential calls to charAt
    private Map.Entry<Integer, CharSequence> cachedEntry;
    private int endOfCachedEntry;

    public AppendableCharSequence() {
    }

    public AppendableCharSequence(CharSequence initialContent) {
        append(initialContent);
    }

    /**
     * Appends the given {@link java.lang.CharSequence} onto the content of this instance.
     * @param charSequence the content to append
     * @return itself to enable method chaining
     */
    public AppendableCharSequence append(CharSequence charSequence) {

        sequences.put(length, charSequence);
        length += charSequence.length();

        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        return append(csq.subSequence(start, end));
    }

    @Override
    public Appendable append(char c) throws IOException {
        return append(String.valueOf(c));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppendableCharSequence that = (AppendableCharSequence) o;

        if (!sequences.equals(that.sequences)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sequences.hashCode();
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CharSequence seq : sequences.values()) {
            sb.append(seq);
        }
        return sb.toString();
    }

    @Override
    public char charAt(int index) {
        if (index >= length) {
            throw new IllegalArgumentException("index cannot be greater than current length");
        }

        Map.Entry<Integer, CharSequence> entry = cachedEntry;
        if (entry == null || index < entry.getKey() || index >= endOfCachedEntry) {
            entry = sequences.floorEntry(index);
            cachedEntry = entry;
            endOfCachedEntry = entry.getKey() + entry.getValue().length();
        }

        return entry.getValue().charAt(index - entry.getKey());
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start >= length || end >= length) {
            throw new IllegalArgumentException("start or end cannot be greater than length");
        }

        Integer startOfSubMap = sequences.lowerKey(start);
        NavigableMap<Integer, CharSequence> slice = sequences.subMap(startOfSubMap, true, end, true);

        // if there's only one CharSequence involved, then it's easy...
        if (slice.size() == 1) {
            Map.Entry<Integer, CharSequence> chunk = slice.firstEntry();
            Integer startOfChunk = chunk.getKey();
            return chunk.getValue().subSequence(start - startOfChunk, end - startOfChunk);
        }
        else if (slice.size() > 1) {
            // ...otherwise, we'll build up another one of our own kind
            AppendableCharSequence result = new AppendableCharSequence();

            for (Map.Entry<Integer, CharSequence> chunk : slice.entrySet()) {
                Integer startOfChunk = chunk.getKey();
                CharSequence charSequence = chunk.getValue();
                int endOfChunk = startOfChunk + charSequence.length();

                int startOfSubSeq = start > startOfChunk ? start - startOfChunk : 0;
                int endOfSubSeq = (end < endOfChunk ? end : endOfChunk) - startOfChunk;

                if (startOfSubSeq == 0 && endOfSubSeq == charSequence.length()) {
                    result.append(charSequence);
                }
                else {
                    result.append(charSequence.subSequence(startOfSubSeq, endOfSubSeq));
                }
            }

            return result;
        }
        else {
            throw new IllegalArgumentException("Requested sub-sequence did not fall within contained bounds");
        }
    }
}

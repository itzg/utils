package me.itzg.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class AppendableCharSequenceTest {
    @Test
    public void testSeqOfOne() throws Exception {
        AppendableCharSequence appendable = new AppendableCharSequence();

        appendable.append("just one");

        assertEquals(8, appendable.length());
        assertEquals('j', appendable.charAt(0));
        assertEquals('t', appendable.charAt(3));
        assertEquals("t o", appendable.subSequence(3, 6));
        assertTrue(appendable.equals(appendable));
        assertEquals("just one", appendable.toString());
    }

    @Test
    public void testSeqOfTwo() throws Exception {
        AppendableCharSequence appendable = new AppendableCharSequence();

        appendable.append("just one");
        appendable.append(" and another");

        assertEquals(20, appendable.length());
        assertEquals('j', appendable.charAt(0));
        assertEquals('t', appendable.charAt(3));
        assertEquals(' ', appendable.charAt(8));
        assertEquals('a', appendable.charAt(9));
        assertEquals("t o", appendable.subSequence(3, 6));
        assertEquals("and a", appendable.subSequence(9, 14));
        assertEquals(new StringBuilder("one and").toString(),
                new StringBuilder(appendable.subSequence(5, 12)).toString());
        assertTrue(appendable.equals(appendable));
        assertEquals("just one and another", appendable.toString());
    }

    @Test
    public void testSeqOfThree() throws Exception {
        AppendableCharSequence appendable = new AppendableCharSequence();

        appendable.append("just one");
        appendable.append(" and another");
        appendable.append(" and the last");

        assertEquals(33, appendable.length());
        assertEquals('j', appendable.charAt(0));
        assertEquals('t', appendable.charAt(3));
        assertEquals(' ', appendable.charAt(8));
        assertEquals('a', appendable.charAt(9));
        assertEquals('t', appendable.charAt(32));
        assertEquals("t o", appendable.subSequence(3, 6));
        assertEquals("and a", appendable.subSequence(9, 14));
        // this is the main point of this variant of test...middle chunk is included in its entirety
        assertEquals(new StringBuilder("one and another and").toString(),
                new StringBuilder(appendable.subSequence(5, 24)).toString());
        assertTrue(appendable.equals(appendable));
        assertEquals("just one and another and the last", appendable.toString());


    }

    @Test
    public void testNegativeCases() throws Exception {
        AppendableCharSequence appendable = new AppendableCharSequence();

        assertEquals(0, appendable.length());

        try {
            appendable.charAt(0);
            fail();
        } catch (IllegalArgumentException e) { }

    }

    @Test
    public void testNestedAppendable() throws Exception {
        AppendableCharSequence outer = new AppendableCharSequence();
        AppendableCharSequence inner = new AppendableCharSequence();

        inner.append("one ");
        inner.append("two");

        outer.append("{");
        outer.append(inner);
        outer.append("}");

        assertEquals("{one two}", outer.toString());
    }
}
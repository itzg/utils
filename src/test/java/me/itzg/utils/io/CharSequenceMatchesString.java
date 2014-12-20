package me.itzg.utils.io;

import org.mockito.ArgumentMatcher;

/**
* @author Geoff
* @since 11/8/2014
*/
public class CharSequenceMatchesString extends ArgumentMatcher<CharSequence> {
    private String goal;

    CharSequenceMatchesString(String goal) {
        this.goal = goal;
    }

    @Override
    public boolean matches(Object argument) {
        return argument != null &&
                argument instanceof CharSequence
                && argument.toString().equals(goal);
    }
}

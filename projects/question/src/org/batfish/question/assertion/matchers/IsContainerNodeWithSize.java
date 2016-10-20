package org.batfish.question.assertion.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import com.fasterxml.jackson.databind.node.ContainerNode;

public class IsContainerNodeWithSize<E> extends ContainerNodeMatcher<ContainerNode<? extends E>> {
    private final Matcher<? super Integer> sizeMatcher;

    public IsContainerNodeWithSize(Matcher<? super Integer> sizeMatcher) {
        this.sizeMatcher = sizeMatcher;
    }

    @Override
    public boolean matchesSafely(ContainerNode<? extends E> item) {
        return sizeMatcher.matches(item.size());
    }

    public void describeTo(Description description) {
        description.appendText("a ContainerNode with size ")
            .appendDescriptionOf(sizeMatcher);
    }

}
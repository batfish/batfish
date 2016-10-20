package org.batfish.question.assertion.matchers;

import org.hamcrest.BaseMatcher;

import com.fasterxml.jackson.databind.node.ContainerNode;

public abstract class ContainerNodeMatcher<C extends ContainerNode<?>> extends BaseMatcher<C> {
   @SuppressWarnings("unchecked")
   public boolean matches(Object item) {
       if (!(item instanceof ContainerNode)) {
           return false;
       }
       return matchesSafely((C)item);
   }

   protected abstract boolean matchesSafely(C collection);
}
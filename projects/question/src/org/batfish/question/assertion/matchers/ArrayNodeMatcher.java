package org.batfish.question.assertion.matchers;

import org.hamcrest.BaseMatcher;

import com.fasterxml.jackson.databind.node.ArrayNode;

public abstract class ArrayNodeMatcher extends BaseMatcher<ArrayNode> {
   @Override
   public boolean matches(Object item) {
      if (!(item instanceof ArrayNode)) {
         return false;
      }
      return matchesSafely((ArrayNode) item);
   }

   protected abstract boolean matchesSafely(ArrayNode collection);
}

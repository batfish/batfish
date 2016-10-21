package org.batfish.question.assertion.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ElementsMatcher extends ArrayNodeMatcher {

   private Matcher<JsonNode> _elementMatcher;

   public ElementsMatcher(Matcher<JsonNode> elementMatcher) {
      _elementMatcher = elementMatcher;
   }

   @Override
   public void describeTo(Description description) {
      description.appendText("a ArrayNode with each element matched by ")
            .appendDescriptionOf(_elementMatcher);
   }

   @Override
   public boolean matchesSafely(ArrayNode item) {
      for (JsonNode node : item) {
         if (!_elementMatcher.matches(node)) {
            return false;
         }
      }
      return true;
   }

}

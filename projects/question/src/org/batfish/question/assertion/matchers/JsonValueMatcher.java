package org.batfish.question.assertion.matchers;

import org.batfish.common.BatfishException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonValueMatcher extends BaseMatcher<JsonNode> {

   private Matcher<?> _valueMatcher;

   public JsonValueMatcher(Matcher<?> valueMatcher) {
      _valueMatcher = valueMatcher;
   }

   @Override
   public void describeTo(Description description) {
      description.appendText("A value-type JsonNode with value matched by ")
            .appendDescriptionOf(_valueMatcher);
   }

   @Override
   public boolean matches(Object item) {
      JsonNode node = (JsonNode) item;
      if (!node.isValueNode()) {
         return false;
      }
      else if (node.isBigDecimal()) {
         return _valueMatcher.matches(node.decimalValue());
      }
      else if (node.isBigInteger()) {
         return _valueMatcher.matches(node.bigIntegerValue());
      }
      else if (node.isBoolean()) {
         return _valueMatcher.matches(node.booleanValue());
      }
      else if (node.isDouble()) {
         return _valueMatcher.matches(node.doubleValue());
      }
      else if (node.isFloat()) {
         return _valueMatcher.matches(node.floatValue());
      }
      else if (node.isInt()) {
         return _valueMatcher.matches(node.intValue());
      }
      else if (node.isLong()) {
         return _valueMatcher.matches(node.longValue());
      }
      else if (node.isShort()) {
         return _valueMatcher.matches(node.shortValue());
      }
      else if (node.isTextual()) {
         return _valueMatcher.matches(node.textValue());
      }
      else {
         throw new BatfishException("Unsupported JsonNode value type");
      }
   }

}

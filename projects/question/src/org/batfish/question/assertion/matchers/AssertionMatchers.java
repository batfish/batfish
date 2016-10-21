package org.batfish.question.assertion.matchers;

import java.util.function.Function;

import org.hamcrest.Matcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ContainerNode;

public class AssertionMatchers {

   public static <A extends Comparable<A>> Matcher<A> compare(Object arg,
         Function<A, Matcher<A>> matcherFunction) {
      return new ComparableMatcher<>(arg, matcherFunction);
   }

   public static Matcher<?> elems(Matcher<JsonNode> elemMatcher) {
      return new ElementsMatcher(elemMatcher);
   }

   public static <T extends ContainerNode<T>> Matcher<? super ContainerNode<? extends T>> hasSize(
         Matcher<? super Integer> sizeMatcher) {
      return new IsContainerNodeWithSize<>(sizeMatcher);
   }

   private AssertionMatchers() {
      throw new AssertionError("Attempt to instantiate");
   }

}

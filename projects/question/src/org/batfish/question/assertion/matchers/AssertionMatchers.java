package org.batfish.question.assertion.matchers;

import java.util.function.Function;

import org.hamcrest.Matcher;

import com.fasterxml.jackson.databind.node.ContainerNode;

public class AssertionMatchers {

   public static <T> Matcher<? super ContainerNode<? extends T>> hasSize(
         Matcher<? super Integer> sizeMatcher) {
      return new IsContainerNodeWithSize<T>(sizeMatcher);
   }

   private AssertionMatchers() {
      throw new AssertionError("Attempt to instantiate");
   }

   public static <A extends Comparable<A>> Matcher<A> compare(Object arg,
         Function<A, Matcher<A>> matcherFunction) {
      return new ComparableMatcher<A>(arg, matcherFunction);
   }

}

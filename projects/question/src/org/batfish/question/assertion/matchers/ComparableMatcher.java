package org.batfish.question.assertion.matchers;

import java.util.function.Function;

import org.batfish.common.BatfishException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class ComparableMatcher<A extends Comparable<A>> extends BaseMatcher<A> {

   private Object _arg;

   private Function<A, Matcher<A>> _matcherFunction;

   public ComparableMatcher(Object arg,
         Function<A, Matcher<A>> matcherFunction) {
      _arg = arg;
      if (!(_arg instanceof Comparable<?>)) {
         throw new BatfishException("Not a Comparable: " + _arg);
      }
      _matcherFunction = matcherFunction;
   }

   @SuppressWarnings("unchecked")
   @Override
   public void describeTo(Description description) {
      _matcherFunction.apply((A) _arg).describeTo(description);
   }

   @SuppressWarnings("unchecked")
   @Override
   public boolean matches(Object item) {
      return _matcherFunction.apply((A) _arg).matches(item);
   }

}

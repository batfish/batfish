package org.batfish.symbolic.abstraction;

import java.util.Set;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class SetMatchers {
  public static <T> Matcher<Set<T>> isSubsetOf(Set<T> superSet) {
    return new BaseMatcher<Set<T>>() {
      @Override
      public void describeTo(Description description) {}

      @Override
      public boolean matches(Object subSetObject) {
        @SuppressWarnings("unchecked")
        Set<T> subSet = (Set<T>) subSetObject;
        return superSet.containsAll(subSet);
      }
    };
  }
}

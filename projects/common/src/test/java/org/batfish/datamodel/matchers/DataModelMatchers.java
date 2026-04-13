package org.batfish.datamodel.matchers;

import org.hamcrest.Matcher;

public final class DataModelMatchers {

  /**
   * Provides a matcher for a collection that matches if each element matched by {@code
   * identifiedBy} is matched by {@code conformsTo}
   */
  public static <E> Matcher<Iterable<? extends E>> forAll(
      Matcher<? super E> identifiedBy, Matcher<? super E> conformsTo) {
    return new ForAll<>(identifiedBy, conformsTo);
  }

  private DataModelMatchers() {}
}

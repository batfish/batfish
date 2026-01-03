package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.matchers.MapMatchersImpl.HasKeys;
import org.batfish.datamodel.matchers.MapMatchersImpl.HasValues;
import org.hamcrest.Matcher;

/** Matchers for {@link Map}. */
@ParametersAreNonnullByDefault
public final class MapMatchers {

  /**
   * Returns a matcher that matches a {@link Map} whose keySet contains exactly {@code
   * expectedKeys}.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static @Nonnull <K, V> Matcher<Map<K, V>> hasKeys(K... expectedKeys) {
    return new HasKeys<>(containsInAnyOrder(expectedKeys));
  }

  /**
   * Returns a matcher that matches a {@link Map} whose keySet is matched by the provided {@code
   * subMatcher}.
   */
  public static @Nonnull <K, V> Matcher<Map<K, V>> hasKeys(Matcher<? super Set<K>> subMatcher) {
    return new HasKeys<>(subMatcher);
  }

  /**
   * Returns a matcher that matches a {@link Map} whose values are matched by the provided {@code
   * subMatcher}.
   */
  public static @Nonnull <K, V> Matcher<Map<K, V>> hasValues(
      Matcher<? super Collection<V>> subMatcher) {
    return new HasValues<>(subMatcher);
  }

  /**
   * Returns a matcher that matches a {@link Map} whose values contain exactly {@code
   * expectedValues}.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public static @Nonnull <K, V> Matcher<Map<K, V>> hasValues(V... expectedValues) {
    return new HasValues<>(containsInAnyOrder(expectedValues));
  }

  private MapMatchers() {}
}

package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.hamcrest.FeatureMatcher;
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

  private static final class HasKeys<K, V> extends FeatureMatcher<Map<K, V>, Set<K>> {
    public HasKeys(Matcher<? super Set<K>> subMatcher) {
      super(subMatcher, "A map with keys:", "keys:");
    }

    @Override
    protected @Nonnull Set<K> featureValueOf(Map<K, V> actual) {
      return actual.keySet();
    }
  }

  private static final class HasValues<K, V> extends FeatureMatcher<Map<K, V>, Collection<V>> {
    public HasValues(Matcher<? super Collection<V>> subMatcher) {
      super(subMatcher, "A map with values:", "keys:");
    }

    @Override
    protected @Nonnull Collection<V> featureValueOf(Map<K, V> actual) {
      return actual.values();
    }
  }
}

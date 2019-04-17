package org.batfish.datamodel.matchers;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class MapMatchersImpl {

  static final class HasKeys<K, V, M extends Map<K, V>> extends FeatureMatcher<M, Set<K>> {
    public HasKeys(Matcher<? super Set<K>> subMatcher) {
      super(subMatcher, "A map with keys:", "keys:");
    }

    @Override
    protected @Nonnull Set<K> featureValueOf(M actual) {
      return actual.keySet();
    }
  }

  static final class HasValues<K, V, M extends Map<K, V>> extends FeatureMatcher<M, Collection<V>> {
    public HasValues(Matcher<? super Collection<V>> subMatcher) {
      super(subMatcher, "A map with values:", "keys:");
    }

    @Override
    protected @Nonnull Collection<V> featureValueOf(M actual) {
      return actual.values();
    }
  }

  private MapMatchersImpl() {}
}

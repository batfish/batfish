package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Edge;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class EdgeMatchersImpl {

  static class HasInt1 extends FeatureMatcher<Edge, String> {
    HasInt1(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Edge with int1:", "int1");
    }

    @Override
    protected String featureValueOf(Edge edge) {
      return edge.getInt1();
    }
  }

  static class HasInt2 extends FeatureMatcher<Edge, String> {
    HasInt2(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Edge with int2:", "int2");
    }

    @Override
    protected String featureValueOf(Edge edge) {
      return edge.getInt2();
    }
  }

  static class HasNode1 extends FeatureMatcher<Edge, String> {
    HasNode1(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Edge with node1:", "node1");
    }

    @Override
    protected String featureValueOf(Edge edge) {
      return edge.getNode1();
    }
  }

  static class HasNode2 extends FeatureMatcher<Edge, String> {
    HasNode2(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Edge with node2:", "node2");
    }

    @Override
    protected String featureValueOf(Edge edge) {
      return edge.getNode2();
    }
  }

  private EdgeMatchersImpl() {}
}

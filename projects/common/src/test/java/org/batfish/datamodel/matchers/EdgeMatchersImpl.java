package org.batfish.datamodel.matchers;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class EdgeMatchersImpl {

  static class HasHead extends FeatureMatcher<Edge, NodeInterfacePair> {

    public HasHead(Matcher<? super NodeInterfacePair> subMatcher) {
      super(subMatcher, "An Edge with head:", "head");
    }

    @Override
    protected NodeInterfacePair featureValueOf(Edge actual) {
      return actual.getHead();
    }
  }

  static class HasNode1 extends FeatureMatcher<Edge, String> {

    public HasNode1(Matcher<? super String> subMatcher) {
      super(subMatcher, "An Edge with node1:", "node1");
    }

    @Override
    protected String featureValueOf(Edge actual) {
      return actual.getNode1();
    }
  }

  static class HasNode2 extends FeatureMatcher<Edge, String> {

    public HasNode2(Matcher<? super String> subMatcher) {
      super(subMatcher, "An Edge with node2:", "node1");
    }

    @Override
    protected String featureValueOf(Edge actual) {
      return actual.getNode2();
    }
  }

  static class HasTail extends FeatureMatcher<Edge, NodeInterfacePair> {

    public HasTail(Matcher<? super NodeInterfacePair> subMatcher) {
      super(subMatcher, "An Edge with tail:", "tail");
    }

    @Override
    protected NodeInterfacePair featureValueOf(Edge actual) {
      return actual.getTail();
    }
  }

  private EdgeMatchersImpl() {}
}

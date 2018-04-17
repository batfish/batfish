package org.batfish.datamodel.matchers;

import org.batfish.datamodel.Edge;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class EdgeMatchers {
  /**
   * @param subMatcher a {@Matcher} for the sending interface of the {@Edge}.
   * @return A {@Matcher} for the {@Edge}.
   */
  public static HasInt1 hasInt1(Matcher<String> subMatcher) {
    return new HasInt1(subMatcher);
  }

  /**
   * @param subMatcher a {@Matcher} for the receiving interface of the {@Edge}.
   * @return A {@Matcher} for the {@Edge}.
   */
  public static HasInt2 hasInt2(Matcher<String> subMatcher) {
    return new HasInt2(subMatcher);
  }

  /**
   * @param subMatcher a {@Matcher} for the sending node of the {@Edge}.
   * @return A {@Matcher} for the {@Edge}.
   */
  public static HasNode1 hasNode1(Matcher<String> subMatcher) {
    return new HasNode1(subMatcher);
  }

  /**
   * @param subMatcher a {@Matcher} for the receiving node of the {@Edge}.
   * @return A {@Matcher} for the {@Edge}.
   */
  public static HasNode2 hasNode2(Matcher<String> subMatcher) {
    return new HasNode2(subMatcher);
  }

  private EdgeMatchers() {}

  private static class HasInt1 extends FeatureMatcher<Edge, String> {
    public HasInt1(Matcher<String> subMatcher) {
      super(subMatcher, "an Edge with int1:", "int1");
    }

    @Override protected String featureValueOf(Edge edge) {
      return edge.getInt1();
    }
  }

  private static class HasInt2 extends FeatureMatcher<Edge, String> {
    public HasInt2(Matcher<String> subMatcher) {
      super(subMatcher, "an Edge with int2:", "int2");
    }

    @Override protected String featureValueOf(Edge edge) {
      return edge.getInt2();
    }
  }

  private static class HasNode1 extends FeatureMatcher<Edge, String> {
    HasNode1(Matcher<String> subMatcher) {
      super(subMatcher, "an Edge with node1:", "node1");
    }

    @Override protected String featureValueOf(Edge edge) {
      return edge.getNode1();
    }
  }

  private static class HasNode2 extends FeatureMatcher<Edge, String> {
    HasNode2(Matcher<String> subMatcher) {
      super(subMatcher, "an Edge with node2:", "node2");
    }

    @Override protected String featureValueOf(Edge edge) {
      return edge.getNode2();
    }
  }
}

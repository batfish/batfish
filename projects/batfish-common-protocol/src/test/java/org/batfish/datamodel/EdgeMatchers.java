package org.batfish.datamodel;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class EdgeMatchers {
  public static HasInt1 hasInt1(Matcher<String> subMatcher) {
    return new HasInt1(subMatcher);
  }

  public static HasInt2 hasInt2(Matcher<String> subMatcher) {
    return new HasInt2(subMatcher);
  }

  public static HasNode1 hasNode1(Matcher<String> subMatcher) {
    return new HasNode1(subMatcher);
  }

  public static HasNode2 hasNode2(Matcher<String> subMatcher) {
    return new HasNode2(subMatcher);
  }

  private EdgeMatchers() {}

  private static class HasInt1 extends TypeSafeDiagnosingMatcher<Edge> {
    private final Matcher<String> _matcher;

    HasInt1(Matcher<String> matcher) {
      _matcher = matcher;
    }

    @Override
    protected boolean matchesSafely(Edge edge, Description description) {
      return _matcher.matches(edge.getInt1());
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("Edge with int1").appendDescriptionOf(_matcher);
    }
  }

  private static class HasInt2 extends TypeSafeDiagnosingMatcher<Edge> {
    private final Matcher<String> _matcher;

    HasInt2(Matcher<String> matcher) {
      _matcher = matcher;
    }

    @Override
    protected boolean matchesSafely(Edge edge, Description description) {
      return _matcher.matches(edge.getInt2());
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("Edge with int2").appendDescriptionOf(_matcher);
    }
  }

  private static class HasNode1 extends TypeSafeDiagnosingMatcher<Edge> {
    private final Matcher<String> _matcher;

    HasNode1(Matcher<String> matcher) {
      _matcher = matcher;
    }

    @Override
    protected boolean matchesSafely(Edge edge, Description description) {
      return _matcher.matches(edge.getNode1());
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("Edge with node1").appendDescriptionOf(_matcher);
    }
  }

  private static class HasNode2 extends TypeSafeDiagnosingMatcher<Edge> {
    private final Matcher<String> _matcher;

    HasNode2(Matcher<String> matcher) {
      _matcher = matcher;
    }

    @Override
    protected boolean matchesSafely(Edge edge, Description description) {
      return _matcher.matches(edge.getNode2());
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("Edge with node2").appendDescriptionOf(_matcher);
    }
  }
}

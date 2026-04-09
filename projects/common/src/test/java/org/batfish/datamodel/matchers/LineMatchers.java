package org.batfish.datamodel.matchers;

import static org.batfish.datamodel.matchers.AaaAuthenticationLoginListMatchers.hasMethods;
import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.Line;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class LineMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link Line}'s
   * {@code AaaAuthenticationLoginList}
   */
  public static Matcher<Line> hasAuthenticationLoginList(
      Matcher<? super AaaAuthenticationLoginList> subMatcher) {
    return new HasAuthenticationLoginList(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@link Line}'s {@code AaaAuthenticationLoginList} is
   * not null and not empty
   */
  public static Matcher<Line> hasAuthenticationLoginList() {
    return new HasAuthenticationLoginList(hasMethods());
  }

  /** Provides a matcher that matches if the {@link Line} requires authentication */
  public static Matcher<Line> requiresAuthentication() {
    return new RequiresAuthentication(equalTo(true));
  }

  private LineMatchers() {}

  private static final class HasAuthenticationLoginList
      extends FeatureMatcher<Line, AaaAuthenticationLoginList> {
    HasAuthenticationLoginList(@Nonnull Matcher<? super AaaAuthenticationLoginList> subMatcher) {
      super(subMatcher, "a line with a AaaAuthenticationLoginList", "AaaAuthenticationLoginList");
    }

    @Override
    protected AaaAuthenticationLoginList featureValueOf(Line actual) {
      return actual.getAaaAuthenticationLoginList();
    }
  }

  private static final class RequiresAuthentication extends FeatureMatcher<Line, Boolean> {
    RequiresAuthentication(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "a Line that requires authentication", "requires authentication");
    }

    @Override
    protected Boolean featureValueOf(Line actual) {
      return actual.requiresAuthentication();
    }
  }
}

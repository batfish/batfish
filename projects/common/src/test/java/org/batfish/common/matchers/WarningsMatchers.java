package org.batfish.common.matchers;

import static org.hamcrest.Matchers.hasItem;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link Warnings}. */
@ParametersAreNonnullByDefault
public class WarningsMatchers {

  /**
   * Provides a matcher that matches if the warnings contains parseWarnings matched by the provided
   * {@code subMatcher}.
   */
  public static Matcher<Warnings> hasParseWarnings(Matcher<? super List<ParseWarning>> subMatcher) {
    return new HasParseWarnings(subMatcher);
  }

  /**
   * Provides a matcher that matches if the warnings has a parseWarning matched by the provided
   * {@code subMatcher}.
   */
  public static Matcher<Warnings> hasParseWarning(Matcher<? super ParseWarning> subMatcher) {
    return new HasParseWarnings(hasItem(subMatcher));
  }

  /**
   * Provides a matcher that matches if the warnings contains redFlags matched by the provided
   * {@code subMatcher}.
   */
  public static Matcher<Warnings> hasRedFlags(Matcher<? super Set<Warning>> subMatcher) {
    return new HasRedFlags(subMatcher);
  }

  /**
   * Provides a matcher that matches if the warnings has a redFlag matched by the provided {@code
   * subMatcher}.
   */
  public static Matcher<Warnings> hasRedFlag(Matcher<? super Warning> subMatcher) {
    return new HasRedFlags(hasItem(subMatcher));
  }

  /**
   * Provides a matcher that matches if the warnings contains pedantic warnings matched by the
   * provided {@code subMatcher}.
   */
  public static Matcher<Warnings> hasPedanticWarnings(Matcher<? super Set<Warning>> subMatcher) {
    return new HasPedanticWarnings(subMatcher);
  }

  /**
   * Provides a matcher that matches if the warnings contains unimplemented warnings matched by the
   * provided {@code subMatcher}.
   */
  public static Matcher<Warnings> hasUnimplementedWarnings(
      Matcher<? super Set<Warning>> subMatcher) {
    return new HasUnimplementedWarnings(subMatcher);
  }

  /**
   * Provides a matcher that matches if the warnings has an unimplemented warning matched by the
   * provided {@code subMatcher}.
   */
  public static Matcher<Warnings> hasUnimplementedWarning(Matcher<? super Warning> subMatcher) {
    return new HasUnimplementedWarnings(hasItem(subMatcher));
  }

  private WarningsMatchers() {}

  private static final class HasParseWarnings extends FeatureMatcher<Warnings, List<ParseWarning>> {
    HasParseWarnings(@Nonnull Matcher<? super List<ParseWarning>> subMatcher) {
      super(subMatcher, "Warnings with parse warnings:", "parse warnings");
    }

    @Override
    protected List<ParseWarning> featureValueOf(Warnings actual) {
      return actual.getParseWarnings();
    }
  }

  private static final class HasRedFlags extends FeatureMatcher<Warnings, Set<Warning>> {
    HasRedFlags(@Nonnull Matcher<? super Set<Warning>> subMatcher) {
      super(subMatcher, "Warnings with redFlag warnings:", "redFlag warnings");
    }

    @Override
    protected Set<Warning> featureValueOf(Warnings actual) {
      return actual.getRedFlagWarnings();
    }
  }

  private static final class HasPedanticWarnings extends FeatureMatcher<Warnings, Set<Warning>> {
    HasPedanticWarnings(@Nonnull Matcher<? super Set<Warning>> subMatcher) {
      super(subMatcher, "Warnings with pedantic warnings:", "pedantic warnings");
    }

    @Override
    protected Set<Warning> featureValueOf(Warnings actual) {
      return actual.getPedanticWarnings();
    }
  }

  private static final class HasUnimplementedWarnings
      extends FeatureMatcher<Warnings, Set<Warning>> {
    HasUnimplementedWarnings(@Nonnull Matcher<? super Set<Warning>> subMatcher) {
      super(subMatcher, "Warnings with unimplemented warnings:", "unimplemented warnings");
    }

    @Override
    protected Set<Warning> featureValueOf(Warnings actual) {
      return actual.getUnimplementedWarnings();
    }
  }
}

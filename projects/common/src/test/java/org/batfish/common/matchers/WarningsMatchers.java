package org.batfish.common.matchers;

import static org.hamcrest.Matchers.hasItem;

import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.matchers.WarningsMatchersImpl.HasParseWarnings;
import org.batfish.common.matchers.WarningsMatchersImpl.HasPedanticWarnings;
import org.batfish.common.matchers.WarningsMatchersImpl.HasRedFlags;
import org.batfish.common.matchers.WarningsMatchersImpl.HasUnimplementedWarnings;
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
}

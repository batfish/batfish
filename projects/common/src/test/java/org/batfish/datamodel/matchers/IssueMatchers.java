package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Issue;
import org.batfish.datamodel.matchers.IssueMatchersImpl.HasMajorType;
import org.batfish.datamodel.matchers.IssueMatchersImpl.HasMinorType;

public final class IssueMatchers {

  /**
   * Provides a matcher that matches if the major type of {@link Issue} equals {@code minorType}.
   */
  public static @Nonnull HasMajorType hasMajorType(@Nonnull String majorType) {
    return new HasMajorType(equalTo(majorType));
  }

  /**
   * Provides a matcher that matches if the minor type of {@link Issue} equals {@code minorType}.
   */
  public static @Nonnull HasMinorType hasMinorType(@Nonnull String minorType) {
    return new HasMinorType(equalTo(minorType));
  }

  private IssueMatchers() {}
}

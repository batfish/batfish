package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.DeniedByIpAccessListLine;
import org.batfish.datamodel.matchers.DeniedByIpAccessListLineMatchersImpl.HasIndex;
import org.batfish.datamodel.matchers.DeniedByIpAccessListLineMatchersImpl.HasLineDescription;
import org.batfish.datamodel.matchers.DeniedByIpAccessListLineMatchersImpl.HasName;
import org.hamcrest.Matcher;

public final class DeniedByIpAccessListLineMatchers {

  /**
   * Provides a matcher that matches if the {@link DeniedByIpAccessListLine}'s index is equal to
   * {@code expectedIndex}.
   */
  public static @Nonnull Matcher<DeniedByIpAccessListLine> hasIndex(int expectedIndex) {
    return new HasIndex(equalTo(expectedIndex));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * DeniedByIpAccessListLine}'s index.
   */
  public static @Nonnull Matcher<DeniedByIpAccessListLine> hasIndex(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasIndex(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * DeniedByIpAccessListLine}'s lineDescription.
   */
  public static @Nonnull Matcher<DeniedByIpAccessListLine> hasLineDescription(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasLineDescription(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link DeniedByIpAccessListLine}'s lineDescription is
   * equal to {@code expectedLineDescription}.
   */
  public static @Nonnull Matcher<DeniedByIpAccessListLine> hasLineDescription(
      String expectedLineDescription) {
    return new HasLineDescription(equalTo(expectedLineDescription));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * DeniedByIpAccessListLine}'s name.
   */
  public static @Nonnull Matcher<DeniedByIpAccessListLine> hasName(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link DeniedByIpAccessListLine}'s name is equal to
   * {@code expectedName}.
   */
  public static @Nonnull Matcher<DeniedByIpAccessListLine> hasName(String expectedName) {
    return new HasName(equalTo(expectedName));
  }
}

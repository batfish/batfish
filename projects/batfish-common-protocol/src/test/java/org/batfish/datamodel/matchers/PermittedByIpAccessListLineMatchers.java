package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.PermittedByIpAccessListLine;
import org.batfish.datamodel.matchers.PermittedByIpAccessListLineMatchersImpl.HasIndex;
import org.batfish.datamodel.matchers.PermittedByIpAccessListLineMatchersImpl.HasLineDescription;
import org.batfish.datamodel.matchers.PermittedByIpAccessListLineMatchersImpl.HasName;
import org.hamcrest.Matcher;

public final class PermittedByIpAccessListLineMatchers {

  /**
   * Provides a matcher that matches if the {@link PermittedByIpAccessListLine}'s index is equal to
   * {@code expectedIndex}.
   */
  public static @Nonnull Matcher<PermittedByIpAccessListLine> hasIndex(int expectedIndex) {
    return new HasIndex(equalTo(expectedIndex));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * PermittedByIpAccessListLine}'s index.
   */
  public static @Nonnull Matcher<PermittedByIpAccessListLine> hasIndex(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasIndex(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * PermittedByIpAccessListLine}'s lineDescription.
   */
  public static @Nonnull Matcher<PermittedByIpAccessListLine> hasLineDescription(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasLineDescription(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link PermittedByIpAccessListLine}'s lineDescription is
   * equal to {@code expectedLineDescription}.
   */
  public static @Nonnull Matcher<PermittedByIpAccessListLine> hasLineDescription(
      String expectedLineDescription) {
    return new HasLineDescription(equalTo(expectedLineDescription));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * PermittedByIpAccessListLine}'s name.
   */
  public static @Nonnull Matcher<PermittedByIpAccessListLine> hasName(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link PermittedByIpAccessListLine}'s name is equal to
   * {@code expectedName}.
   */
  public static @Nonnull Matcher<PermittedByIpAccessListLine> hasName(String expectedName) {
    return new HasName(equalTo(expectedName));
  }
}

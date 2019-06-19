package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.PermittedByAclIpSpaceLine;
import org.batfish.datamodel.matchers.PermittedByAclIpSpaceLineMatchersImpl.HasIndex;
import org.batfish.datamodel.matchers.PermittedByAclIpSpaceLineMatchersImpl.HasLineDescription;
import org.batfish.datamodel.matchers.PermittedByAclIpSpaceLineMatchersImpl.HasName;
import org.hamcrest.Matcher;

public final class PermittedByAclIpSpaceLineMatchers {

  /**
   * Provides a matcher that matches if the {@link PermittedByAclIpSpaceLine}'s index is equal to
   * {@code expectedIndex}.
   */
  public static @Nonnull Matcher<PermittedByAclIpSpaceLine> hasIndex(int expectedIndex) {
    return new HasIndex(equalTo(expectedIndex));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * PermittedByAclIpSpaceLine}'s index.
   */
  public static @Nonnull Matcher<PermittedByAclIpSpaceLine> hasIndex(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasIndex(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * PermittedByAclIpSpaceLine}'s lineDescription.
   */
  public static @Nonnull Matcher<PermittedByAclIpSpaceLine> hasLineDescription(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasLineDescription(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link PermittedByAclIpSpaceLine}'s lineDescription is
   * equal to {@code expectedLineDescription}.
   */
  public static @Nonnull Matcher<PermittedByAclIpSpaceLine> hasLineDescription(
      String expectedLineDescription) {
    return new HasLineDescription(equalTo(expectedLineDescription));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * PermittedByAclIpSpaceLine}'s name.
   */
  public static @Nonnull Matcher<PermittedByAclIpSpaceLine> hasName(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link PermittedByAclIpSpaceLine}'s name is equal to
   * {@code expectedName}.
   */
  public static @Nonnull Matcher<PermittedByAclIpSpaceLine> hasName(String expectedName) {
    return new HasName(equalTo(expectedName));
  }
}

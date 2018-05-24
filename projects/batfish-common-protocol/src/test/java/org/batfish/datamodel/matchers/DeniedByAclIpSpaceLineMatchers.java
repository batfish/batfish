package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.DeniedByAclIpSpaceLine;
import org.batfish.datamodel.matchers.DeniedByAclIpSpaceLineMatchersImpl.HasIndex;
import org.batfish.datamodel.matchers.DeniedByAclIpSpaceLineMatchersImpl.HasLineDescription;
import org.batfish.datamodel.matchers.DeniedByAclIpSpaceLineMatchersImpl.HasName;
import org.hamcrest.Matcher;

public final class DeniedByAclIpSpaceLineMatchers {

  /**
   * Provides a matcher that matches if the {@link DeniedByAclIpSpaceLine}'s index is equal to
   * {@code expectedIndex}.
   */
  public static @Nonnull Matcher<DeniedByAclIpSpaceLine> hasIndex(int expectedIndex) {
    return new HasIndex(equalTo(expectedIndex));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * DeniedByAclIpSpaceLine}'s index.
   */
  public static @Nonnull Matcher<DeniedByAclIpSpaceLine> hasIndex(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasIndex(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * DeniedByAclIpSpaceLine}'s lineDescription.
   */
  public static @Nonnull Matcher<DeniedByAclIpSpaceLine> hasLineDescription(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasLineDescription(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link DeniedByAclIpSpaceLine}'s lineDescription is
   * equal to {@code expectedLineDescription}.
   */
  public static @Nonnull Matcher<DeniedByAclIpSpaceLine> hasLineDescription(
      String expectedLineDescription) {
    return new HasLineDescription(equalTo(expectedLineDescription));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * DeniedByAclIpSpaceLine}'s name.
   */
  public static @Nonnull Matcher<DeniedByAclIpSpaceLine> hasName(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link DeniedByAclIpSpaceLine}'s name is equal to {@code
   * expectedName}.
   */
  public static @Nonnull Matcher<DeniedByAclIpSpaceLine> hasName(String expectedName) {
    return new HasName(equalTo(expectedName));
  }
}

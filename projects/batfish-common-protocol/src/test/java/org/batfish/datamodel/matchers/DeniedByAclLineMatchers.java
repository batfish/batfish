package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.DeniedByAclLine;
import org.batfish.datamodel.matchers.DeniedByIpAccessListLineMatchersImpl.HasIndex;
import org.batfish.datamodel.matchers.DeniedByIpAccessListLineMatchersImpl.HasLineDescription;
import org.batfish.datamodel.matchers.DeniedByIpAccessListLineMatchersImpl.HasName;
import org.hamcrest.Matcher;

public final class DeniedByAclLineMatchers {

  /**
   * Provides a matcher that matches if the {@link DeniedByAclLine}'s index is equal to {@code
   * expectedIndex}.
   */
  public static @Nonnull Matcher<DeniedByAclLine> hasIndex(int expectedIndex) {
    return new HasIndex(equalTo(expectedIndex));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * DeniedByAclLine}'s index.
   */
  public static @Nonnull Matcher<DeniedByAclLine> hasIndex(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasIndex(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * DeniedByAclLine}'s lineDescription.
   */
  public static @Nonnull Matcher<DeniedByAclLine> hasLineDescription(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasLineDescription(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link DeniedByAclLine}'s lineDescription is equal to
   * {@code expectedLineDescription}.
   */
  public static @Nonnull Matcher<DeniedByAclLine> hasLineDescription(
      String expectedLineDescription) {
    return new HasLineDescription(equalTo(expectedLineDescription));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * DeniedByAclLine}'s name.
   */
  public static @Nonnull Matcher<DeniedByAclLine> hasName(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link DeniedByAclLine}'s name is equal to {@code
   * expectedName}.
   */
  public static @Nonnull Matcher<DeniedByAclLine> hasName(String expectedName) {
    return new HasName(equalTo(expectedName));
  }
}

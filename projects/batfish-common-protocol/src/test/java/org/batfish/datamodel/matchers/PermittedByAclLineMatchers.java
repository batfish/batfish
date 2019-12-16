package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.PermittedByAclLine;
import org.batfish.datamodel.matchers.PermittedByAclLineMatchersImpl.HasIndex;
import org.batfish.datamodel.matchers.PermittedByAclLineMatchersImpl.HasLineDescription;
import org.batfish.datamodel.matchers.PermittedByAclLineMatchersImpl.HasName;
import org.hamcrest.Matcher;

public final class PermittedByAclLineMatchers {

  /**
   * Provides a matcher that matches if the {@link PermittedByAclLine}'s index is equal to {@code
   * expectedIndex}.
   */
  public static @Nonnull Matcher<PermittedByAclLine> hasIndex(int expectedIndex) {
    return new HasIndex(equalTo(expectedIndex));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * PermittedByAclLine}'s index.
   */
  public static @Nonnull Matcher<PermittedByAclLine> hasIndex(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasIndex(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * PermittedByAclLine}'s lineDescription.
   */
  public static @Nonnull Matcher<PermittedByAclLine> hasLineDescription(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasLineDescription(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link PermittedByAclLine}'s lineDescription is equal to
   * {@code expectedLineDescription}.
   */
  public static @Nonnull Matcher<PermittedByAclLine> hasLineDescription(
      String expectedLineDescription) {
    return new HasLineDescription(equalTo(expectedLineDescription));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * PermittedByAclLine}'s name.
   */
  public static @Nonnull Matcher<PermittedByAclLine> hasName(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link PermittedByAclLine}'s name is equal to {@code
   * expectedName}.
   */
  public static @Nonnull Matcher<PermittedByAclLine> hasName(String expectedName) {
    return new HasName(equalTo(expectedName));
  }
}

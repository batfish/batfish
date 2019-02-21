package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.isis.IsisLevelSettings;
import org.batfish.datamodel.isis.IsisProcess;
import org.batfish.datamodel.matchers.IsisProcessMatchersImpl.HasLevel1;
import org.batfish.datamodel.matchers.IsisProcessMatchersImpl.HasLevel2;
import org.batfish.datamodel.matchers.IsisProcessMatchersImpl.HasNetAddress;
import org.batfish.datamodel.matchers.IsisProcessMatchersImpl.HasOverload;
import org.batfish.datamodel.matchers.IsisProcessMatchersImpl.HasReferenceBandwidth;
import org.hamcrest.Matcher;

public final class IsisProcessMatchers {
  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s level1.
   */
  public static @Nonnull Matcher<IsisProcess> hasLevel1(
      @Nonnull Matcher<? super IsisLevelSettings> subMatcher) {
    return new HasLevel1(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s level2.
   */
  public static @Nonnull Matcher<IsisProcess> hasLevel2(
      @Nonnull Matcher<? super IsisLevelSettings> subMatcher) {
    return new HasLevel2(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link IsisProcess}'s overload is {@code
   * expectedOverload}.
   */
  public static @Nonnull Matcher<IsisProcess> hasOverload(boolean expectedOverload) {
    return new HasOverload(equalTo(expectedOverload));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s overload.
   */
  public static @Nonnull Matcher<IsisProcess> hasOverload(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasOverload(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link IsisProcess}'s referenceBandwidth is {@code
   * expectedReferenceBandwidth}.
   */
  public static @Nonnull Matcher<IsisProcess> hasReferenceBandwidth(
      @Nullable Double expectedReferenceBandwidth) {
    return new HasReferenceBandwidth(equalTo(expectedReferenceBandwidth));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s referenceBandwidth.
   */
  public static @Nonnull Matcher<IsisProcess> hasReferenceBandwidth(
      @Nonnull Matcher<? super Double> subMatcher) {
    return new HasReferenceBandwidth(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisProcess}'s netAddress.
   */
  public static @Nonnull Matcher<IsisProcess> hasNetAddress(
      @Nonnull Matcher<? super IsoAddress> subMatcher) {
    return new HasNetAddress(subMatcher);
  }

  private IsisProcessMatchers() {}
}

package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.isis.IsisInterfaceLevelSettings;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IsisInterfaceSettingsMatchers {

  /**
   * Provides a matcher that matches if the {@link IsisInterfaceSettings}'s
   * bfdLivenessDetectionMinimumInterval is {@code expectedBfdLivenessDetectionMinimumInterval}.
   */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasBfdLivenessDetectionMinimumInterval(
      @Nullable Integer expectedBfdLivenessDetectionMinimumInterval) {
    return new HasBfdLivenessDetectionMinimumInterval(
        equalTo(expectedBfdLivenessDetectionMinimumInterval));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisInterfaceSettings}'s bfdLivenessDetectionMinimumInterval.
   */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasBfdLivenessDetectionMinimumInterval(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasBfdLivenessDetectionMinimumInterval(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link IsisInterfaceSettings}'s
   * bfdLivenessDetectionMultiplier is {@code expectedBfdLivenessDetectionMultiplier}.
   */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasBfdLivenessDetectionMultiplier(
      @Nullable Integer expectedBfdLivenessDetectionMultiplier) {
    return new HasBfdLivenessDetectionMultiplier(equalTo(expectedBfdLivenessDetectionMultiplier));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisInterfaceSettings}'s bfdLivenessDetectionMultiplier.
   */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasBfdLivenessDetectionMultiplier(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasBfdLivenessDetectionMultiplier(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link IsisInterfaceSettings}'s isoAddress is {@code
   * expectedIsoAddress}.
   */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasIsoAddress(
      @Nullable IsoAddress expectedIsoAddress) {
    return new HasIsoAddress(equalTo(expectedIsoAddress));
  }

  /**
   * Provides a matcher that matches if the provied {@code subMatcher} matches the {@link
   * IsisInterfaceSettings}'s isoAddress.
   */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasIsoAddress(
      @Nonnull Matcher<? super IsoAddress> subMatcher) {
    return new HasIsoAddress(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisInterfaceSettings}'s level1.
   */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasLevel1(
      @Nonnull Matcher<? super IsisInterfaceLevelSettings> subMatcher) {
    return new HasLevel1(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisInterfaceSettings}'s level2.
   */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasLevel2(
      @Nonnull Matcher<? super IsisInterfaceLevelSettings> subMatcher) {
    return new HasLevel2(subMatcher);
  }

  /** Provides a matcher that matches if the {@link IsisInterfaceSettings}'s is pointToPoint. */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasPointToPoint() {
    return new HasPointToPoint(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisInterfaceSettings}'s pointToPoint.
   */
  public static @Nonnull Matcher<IsisInterfaceSettings> hasPointToPoint(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasPointToPoint(subMatcher);
  }

  private IsisInterfaceSettingsMatchers() {}

  private static final class HasBfdLivenessDetectionMinimumInterval
      extends FeatureMatcher<IsisInterfaceSettings, Integer> {
    public HasBfdLivenessDetectionMinimumInterval(@Nonnull Matcher<? super Integer> subMatcher) {
      super(
          subMatcher,
          "An IsisInterfaceSettings with bfdLivenessDetectionMinimumInterval:",
          "bfdLivenessDetectionMinimumInterval");
    }

    @Override
    protected Integer featureValueOf(IsisInterfaceSettings actual) {
      return actual.getBfdLivenessDetectionMinimumInterval();
    }
  }

  private static final class HasBfdLivenessDetectionMultiplier
      extends FeatureMatcher<IsisInterfaceSettings, Integer> {
    public HasBfdLivenessDetectionMultiplier(@Nonnull Matcher<? super Integer> subMatcher) {
      super(
          subMatcher,
          "An IsisInterfaceSettings with bfdLivenessDetectionMultiplier:",
          "bfdLivenessDetectionMultiplier");
    }

    @Override
    protected Integer featureValueOf(IsisInterfaceSettings actual) {
      return actual.getBfdLivenessDetectionMultiplier();
    }
  }

  private static final class HasPointToPoint
      extends FeatureMatcher<IsisInterfaceSettings, Boolean> {
    public HasPointToPoint(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An IsisInterfaceSettings with pointToPoint:", "pointToPoint");
    }

    @Override
    protected Boolean featureValueOf(IsisInterfaceSettings actual) {
      return actual.getPointToPoint();
    }
  }

  private static final class HasIsoAddress
      extends FeatureMatcher<IsisInterfaceSettings, IsoAddress> {
    public HasIsoAddress(@Nonnull Matcher<? super IsoAddress> subMatcher) {
      super(subMatcher, "An IsisInterfaceSettings with isoAddress:", "isoAddress");
    }

    @Override
    protected IsoAddress featureValueOf(IsisInterfaceSettings actual) {
      return actual.getIsoAddress();
    }
  }

  private static final class HasLevel1
      extends FeatureMatcher<IsisInterfaceSettings, IsisInterfaceLevelSettings> {
    public HasLevel1(@Nonnull Matcher<? super IsisInterfaceLevelSettings> subMatcher) {
      super(subMatcher, "An IsisInterfaceSettings with level1:", "level1");
    }

    @Override
    protected IsisInterfaceLevelSettings featureValueOf(IsisInterfaceSettings actual) {
      return actual.getLevel1();
    }
  }

  private static final class HasLevel2
      extends FeatureMatcher<IsisInterfaceSettings, IsisInterfaceLevelSettings> {
    public HasLevel2(@Nonnull Matcher<? super IsisInterfaceLevelSettings> subMatcher) {
      super(subMatcher, "An IsisInterfaceSettings with level2:", "level2");
    }

    @Override
    protected IsisInterfaceLevelSettings featureValueOf(IsisInterfaceSettings actual) {
      return actual.getLevel2();
    }
  }
}

package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.isis.IsisHelloAuthenticationType;
import org.batfish.datamodel.isis.IsisInterfaceLevelSettings;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IsisInterfaceLevelSettingsMatchers {

  /**
   * Provides a matcher that matches when the {@link IsisInterfaceLevelSettings}'s cost is {@code
   * expectedCost}.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasCost(@Nullable Long expectedCost) {
    return new HasCost(equalTo(expectedCost));
  }

  /**
   * Provides a matcher that matches when the provided {@code subMatcher} matches the {@link
   * IsisInterfaceLevelSettings}'s cost.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasCost(
      @Nonnull Matcher<? super Long> subMatcher) {
    return new HasCost(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@link IsisInterfaceLevelSettings}'s
   * helloAuthenticationType is {@code expectedHelloAuthenticationType}.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasHelloAuthenticationType(
      @Nullable IsisHelloAuthenticationType expectedHelloAuthenticationType) {
    return new HasHelloAuthenticationType(equalTo(expectedHelloAuthenticationType));
  }

  /**
   * Provides a matcher that matches when the provided {@code subMatcher} matches the {@link
   * IsisInterfaceLevelSettings}'s helloAuthenticationType.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasHelloAuthenticationType(
      @Nonnull Matcher<? super IsisHelloAuthenticationType> subMatcher) {
    return new HasHelloAuthenticationType(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link IsisInterfaceSettings}'s helloInterval is {@code
   * expectedHelloInterval}.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasHelloInterval(
      @Nullable Integer expectedHelloInterval) {
    return new HasHelloInterval(equalTo(expectedHelloInterval));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * IsisInterfaceSettings}'s helloInterval.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasHelloInterval(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasHelloInterval(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@link IsisInterfaceLevelSettings}'s holdTime is
   * {@code expectedHoldTime}.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasHoldTime(
      @Nullable Integer expectedHoldTime) {
    return new HasHoldTime(equalTo(expectedHoldTime));
  }

  /**
   * Provides a matcher that matches when the provided {@code subMatcher} matches the {@link
   * IsisInterfaceLevelSettings}'s holdTime.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasHoldTime(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasHoldTime(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@link IsisInterfaceLevelSettings}'s mode is {@code
   * expectedMode}.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasMode(
      @Nullable IsisInterfaceMode expectedMode) {
    return new HasMode(equalTo(expectedMode));
  }

  /**
   * Provides a matcher that matches when the provided {@code subMatcher} matches the {@link
   * IsisInterfaceLevelSettings}'s mode.
   */
  public static @Nonnull Matcher<IsisInterfaceLevelSettings> hasMode(
      @Nonnull Matcher<? super IsisInterfaceMode> subMatcher) {
    return new HasMode(subMatcher);
  }

  private IsisInterfaceLevelSettingsMatchers() {}

  private static final class HasCost extends FeatureMatcher<IsisInterfaceLevelSettings, Long> {
    public HasCost(Matcher<? super Long> subMatcher) {
      super(subMatcher, "An IsisInterfaceLevelSettings with cost:", "cost");
    }

    @Override
    protected Long featureValueOf(IsisInterfaceLevelSettings actual) {
      return actual.getCost();
    }
  }

  private static final class HasHelloAuthenticationType
      extends FeatureMatcher<IsisInterfaceLevelSettings, IsisHelloAuthenticationType> {
    public HasHelloAuthenticationType(Matcher<? super IsisHelloAuthenticationType> subMatcher) {
      super(
          subMatcher,
          "An IsisInterfaceLevelSettings with helloAuthenticationType:",
          "helloAuthenticationType");
    }

    @Override
    protected IsisHelloAuthenticationType featureValueOf(IsisInterfaceLevelSettings actual) {
      return actual.getHelloAuthenticationType();
    }
  }

  private static final class HasHelloInterval
      extends FeatureMatcher<IsisInterfaceLevelSettings, Integer> {
    public HasHelloInterval(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "An IsisInterfaceSettings with helloInterval:", "helloInterval");
    }

    @Override
    protected Integer featureValueOf(IsisInterfaceLevelSettings actual) {
      return actual.getHelloInterval();
    }
  }

  private static final class HasHoldTime
      extends FeatureMatcher<IsisInterfaceLevelSettings, Integer> {
    public HasHoldTime(Matcher<? super Integer> subMatcher) {
      super(subMatcher, "An IsisInterfaceLevelSettings with holdTime:", "holdTime");
    }

    @Override
    protected Integer featureValueOf(IsisInterfaceLevelSettings actual) {
      return actual.getHoldTime();
    }
  }

  private static final class HasMode
      extends FeatureMatcher<IsisInterfaceLevelSettings, IsisInterfaceMode> {
    public HasMode(Matcher<? super IsisInterfaceMode> subMatcher) {
      super(subMatcher, "An IsisInterfaceLevelSettings with mode:", "mode");
    }

    @Override
    protected IsisInterfaceMode featureValueOf(IsisInterfaceLevelSettings actual) {
      return actual.getMode();
    }
  }
}

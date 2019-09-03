package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.tracking.TrackAction;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class HsrpGroupMatchers {

  private static final class HasAuthentication extends FeatureMatcher<HsrpGroup, String> {
    public HasAuthentication(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An HsrpGroup with authentication:", "authentication");
    }

    @Override
    protected String featureValueOf(HsrpGroup actual) {
      return actual.getAuthentication();
    }
  }

  private static final class HasHelloTime extends FeatureMatcher<HsrpGroup, Integer> {
    public HasHelloTime(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "An HsrpGroup with helloTime:", "helloTime");
    }

    @Override
    protected Integer featureValueOf(HsrpGroup actual) {
      return actual.getHelloTime();
    }
  }

  private static final class HasHoldTime extends FeatureMatcher<HsrpGroup, Integer> {
    public HasHoldTime(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "An HsrpGroup with holdTime:", "holdTime");
    }

    @Override
    protected Integer featureValueOf(HsrpGroup actual) {
      return actual.getHoldTime();
    }
  }

  private static final class HasIp extends FeatureMatcher<HsrpGroup, Ip> {
    public HasIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An HsrpGroup with ip:", "ip");
    }

    @Override
    protected Ip featureValueOf(HsrpGroup actual) {
      return actual.getIp();
    }
  }

  private static final class HasPreempt extends FeatureMatcher<HsrpGroup, Boolean> {
    public HasPreempt(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An HsrpGroup with preempt:", "preempt");
    }

    @Override
    protected Boolean featureValueOf(HsrpGroup actual) {
      return actual.getPreempt();
    }
  }

  private static final class HasPriority extends FeatureMatcher<HsrpGroup, Integer> {
    public HasPriority(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "An HsrpGroup with priority:", "priority");
    }

    @Override
    protected Integer featureValueOf(HsrpGroup actual) {
      return actual.getPriority();
    }
  }

  private static final class HasTrackActions
      extends FeatureMatcher<HsrpGroup, SortedMap<String, TrackAction>> {
    public HasTrackActions(@Nonnull Matcher<? super SortedMap<String, TrackAction>> subMatcher) {
      super(subMatcher, "An HsrpGroup with trackActions:", "trackActions");
    }

    @Override
    protected SortedMap<String, TrackAction> featureValueOf(HsrpGroup actual) {
      return actual.getTrackActions();
    }
  }

  /**
   * Provides a matcher that matches if the {@link HsrpGroup}'s SHA256-hashed authentication string
   * is equal to {@code expectedAuthentication}.
   */
  public static @Nonnull Matcher<HsrpGroup> hasAuthentication(
      @Nullable String expectedAuthentication) {
    return new HasAuthentication(equalTo(expectedAuthentication));
  }

  /**
   * Provides a matcher that matches if the {@link HsrpGroup}'s helloTime is equal to {@code
   * expectedHoldTime}.
   */
  public static @Nonnull Matcher<HsrpGroup> hasHelloTime(int expectedHelloTime) {
    return new HasHelloTime(equalTo(expectedHelloTime));
  }

  /**
   * Provides a matcher that matches if the {@link HsrpGroup}'s holdTime is equal to {@code
   * expectedHoldTime}.
   */
  public static @Nonnull Matcher<HsrpGroup> hasHoldTime(int expectedHoldTime) {
    return new HasHoldTime(equalTo(expectedHoldTime));
  }

  /**
   * Provides a matcher that matches if the {@link HsrpGroup}'s ip is equal to {@code expectedIp}.
   */
  public static @Nonnull Matcher<HsrpGroup> hasIp(@Nonnull Ip expectedIp) {
    return new HasIp(equalTo(expectedIp));
  }

  /** Provides a matcher that matches if the {@link HsrpGroup} is set to preempt. */
  public static @Nonnull Matcher<HsrpGroup> hasPreempt() {
    return hasPreempt(true);
  }

  /** Provides a matcher that matches if the {@link HsrpGroup} is set to the given value. */
  public static @Nonnull Matcher<HsrpGroup> hasPreempt(boolean value) {
    return new HasPreempt(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link HsrpGroup}'s priority is equal to {@code
   * expectedPriority}.
   */
  public static @Nonnull Matcher<HsrpGroup> hasPriority(int expectedPriority) {
    return new HasPriority(equalTo(expectedPriority));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * HsrpGroup}'s {@code trackActions}.
   */
  public static @Nonnull Matcher<HsrpGroup> hasTrackActions(
      @Nonnull Matcher<? super SortedMap<String, TrackAction>> subMatcher) {
    return new HasTrackActions(subMatcher);
  }

  private HsrpGroupMatchers() {}
}

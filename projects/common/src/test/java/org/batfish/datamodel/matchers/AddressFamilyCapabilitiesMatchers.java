package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.AllowRemoteAsOutMode;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for {@link AddressFamilyCapabilities} */
@ParametersAreNonnullByDefault
public final class AddressFamilyCapabilitiesMatchers {

  /**
   * Provides a matcher that matches if the {@link AddressFamilyCapabilities}'s allowLocalAsIn is
   * {@code value}.
   */
  public static @Nonnull Matcher<AddressFamilyCapabilities> hasAllowLocalAsIn(boolean value) {
    return new HasAllowLocalAsIn(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamilyCapabilities}'s allowRemoteAsOut is
   * {@code value}.
   */
  public static @Nonnull Matcher<AddressFamilyCapabilities> hasAllowRemoteAsOut(
      AllowRemoteAsOutMode value) {
    return new HasAllowRemoteAsOut(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamilyCapabilities}'s sendCommunity is
   * equal to the given value.
   */
  public static @Nonnull Matcher<AddressFamilyCapabilities> hasSendCommunity(boolean value) {
    return new HasSendCommunity(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamilyCapabilities}'s
   * sendExtendedCommunity is equal to the given value.
   */
  public static @Nonnull Matcher<AddressFamilyCapabilities> hasSendExtendedCommunity(
      boolean value) {
    return new HasSendExtendedCommunity(equalTo(value));
  }

  private AddressFamilyCapabilitiesMatchers() {}

  private static final class HasAllowLocalAsIn
      extends FeatureMatcher<AddressFamilyCapabilities, Boolean> {
    HasAllowLocalAsIn(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AddressFamilyCapabilities with allowLocalAsIn:", "allowLocalAsIn");
    }

    @Override
    protected Boolean featureValueOf(AddressFamilyCapabilities actual) {
      return actual.getAllowLocalAsIn();
    }
  }

  private static final class HasAllowRemoteAsOut
      extends FeatureMatcher<AddressFamilyCapabilities, AllowRemoteAsOutMode> {
    HasAllowRemoteAsOut(@Nonnull Matcher<? super AllowRemoteAsOutMode> subMatcher) {
      super(subMatcher, "An AddressFamilyCapabilities with allowRemoteAsOut:", "allowRemoteAsOut");
    }

    @Override
    protected AllowRemoteAsOutMode featureValueOf(AddressFamilyCapabilities actual) {
      return actual.getAllowRemoteAsOut();
    }
  }

  private static final class HasSendCommunity
      extends FeatureMatcher<AddressFamilyCapabilities, Boolean> {
    HasSendCommunity(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AddressFamilyCapabilities with sendCommunity:", "sendCommunity");
    }

    @Override
    protected Boolean featureValueOf(AddressFamilyCapabilities actual) {
      return actual.getSendCommunity();
    }
  }

  private static final class HasSendExtendedCommunity
      extends FeatureMatcher<AddressFamilyCapabilities, Boolean> {
    HasSendExtendedCommunity(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(
          subMatcher,
          "An AddressFamilyCapabilities with sendExtendedCommunity:",
          "sendExtendedCommunity");
    }

    @Override
    protected Boolean featureValueOf(AddressFamilyCapabilities actual) {
      return actual.getSendExtendedCommunity();
    }
  }
}

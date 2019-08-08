package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchersImpl.HasAllowLocalAsIn;
import org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchersImpl.HasAllowRemoteAsOut;
import org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchersImpl.HasSendCommunity;
import org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchersImpl.HasSendExtendedCommunity;
import org.hamcrest.Matcher;

/** Matchers for {@link AddressFamilyCapabilities} */
public final class AddressFamilyCapabilitiesMatchers {

  /**
   * Provides a matcher that matches if the {@link AddressFamilyCapabilities}'s allowLocalAsIn is
   * {@code value}.
   */
  @Nonnull
  public static Matcher<AddressFamilyCapabilities> hasAllowLocalAsIn(boolean value) {
    return new HasAllowLocalAsIn(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamilyCapabilities}'s allowRemoteAsOut is
   * {@code value}.
   */
  @Nonnull
  public static Matcher<AddressFamilyCapabilities> hasAllowRemoteAsOut(boolean value) {
    return new HasAllowRemoteAsOut(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamilyCapabilities}'s sendCommunity is
   * equal to the given value.
   */
  @Nonnull
  public static Matcher<AddressFamilyCapabilities> hasSendCommunity(boolean value) {
    return new HasSendCommunity(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamilyCapabilities}'s
   * sendExtendedCommunity is equal to the given value.
   */
  @Nonnull
  public static Matcher<AddressFamilyCapabilities> hasSendExtendedCommunity(boolean value) {
    return new HasSendExtendedCommunity(equalTo(value));
  }

  private AddressFamilyCapabilitiesMatchers() {}
}

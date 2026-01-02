package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.AllowRemoteAsOutMode;
import org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchersImpl.HasAllowLocalAsIn;
import org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchersImpl.HasAllowRemoteAsOut;
import org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchersImpl.HasSendCommunity;
import org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchersImpl.HasSendExtendedCommunity;
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
}

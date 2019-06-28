package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.bgp.AddressFamilySettings;
import org.batfish.datamodel.matchers.AddressFamilySettingsMatchersImpl.HasAllowLocalAsIn;
import org.batfish.datamodel.matchers.AddressFamilySettingsMatchersImpl.HasAllowRemoteAsOut;
import org.batfish.datamodel.matchers.AddressFamilySettingsMatchersImpl.HasSendCommunity;
import org.hamcrest.Matcher;

/** Matchers for {@link AddressFamilySettings} */
public final class AddressFamilySettingsMatchers {

  /**
   * Provides a matcher that matches if the {@link AddressFamilySettings}'s allowLocalAsIn is {@code
   * value}.
   */
  public static HasAllowLocalAsIn hasAllowLocalAsIn(boolean value) {
    return new HasAllowLocalAsIn(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamilySettings}'s allowRemoteAsOut is
   * {@code value}.
   */
  public static HasAllowRemoteAsOut hasAllowRemoteAsOut(boolean value) {
    return new HasAllowRemoteAsOut(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link AddressFamilySettings}'s sendCommunity is equal
   * to the given value.
   */
  public static Matcher<AddressFamilySettings> hasSendCommunity(boolean value) {
    return new HasSendCommunity(equalTo(value));
  }

  private AddressFamilySettingsMatchers() {}
}

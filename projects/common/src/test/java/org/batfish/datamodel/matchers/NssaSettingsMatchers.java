package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.matchers.NssaSettingsMatchersImpl.HasDefaultOriginateType;
import org.batfish.datamodel.matchers.NssaSettingsMatchersImpl.HasSuppressType3;
import org.batfish.datamodel.matchers.NssaSettingsMatchersImpl.HasSuppressType7;
import org.batfish.datamodel.ospf.NssaSettings;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.hamcrest.Matcher;

public final class NssaSettingsMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * NssaSettings}'s defaultOriginateType.
   */
  public static @Nonnull Matcher<NssaSettings> hasDefaultOriginateType(
      @Nonnull Matcher<? super OspfDefaultOriginateType> subMatcher) {
    return new HasDefaultOriginateType(subMatcher);
  }

  /**
   * Provides a matcher that matches if the the {@link NssaSettings}'s defaultOriginateType is
   * {@code expectedDefaultOriginateType}.
   */
  public static @Nonnull Matcher<NssaSettings> hasDefaultOriginateType(
      @Nonnull OspfDefaultOriginateType expectedDefaultOriginateType) {
    return new HasDefaultOriginateType(equalTo(expectedDefaultOriginateType));
  }

  /**
   * Provides a matcher that matches if the the {@link NssaSettings}'s suppressType3 is {@code
   * true}.
   */
  public static @Nonnull Matcher<NssaSettings> hasSuppressType3() {
    return new HasSuppressType3(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the the {@link NssaSettings}'s suppressType3 is {@code
   * expectedSuppressType3}.
   */
  public static @Nonnull Matcher<NssaSettings> hasSuppressType3(boolean expectedSuppressType3) {
    return new HasSuppressType3(equalTo(expectedSuppressType3));
  }

  /**
   * Provides a matcher that matches if the the {@link NssaSettings}'s suppressType7 is {@code
   * expectedSuppressType7}.
   */
  public static @Nonnull Matcher<NssaSettings> hasSuppressType7(boolean expectedSuppressType7) {
    return new HasSuppressType7(equalTo(expectedSuppressType7));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * NssaSettings}'s suppressType3.
   */
  public static @Nonnull Matcher<NssaSettings> hasSuppressType3(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasSuppressType3(subMatcher);
  }

  private NssaSettingsMatchers() {}
}

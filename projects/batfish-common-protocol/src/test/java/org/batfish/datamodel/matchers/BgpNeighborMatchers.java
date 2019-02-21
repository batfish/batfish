package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasAllowLocalAsIn;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasAllowRemoteAsOut;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasClusterId;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasEnforceFirstAs;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasLocalAs;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasRemoteAs;
import org.hamcrest.Matcher;

/** Matchers for {@link BgpPeerConfig} */
@ParametersAreNonnullByDefault
public class BgpNeighborMatchers {

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig}'s allowLocalAsIn is {@code value}.
   */
  public static HasAllowLocalAsIn hasAllowLocalAsIn(boolean value) {
    return new HasAllowLocalAsIn(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig}'s allowRemoteAsOut is {@code
   * value}.
   */
  public static HasAllowRemoteAsOut hasAllowRemoteAsOut(boolean value) {
    return new HasAllowRemoteAsOut(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig}'s clusterId is {@code
   * expectedClusterId}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasClusterId(@Nullable Long expectedClusterId) {
    return new HasClusterId(equalTo(expectedClusterId));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig} has the specified {@code
   * expectedExportPolicy}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasExportPolicy(String expectedExportPolicy) {
    return new HasExportPolicy(equalTo(expectedExportPolicy));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig} has the specified {@code
   * expectedDescription}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasDescription(String expectedDescription) {
    return new HasDescription(equalTo(expectedDescription));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig} has the specified {@code
   * expectedLocalIp}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasLocalIp(String expectedLocalIp) {
    return new HasLocalIp(equalTo(expectedLocalIp));
  }

  /** Provides a matcher that matches if the BGP neighbor has the specified localAs. */
  public static HasLocalAs hasLocalAs(Long localAs) {
    return new HasLocalAs(equalTo(localAs));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP neighbor's
   * localAs.
   */
  public static HasLocalAs hasLocalAs(Matcher<? super Long> subMatcher) {
    return new HasLocalAs(subMatcher);
  }

  /**
   * Provides a matcher that matches if the BGP neighbor's value of {@code enforceFirstAs} matches
   * {@code subMatcher}
   */
  public static HasEnforceFirstAs hasEnforceFirstAs(Matcher<? super Boolean> subMatcher) {
    return new HasEnforceFirstAs(subMatcher);
  }

  /**
   * Provides a matcher that matches if the BGP neighbor's value of {@code enforceFirstAs} is {@code
   * true}.
   */
  public static HasEnforceFirstAs hasEnforceFirstAs() {
    return new HasEnforceFirstAs(equalTo(true));
  }

  /** Provides a matcher that matches if the BGP neighbor has the specified remoteAs. */
  public static HasRemoteAs hasRemoteAs(Long remoteAs) {
    return new HasRemoteAs(equalTo(remoteAs));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP neighbor's
   * remoteAs.
   */
  public static HasRemoteAs hasRemoteAs(Matcher<? super Long> subMatcher) {
    return new HasRemoteAs(subMatcher);
  }
}

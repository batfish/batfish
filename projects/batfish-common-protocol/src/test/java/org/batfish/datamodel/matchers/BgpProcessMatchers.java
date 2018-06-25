package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.matchers.BgpProcessMatchersImpl.HasMultipathEbgp;
import org.batfish.datamodel.matchers.BgpProcessMatchersImpl.HasMultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.matchers.BgpProcessMatchersImpl.HasMultipathIbgp;
import org.batfish.datamodel.matchers.BgpProcessMatchersImpl.HasNeighbor;
import org.batfish.datamodel.matchers.BgpProcessMatchersImpl.HasNeighbors;
import org.batfish.datamodel.matchers.BgpProcessMatchersImpl.HasRouterId;
import org.hamcrest.Matcher;

/** {@link Matcher Hamcrest matchers} for {@link BgpProcess}. */
public class BgpProcessMatchers {

  /**
   * Provides a matcher that matches if the provided {@code value} matches whether the BGP process
   * uses multipath for EBGP.
   */
  public static HasMultipathEbgp hasMultipathEbgp(boolean value) {
    return new HasMultipathEbgp(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches whether the BGP
   * process uses multipath for EBGP.
   */
  public static HasMultipathEbgp hasMultipathEbgp(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasMultipathEbgp(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provides {@code subMatcher} matches whther the BGP
   * process has specified {@link MultipathEquivalentAsPathMatchMode}.
   */
  public static HasMultipathEquivalentAsPathMatchMode hasMultipathEquivalentAsPathMatchMode(
      MultipathEquivalentAsPathMatchMode mode) {
    return new HasMultipathEquivalentAsPathMatchMode(equalTo(mode));
  }

  /**
   * Provides a matcher that matches if the provided {@code value} matches whether the BGP process
   * uses multipath for IBGP.
   */
  public static HasMultipathIbgp hasMultipathIbgp(boolean value) {
    return new HasMultipathIbgp(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches whether the BGP
   * process uses multipath for IBGP.
   */
  public static HasMultipathIbgp hasMultipathIbgp(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasMultipathIbgp(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP process's
   * neighbor with specified prefix.
   */
  public static HasNeighbor hasNeighbor(
      @Nonnull Prefix prefix, @Nonnull Matcher<? super BgpPeerConfig> subMatcher) {
    return new HasNeighbor(prefix, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP process's
   * neighbors.
   */
  public static HasNeighbors hasNeighbors(
      @Nonnull Matcher<? super Map<Prefix, BgpPeerConfig>> subMatcher) {
    return new HasNeighbors(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP process's
   * router id.
   */
  public static HasRouterId hasRouterId(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasRouterId(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code ip} matches the BGP process's router id.
   */
  public static HasRouterId hasRouterId(Ip ip) {
    return new HasRouterId(equalTo(ip));
  }
}

package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** {@link Matcher Hamcrest matchers} for {@link BgpProcess}. */
public class BgpProcessMatchers {

  /**
   * Provides a matcher that matches if the BGP process's interfaceNeighbor with the specified
   * {@code peerInterface} is matched by the provided {@code subMatcher}.
   */
  public static Matcher<BgpProcess> hasInterfaceNeighbor(
      @Nonnull String peerInterface, @Nonnull Matcher<? super BgpUnnumberedPeerConfig> subMatcher) {
    return new HasInterfaceNeighbor(peerInterface, subMatcher);
  }

  /**
   * Provides a matcher that matches if the BGP process's interfaceNeighbors are matched by the
   * provided {@code subMatcher}.
   */
  public static Matcher<BgpProcess> hasInterfaceNeighbors(
      @Nonnull Matcher<? super Map<String, BgpUnnumberedPeerConfig>> subMatcher) {
    return new HasInterfaceNeighbors(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code value} matches whether the BGP process
   * uses multipath for EBGP.
   */
  public static Matcher<BgpProcess> hasMultipathEbgp(boolean value) {
    return new HasMultipathEbgp(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches whether the BGP
   * process uses multipath for EBGP.
   */
  public static Matcher<BgpProcess> hasMultipathEbgp(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasMultipathEbgp(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provides {@code subMatcher} matches whther the BGP
   * process has specified {@link MultipathEquivalentAsPathMatchMode}.
   */
  public static Matcher<BgpProcess> hasMultipathEquivalentAsPathMatchMode(
      MultipathEquivalentAsPathMatchMode mode) {
    return new HasMultipathEquivalentAsPathMatchMode(equalTo(mode));
  }

  /**
   * Provides a matcher that matches if the provided {@code value} matches whether the BGP process
   * uses multipath for IBGP.
   */
  public static Matcher<BgpProcess> hasMultipathIbgp(boolean value) {
    return new HasMultipathIbgp(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches whether the BGP
   * process uses multipath for IBGP.
   */
  public static Matcher<BgpProcess> hasMultipathIbgp(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasMultipathIbgp(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP process's
   * neighbor with specified prefix.
   */
  public static Matcher<BgpProcess> hasActiveNeighbor(
      @Nonnull Ip ip, @Nonnull Matcher<? super BgpActivePeerConfig> subMatcher) {
    return new HasActiveNeighbor(ip, subMatcher);
  }

  public static Matcher<BgpProcess> hasPassiveNeighbor(
      @Nonnull Prefix prefix, @Nonnull Matcher<? super BgpPassivePeerConfig> subMatcher) {
    return new HasPassiveNeighbor(prefix, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP process's
   * neighbors.
   */
  public static Matcher<BgpProcess> hasNeighbors(
      @Nonnull Matcher<? super Map<Ip, BgpActivePeerConfig>> subMatcher) {
    return new HasNeighbors(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP process's
   * router id.
   */
  public static Matcher<BgpProcess> hasRouterId(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasRouterId(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code ip} matches the BGP process's router id.
   */
  public static Matcher<BgpProcess> hasRouterId(Ip ip) {
    return new HasRouterId(equalTo(ip));
  }

  private static final class HasInterfaceNeighbor
      extends FeatureMatcher<BgpProcess, BgpUnnumberedPeerConfig> {
    private final @Nonnull String _peerInterface;

    HasInterfaceNeighbor(
        @Nonnull String peerInterface,
        @Nonnull Matcher<? super BgpUnnumberedPeerConfig> subMatcher) {
      super(
          subMatcher,
          String.format("A BGP process with interfaceNeighbor %s:", peerInterface),
          String.format("interfaceNeighbor %s:", peerInterface));
      _peerInterface = peerInterface;
    }

    @Override
    protected BgpUnnumberedPeerConfig featureValueOf(BgpProcess actual) {
      return actual.getInterfaceNeighbors().get(_peerInterface);
    }
  }

  private static final class HasInterfaceNeighbors
      extends FeatureMatcher<BgpProcess, Map<String, BgpUnnumberedPeerConfig>> {
    HasInterfaceNeighbors(
        @Nonnull Matcher<? super Map<String, BgpUnnumberedPeerConfig>> subMatcher) {
      super(subMatcher, "A BGP process with interfaceNeighbors:", "interfaceNeighbors:");
    }

    @Override
    protected Map<String, BgpUnnumberedPeerConfig> featureValueOf(BgpProcess actual) {
      return actual.getInterfaceNeighbors();
    }
  }

  private static final class HasMultipathEbgp extends FeatureMatcher<BgpProcess, Boolean> {
    HasMultipathEbgp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BGP process with multipath EBGP:", "multipath EBGP");
    }

    @Override
    protected Boolean featureValueOf(BgpProcess actual) {
      return actual.getMultipathEbgp();
    }
  }

  private static final class HasMultipathIbgp extends FeatureMatcher<BgpProcess, Boolean> {
    HasMultipathIbgp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BGP process with multipath IBGP:", "multipath IBGP");
    }

    @Override
    protected Boolean featureValueOf(BgpProcess actual) {
      return actual.getMultipathIbgp();
    }
  }

  private static final class HasActiveNeighbor
      extends FeatureMatcher<BgpProcess, BgpActivePeerConfig> {
    private final Ip _ip;

    HasActiveNeighbor(@Nonnull Ip ip, @Nonnull Matcher<? super BgpActivePeerConfig> subMatcher) {
      super(subMatcher, "A BGP process with active neighbor " + ip + ":", "neighbor " + ip);
      _ip = ip;
    }

    @Override
    protected BgpActivePeerConfig featureValueOf(BgpProcess actual) {
      return actual.getActiveNeighbors().get(_ip);
    }
  }

  private static final class HasPassiveNeighbor
      extends FeatureMatcher<BgpProcess, BgpPassivePeerConfig> {
    private final Prefix _prefix;

    HasPassiveNeighbor(
        @Nonnull Prefix prefix, @Nonnull Matcher<? super BgpPassivePeerConfig> subMatcher) {
      super(
          subMatcher, "A BGP process with passive neighbor " + prefix + ":", "neighbor " + prefix);
      _prefix = prefix;
    }

    @Override
    protected BgpPassivePeerConfig featureValueOf(BgpProcess actual) {
      return actual.getPassiveNeighbors().get(_prefix);
    }
  }

  private static final class HasNeighbors
      extends FeatureMatcher<BgpProcess, Map<Ip, BgpActivePeerConfig>> {
    HasNeighbors(@Nonnull Matcher<? super Map<Ip, BgpActivePeerConfig>> subMatcher) {
      super(subMatcher, "A BGP process with neighbors:", "neighbors");
    }

    @Override
    protected Map<Ip, BgpActivePeerConfig> featureValueOf(BgpProcess actual) {
      return actual.getActiveNeighbors();
    }
  }

  private static final class HasRouterId extends FeatureMatcher<BgpProcess, Ip> {
    HasRouterId(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A BGP process with router id:", "router id");
    }

    @Override
    protected Ip featureValueOf(BgpProcess actual) {
      return actual.getRouterId();
    }
  }

  private static final class HasMultipathEquivalentAsPathMatchMode
      extends FeatureMatcher<BgpProcess, MultipathEquivalentAsPathMatchMode> {
    public HasMultipathEquivalentAsPathMatchMode(
        Matcher<? super MultipathEquivalentAsPathMatchMode> subMatcher) {
      super(
          subMatcher,
          "A BGP process with multipath equivalency match mode:",
          "multipath equivalency match mode");
    }

    @Override
    protected MultipathEquivalentAsPathMatchMode featureValueOf(BgpProcess bgpProcess) {
      return bgpProcess.getMultipathEquivalentAsPathMatchMode();
    }
  }
}

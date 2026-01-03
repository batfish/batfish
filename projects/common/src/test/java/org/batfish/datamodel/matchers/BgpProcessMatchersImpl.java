package org.batfish.datamodel.matchers;

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

final class BgpProcessMatchersImpl {

  static final class HasInterfaceNeighbor
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

  static final class HasInterfaceNeighbors
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

  static final class HasMultipathEbgp extends FeatureMatcher<BgpProcess, Boolean> {
    HasMultipathEbgp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BGP process with multipath EBGP:", "multipath EBGP");
    }

    @Override
    protected Boolean featureValueOf(BgpProcess actual) {
      return actual.getMultipathEbgp();
    }
  }

  static final class HasMultipathIbgp extends FeatureMatcher<BgpProcess, Boolean> {
    HasMultipathIbgp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BGP process with multipath IBGP:", "multipath IBGP");
    }

    @Override
    protected Boolean featureValueOf(BgpProcess actual) {
      return actual.getMultipathIbgp();
    }
  }

  static final class HasActiveNeighbor extends FeatureMatcher<BgpProcess, BgpActivePeerConfig> {
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

  static final class HasPassiveNeighbor extends FeatureMatcher<BgpProcess, BgpPassivePeerConfig> {
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

  static final class HasNeighbors extends FeatureMatcher<BgpProcess, Map<Ip, BgpActivePeerConfig>> {
    HasNeighbors(@Nonnull Matcher<? super Map<Ip, BgpActivePeerConfig>> subMatcher) {
      super(subMatcher, "A BGP process with neighbors:", "neighbors");
    }

    @Override
    protected Map<Ip, BgpActivePeerConfig> featureValueOf(BgpProcess actual) {
      return actual.getActiveNeighbors();
    }
  }

  static final class HasRouterId extends FeatureMatcher<BgpProcess, Ip> {
    HasRouterId(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A BGP process with router id:", "router id");
    }

    @Override
    protected Ip featureValueOf(BgpProcess actual) {
      return actual.getRouterId();
    }
  }

  static final class HasMultipathEquivalentAsPathMatchMode
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

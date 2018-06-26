package org.batfish.datamodel.matchers;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Prefix;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class BgpProcessMatchersImpl {

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

  static final class HasNeighbor extends FeatureMatcher<BgpProcess, BgpPeerConfig> {
    private final Prefix _prefix;

    HasNeighbor(@Nonnull Prefix prefix, @Nonnull Matcher<? super BgpPeerConfig> subMatcher) {
      super(subMatcher, "A BGP process with neighbor " + prefix + ":", "neighbor " + prefix);
      _prefix = prefix;
    }

    @Override
    protected BgpPeerConfig featureValueOf(BgpProcess actual) {
      return actual.getNeighbors().get(_prefix);
    }
  }

  static final class HasNeighbors extends FeatureMatcher<BgpProcess, Map<Prefix, BgpPeerConfig>> {
    HasNeighbors(@Nonnull Matcher<? super Map<Prefix, BgpPeerConfig>> subMatcher) {
      super(subMatcher, "A BGP process with neighbors:", "neighbors");
    }

    @Override
    protected Map<Prefix, BgpPeerConfig> featureValueOf(BgpProcess actual) {
      return actual.getNeighbors();
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

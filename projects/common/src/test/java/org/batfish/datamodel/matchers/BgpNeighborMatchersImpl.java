package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class BgpNeighborMatchersImpl {

  static final class HasClusterId extends FeatureMatcher<BgpPeerConfig, Long> {
    HasClusterId(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with clusterId:", "clusterId");
    }

    @Override
    protected Long featureValueOf(BgpPeerConfig actual) {
      return actual.getClusterId();
    }
  }

  static final class HasDescription extends FeatureMatcher<BgpPeerConfig, String> {
    HasDescription(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with description:", "description");
    }

    @Override
    protected String featureValueOf(BgpPeerConfig actual) {
      return actual.getDescription();
    }
  }

  static final class HasIpv4UnicastAddressFamily
      extends FeatureMatcher<BgpPeerConfig, Ipv4UnicastAddressFamily> {
    HasIpv4UnicastAddressFamily(@Nonnull Matcher<? super Ipv4UnicastAddressFamily> subMatcher) {
      super(
          subMatcher, "A BgpPeerConfig with ipv4UnicastAddressFamily:", "ipv4UnicastAddressFamily");
    }

    @Override
    protected Ipv4UnicastAddressFamily featureValueOf(BgpPeerConfig actual) {
      return actual.getIpv4UnicastAddressFamily();
    }
  }

  static final class HasEvpnAddressFamily extends FeatureMatcher<BgpPeerConfig, EvpnAddressFamily> {
    HasEvpnAddressFamily(@Nonnull Matcher<? super EvpnAddressFamily> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with evpnAddressFamily:", "evpnAddressFamily");
    }

    @Override
    protected EvpnAddressFamily featureValueOf(BgpPeerConfig actual) {
      return actual.getEvpnAddressFamily();
    }
  }

  static final class HasLocalAs extends FeatureMatcher<BgpPeerConfig, Long> {
    HasLocalAs(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with localAs:", "localAs");
    }

    @Override
    protected Long featureValueOf(BgpPeerConfig actual) {
      return actual.getLocalAs();
    }
  }

  static final class HasLocalIp extends FeatureMatcher<BgpPeerConfig, Ip> {
    HasLocalIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with localIp:", "localIp");
    }

    @Override
    protected Ip featureValueOf(BgpPeerConfig actual) {
      return actual.getLocalIp();
    }
  }

  static final class HasEnforceFirstAs extends FeatureMatcher<BgpPeerConfig, Boolean> {
    HasEnforceFirstAs(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with enforce-first-as:", "enforce-first-as");
    }

    @Override
    protected Boolean featureValueOf(BgpPeerConfig actual) {
      return actual.getEnforceFirstAs();
    }
  }

  static final class HasRemoteAs extends FeatureMatcher<BgpPeerConfig, LongSpace> {
    HasRemoteAs(@Nonnull Matcher<? super LongSpace> subMatcher) {
      super(subMatcher, "A BgpPeerConfig with remoteAs:", "remoteAs");
    }

    @Override
    protected LongSpace featureValueOf(BgpPeerConfig actual) {
      return actual.getRemoteAsns();
    }
  }

  private BgpNeighborMatchersImpl() {}
}

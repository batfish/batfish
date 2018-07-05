package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecStaticPeerConfigMatchersImpl {

  static class HasSourceAddress extends FeatureMatcher<IpsecStaticPeerConfig, Ip> {

    public HasSourceAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec peer config with SourceAddress:", "SourceAddress");
    }

    @Override
    protected Ip featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getSourceAddress();
    }
  }

  static class HasPhysicalInterface extends FeatureMatcher<IpsecStaticPeerConfig, String> {

    public HasPhysicalInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with PhysicalInterface:", "PhysicalInterface");
    }

    @Override
    protected String featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getPhysicalInterface();
    }
  }

  static class HasTunnelInterface extends FeatureMatcher<IpsecStaticPeerConfig, String> {

    public HasTunnelInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with TunnelInterface:", "TunnelInterface");
    }

    @Override
    protected String featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getTunnelInterface();
    }
  }

  static class HasIpsecPolicy extends FeatureMatcher<IpsecStaticPeerConfig, String> {

    public HasIpsecPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with IpsecPolicy:", "IpsecPolicy");
    }

    @Override
    protected String featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getIpsecPolicy();
    }
  }

  static class HasPolicyAccessList extends FeatureMatcher<IpsecStaticPeerConfig, IpAccessList> {

    public HasPolicyAccessList(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "An IPSec peer config with PolicyAccessList:", "PolicyAccessList");
    }

    @Override
    protected IpAccessList featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getPolicyAccessList();
    }
  }

  static class HasIkePhase1Policy extends FeatureMatcher<IpsecStaticPeerConfig, String> {

    public HasIkePhase1Policy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with IkePhase1Policy:", "IkePhase1Policy");
    }

    @Override
    protected String featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getIpsecPolicy();
    }
  }

  static class HasPeerConfig extends FeatureMatcher<IpsecStaticPeerConfig, IpsecPeerConfig> {

    public HasPeerConfig(@Nonnull Matcher<? super IpsecPeerConfig> subMatcher) {
      super(subMatcher, "An IPSec peer config with PeerConfig:", "PeerConfig");
    }

    @Override
    protected IpsecPeerConfig featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getPeerConfig();
    }
  }

  static class HasDestinationAddress extends FeatureMatcher<IpsecStaticPeerConfig, Ip> {

    public HasDestinationAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec peer config with DestinationAddress:", "DestinationAddress");
    }

    @Override
    protected Ip featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getDestinationAddress();
    }
  }
}

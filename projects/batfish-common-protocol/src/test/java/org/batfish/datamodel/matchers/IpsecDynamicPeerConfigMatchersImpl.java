package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecPeerConfig;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecDynamicPeerConfigMatchersImpl {

  static class HasSourceAddress extends FeatureMatcher<IpsecDynamicPeerConfig, Ip> {

    public HasSourceAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec peer config with SourceAddress:", "SourceAddress");
    }

    @Override
    protected Ip featureValueOf(IpsecDynamicPeerConfig actual) {
      return actual.getSourceAddress();
    }
  }

  static class HasPhysicalInterface extends FeatureMatcher<IpsecDynamicPeerConfig, String> {

    public HasPhysicalInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with PhysicalInterface:", "PhysicalInterface");
    }

    @Override
    protected String featureValueOf(IpsecDynamicPeerConfig actual) {
      return actual.getPhysicalInterface();
    }
  }

  static class HasTunnelInterface extends FeatureMatcher<IpsecDynamicPeerConfig, String> {

    public HasTunnelInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with TunnelInterface:", "TunnelInterface");
    }

    @Override
    protected String featureValueOf(IpsecDynamicPeerConfig actual) {
      return actual.getTunnelInterface();
    }
  }

  static class HasIpsecPolicy extends FeatureMatcher<IpsecDynamicPeerConfig, String> {

    public HasIpsecPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with IpsecPolicy:", "IpsecPolicy");
    }

    @Override
    protected String featureValueOf(IpsecDynamicPeerConfig actual) {
      return actual.getIpsecPolicy();
    }
  }

  static class HasPolicyAccessList extends FeatureMatcher<IpsecDynamicPeerConfig, IpAccessList> {

    public HasPolicyAccessList(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "An IPSec peer config with PolicyAccessList:", "PolicyAccessList");
    }

    @Override
    protected IpAccessList featureValueOf(IpsecDynamicPeerConfig actual) {
      return actual.getPolicyAccessList();
    }
  }

  static class HasIkePhase1Policies extends FeatureMatcher<IpsecDynamicPeerConfig, List<String>> {

    public HasIkePhase1Policies(@Nonnull Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "An IPSec peer config with IkePhase1Policies:", "IkePhase1Policies");
    }

    @Override
    protected List<String> featureValueOf(IpsecDynamicPeerConfig actual) {
      return actual.getIkePhase1Poliies();
    }
  }

  static class HasPeerConfigs
      extends FeatureMatcher<IpsecDynamicPeerConfig, List<IpsecPeerConfig>> {

    public HasPeerConfigs(@Nonnull Matcher<? super List<IpsecPeerConfig>> subMatcher) {
      super(subMatcher, "An IPSec peer config with PeerConfigs:", "PeerConfigs");
    }

    @Override
    protected List<IpsecPeerConfig> featureValueOf(IpsecDynamicPeerConfig actual) {
      return actual.getPeerConfigs();
    }
  }
}

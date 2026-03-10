package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecPeerConfigMatchersImpl {

  static class HasLocalAddress extends FeatureMatcher<IpsecPeerConfig, Ip> {

    HasLocalAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec peer config with LocalAddress:", "LocalAddress");
    }

    @Override
    protected Ip featureValueOf(IpsecPeerConfig actual) {
      return actual.getLocalAddress();
    }
  }

  static class HasSourceInterface extends FeatureMatcher<IpsecPeerConfig, String> {

    HasSourceInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with SourceInterface:", "SourceInterface");
    }

    @Override
    protected String featureValueOf(IpsecPeerConfig actual) {
      return actual.getSourceInterface();
    }
  }

  static class HasTunnelInterface extends FeatureMatcher<IpsecPeerConfig, String> {

    HasTunnelInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with TunnelInterface:", "TunnelInterface");
    }

    @Override
    protected String featureValueOf(IpsecPeerConfig actual) {
      return actual.getTunnelInterface();
    }
  }

  static class HasIpsecPolicy extends FeatureMatcher<IpsecPeerConfig, String> {

    HasIpsecPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with IpsecPolicy:", "IpsecPolicy");
    }

    @Override
    protected String featureValueOf(IpsecPeerConfig actual) {
      return actual.getIpsecPolicy();
    }
  }

  static class HasPolicyAccessList extends FeatureMatcher<IpsecPeerConfig, IpAccessList> {

    HasPolicyAccessList(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "An IPSec peer config with PolicyAccessList:", "PolicyAccessList");
    }

    @Override
    protected IpAccessList featureValueOf(IpsecPeerConfig actual) {
      return actual.getPolicyAccessList();
    }
  }

  static class HasIkePhase1Policies extends FeatureMatcher<IpsecDynamicPeerConfig, List<String>> {

    HasIkePhase1Policies(@Nonnull Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "An IPSec peer config with IkePhase1Policies:", "IkePhase1Policies");
    }

    @Override
    protected List<String> featureValueOf(IpsecDynamicPeerConfig actual) {
      return actual.getIkePhase1Poliies();
    }
  }

  static class HasIkePhase1Policy extends FeatureMatcher<IpsecStaticPeerConfig, String> {

    HasIkePhase1Policy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with IkePhase1Policy:", "IkePhase1Policy");
    }

    @Override
    protected String featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getIkePhase1Policy();
    }
  }

  static class HasDestinationAddress extends FeatureMatcher<IpsecStaticPeerConfig, Ip> {

    HasDestinationAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec peer config with DestinationAddress:", "DestinationAddress");
    }

    @Override
    protected Ip featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getDestinationAddress();
    }
  }

  static class IsIpsecDynamicPeerConfig
      extends IsInstanceThat<IpsecPeerConfig, IpsecDynamicPeerConfig> {
    IsIpsecDynamicPeerConfig(@Nonnull Matcher<? super IpsecDynamicPeerConfig> subMatcher) {
      super(IpsecDynamicPeerConfig.class, subMatcher);
    }
  }

  static class IsIpsecStaticPeerConfig
      extends IsInstanceThat<IpsecPeerConfig, IpsecStaticPeerConfig> {
    IsIpsecStaticPeerConfig(@Nonnull Matcher<? super IpsecStaticPeerConfig> subMatcher) {
      super(IpsecStaticPeerConfig.class, subMatcher);
    }
  }
}

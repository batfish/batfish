package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IpsecPeerConfigMatchers {

  /**
   * Provides a matcher that matches if the provided {@code localAddress} matches the IPSec peer
   * config's {@code localAddress}
   */
  public static @Nonnull Matcher<IpsecPeerConfig> hasLocalAddress(Ip localAddress) {
    return new HasLocalAddress(equalTo(localAddress));
  }

  /**
   * Provides a matcher that matches if the provided {@code sourceInterface} matches the IPSec peer
   * config's {@code sourceInterface}
   */
  public static @Nonnull Matcher<IpsecPeerConfig> hasSourceInterface(String sourceInterface) {
    return new HasSourceInterface(equalTo(sourceInterface));
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peer
   * config's {@code tunnelInterface}
   */
  public static @Nonnull Matcher<IpsecPeerConfig> hasTunnelInterface(
      Matcher<? super String> subMatcher) {
    return new HasTunnelInterface(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code ipsecPolicy} matches the IPSec peer
   * config's {@code ipsecPolicy}
   */
  public static @Nonnull Matcher<IpsecPeerConfig> hasIpsecPolicy(String ipsecPolicy) {
    return new HasIpsecPolicy(equalTo(ipsecPolicy));
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peer
   * config's {@code policyAccessList}
   */
  public static @Nonnull Matcher<IpsecPeerConfig> hasPolicyAccessList(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasPolicyAccessList(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peer
   * config's {@code ikePhase1Policies}
   */
  public static @Nonnull Matcher<IpsecDynamicPeerConfig> hasIkePhase1Policies(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasIkePhase1Policies(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peer
   * config's {@code ikePhase1Policy}
   */
  public static @Nonnull Matcher<IpsecStaticPeerConfig> hasIkePhase1Policy(String ikePhase1Policy) {
    return new HasIkePhase1Policy(equalTo(ikePhase1Policy));
  }

  /**
   * Provides a matcher that matches if the provided {@code destinationAddress} matches the IPSec
   * peer config's {@code destinationAddress}
   */
  public static @Nonnull Matcher<IpsecStaticPeerConfig> hasDestinationAddress(
      Ip destinationAddress) {
    return new HasDestinationAddress(equalTo(destinationAddress));
  }

  /**
   * Provides a matcher that matches if the object is a {@link IpsecStaticPeerConfig} matched by the
   * provided {@code subMatcher}.
   */
  public static Matcher<IpsecPeerConfig> isIpsecStaticPeerConfigThat(
      @Nonnull Matcher<? super IpsecStaticPeerConfig> subMatcher) {
    return new IsIpsecStaticPeerConfig(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link IpsecDynamicPeerConfig} matched by
   * the provided {@code subMatcher}.
   */
  public static Matcher<IpsecPeerConfig> isIpsecDynamicPeerConfigThat(
      @Nonnull Matcher<? super IpsecDynamicPeerConfig> subMatcher) {
    return new IsIpsecDynamicPeerConfig(subMatcher);
  }

  private IpsecPeerConfigMatchers() {}

  private static final class HasLocalAddress extends FeatureMatcher<IpsecPeerConfig, Ip> {

    HasLocalAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec peer config with LocalAddress:", "LocalAddress");
    }

    @Override
    protected Ip featureValueOf(IpsecPeerConfig actual) {
      return actual.getLocalAddress();
    }
  }

  private static final class HasSourceInterface extends FeatureMatcher<IpsecPeerConfig, String> {

    HasSourceInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with SourceInterface:", "SourceInterface");
    }

    @Override
    protected String featureValueOf(IpsecPeerConfig actual) {
      return actual.getSourceInterface();
    }
  }

  private static final class HasTunnelInterface extends FeatureMatcher<IpsecPeerConfig, String> {

    HasTunnelInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with TunnelInterface:", "TunnelInterface");
    }

    @Override
    protected String featureValueOf(IpsecPeerConfig actual) {
      return actual.getTunnelInterface();
    }
  }

  private static final class HasIpsecPolicy extends FeatureMatcher<IpsecPeerConfig, String> {

    HasIpsecPolicy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with IpsecPolicy:", "IpsecPolicy");
    }

    @Override
    protected String featureValueOf(IpsecPeerConfig actual) {
      return actual.getIpsecPolicy();
    }
  }

  private static final class HasPolicyAccessList
      extends FeatureMatcher<IpsecPeerConfig, IpAccessList> {

    HasPolicyAccessList(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "An IPSec peer config with PolicyAccessList:", "PolicyAccessList");
    }

    @Override
    protected IpAccessList featureValueOf(IpsecPeerConfig actual) {
      return actual.getPolicyAccessList();
    }
  }

  private static final class HasIkePhase1Policies
      extends FeatureMatcher<IpsecDynamicPeerConfig, List<String>> {

    HasIkePhase1Policies(@Nonnull Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "An IPSec peer config with IkePhase1Policies:", "IkePhase1Policies");
    }

    @Override
    protected List<String> featureValueOf(IpsecDynamicPeerConfig actual) {
      return actual.getIkePhase1Poliies();
    }
  }

  private static final class HasIkePhase1Policy
      extends FeatureMatcher<IpsecStaticPeerConfig, String> {

    HasIkePhase1Policy(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peer config with IkePhase1Policy:", "IkePhase1Policy");
    }

    @Override
    protected String featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getIkePhase1Policy();
    }
  }

  private static final class HasDestinationAddress
      extends FeatureMatcher<IpsecStaticPeerConfig, Ip> {

    HasDestinationAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec peer config with DestinationAddress:", "DestinationAddress");
    }

    @Override
    protected Ip featureValueOf(IpsecStaticPeerConfig actual) {
      return actual.getDestinationAddress();
    }
  }

  private static final class IsIpsecDynamicPeerConfig
      extends IsInstanceThat<IpsecPeerConfig, IpsecDynamicPeerConfig> {
    IsIpsecDynamicPeerConfig(@Nonnull Matcher<? super IpsecDynamicPeerConfig> subMatcher) {
      super(IpsecDynamicPeerConfig.class, subMatcher);
    }
  }

  private static final class IsIpsecStaticPeerConfig
      extends IsInstanceThat<IpsecPeerConfig, IpsecStaticPeerConfig> {
    IsIpsecStaticPeerConfig(@Nonnull Matcher<? super IpsecStaticPeerConfig> subMatcher) {
      super(IpsecStaticPeerConfig.class, subMatcher);
    }
  }
}

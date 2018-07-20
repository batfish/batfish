package org.batfish.question.ipsecpeers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecPeeringInfoMatchersImpl {

  static class HasInitiatorHostname extends FeatureMatcher<IpsecPeeringInfo, String> {

    HasInitiatorHostname(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peering info with InitiatorHostname:", "InitiatorHostname");
    }

    @Override
    protected String featureValueOf(IpsecPeeringInfo actual) {
      return actual.getInitiatorHostname();
    }
  }

  static class HasInitiatorInterface extends FeatureMatcher<IpsecPeeringInfo, String> {

    HasInitiatorInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peering info with InitiatorInterface:", "InitiatorInterface");
    }

    @Override
    protected String featureValueOf(IpsecPeeringInfo actual) {
      return actual.getInitiatorInterface();
    }
  }

  static class HasInitiatorIp extends FeatureMatcher<IpsecPeeringInfo, Ip> {

    HasInitiatorIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec peering info with InitiatorIp:", "InitiatorIp");
    }

    @Override
    protected Ip featureValueOf(IpsecPeeringInfo actual) {
      return actual.getInitiatorIp();
    }
  }

  static class HasInitiatorTunnelInterface extends FeatureMatcher<IpsecPeeringInfo, String> {

    HasInitiatorTunnelInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(
          subMatcher,
          "An IPSec peering info with InitiatorTunnelInterface:",
          "InitiatorTunnelInterface");
    }

    @Override
    protected String featureValueOf(IpsecPeeringInfo actual) {
      return actual.getInitiatorTunnelInterface();
    }
  }

  static class HasResponderHostname extends FeatureMatcher<IpsecPeeringInfo, String> {

    HasResponderHostname(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peering info with ResponderHostname:", "ResponderHostname");
    }

    @Override
    protected String featureValueOf(IpsecPeeringInfo actual) {
      return actual.getResponderHostname();
    }
  }

  static class HasResponderInterface extends FeatureMatcher<IpsecPeeringInfo, String> {

    HasResponderInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec peering info with ResponderInterface:", "ResponderInterface");
    }

    @Override
    protected String featureValueOf(IpsecPeeringInfo actual) {
      return actual.getResponderInterface();
    }
  }

  static class HasResponderIp extends FeatureMatcher<IpsecPeeringInfo, Ip> {

    HasResponderIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec peering info with ResponderIp:", "ResponderIp");
    }

    @Override
    protected Ip featureValueOf(IpsecPeeringInfo actual) {
      return actual.getResponderIp();
    }
  }

  static class HasResponderTunnelInterface extends FeatureMatcher<IpsecPeeringInfo, String> {

    HasResponderTunnelInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(
          subMatcher,
          "An IPSec peering info with ResponderTunnelInterface:",
          "ResponderTunnelInterface");
    }

    @Override
    protected String featureValueOf(IpsecPeeringInfo actual) {
      return actual.getResponderTunnelInterface();
    }
  }

  static class HasIpsecPeeringStatus extends FeatureMatcher<IpsecPeeringInfo, IpsecPeeringStatus> {

    HasIpsecPeeringStatus(@Nonnull Matcher<? super IpsecPeeringStatus> subMatcher) {
      super(subMatcher, "An IPSec peering info with IpsecPeeringStatus:", "IpsecPeeringStatus");
    }

    @Override
    protected IpsecPeeringStatus featureValueOf(IpsecPeeringInfo actual) {
      return actual.getIpsecPeeringStatus();
    }
  }
}

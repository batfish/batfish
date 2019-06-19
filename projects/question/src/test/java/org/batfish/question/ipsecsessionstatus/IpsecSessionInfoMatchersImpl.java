package org.batfish.question.ipsecsessionstatus;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.questions.IpsecSessionStatus;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecSessionInfoMatchersImpl {

  static class HasInitiatorHostname extends FeatureMatcher<IpsecSessionInfo, String> {

    HasInitiatorHostname(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec session info with InitiatorHostname:", "InitiatorHostname");
    }

    @Override
    protected String featureValueOf(IpsecSessionInfo actual) {
      return actual.getInitiatorHostname();
    }
  }

  static class HasInitiatorInterface extends FeatureMatcher<IpsecSessionInfo, String> {

    HasInitiatorInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec session info with InitiatorInterface:", "InitiatorInterface");
    }

    @Override
    protected String featureValueOf(IpsecSessionInfo actual) {
      return actual.getInitiatorInterface();
    }
  }

  static class HasInitiatorIp extends FeatureMatcher<IpsecSessionInfo, Ip> {

    HasInitiatorIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec session info with InitiatorIp:", "InitiatorIp");
    }

    @Override
    protected Ip featureValueOf(IpsecSessionInfo actual) {
      return actual.getInitiatorIp();
    }
  }

  static class HasInitiatorTunnelInterface extends FeatureMatcher<IpsecSessionInfo, String> {

    HasInitiatorTunnelInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(
          subMatcher,
          "An IPSec session info with InitiatorTunnelInterface:",
          "InitiatorTunnelInterface");
    }

    @Override
    protected String featureValueOf(IpsecSessionInfo actual) {
      return actual.getInitiatorTunnelInterface();
    }
  }

  static class HasResponderHostname extends FeatureMatcher<IpsecSessionInfo, String> {

    HasResponderHostname(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec session info with ResponderHostname:", "ResponderHostname");
    }

    @Override
    protected String featureValueOf(IpsecSessionInfo actual) {
      return actual.getResponderHostname();
    }
  }

  static class HasResponderInterface extends FeatureMatcher<IpsecSessionInfo, String> {

    HasResponderInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec session info with ResponderInterface:", "ResponderInterface");
    }

    @Override
    protected String featureValueOf(IpsecSessionInfo actual) {
      return actual.getResponderInterface();
    }
  }

  static class HasResponderIp extends FeatureMatcher<IpsecSessionInfo, Ip> {

    HasResponderIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An IPSec session info with ResponderIp:", "ResponderIp");
    }

    @Override
    protected Ip featureValueOf(IpsecSessionInfo actual) {
      return actual.getResponderIp();
    }
  }

  static class HasResponderTunnelInterface extends FeatureMatcher<IpsecSessionInfo, String> {

    HasResponderTunnelInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(
          subMatcher,
          "An IPSec session info with ResponderTunnelInterface:",
          "ResponderTunnelInterface");
    }

    @Override
    protected String featureValueOf(IpsecSessionInfo actual) {
      return actual.getResponderTunnelInterface();
    }
  }

  static class HasIpsecSessionStatus extends FeatureMatcher<IpsecSessionInfo, IpsecSessionStatus> {

    HasIpsecSessionStatus(@Nonnull Matcher<? super IpsecSessionStatus> subMatcher) {
      super(subMatcher, "An IPSec session info with IpsecSessionStatus:", "IpsecSessionStatus");
    }

    @Override
    protected IpsecSessionStatus featureValueOf(IpsecSessionInfo actual) {
      return actual.getIpsecSessionStatus();
    }
  }
}

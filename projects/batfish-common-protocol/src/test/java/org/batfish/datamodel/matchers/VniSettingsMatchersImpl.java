package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.VniSettings;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class VniSettingsMatchersImpl {

  private VniSettingsMatchersImpl() {}

  static final class HasBumTransportIps extends FeatureMatcher<VniSettings, Iterable<Ip>> {
    HasBumTransportIps(@Nonnull Matcher<? super Iterable<Ip>> subMatcher) {
      super(subMatcher, "VniSettings with BUM transport IPs:", "bumTransportIps");
    }

    @Override
    protected Iterable<Ip> featureValueOf(VniSettings actual) {
      return actual.getBumTransportIps();
    }
  }

  static final class HasBumTransportMethod extends FeatureMatcher<VniSettings, BumTransportMethod> {
    HasBumTransportMethod(@Nonnull Matcher<? super BumTransportMethod> subMatcher) {
      super(subMatcher, "VniSettings with BUM transport method:", "bumTransportMethod");
    }

    @Override
    protected BumTransportMethod featureValueOf(VniSettings actual) {
      return actual.getBumTransportMethod();
    }
  }

  static final class HasSourceAddress extends FeatureMatcher<VniSettings, Ip> {
    HasSourceAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "VniSettings with source IP address:", "sourceAddress");
    }

    @Override
    protected Ip featureValueOf(VniSettings actual) {
      return actual.getSourceAddress();
    }
  }

  static final class HasUdpPort extends FeatureMatcher<VniSettings, Integer> {
    HasUdpPort(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "VniSettings with UDP port:", "udpPort");
    }

    @Override
    protected Integer featureValueOf(VniSettings actual) {
      return actual.getUdpPort();
    }
  }

  static final class HasVlan extends FeatureMatcher<VniSettings, Integer> {
    HasVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "VniSettings with VLAN number:", "vlan");
    }

    @Override
    protected Integer featureValueOf(VniSettings actual) {
      return actual.getVlan();
    }
  }

  static final class HasVni extends FeatureMatcher<VniSettings, Integer> {
    HasVni(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "VniSettings with VLAN number:", "vlan");
    }

    @Override
    protected Integer featureValueOf(VniSettings actual) {
      return actual.getVni();
    }
  }
}

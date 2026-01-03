package org.batfish.datamodel.matchers;

import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;
import org.batfish.datamodel.vxlan.Vni;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class VniMatchersImpl {

  private VniMatchersImpl() {}

  static final class HasBumTransportIps extends FeatureMatcher<Layer2Vni, Set<Ip>> {
    HasBumTransportIps(@Nonnull Matcher<? super Set<Ip>> subMatcher) {
      super(subMatcher, "Layer2Vni with BUM transport IPs:", "bumTransportIps");
    }

    @Override
    protected Set<Ip> featureValueOf(Layer2Vni actual) {
      return actual.getBumTransportIps();
    }
  }

  static final class HasLearnedNexthopVtepIps extends FeatureMatcher<Layer3Vni, Collection<Ip>> {
    HasLearnedNexthopVtepIps(@Nonnull Matcher<? super Collection<Ip>> subMatcher) {
      super(subMatcher, "Layer3Vni with learned next-hop VTEP IPs:", "learnedNexthopVtepIps");
    }

    @Override
    protected Collection<Ip> featureValueOf(Layer3Vni actual) {
      return actual.getLearnedNexthopVtepIps();
    }
  }

  static final class HasBumTransportMethod extends FeatureMatcher<Layer2Vni, BumTransportMethod> {
    HasBumTransportMethod(@Nonnull Matcher<? super BumTransportMethod> subMatcher) {
      super(subMatcher, "Layer2Vni with BUM transport method:", "bumTransportMethod");
    }

    @Override
    protected BumTransportMethod featureValueOf(Layer2Vni actual) {
      return actual.getBumTransportMethod();
    }
  }

  static final class HasSourceAddress extends FeatureMatcher<Vni, Ip> {
    HasSourceAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "Vni with source IP address:", "sourceAddress");
    }

    @Override
    protected Ip featureValueOf(Vni actual) {
      return actual.getSourceAddress();
    }
  }

  static final class HasUdpPort extends FeatureMatcher<Vni, Integer> {
    HasUdpPort(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "Vni with UDP port:", "udpPort");
    }

    @Override
    protected Integer featureValueOf(Vni actual) {
      return actual.getUdpPort();
    }
  }

  static final class HasVlan extends FeatureMatcher<Layer2Vni, Integer> {
    HasVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "Layer2Vni with VLAN number:", "vlan");
    }

    @Override
    protected Integer featureValueOf(Layer2Vni actual) {
      return actual.getVlan();
    }
  }

  static final class HasVni extends FeatureMatcher<Vni, Integer> {
    HasVni(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "Vni with VLAN number:", "vlan");
    }

    @Override
    protected Integer featureValueOf(Vni actual) {
      return actual.getVni();
    }
  }
}

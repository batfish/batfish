package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

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

public class VniMatchers {

  private VniMatchers() {}

  /**
   * Provides a matcher that matches if the {@link Layer2Vni}'s BUM transport IPs matches the {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<Layer2Vni> hasBumTransportIps(
      @Nonnull Matcher<? super Set<Ip>> subMatcher) {
    return new HasBumTransportIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link Layer3Vni}'s learned next-hop VTEP IPs matches
   * the {@code subMatcher}.
   */
  public static @Nonnull Matcher<Layer3Vni> hasLearnedNexthopVtepIps(
      @Nonnull Matcher<? super Collection<Ip>> subMatcher) {
    return new HasLearnedNexthopVtepIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link Vni}'s BUM transport method matches the {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<Layer2Vni> hasBumTransportMethod(
      @Nonnull Matcher<? super BumTransportMethod> subMatcher) {
    return new HasBumTransportMethod(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link Vni}'s source IP address matches the {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<Vni> hasSourceAddress(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasSourceAddress(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link Vni}'s UDP port matches the {@code subMatcher}.
   */
  public static @Nonnull Matcher<Vni> hasUdpPort(@Nonnull Matcher<? super Integer> subMatcher) {
    return new HasUdpPort(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link Vni}'s VLAN number matches the {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<Layer2Vni> hasVlan(@Nonnull Matcher<? super Integer> subMatcher) {
    return new HasVlan(subMatcher);
  }

  /** Provides a matcher that matches if the {@link Vni}'s VLAN number matches the {@code vni}. */
  public static @Nonnull Matcher<Vni> hasVni(int vni) {
    return new HasVni(equalTo(vni));
  }

  private static final class HasBumTransportIps extends FeatureMatcher<Layer2Vni, Set<Ip>> {
    HasBumTransportIps(@Nonnull Matcher<? super Set<Ip>> subMatcher) {
      super(subMatcher, "Layer2Vni with BUM transport IPs:", "bumTransportIps");
    }

    @Override
    protected Set<Ip> featureValueOf(Layer2Vni actual) {
      return actual.getBumTransportIps();
    }
  }

  private static final class HasLearnedNexthopVtepIps
      extends FeatureMatcher<Layer3Vni, Collection<Ip>> {
    HasLearnedNexthopVtepIps(@Nonnull Matcher<? super Collection<Ip>> subMatcher) {
      super(subMatcher, "Layer3Vni with learned next-hop VTEP IPs:", "learnedNexthopVtepIps");
    }

    @Override
    protected Collection<Ip> featureValueOf(Layer3Vni actual) {
      return actual.getLearnedNexthopVtepIps();
    }
  }

  private static final class HasBumTransportMethod
      extends FeatureMatcher<Layer2Vni, BumTransportMethod> {
    HasBumTransportMethod(@Nonnull Matcher<? super BumTransportMethod> subMatcher) {
      super(subMatcher, "Layer2Vni with BUM transport method:", "bumTransportMethod");
    }

    @Override
    protected BumTransportMethod featureValueOf(Layer2Vni actual) {
      return actual.getBumTransportMethod();
    }
  }

  private static final class HasSourceAddress extends FeatureMatcher<Vni, Ip> {
    HasSourceAddress(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "Vni with source IP address:", "sourceAddress");
    }

    @Override
    protected Ip featureValueOf(Vni actual) {
      return actual.getSourceAddress();
    }
  }

  private static final class HasUdpPort extends FeatureMatcher<Vni, Integer> {
    HasUdpPort(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "Vni with UDP port:", "udpPort");
    }

    @Override
    protected Integer featureValueOf(Vni actual) {
      return actual.getUdpPort();
    }
  }

  private static final class HasVlan extends FeatureMatcher<Layer2Vni, Integer> {
    HasVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "Layer2Vni with VLAN number:", "vlan");
    }

    @Override
    protected Integer featureValueOf(Layer2Vni actual) {
      return actual.getVlan();
    }
  }

  private static final class HasVni extends FeatureMatcher<Vni, Integer> {
    HasVni(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "Vni with VLAN number:", "vlan");
    }

    @Override
    protected Integer featureValueOf(Vni actual) {
      return actual.getVni();
    }
  }
}

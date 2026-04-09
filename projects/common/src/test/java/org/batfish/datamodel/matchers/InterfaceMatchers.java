package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class InterfaceMatchers {

  /** Provides a matcher that matches if the provided value matches the interface's Access VLAN. */
  public static Matcher<Interface> hasAccessVlan(int value) {
    return hasAccessVlan(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Access VLAN.
   */
  public static Matcher<Interface> hasAccessVlan(Matcher<? super Integer> subMatcher) {
    return new HasAccessVlan(subMatcher);
  }

  /** Provides a matcher that matches if the interface's address is {@code expectedAddress}. */
  public static @Nonnull Matcher<Interface> hasAddress(@Nonnull String expectedAddress) {
    return new HasAddress(equalTo(ConcreteInterfaceAddress.parse(expectedAddress)));
  }

  /** Provides a matcher that matches if the interface's address is {@code expectedAddress}. */
  public static @Nonnull Matcher<Interface> hasAddress(@Nonnull InterfaceAddress expectedAddress) {
    return new HasAddress(equalTo(expectedAddress));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * address.
   */
  public static @Nonnull Matcher<Interface> hasAddress(
      @Nonnull Matcher<? super InterfaceAddress> subMatcher) {
    return new HasAddress(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * {@link Interface#getAddressMetadata() address metadata}.
   */
  public static Matcher<Interface> hasAddressMetadata(
      Matcher<? super SortedMap<ConcreteInterfaceAddress, ConnectedRouteMetadata>> subMatcher) {
    return new HasAddressMetadata(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * allAddresses.
   */
  public static Matcher<Interface> hasAllAddresses(
      Matcher<? super Set<InterfaceAddress>> subMatcher) {
    return new HasAllAddresses(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided value matches the interface's Allowed VLANs.
   */
  public static Matcher<Interface> hasAllowedVlans(IntegerSpace value) {
    return hasAllowedVlans(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Allowed VLANs.
   */
  public static Matcher<Interface> hasAllowedVlans(Matcher<? super IntegerSpace> subMatcher) {
    return new HasAllowedVlans(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * additional arp IPs.
   */
  public static Matcher<Interface> hasAdditionalArpIps(
      @Nonnull Matcher<? super IpSpace> subMatcher) {
    return new HasAdditionalArpIps(subMatcher);
  }

  /** Provides a matcher that matches if the interface's bandwidth is {@code expectedBandwidth}. */
  public static @Nonnull Matcher<Interface> hasBandwidth(double expectedBandwidth) {
    return hasBandwidth(equalTo(expectedBandwidth));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * bandwidth.
   */
  public static Matcher<Interface> hasBandwidth(@Nonnull Matcher<? super Double> subMatcher) {
    return new HasBandwidth(subMatcher);
  }

  /**
   * Provides a matcher that matches if the interface's channelGroup is {@code
   * expectedChannelGroup}.
   */
  public static @Nonnull Matcher<Interface> hasChannelGroup(@Nonnull String expectedChannelGroup) {
    return hasChannelGroup(equalTo(expectedChannelGroup));
  }

  /**
   * Provides a matcher that matches if the interface's channelGroup is matched by the provided
   * {@code subMatcher}.
   */
  public static @Nonnull Matcher<Interface> hasChannelGroup(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasChannelGroup(subMatcher);
  }

  /**
   * Provides a matcher that matches if the interface's channelGroupMembers are matched by the
   * provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<Interface> hasChannelGroupMembers(
      @Nonnull Matcher<? super SortedSet<String>> subMatcher) {
    return new HasChannelGroupMembers(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided the interface's declared names comprise the set
   * of unique strings in {@code expectedDeclaredNames}.
   */
  public static Matcher<Interface> hasDeclaredNames(
      @Nonnull Iterable<String> expectedDeclaredNames) {
    return new HasDeclaredNames(
        containsInAnyOrder(ImmutableSet.copyOf(expectedDeclaredNames).toArray()));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * declared names.
   */
  public static Matcher<Interface> hasDeclaredNames(
      @Nonnull Matcher<? super Set<String>> subMatcher) {
    return new HasDeclaredNames(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided the interface's declared names comprise the set
   * of unique strings in {@code expectedDeclaredNames}.
   */
  public static Matcher<Interface> hasDeclaredNames(@Nonnull String... expectedDeclaredNames) {
    return new HasDeclaredNames(
        containsInAnyOrder(ImmutableSet.copyOf(expectedDeclaredNames).toArray()));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s dependencies.
   */
  public static @Nonnull Matcher<Interface> hasDependencies(
      @Nonnull Matcher<? super Set<Dependency>> subMatcher) {
    return new HasDependencies(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link Interface}'s description is {@code
   * expectedDescription}.
   */
  public static Matcher<Interface> hasDescription(String expectedDescription) {
    return hasDescription(equalTo(expectedDescription));
  }

  /**
   * Provides a matcher that matches if the {@link Interface}'s description matches the given
   * subMatcher.
   */
  public static Matcher<Interface> hasDescription(Matcher<? super String> subMatcher) {
    return new HasDescription(subMatcher);
  }

  /**
   * Provides a matcher that matches if the interface's dhcpRelayAddresses are matched by the
   * provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<Interface> hasDhcpRelayAddresses(
      @Nonnull Matcher<? super List<Ip>> subMatcher) {
    return new HasDhcpRelayAddresses(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * eigrp.
   */
  public static @Nonnull Matcher<Interface> hasEigrp(
      @Nonnull Matcher<? super EigrpInterfaceSettings> subMatcher) {
    return new HasEigrp(subMatcher);
  }

  /**
   * Provides a matcher that matches if interface's encapsulationVlan is {@code
   * expectedEncapsulationVlan}.
   */
  public static @Nonnull Matcher<Interface> hasEncapsulationVlan(int expectedEncapsulationVlan) {
    return new HasEncapsulationVlan(equalTo(expectedEncapsulationVlan));
  }

  /**
   * Provides a matcher that matches if interface's encapsulationVlan is matched by the provided
   * {@code subMatcher}.
   */
  public static @Nonnull Matcher<Interface> hasEncapsulationVlan(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasEncapsulationVlan(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link Interface}'s interfaceType is {@code
   * expectedInterfaceType}.
   */
  public static @Nonnull Matcher<Interface> hasInterfaceType(
      @Nonnull InterfaceType expectedInterfaceType) {
    return new HasInterfaceType(equalTo(expectedInterfaceType));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s hsrpGroup with the specified {@code number}.
   */
  public static @Nonnull Matcher<Interface> hasHsrpGroup(
      int number, @Nonnull Matcher<? super HsrpGroup> subMatcher) {
    return new HasHsrpGroup(number, subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link Interface}'s hsrpVersion is equal to {@code
   * expectedHsrpVersion}.
   */
  public static @Nonnull Matcher<Interface> hasHsrpVersion(@Nullable String expectedHsrpVersion) {
    return new HasHsrpVersion(equalTo(expectedHsrpVersion));
  }

  /**
   * Provides a matcher that matches if the {@link Interface}'s human name is {@code
   * expectedHumanName}.
   */
  public static @Nonnull Matcher<Interface> hasHumanName(String expectedHumanName) {
    return hasHumanName(equalTo(expectedHumanName));
  }

  /**
   * Provides a matcher that matches if the {@link Interface}'s human name matches the given
   * subMatcher.
   */
  public static @Nonnull Matcher<Interface> hasHumanName(Matcher<? super String> subMatcher) {
    return new HasHumanName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * isis.
   */
  public static @Nonnull Matcher<Interface> hasIsis(
      @Nonnull Matcher<? super IsisInterfaceSettings> subMatcher) {
    return new HasIsis(subMatcher);
  }

  /** Provides a matcher that matches if the provided value matches the interface's MLAG ID. */
  public static Matcher<Interface> hasMlagId(int value) {
    return hasMlagId(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's MLAG
   * ID.
   */
  public static Matcher<Interface> hasMlagId(Matcher<? super Integer> subMatcher) {
    return new HasMlagId(subMatcher);
  }

  /** Provides a matcher that matches if the provided value matches the interface's MTU. */
  public static Matcher<Interface> hasMtu(int value) {
    return hasMtu(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's MTU.
   */
  public static Matcher<Interface> hasMtu(Matcher<? super Integer> subMatcher) {
    return new HasMtu(subMatcher);
  }

  /** Provides a matcher that matches if the provided name matches the interface's name. */
  public static Matcher<Interface> hasName(String expectedName) {
    return new HasName(equalTo(expectedName));
  }

  /** Provides a matcher that matches if the provided value matches the interface's Native VLAN. */
  public static Matcher<Interface> hasNativeVlan(@Nullable Integer value) {
    return hasNativeVlan(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Native VLAN.
   */
  public static Matcher<Interface> hasNativeVlan(Matcher<? super Integer> subMatcher) {
    return new HasNativeVlan(subMatcher);
  }

  /**
   * Provides a matcher that matches if the the interface's OSPF area ID is {@code expectedArea}.
   */
  public static @Nonnull Matcher<Interface> hasOspfAreaName(long expectedArea) {
    return new HasOspfAreaName(equalTo(expectedArea));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's OSPF
   * area ID.
   */
  public static @Nonnull Matcher<Interface> hasOspfAreaName(
      @Nonnull Matcher<? super Long> subMatcher) {
    return new HasOspfAreaName(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's OSPF
   * cost.
   */
  public static Matcher<Interface> hasOspfCost(Matcher<Integer> subMatcher) {
    return new HasOspfCost(subMatcher);
  }

  /** Provides an {@link Interface} matcher that matches if the interface has OSPF enabled. */
  public static Matcher<Interface> hasOspfEnabled() {
    return new HasOspfEnabled(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's OSPF
   * point to point.
   */
  public static @Nonnull Matcher<Interface> hasOspfPointToPoint(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasOspfPointToPoint(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's OSPF
   * network type.
   */
  public static @Nonnull Matcher<Interface> hasOspfNetworkType(
      @Nonnull Matcher<? super OspfNetworkType> subMatcher) {
    return new HasOspfNetworkType(subMatcher);
  }

  /** Provides a matcher that matches if the interface's speed is {@code expectedSpeed}. */
  public static @Nonnull Matcher<Interface> hasSpeed(@Nullable Double expectedSpeed) {
    return new HasSpeed(equalTo(expectedSpeed));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * speed.
   */
  public static @Nonnull Matcher<Interface> hasSpeed(@Nonnull Matcher<? super Double> subMatcher) {
    return new HasSpeed(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided value matches the interface's Switch Port
   * encapsulation type
   */
  public static Matcher<Interface> hasSwitchPortEncapsulation(
      SwitchportEncapsulationType switchportEncapsulationType) {
    return hasSwitchPortEncapsulation(equalTo(switchportEncapsulationType));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Switch Port encapsulation type
   */
  public static Matcher<Interface> hasSwitchPortEncapsulation(
      Matcher<? super SwitchportEncapsulationType> subMatcher) {
    return new HasSwitchPortEncapsulation(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided value matches the interface's Switch Port mode.
   */
  public static Matcher<Interface> hasSwitchPortMode(SwitchportMode switchportMode) {
    return hasSwitchPortMode(equalTo(switchportMode));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Switch Port mode.
   */
  public static Matcher<Interface> hasSwitchPortMode(Matcher<? super SwitchportMode> subMatcher) {
    return new HasSwitchPortMode(subMatcher);
  }

  /** Provides a matcher that matches if the interface's VLAN is {@code expectedVlan}. */
  public static @Nonnull Matcher<Interface> hasVlan(int expectedVlan) {
    return hasVlan(equalTo(expectedVlan));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * VLAN.
   */
  public static @Nonnull Matcher<Interface> hasVlan(@Nonnull Matcher<? super Integer> subMatcher) {
    return new HasVlan(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's VRF.
   */
  public static Matcher<Interface> hasVrf(Matcher<? super Vrf> subMatcher) {
    return new HasVrf(subMatcher);
  }

  /** Provides a matcher that matches if the interface's vrfName is {@code expectedVrfName}. */
  public static @Nonnull Matcher<Interface> hasVrfName(@Nonnull String expectedVrfName) {
    return new HasVrfName(equalTo(expectedVrfName));
  }

  /** Matcher for the interface's {@link org.batfish.datamodel.FirewallSessionInterfaceInfo}. */
  public static @Nonnull Matcher<Interface> hasFirewallSessionInterfaceInfo(
      @Nonnull Matcher<? super FirewallSessionInterfaceInfo> matcher) {
    return new HasFirewallSessionInterfaceInfo(matcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code name} matches the interface's Zone name.
   */
  public static Matcher<Interface> hasZoneName(String name) {
    return new HasZoneName(equalTo(name));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's Zone
   * name.
   */
  public static Matcher<Interface> hasZoneName(Matcher<? super String> subMatcher) {
    return new HasZoneName(subMatcher);
  }

  /** Provides a matcher that matches if the interface is active. */
  public static @Nonnull Matcher<Interface> isActive() {
    return new IsActive(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided active flag matches the interface's active
   * flag.
   */
  public static @Nonnull Matcher<Interface> isActive(boolean expectedActive) {
    return new IsActive(equalTo(expectedActive));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * active flag.
   */
  public static @Nonnull Matcher<Interface> isActive(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new IsActive(subMatcher);
  }

  /** Provides a matcher that matches if the interface's adminUp flag is true. */
  public static @Nonnull Matcher<Interface> isAdminUp() {
    return new IsAdminUp(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided adminUp flag matches the interface's adminUp
   * flag.
   */
  public static @Nonnull Matcher<Interface> isAdminUp(boolean expectedAdminUp) {
    return new IsAdminUp(equalTo(expectedAdminUp));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * adminUp flag.
   */
  public static @Nonnull Matcher<Interface> isAdminUp(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new IsAdminUp(subMatcher);
  }

  /** Provides a matcher that matches if the interface's blacklisted flag is true. */
  public static @Nonnull Matcher<Interface> isBlacklisted() {
    return new IsBlacklisted(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided blacklisted flag matches the interface's
   * blacklisted flag.
   */
  public static @Nonnull Matcher<Interface> isBlacklisted(boolean expectedBlacklisted) {
    return new IsBlacklisted(equalTo(expectedBlacklisted));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * blacklisted flag.
   */
  public static @Nonnull Matcher<Interface> isBlacklisted(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new IsBlacklisted(subMatcher);
  }

  /** Provides a matcher that matches if the interface's lineUp flag is true. */
  public static @Nonnull Matcher<Interface> isLineUp() {
    return new IsLineUp(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided lineUp flag matches the interface's lineUp
   * flag.
   */
  public static @Nonnull Matcher<Interface> isLineUp(boolean expectedActive) {
    return new IsLineUp(equalTo(expectedActive));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * lineUp flag.
   */
  public static @Nonnull Matcher<Interface> isLineUp(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new IsLineUp(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@code expectedInactiveReason} matches the interface's
   * inactiveReason.
   */
  public static @Nonnull Matcher<Interface> hasInactiveReason(
      @Nullable InactiveReason expectedInactiveReason) {
    return new HasInactiveReason(equalTo(expectedInactiveReason));
  }

  /**
   * Provides a matcher that matches if the inactiveReason is matched by the provided {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<Interface> hasInactiveReason(
      Matcher<? super InactiveReason> subMatcher) {
    return new HasInactiveReason(subMatcher);
  }

  /** A matcher that matches if the interface's autoState flag is {@code true}. */
  public static @Nonnull Matcher<Interface> isAutoState() {
    return isAutoState(true);
  }

  /** A matcher that matches if the interface's autoState flag is {@code expectedAutoState}. */
  public static @Nonnull Matcher<Interface> isAutoState(boolean expectedAutoState) {
    return isAutoState(equalTo(expectedAutoState));
  }

  /**
   * A matcher that matches if the interface's autoState flag matches the provided {@code
   * subMatcher}.
   */
  public static Matcher<Interface> isAutoState(Matcher<? super Boolean> subMatcher) {
    return new IsAutoState(subMatcher);
  }

  /** Provides a matcher that matches if the interface runs OSPF in passive mode. */
  public static Matcher<Interface> isOspfPassive() {
    return isOspfPassive(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * ospfPassive flag.
   */
  public static Matcher<Interface> isOspfPassive(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new IsOspfPassive(subMatcher);
  }

  /** Provides a matcher that matches if the interface runs OSPF in point-to-point mode. */
  public static Matcher<Interface> isOspfPointToPoint() {
    return new IsOspfPointToPoint(equalTo(true));
  }

  /** Provides a matcher that matches if the interface has proxy-arp enabled. */
  public static Matcher<Interface> isProxyArp() {
    return new IsProxyArp(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * proxy-arp setting.
   */
  public static Matcher<Interface> isProxyArp(Matcher<? super Boolean> subMatcher) {
    return new IsProxyArp(subMatcher);
  }

  /** Provides a matcher that matches if the interface is configured as a switchport. */
  public static Matcher<Interface> isSwitchport() {
    return new IsSwitchport(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided switchport flag matches the interface's
   * switchport flag.
   */
  public static Matcher<Interface> isSwitchport(boolean switchport) {
    return new IsSwitchport(equalTo(switchport));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code incomingFilter}.
   */
  public static @Nonnull Matcher<Interface> hasIncomingFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasIncomingFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code outgoingOriginalFlowFilter}.
   */
  public static @Nonnull Matcher<Interface> hasOutgoingOriginalFlowFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasOutgoingOriginalFlowFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code outgoingFilter}.
   */
  public static @Nonnull Matcher<Interface> hasOutgoingFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasOutgoingFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code postTransformationIncomingFilter}.
   */
  public static @Nonnull Matcher<Interface> hasPostTransformationIncomingFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasPostTransformationIncomingFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code preTransformationOutgoingFilter}.
   */
  public static @Nonnull Matcher<Interface> hasPreTransformationOutgoingFilter(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasPreTransformationOutgoingFilter(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * Interface}'s {@code outgoingFilterName}.
   */
  public static @Nonnull Matcher<Interface> hasOutgoingFilterName(
      @Nonnull Matcher<? super String> subMatcher) {
    return hasOutgoingFilter(IpAccessListMatchers.hasName(subMatcher));
  }

  /**
   * Provides a matcher that matches if the {@link Interface}'s {@code outgoingFilterName} is equal
   * to {@code expectedName}.
   */
  public static @Nonnull Matcher<Interface> hasOutgoingFilterName(@Nullable String expectedName) {
    return hasOutgoingFilter(IpAccessListMatchers.hasName(expectedName));
  }

  private InterfaceMatchers() {}

  private static final class HasAccessVlan extends FeatureMatcher<Interface, Integer> {
    HasAccessVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with accessVlan:", "accessVlan");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getAccessVlan();
    }
  }

  private static final class HasAdditionalArpIps extends FeatureMatcher<Interface, IpSpace> {
    HasAdditionalArpIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "An interface with additionalArpIps:", "additionalArpIps");
    }

    @Override
    protected IpSpace featureValueOf(Interface actual) {
      return actual.getAdditionalArpIps();
    }
  }

  private static final class HasAddress extends FeatureMatcher<Interface, InterfaceAddress> {
    HasAddress(@Nonnull Matcher<? super InterfaceAddress> subMatcher) {
      super(subMatcher, "An Interface with address:", "address");
    }

    @Override
    protected InterfaceAddress featureValueOf(Interface actual) {
      return actual.getAddress();
    }
  }

  private static final class HasAddressMetadata
      extends FeatureMatcher<
          Interface, SortedMap<ConcreteInterfaceAddress, ConnectedRouteMetadata>> {
    HasAddressMetadata(
        @Nonnull
            Matcher<? super SortedMap<ConcreteInterfaceAddress, ConnectedRouteMetadata>>
                subMatcher) {
      super(subMatcher, "An Interface with addressMetadata:", "addressMetadata");
    }

    @Override
    protected SortedMap<ConcreteInterfaceAddress, ConnectedRouteMetadata> featureValueOf(
        Interface actual) {
      return actual.getAddressMetadata();
    }
  }

  private static final class HasAllAddresses
      extends FeatureMatcher<Interface, Set<InterfaceAddress>> {
    HasAllAddresses(@Nonnull Matcher<? super Set<InterfaceAddress>> subMatcher) {
      super(subMatcher, "An Interface with allAddresses:", "allAddresses");
    }

    @Override
    protected Set<InterfaceAddress> featureValueOf(Interface actual) {
      return actual.getAllAddresses();
    }
  }

  private static final class HasAllowedVlans extends FeatureMatcher<Interface, IntegerSpace> {
    HasAllowedVlans(@Nonnull Matcher<? super IntegerSpace> subMatcher) {
      super(subMatcher, "an Interface with allowedVlans:", "allowedVlans");
    }

    @Override
    protected IntegerSpace featureValueOf(Interface actual) {
      return actual.getAllowedVlans();
    }
  }

  private static final class HasBandwidth extends FeatureMatcher<Interface, Double> {
    HasBandwidth(@Nonnull Matcher<? super Double> subMatcher) {
      super(subMatcher, "an Interface with bandwidth:", "bandwidth");
    }

    @Override
    protected Double featureValueOf(Interface actual) {
      return actual.getBandwidth();
    }
  }

  private static final class HasChannelGroup extends FeatureMatcher<Interface, String> {
    HasChannelGroup(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An Interface with channelGroup", "channelGroup");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getChannelGroup();
    }
  }

  private static final class HasChannelGroupMembers
      extends FeatureMatcher<Interface, SortedSet<String>> {
    HasChannelGroupMembers(@Nonnull Matcher<? super SortedSet<String>> subMatcher) {
      super(subMatcher, "An Interface with channelGroupMembers", "channelGroupMembers");
    }

    @Override
    protected SortedSet<String> featureValueOf(Interface actual) {
      return actual.getChannelGroupMembers();
    }
  }

  private static final class HasDeclaredNames extends FeatureMatcher<Interface, Set<String>> {
    HasDeclaredNames(@Nonnull Matcher<? super Set<String>> subMatcher) {
      super(subMatcher, "declared names", "declared names");
    }

    @Override
    protected Set<String> featureValueOf(Interface actual) {
      return actual.getDeclaredNames();
    }
  }

  private static final class HasDependencies extends FeatureMatcher<Interface, Set<Dependency>> {
    HasDependencies(@Nonnull Matcher<? super Set<Dependency>> subMatcher) {
      super(subMatcher, "An Interface with dependencies:", "dependencies");
    }

    @Override
    protected Set<Dependency> featureValueOf(Interface actual) {
      return actual.getDependencies();
    }
  }

  private static final class HasDescription extends FeatureMatcher<Interface, String> {
    HasDescription(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An Interface with description:", "description");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getDescription();
    }
  }

  private static final class HasDhcpRelayAddresses extends FeatureMatcher<Interface, List<Ip>> {
    HasDhcpRelayAddresses(@Nonnull Matcher<? super List<Ip>> subMatcher) {
      super(subMatcher, "An Interface with dhcpRelayAddresses", "dhcpRelayAddresses");
    }

    @Override
    protected List<Ip> featureValueOf(Interface actual) {
      return actual.getDhcpRelayAddresses();
    }
  }

  private static final class HasEigrp extends FeatureMatcher<Interface, EigrpInterfaceSettings> {
    HasEigrp(@Nonnull Matcher<? super EigrpInterfaceSettings> subMatcher) {
      super(subMatcher, "An Interface with eigrp:", "eigrp");
    }

    @Override
    protected @Nullable EigrpInterfaceSettings featureValueOf(Interface actual) {
      return actual.getEigrp();
    }
  }

  private static final class HasEncapsulationVlan extends FeatureMatcher<Interface, Integer> {
    HasEncapsulationVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with encapsulationVlan:", "encapsulationVlan");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getEncapsulationVlan();
    }
  }

  private static final class HasHsrpGroup extends FeatureMatcher<Interface, HsrpGroup> {
    private final int _number;

    HasHsrpGroup(int number, @Nonnull Matcher<? super HsrpGroup> subMatcher) {
      super(
          subMatcher,
          String.format("An Interface with hsrpGroup %d:", number),
          String.format("hsrpGroup %d", number));
      _number = number;
    }

    @Override
    protected HsrpGroup featureValueOf(Interface actual) {
      return actual.getHsrpGroups().get(_number);
    }
  }

  private static final class HasHsrpVersion extends FeatureMatcher<Interface, String> {
    HasHsrpVersion(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An Interface with hsrpVersion:", "hsrpVersion");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getHsrpVersion();
    }
  }

  private static final class HasHumanName extends FeatureMatcher<Interface, String> {
    HasHumanName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An Interface with human name:", "human name");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getHumanName();
    }
  }

  private static final class HasIsis extends FeatureMatcher<Interface, IsisInterfaceSettings> {
    HasIsis(@Nonnull Matcher<? super IsisInterfaceSettings> subMatcher) {
      super(subMatcher, "An Interface with isis:", "isis");
    }

    @Override
    protected IsisInterfaceSettings featureValueOf(Interface actual) {
      return actual.getIsis();
    }
  }

  private static final class HasMlagId extends FeatureMatcher<Interface, Integer> {
    HasMlagId(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with MLAG ID:", "mlagId");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getMlagId();
    }
  }

  private static final class HasMtu extends FeatureMatcher<Interface, Integer> {
    HasMtu(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with MTU:", "MTU");
    }

    @Override
    protected Integer featureValueOf(Interface actual) {
      return actual.getMtu();
    }
  }

  private static final class HasName extends FeatureMatcher<Interface, String> {
    HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Interface with name:", "name");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getName();
    }
  }

  private static final class HasNativeVlan extends FeatureMatcher<Interface, Integer> {
    HasNativeVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with nativeVlan:", "nativeVlan");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getNativeVlan();
    }
  }

  private static final class HasOspfAreaName extends FeatureMatcher<Interface, Long> {
    HasOspfAreaName(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "an Interface with ospfAreaName:", "ospfAreaName");
    }

    @Override
    protected Long featureValueOf(Interface actual) {
      return actual.getOspfAreaName();
    }
  }

  private static final class HasOspfCost extends FeatureMatcher<Interface, Integer> {
    HasOspfCost(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with ospfCost:", "ospfCost");
    }

    @Override
    protected Integer featureValueOf(Interface actual) {
      return actual.getOspfCost();
    }
  }

  private static final class HasOspfEnabled extends FeatureMatcher<Interface, Boolean> {
    HasOspfEnabled(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with ospfEnabled:", "ospfEnabled");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfEnabled();
    }
  }

  private static final class HasOspfPointToPoint extends FeatureMatcher<Interface, Boolean> {
    HasOspfPointToPoint(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with ospfPointToPoint:", "ospfPointToPoint");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfNetworkType() == OspfNetworkType.POINT_TO_POINT;
    }
  }

  private static final class HasOspfNetworkType extends FeatureMatcher<Interface, OspfNetworkType> {
    HasOspfNetworkType(@Nonnull Matcher<? super OspfNetworkType> subMatcher) {
      super(subMatcher, "an Interface with ospfNetworkType:", "ospfNetworkType");
    }

    @Override
    protected OspfNetworkType featureValueOf(Interface actual) {
      return actual.getOspfNetworkType();
    }
  }

  private static final class HasIncomingFilter extends FeatureMatcher<Interface, IpAccessList> {
    HasIncomingFilter(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "an Interface with incomingFilter:", "incomingFilter");
    }

    @Override
    protected IpAccessList featureValueOf(Interface actual) {
      return actual.getIncomingFilter();
    }
  }

  private static final class HasInterfaceType extends FeatureMatcher<Interface, InterfaceType> {
    HasInterfaceType(@Nonnull Matcher<? super InterfaceType> subMatcher) {
      super(subMatcher, "An Interface with interfaceType:", "interfaceType");
    }

    @Override
    protected InterfaceType featureValueOf(Interface actual) {
      return actual.getInterfaceType();
    }
  }

  private static final class HasOutgoingOriginalFlowFilter
      extends FeatureMatcher<Interface, IpAccessList> {
    HasOutgoingOriginalFlowFilter(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(
          subMatcher,
          "an Interface with outgoingOriginalFlowFilter:",
          "outgoingOriginalFlowFilter");
    }

    @Override
    protected IpAccessList featureValueOf(Interface actual) {
      return actual.getOutgoingOriginalFlowFilter();
    }
  }

  private static final class HasOutgoingFilter extends FeatureMatcher<Interface, IpAccessList> {
    HasOutgoingFilter(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "an Interface with outgoingFilter:", "outgoingFilter");
    }

    @Override
    protected IpAccessList featureValueOf(Interface actual) {
      return actual.getOutgoingFilter();
    }
  }

  private static final class HasPostTransformationIncomingFilter
      extends FeatureMatcher<Interface, IpAccessList> {
    HasPostTransformationIncomingFilter(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(
          subMatcher,
          "an Interface with postTransformationIncomingFilter:",
          "postTransformationIncomingFilter");
    }

    @Override
    protected IpAccessList featureValueOf(Interface actual) {
      return actual.getPostTransformationIncomingFilter();
    }
  }

  private static final class HasPreTransformationOutgoingFilter
      extends FeatureMatcher<Interface, IpAccessList> {
    HasPreTransformationOutgoingFilter(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(
          subMatcher,
          "an Interface with preTransformationOutgoingFilter:",
          "preTransformationOutgoingFilter");
    }

    @Override
    protected IpAccessList featureValueOf(Interface actual) {
      return actual.getPreTransformationOutgoingFilter();
    }
  }

  private static final class HasSpeed extends FeatureMatcher<Interface, Double> {
    HasSpeed(@Nonnull Matcher<? super Double> subMatcher) {
      super(subMatcher, "an Interface with speed:", "speed");
    }

    @Override
    protected Double featureValueOf(Interface actual) {
      return actual.getSpeed();
    }
  }

  private static final class HasSwitchPortEncapsulation
      extends FeatureMatcher<Interface, SwitchportEncapsulationType> {
    HasSwitchPortEncapsulation(@Nonnull Matcher<? super SwitchportEncapsulationType> subMatcher) {
      super(subMatcher, "an Interface with switchPortEncapsulation:", "switchPortEncapsulation");
    }

    @Override
    protected SwitchportEncapsulationType featureValueOf(Interface actual) {
      return actual.getSwitchportTrunkEncapsulation();
    }
  }

  private static final class HasSwitchPortMode extends FeatureMatcher<Interface, SwitchportMode> {
    HasSwitchPortMode(@Nonnull Matcher<? super SwitchportMode> subMatcher) {
      super(subMatcher, "an Interface with switchPortMode:", "switchPortMode");
    }

    @Override
    protected SwitchportMode featureValueOf(Interface actual) {
      return actual.getSwitchportMode();
    }
  }

  private static final class HasVlan extends FeatureMatcher<Interface, Integer> {
    HasVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with vlan:", "vlan");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getVlan();
    }
  }

  private static final class HasVrf extends FeatureMatcher<Interface, Vrf> {
    HasVrf(@Nonnull Matcher<? super Vrf> subMatcher) {
      super(subMatcher, "an Interface with vrf:", "vrf");
    }

    @Override
    protected Vrf featureValueOf(Interface actual) {
      return actual.getVrf();
    }
  }

  private static final class HasVrfName extends FeatureMatcher<Interface, String> {
    HasVrfName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Interface with vrfName:", "vrfName");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getVrfName();
    }
  }

  private static final class HasFirewallSessionInterfaceInfo
      extends FeatureMatcher<Interface, FirewallSessionInterfaceInfo> {
    HasFirewallSessionInterfaceInfo(
        @Nonnull Matcher<? super FirewallSessionInterfaceInfo> subMatcher) {
      super(
          subMatcher,
          "an Interface with FirewallSessionInterfaceInfo:",
          "firewallSessionInterfaceInfo");
    }

    @Override
    protected FirewallSessionInterfaceInfo featureValueOf(Interface actual) {
      return actual.getFirewallSessionInterfaceInfo();
    }
  }

  private static final class HasZoneName extends FeatureMatcher<Interface, String> {
    HasZoneName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Interface with zoneName:", "zoneName");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getZoneName();
    }
  }

  private static final class IsActive extends FeatureMatcher<Interface, Boolean> {
    IsActive(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with active:", "active");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getActive();
    }
  }

  private static final class IsAdminUp extends FeatureMatcher<Interface, Boolean> {
    IsAdminUp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with adminUp:", "adminUp");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getAdminUp();
    }
  }

  private static final class IsBlacklisted extends FeatureMatcher<Interface, Boolean> {
    IsBlacklisted(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with blacklisted:", "blacklisted");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getBlacklisted();
    }
  }

  private static final class IsLineUp extends FeatureMatcher<Interface, Boolean> {
    IsLineUp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with lineUp:", "lineUp");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getLineUp();
    }
  }

  private static final class HasInactiveReason extends FeatureMatcher<Interface, InactiveReason> {
    HasInactiveReason(@Nonnull Matcher<? super InactiveReason> subMatcher) {
      super(subMatcher, "an Interface with inactiveReason:", "inactiveReason");
    }

    @Override
    protected @Nullable InactiveReason featureValueOf(Interface anInterface) {
      return anInterface.getInactiveReason();
    }
  }

  private static final class IsAutoState extends FeatureMatcher<Interface, Boolean> {
    IsAutoState(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with autoState:", "autoState");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getAutoState();
    }
  }

  private static final class IsOspfPassive extends FeatureMatcher<Interface, Boolean> {
    IsOspfPassive(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with ospfPassive:", "ospfPassive");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfPassive();
    }
  }

  private static final class IsOspfPointToPoint extends FeatureMatcher<Interface, Boolean> {
    IsOspfPointToPoint(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with ospfPointToPoint:", "ospfPointToPoint");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfNetworkType() == OspfNetworkType.POINT_TO_POINT;
    }
  }

  private static final class IsProxyArp extends FeatureMatcher<Interface, Boolean> {
    IsProxyArp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with proxyArp:", "proxyArp");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getProxyArp();
    }
  }

  private static final class IsSwitchport extends FeatureMatcher<Interface, Boolean> {
    IsSwitchport(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with switchport:", "switchport");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getSwitchport();
    }
  }
}

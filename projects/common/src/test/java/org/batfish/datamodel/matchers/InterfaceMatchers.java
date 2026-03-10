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
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasAccessVlan;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasAdditionalArpIps;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasAddress;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasAddressMetadata;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasAllAddresses;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasAllowedVlans;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasBandwidth;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasChannelGroup;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasChannelGroupMembers;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasDeclaredNames;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasDependencies;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasDescription;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasDhcpRelayAddresses;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasEigrp;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasEncapsulationVlan;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasFirewallSessionInterfaceInfo;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasHsrpGroup;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasHsrpVersion;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasHumanName;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasInactiveReason;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasInterfaceType;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasIsis;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasMlagId;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasMtu;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasName;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasNativeVlan;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasOspfAreaName;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasOspfCost;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasOspfEnabled;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasOspfNetworkType;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasOspfPointToPoint;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasSpeed;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasSwitchPortEncapsulation;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasSwitchPortMode;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasVlan;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasVrf;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasVrfName;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.HasZoneName;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsActive;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsAdminUp;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsAutoState;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsBlacklisted;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsLineUp;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsOspfPassive;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsOspfPointToPoint;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsProxyArp;
import org.batfish.datamodel.matchers.InterfaceMatchersImpl.IsSwitchport;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.hamcrest.Matcher;

public final class InterfaceMatchers {

  /** Provides a matcher that matches if the provided value matches the interface's Access VLAN. */
  public static HasAccessVlan hasAccessVlan(int value) {
    return hasAccessVlan(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Access VLAN.
   */
  public static HasAccessVlan hasAccessVlan(Matcher<? super Integer> subMatcher) {
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
  public static HasAllowedVlans hasAllowedVlans(IntegerSpace value) {
    return hasAllowedVlans(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Allowed VLANs.
   */
  public static HasAllowedVlans hasAllowedVlans(Matcher<? super IntegerSpace> subMatcher) {
    return new HasAllowedVlans(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * additional arp IPs.
   */
  public static HasAdditionalArpIps hasAdditionalArpIps(
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
  public static HasBandwidth hasBandwidth(@Nonnull Matcher<? super Double> subMatcher) {
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
  public static HasDeclaredNames hasDeclaredNames(@Nonnull Iterable<String> expectedDeclaredNames) {
    return new HasDeclaredNames(
        containsInAnyOrder(ImmutableSet.copyOf(expectedDeclaredNames).toArray()));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * declared names.
   */
  public static HasDeclaredNames hasDeclaredNames(
      @Nonnull Matcher<? super Set<String>> subMatcher) {
    return new HasDeclaredNames(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided the interface's declared names comprise the set
   * of unique strings in {@code expectedDeclaredNames}.
   */
  public static HasDeclaredNames hasDeclaredNames(@Nonnull String... expectedDeclaredNames) {
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
  public static HasMlagId hasMlagId(int value) {
    return hasMlagId(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's MLAG
   * ID.
   */
  public static HasMlagId hasMlagId(Matcher<? super Integer> subMatcher) {
    return new HasMlagId(subMatcher);
  }

  /** Provides a matcher that matches if the provided value matches the interface's MTU. */
  public static HasMtu hasMtu(int value) {
    return hasMtu(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's MTU.
   */
  public static HasMtu hasMtu(Matcher<? super Integer> subMatcher) {
    return new HasMtu(subMatcher);
  }

  /** Provides a matcher that matches if the provided name matches the interface's name. */
  public static HasName hasName(String expectedName) {
    return new HasName(equalTo(expectedName));
  }

  /** Provides a matcher that matches if the provided value matches the interface's Native VLAN. */
  public static HasNativeVlan hasNativeVlan(@Nullable Integer value) {
    return hasNativeVlan(equalTo(value));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Native VLAN.
   */
  public static HasNativeVlan hasNativeVlan(Matcher<? super Integer> subMatcher) {
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
  public static HasOspfCost hasOspfCost(Matcher<Integer> subMatcher) {
    return new HasOspfCost(subMatcher);
  }

  /** Provides an {@link Interface} matcher that matches if the interface has OSPF enabled. */
  public static HasOspfEnabled hasOspfEnabled() {
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
  public static @Nonnull HasSpeed hasSpeed(@Nullable Double expectedSpeed) {
    return new HasSpeed(equalTo(expectedSpeed));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * speed.
   */
  public static @Nonnull HasSpeed hasSpeed(@Nonnull Matcher<? super Double> subMatcher) {
    return new HasSpeed(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided value matches the interface's Switch Port
   * encapsulation type
   */
  public static HasSwitchPortEncapsulation hasSwitchPortEncapsulation(
      SwitchportEncapsulationType switchportEncapsulationType) {
    return hasSwitchPortEncapsulation(equalTo(switchportEncapsulationType));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Switch Port encapsulation type
   */
  public static HasSwitchPortEncapsulation hasSwitchPortEncapsulation(
      Matcher<? super SwitchportEncapsulationType> subMatcher) {
    return new HasSwitchPortEncapsulation(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided value matches the interface's Switch Port mode.
   */
  public static HasSwitchPortMode hasSwitchPortMode(SwitchportMode switchportMode) {
    return hasSwitchPortMode(equalTo(switchportMode));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * Switch Port mode.
   */
  public static HasSwitchPortMode hasSwitchPortMode(Matcher<? super SwitchportMode> subMatcher) {
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
  public static HasVrf hasVrf(Matcher<? super Vrf> subMatcher) {
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
  public static HasZoneName hasZoneName(String name) {
    return new HasZoneName(equalTo(name));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's Zone
   * name.
   */
  public static HasZoneName hasZoneName(Matcher<? super String> subMatcher) {
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
  public static IsOspfPassive isOspfPassive() {
    return isOspfPassive(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * ospfPassive flag.
   */
  public static IsOspfPassive isOspfPassive(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new IsOspfPassive(subMatcher);
  }

  /** Provides a matcher that matches if the interface runs OSPF in point-to-point mode. */
  public static IsOspfPointToPoint isOspfPointToPoint() {
    return new IsOspfPointToPoint(equalTo(true));
  }

  /** Provides a matcher that matches if the interface has proxy-arp enabled. */
  public static IsProxyArp isProxyArp() {
    return new IsProxyArp(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the interface's
   * proxy-arp setting.
   */
  public static IsProxyArp isProxyArp(Matcher<? super Boolean> subMatcher) {
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

  private InterfaceMatchers() {}
}

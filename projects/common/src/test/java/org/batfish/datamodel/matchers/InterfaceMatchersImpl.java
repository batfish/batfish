package org.batfish.datamodel.matchers;

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

final class InterfaceMatchersImpl {

  static final class HasAccessVlan extends FeatureMatcher<Interface, Integer> {
    HasAccessVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with accessVlan:", "accessVlan");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getAccessVlan();
    }
  }

  static final class HasAdditionalArpIps extends FeatureMatcher<Interface, IpSpace> {
    HasAdditionalArpIps(@Nonnull Matcher<? super IpSpace> subMatcher) {
      super(subMatcher, "An interface with additionalArpIps:", "additionalArpIps");
    }

    @Override
    protected IpSpace featureValueOf(Interface actual) {
      return actual.getAdditionalArpIps();
    }
  }

  static final class HasAddress extends FeatureMatcher<Interface, InterfaceAddress> {
    HasAddress(@Nonnull Matcher<? super InterfaceAddress> subMatcher) {
      super(subMatcher, "An Interface with address:", "address");
    }

    @Override
    protected InterfaceAddress featureValueOf(Interface actual) {
      return actual.getAddress();
    }
  }

  static final class HasAddressMetadata
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

  static final class HasAllAddresses extends FeatureMatcher<Interface, Set<InterfaceAddress>> {
    HasAllAddresses(@Nonnull Matcher<? super Set<InterfaceAddress>> subMatcher) {
      super(subMatcher, "An Interface with allAddresses:", "allAddresses");
    }

    @Override
    protected Set<InterfaceAddress> featureValueOf(Interface actual) {
      return actual.getAllAddresses();
    }
  }

  static final class HasAllowedVlans extends FeatureMatcher<Interface, IntegerSpace> {
    HasAllowedVlans(@Nonnull Matcher<? super IntegerSpace> subMatcher) {
      super(subMatcher, "an Interface with allowedVlans:", "allowedVlans");
    }

    @Override
    protected IntegerSpace featureValueOf(Interface actual) {
      return actual.getAllowedVlans();
    }
  }

  static final class HasBandwidth extends FeatureMatcher<Interface, Double> {
    HasBandwidth(@Nonnull Matcher<? super Double> subMatcher) {
      super(subMatcher, "an Interface with bandwidth:", "bandwidth");
    }

    @Override
    protected Double featureValueOf(Interface actual) {
      return actual.getBandwidth();
    }
  }

  static final class HasChannelGroup extends FeatureMatcher<Interface, String> {
    HasChannelGroup(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An Interface with channelGroup", "channelGroup");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getChannelGroup();
    }
  }

  static final class HasChannelGroupMembers extends FeatureMatcher<Interface, SortedSet<String>> {
    HasChannelGroupMembers(@Nonnull Matcher<? super SortedSet<String>> subMatcher) {
      super(subMatcher, "An Interface with channelGroupMembers", "channelGroupMembers");
    }

    @Override
    protected SortedSet<String> featureValueOf(Interface actual) {
      return actual.getChannelGroupMembers();
    }
  }

  static final class HasDeclaredNames extends FeatureMatcher<Interface, Set<String>> {
    HasDeclaredNames(@Nonnull Matcher<? super Set<String>> subMatcher) {
      super(subMatcher, "declared names", "declared names");
    }

    @Override
    protected Set<String> featureValueOf(Interface actual) {
      return actual.getDeclaredNames();
    }
  }

  static final class HasDependencies extends FeatureMatcher<Interface, Set<Dependency>> {
    HasDependencies(@Nonnull Matcher<? super Set<Dependency>> subMatcher) {
      super(subMatcher, "An Interface with dependencies:", "dependencies");
    }

    @Override
    protected Set<Dependency> featureValueOf(Interface actual) {
      return actual.getDependencies();
    }
  }

  static final class HasDescription extends FeatureMatcher<Interface, String> {
    HasDescription(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An Interface with description:", "description");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getDescription();
    }
  }

  static final class HasDhcpRelayAddresses extends FeatureMatcher<Interface, List<Ip>> {
    HasDhcpRelayAddresses(@Nonnull Matcher<? super List<Ip>> subMatcher) {
      super(subMatcher, "An Interface with dhcpRelayAddresses", "dhcpRelayAddresses");
    }

    @Override
    protected List<Ip> featureValueOf(Interface actual) {
      return actual.getDhcpRelayAddresses();
    }
  }

  static final class HasEigrp extends FeatureMatcher<Interface, EigrpInterfaceSettings> {
    HasEigrp(@Nonnull Matcher<? super EigrpInterfaceSettings> subMatcher) {
      super(subMatcher, "An Interface with eigrp:", "eigrp");
    }

    @Override
    protected @Nullable EigrpInterfaceSettings featureValueOf(Interface actual) {
      return actual.getEigrp();
    }
  }

  static final class HasEncapsulationVlan extends FeatureMatcher<Interface, Integer> {
    HasEncapsulationVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with encapsulationVlan:", "encapsulationVlan");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getEncapsulationVlan();
    }
  }

  static final class HasHsrpGroup extends FeatureMatcher<Interface, HsrpGroup> {
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

  static final class HasHsrpVersion extends FeatureMatcher<Interface, String> {
    HasHsrpVersion(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An Interface with hsrpVersion:", "hsrpVersion");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getHsrpVersion();
    }
  }

  static final class HasHumanName extends FeatureMatcher<Interface, String> {
    HasHumanName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An Interface with human name:", "human name");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getHumanName();
    }
  }

  static final class HasIsis extends FeatureMatcher<Interface, IsisInterfaceSettings> {
    HasIsis(@Nonnull Matcher<? super IsisInterfaceSettings> subMatcher) {
      super(subMatcher, "An Interface with isis:", "isis");
    }

    @Override
    protected IsisInterfaceSettings featureValueOf(Interface actual) {
      return actual.getIsis();
    }
  }

  static final class HasMlagId extends FeatureMatcher<Interface, Integer> {
    HasMlagId(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with MLAG ID:", "mlagId");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getMlagId();
    }
  }

  static final class HasMtu extends FeatureMatcher<Interface, Integer> {
    HasMtu(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with MTU:", "MTU");
    }

    @Override
    protected Integer featureValueOf(Interface actual) {
      return actual.getMtu();
    }
  }

  static final class HasName extends FeatureMatcher<Interface, String> {
    HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Interface with name:", "name");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getName();
    }
  }

  static final class HasNativeVlan extends FeatureMatcher<Interface, Integer> {
    HasNativeVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with nativeVlan:", "nativeVlan");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getNativeVlan();
    }
  }

  static final class HasOspfAreaName extends FeatureMatcher<Interface, Long> {
    HasOspfAreaName(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "an Interface with ospfAreaName:", "ospfAreaName");
    }

    @Override
    protected Long featureValueOf(Interface actual) {
      return actual.getOspfAreaName();
    }
  }

  static final class HasOspfCost extends FeatureMatcher<Interface, Integer> {
    HasOspfCost(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with ospfCost:", "ospfCost");
    }

    @Override
    protected Integer featureValueOf(Interface actual) {
      return actual.getOspfCost();
    }
  }

  static final class HasOspfEnabled extends FeatureMatcher<Interface, Boolean> {
    HasOspfEnabled(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with ospfEnabled:", "ospfEnabled");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfEnabled();
    }
  }

  static final class HasOspfPointToPoint extends FeatureMatcher<Interface, Boolean> {
    HasOspfPointToPoint(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with ospfPointToPoint:", "ospfPointToPoint");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfNetworkType() == OspfNetworkType.POINT_TO_POINT;
    }
  }

  static final class HasOspfNetworkType extends FeatureMatcher<Interface, OspfNetworkType> {
    HasOspfNetworkType(@Nonnull Matcher<? super OspfNetworkType> subMatcher) {
      super(subMatcher, "an Interface with ospfNetworkType:", "ospfNetworkType");
    }

    @Override
    protected OspfNetworkType featureValueOf(Interface actual) {
      return actual.getOspfNetworkType();
    }
  }

  static final class HasIncomingFilter extends FeatureMatcher<Interface, IpAccessList> {
    HasIncomingFilter(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "an Interface with incomingFilter:", "incomingFilter");
    }

    @Override
    protected IpAccessList featureValueOf(Interface actual) {
      return actual.getIncomingFilter();
    }
  }

  static final class HasInterfaceType extends FeatureMatcher<Interface, InterfaceType> {
    HasInterfaceType(@Nonnull Matcher<? super InterfaceType> subMatcher) {
      super(subMatcher, "An Interface with interfaceType:", "interfaceType");
    }

    @Override
    protected InterfaceType featureValueOf(Interface actual) {
      return actual.getInterfaceType();
    }
  }

  static final class HasOutgoingOriginalFlowFilter extends FeatureMatcher<Interface, IpAccessList> {
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

  static final class HasOutgoingFilter extends FeatureMatcher<Interface, IpAccessList> {
    HasOutgoingFilter(@Nonnull Matcher<? super IpAccessList> subMatcher) {
      super(subMatcher, "an Interface with outgoingFilter:", "outgoingFilter");
    }

    @Override
    protected IpAccessList featureValueOf(Interface actual) {
      return actual.getOutgoingFilter();
    }
  }

  static final class HasPostTransformationIncomingFilter
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

  static final class HasPreTransformationOutgoingFilter
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

  static final class HasSpeed extends FeatureMatcher<Interface, Double> {
    HasSpeed(@Nonnull Matcher<? super Double> subMatcher) {
      super(subMatcher, "an Interface with speed:", "speed");
    }

    @Override
    protected Double featureValueOf(Interface actual) {
      return actual.getSpeed();
    }
  }

  static final class HasSwitchPortEncapsulation
      extends FeatureMatcher<Interface, SwitchportEncapsulationType> {
    HasSwitchPortEncapsulation(@Nonnull Matcher<? super SwitchportEncapsulationType> subMatcher) {
      super(subMatcher, "an Interface with switchPortEncapsulation:", "switchPortEncapsulation");
    }

    @Override
    protected SwitchportEncapsulationType featureValueOf(Interface actual) {
      return actual.getSwitchportTrunkEncapsulation();
    }
  }

  static final class HasSwitchPortMode extends FeatureMatcher<Interface, SwitchportMode> {
    HasSwitchPortMode(@Nonnull Matcher<? super SwitchportMode> subMatcher) {
      super(subMatcher, "an Interface with switchPortMode:", "switchPortMode");
    }

    @Override
    protected SwitchportMode featureValueOf(Interface actual) {
      return actual.getSwitchportMode();
    }
  }

  static final class HasVlan extends FeatureMatcher<Interface, Integer> {
    HasVlan(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "an Interface with vlan:", "vlan");
    }

    @Override
    protected @Nullable Integer featureValueOf(Interface actual) {
      return actual.getVlan();
    }
  }

  static final class HasVrf extends FeatureMatcher<Interface, Vrf> {
    HasVrf(@Nonnull Matcher<? super Vrf> subMatcher) {
      super(subMatcher, "an Interface with vrf:", "vrf");
    }

    @Override
    protected Vrf featureValueOf(Interface actual) {
      return actual.getVrf();
    }
  }

  static final class HasVrfName extends FeatureMatcher<Interface, String> {
    HasVrfName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Interface with vrfName:", "vrfName");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getVrfName();
    }
  }

  static final class HasFirewallSessionInterfaceInfo
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

  static final class HasZoneName extends FeatureMatcher<Interface, String> {
    HasZoneName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Interface with zoneName:", "zoneName");
    }

    @Override
    protected String featureValueOf(Interface actual) {
      return actual.getZoneName();
    }
  }

  static final class IsActive extends FeatureMatcher<Interface, Boolean> {
    IsActive(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with active:", "active");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getActive();
    }
  }

  static final class IsAdminUp extends FeatureMatcher<Interface, Boolean> {
    IsAdminUp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with adminUp:", "adminUp");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getAdminUp();
    }
  }

  static final class IsBlacklisted extends FeatureMatcher<Interface, Boolean> {
    IsBlacklisted(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with blacklisted:", "blacklisted");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getBlacklisted();
    }
  }

  static final class IsLineUp extends FeatureMatcher<Interface, Boolean> {
    IsLineUp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with lineUp:", "lineUp");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getLineUp();
    }
  }

  static final class HasInactiveReason extends FeatureMatcher<Interface, InactiveReason> {
    HasInactiveReason(@Nonnull Matcher<? super InactiveReason> subMatcher) {
      super(subMatcher, "an Interface with inactiveReason:", "inactiveReason");
    }

    @Override
    protected @Nullable InactiveReason featureValueOf(Interface anInterface) {
      return anInterface.getInactiveReason();
    }
  }

  static final class IsAutoState extends FeatureMatcher<Interface, Boolean> {
    IsAutoState(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with autoState:", "autoState");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getAutoState();
    }
  }

  static final class IsOspfPassive extends FeatureMatcher<Interface, Boolean> {
    IsOspfPassive(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with ospfPassive:", "ospfPassive");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfPassive();
    }
  }

  static final class IsOspfPointToPoint extends FeatureMatcher<Interface, Boolean> {
    IsOspfPointToPoint(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with ospfPointToPoint:", "ospfPointToPoint");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getOspfNetworkType() == OspfNetworkType.POINT_TO_POINT;
    }
  }

  static final class IsProxyArp extends FeatureMatcher<Interface, Boolean> {
    IsProxyArp(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with proxyArp:", "proxyArp");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getProxyArp();
    }
  }

  static final class IsSwitchport extends FeatureMatcher<Interface, Boolean> {
    IsSwitchport(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "an Interface with switchport:", "switchport");
    }

    @Override
    protected Boolean featureValueOf(Interface actual) {
      return actual.getSwitchport();
    }
  }

  private InterfaceMatchersImpl() {}
}

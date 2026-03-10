package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.answers.Schema;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.SpecifierFactories;

/**
 * Enables specification a set of interface properties.
 *
 * <p>Currently supported example specifiers:
 *
 * <ul>
 *   <li>channel_group -&gt; gets the interface's channel groups using a configured Java function
 *   <li>channel.* gets all properties that start with 'channel'
 * </ul>
 *
 * <p>In the future, we might add other specifier types, e.g., those based on Json Path
 */
@ParametersAreNonnullByDefault
public class InterfacePropertySpecifier extends PropertySpecifier {

  public static final String ACCESS_VLAN = "Access_VLAN";
  public static final String ACTIVE = "Active";
  public static final String ADMIN_UP = "Admin_Up";
  public static final String ALLOWED_VLANS = "Allowed_VLANs";
  public static final String ALL_PREFIXES = "All_Prefixes";
  public static final String AUTO_STATE_VLAN = "Auto_State_VLAN";
  public static final String BANDWIDTH = "Bandwidth";
  public static final String BLACKLISTED = "Blacklisted";
  public static final String CHANNEL_GROUP = "Channel_Group";
  public static final String CHANNEL_GROUP_MEMBERS = "Channel_Group_Members";
  public static final String DECLARED_NAMES = "Declared_Names";
  public static final String DESCRIPTION = "Description";
  public static final String DHCP_RELAY_ADDRESSES = "DHCP_Relay_Addresses";
  public static final String ENCAPSULATION_VLAN = "Encapsulation_VLAN";
  public static final String HSRP_GROUPS = "HSRP_Groups";
  public static final String HSRP_VERSION = "HSRP_Version";
  public static final String INACTIVE_REASON = "Inactive_Reason";
  public static final String INCOMING_FILTER_NAME = "Incoming_Filter_Name";
  public static final String INTERFACE_TYPE = "Interface_Type";
  public static final String MLAG_ID = "MLAG_ID";
  public static final String MTU = "MTU";
  public static final String NATIVE_VLAN = "Native_VLAN";
  public static final String OUTGOING_FILTER_NAME = "Outgoing_Filter_Name";
  public static final String PBR_POLICY_NAME = "PBR_Policy_Name";
  public static final String PRIMARY_ADDRESS = "Primary_Address";
  public static final String PRIMARY_NETWORK = "Primary_Network";
  public static final String PROXY_ARP = "Proxy_ARP";
  public static final String RIP_ENABLED = "Rip_Enabled";
  public static final String RIP_PASSIVE = "Rip_Passive";
  public static final String SPANNING_TREE_PORTFAST = "Spanning_Tree_Portfast";
  public static final String SPEED = "Speed";
  public static final String SWITCHPORT = "Switchport";
  public static final String SWITCHPORT_MODE = "Switchport_Mode";
  public static final String SWITCHPORT_TRUNK_ENCAPSULATION = "Switchport_Trunk_Encapsulation";
  public static final String VRF = "VRF";
  public static final String VRRP_GROUPS = "VRRP_Groups";
  public static final String ZONE_NAME = "Zone_Name";

  private static @Nullable String getInactiveReason(@Nonnull Interface i) {
    InactiveReason reason = i.getInactiveReason();
    if (reason == null) {
      return "";
    }
    return reason.description();
  }

  private static @Nullable String getIncomingFilterName(@Nonnull Interface i) {
    IpAccessList acl = i.getIncomingFilter();
    if (acl == null) {
      return null;
    }
    return acl.getName();
  }

  private static @Nullable String getOutgoingFilterName(@Nonnull Interface i) {
    IpAccessList acl = i.getOutgoingFilter();
    if (acl == null) {
      return null;
    }
    return acl.getName();
  }

  private static final Map<String, PropertyDescriptor<Interface>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<Interface>>()
          .put(
              ACCESS_VLAN,
              new PropertyDescriptor<>(
                  Interface::getAccessVlan,
                  Schema.INTEGER,
                  "VLAN number when the switchport mode is access (null otherwise)"))
          .put(
              ACTIVE,
              new PropertyDescriptor<>(
                  Interface::getActive, Schema.BOOLEAN, "Whether the interface is active"))
          .put(
              ADMIN_UP,
              new PropertyDescriptor<>(
                  Interface::getAdminUp,
                  Schema.BOOLEAN,
                  "Whether the interface is administratively enabled"))
          .put(
              ALLOWED_VLANS,
              new PropertyDescriptor<>(
                  Interface::getAllowedVlans,
                  Schema.STRING,
                  "Allowed VLAN numbers when the switchport mode is trunk"))
          .put(
              ALL_PREFIXES,
              new PropertyDescriptor<>(
                  Interface::getAllConcreteAddresses,
                  Schema.list(Schema.STRING),
                  "All IPv4 addresses assigned to the interface"))
          .put(
              AUTO_STATE_VLAN,
              new PropertyDescriptor<>(
                  Interface::getAutoState,
                  Schema.BOOLEAN,
                  "For VLAN interfaces, whether the operational status depends on member"
                      + " switchports"))
          .put(
              BANDWIDTH,
              new PropertyDescriptor<>(
                  Interface::getBandwidth,
                  Schema.DOUBLE,
                  "Nominal bandwidth in bits/sec, used for protocol cost calculations"))
          .put(
              BLACKLISTED,
              new PropertyDescriptor<>(
                  Interface::getBlacklisted,
                  Schema.BOOLEAN,
                  "Whether the interface is considered down for maintenance"))
          .put(
              CHANNEL_GROUP,
              new PropertyDescriptor<>(
                  Interface::getChannelGroup,
                  Schema.STRING,
                  "Name of the aggregated interface (e.g., a port channel) to which this interface"
                      + " belongs"))
          .put(
              CHANNEL_GROUP_MEMBERS,
              new PropertyDescriptor<>(
                  Interface::getChannelGroupMembers,
                  Schema.list(Schema.STRING),
                  "For aggregated interfaces (e.g., a port channel), names of constituent"
                      + " interfaces"))
          .put(
              DECLARED_NAMES,
              new PropertyDescriptor<>(
                  Interface::getDeclaredNames,
                  Schema.list(Schema.STRING),
                  "Any aliases explicitly defined for this interface"))
          .put(
              DESCRIPTION,
              new PropertyDescriptor<>(
                  Interface::getDescription, Schema.STRING, "Configured interface description"))
          .put(
              DHCP_RELAY_ADDRESSES,
              new PropertyDescriptor<>(
                  Interface::getDhcpRelayAddresses,
                  Schema.list(Schema.IP),
                  "IPv4 addresses to which incoming DHCP requests are relayed"))
          .put(
              ENCAPSULATION_VLAN,
              new PropertyDescriptor<>(
                  Interface::getEncapsulationVlan, Schema.INTEGER, "Number for VLAN encapsulation"))
          .put(
              HSRP_GROUPS,
              new PropertyDescriptor<>(
                  Interface::getHsrpGroups, Schema.set(Schema.STRING), "HSRP group identifiers"))
          .put(
              HSRP_VERSION,
              new PropertyDescriptor<>(
                  Interface::getHsrpVersion, Schema.STRING, "HSRP version that will be used"))
          .put(
              INACTIVE_REASON,
              new PropertyDescriptor<>(
                  InterfacePropertySpecifier::getInactiveReason,
                  Schema.STRING,
                  "Reason why interface is inactive"))
          // use incomingFilterName instead of incomingFilter
          .put(
              INCOMING_FILTER_NAME,
              new PropertyDescriptor<>(
                  InterfacePropertySpecifier::getIncomingFilterName,
                  Schema.STRING,
                  "Name of the input IPv4 filter"))
          .put(
              INTERFACE_TYPE,
              new PropertyDescriptor<>(
                  Interface::getInterfaceType, Schema.STRING, "Interface type"))
          .put(
              MLAG_ID,
              new PropertyDescriptor<>(
                  Interface::getMlagId, Schema.INTEGER, "MLAG identifier of the interface"))
          .put(
              MTU,
              new PropertyDescriptor<>(
                  Interface::getMtu, Schema.INTEGER, "Layer3 MTU of the interface"))
          .put(
              NATIVE_VLAN,
              new PropertyDescriptor<>(
                  Interface::getNativeVlan,
                  Schema.INTEGER,
                  "Native VLAN when switchport mode is trunk"))
          // skip outgoing filter
          .put(
              OUTGOING_FILTER_NAME,
              new PropertyDescriptor<>(
                  InterfacePropertySpecifier::getOutgoingFilterName,
                  Schema.STRING,
                  "Name of the output IPv4 filter"))
          // skip getOwner
          .put(
              PRIMARY_ADDRESS,
              new PropertyDescriptor<>(
                  Interface::getAddress,
                  Schema.STRING,
                  "Primary IPv4 address along with the prefix length"))
          .put(
              PRIMARY_NETWORK,
              new PropertyDescriptor<>(
                  Interface::getPrimaryNetwork,
                  Schema.STRING,
                  "Primary IPv4 subnet, in canonical form"))
          .put(
              PROXY_ARP,
              new PropertyDescriptor<>(
                  Interface::getProxyArp, Schema.BOOLEAN, "Whether proxy ARP is enabled"))
          .put(
              RIP_ENABLED,
              new PropertyDescriptor<>(
                  Interface::getRipEnabled, Schema.BOOLEAN, "Whether RIP is enabled"))
          .put(
              RIP_PASSIVE,
              new PropertyDescriptor<>(
                  Interface::getRipPassive,
                  Schema.BOOLEAN,
                  "Whether interface is in RIP passive mode"))
          .put(
              PBR_POLICY_NAME,
              new PropertyDescriptor<>(
                  Interface::getPacketPolicyName,
                  Schema.STRING,
                  "Name of policy-based routing (PBR) policy"))
          .put(
              SPANNING_TREE_PORTFAST,
              new PropertyDescriptor<>(
                  Interface::getSpanningTreePortfast,
                  Schema.BOOLEAN,
                  "Whether spanning-tree portfast feature is enabled"))
          .put(
              SPEED,
              new PropertyDescriptor<>(
                  Interface::getSpeed, Schema.DOUBLE, "Link speed in bits/sec"))
          .put(
              SWITCHPORT,
              new PropertyDescriptor<>(
                  Interface::getSwitchport,
                  Schema.BOOLEAN,
                  "Whether the interface is configured as switchport"))
          .put(
              SWITCHPORT_MODE,
              new PropertyDescriptor<>(
                  Interface::getSwitchportMode,
                  Schema.STRING,
                  "Switchport mode ("
                      + Arrays.stream(SwitchportMode.values())
                          .map(Object::toString)
                          .collect(Collectors.joining(", "))
                      + ") for switchport interfaces"))
          .put(
              SWITCHPORT_TRUNK_ENCAPSULATION,
              new PropertyDescriptor<>(
                  Interface::getSwitchportTrunkEncapsulation,
                  Schema.STRING,
                  "Encapsulation type ("
                      + Arrays.stream(SwitchportEncapsulationType.values())
                          .map(Object::toString)
                          .collect(Collectors.joining(", "))
                      + ") for switchport trunk interfaces"))
          .put(
              VRF,
              new PropertyDescriptor<>(
                  Interface::getVrf,
                  Schema.STRING,
                  "Name of the VRF to which the interface belongs"))
          .put(
              VRRP_GROUPS,
              new PropertyDescriptor<>(
                  Interface::getVrrpGroups,
                  Schema.list(Schema.INTEGER),
                  "All VRRP groups to which the interface belongs"))
          // skip zone
          .put(
              ZONE_NAME,
              new PropertyDescriptor<>(
                  Interface::getZoneName,
                  Schema.STRING,
                  "Name of the firewall zone to which the interface belongs"))
          .build();

  public static final InterfacePropertySpecifier ALL =
      new InterfacePropertySpecifier(JAVA_MAP.keySet());

  /** Returns the property descriptor for {@code property} */
  public static PropertyDescriptor<Interface> getPropertyDescriptor(String property) {
    checkArgument(JAVA_MAP.containsKey(property), "Property " + property + " does not exist");
    return JAVA_MAP.get(property);
  }

  private final @Nonnull List<String> _properties;

  /**
   * Create a node property specifier from provided expression. If the expression is null or empty,
   * a specifier with all properties is returned.
   */
  public static @Nonnull InterfacePropertySpecifier create(@Nullable String expression) {
    // by default, exclude interface type
    Set<String> properties = new HashSet<>(JAVA_MAP.keySet());
    properties.remove(INTERFACE_TYPE);
    return new InterfacePropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression,
                Grammar.INTERFACE_PROPERTY_SPECIFIER,
                new ConstantEnumSetSpecifier<>(properties))
            .resolve());
  }

  public InterfacePropertySpecifier(Set<String> properties) {
    Set<String> diffSet = Sets.difference(properties, JAVA_MAP.keySet());
    checkArgument(
        diffSet.isEmpty(),
        "Invalid properties supplied: %s. Valid properties are %s",
        diffSet,
        JAVA_MAP.keySet());
    _properties = properties.stream().sorted().collect(ImmutableList.toImmutableList());
  }

  @Override
  public List<String> getMatchingProperties() {
    return _properties;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfacePropertySpecifier)) {
      return false;
    }
    return _properties.equals(((InterfacePropertySpecifier) o)._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_properties);
  }
}

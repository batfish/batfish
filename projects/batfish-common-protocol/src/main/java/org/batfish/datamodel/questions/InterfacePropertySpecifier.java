package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;

/**
 * Enables specification a set of interface properties.
 *
 * <p>Currently supported example specifiers:
 *
 * <ul>
 *   <li>channel-group -&gt; gets the interface's channel groups using a configured Java function
 *   <li>channel.* gets all properties that start with 'channel'
 * </ul>
 *
 * <p>In the future, we might add other specifier types, e.g., those based on Json Path
 */
public class InterfacePropertySpecifier extends PropertySpecifier {

  public static Map<String, PropertyDescriptor<Interface>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<Interface>>()
          .put("Access_Vlan", new PropertyDescriptor<>(Interface::getAccessVlan, Schema.INTEGER))
          .put("Active", new PropertyDescriptor<>(Interface::getActive, Schema.BOOLEAN))
          .put(
              "Additional_Arp_Ips",
              new PropertyDescriptor<>(Interface::getAdditionalArpIps, Schema.list(Schema.IP)))
          .put(
              "Allowed_Vlans",
              new PropertyDescriptor<>(Interface::getAllowedVlans, Schema.list(Schema.STRING)))
          .put(
              "All_Prefixes",
              new PropertyDescriptor<>(Interface::getAllAddresses, Schema.list(Schema.STRING)))
          .put("Auto_State_Vlan", new PropertyDescriptor<>(Interface::getAutoState, Schema.BOOLEAN))
          .put("Bandwidth", new PropertyDescriptor<>(Interface::getBandwidth, Schema.DOUBLE))
          .put("Blacklisted", new PropertyDescriptor<>(Interface::getBlacklisted, Schema.BOOLEAN))
          .put("Channel_Group", new PropertyDescriptor<>(Interface::getChannelGroup, Schema.STRING))
          .put(
              "Channel_Group_Members",
              new PropertyDescriptor<>(
                  Interface::getChannelGroupMembers, Schema.list(Schema.STRING)))
          .put(
              "Declared_Names",
              new PropertyDescriptor<>(Interface::getDeclaredNames, Schema.list(Schema.STRING)))
          .put("Description", new PropertyDescriptor<>(Interface::getDescription, Schema.STRING))
          .put(
              "Dhcp_Relay_Addresses",
              new PropertyDescriptor<>(Interface::getDhcpRelayAddresses, Schema.list(Schema.IP)))
          .put(
              "Hsrp_Groups",
              new PropertyDescriptor<>(Interface::getHsrpGroups, Schema.set(Schema.STRING)))
          .put("Hsrp_Version", new PropertyDescriptor<>(Interface::getHsrpVersion, Schema.STRING))
          // skip inbound-filter
          .put(
              "Inbound_Filter_Name",
              new PropertyDescriptor<>(Interface::getInboundFilterName, Schema.STRING))
          // skip incoming filter
          .put(
              "Incoming_Filter_Name",
              new PropertyDescriptor<>(Interface::getIncomingFilter, Schema.STRING))
          .put(
              "Interface_Type",
              new PropertyDescriptor<>(Interface::getInterfaceType, Schema.STRING))
          .put("Mtu", new PropertyDescriptor<>(Interface::getMtu, Schema.INTEGER))
          .put("Native_Vlan", new PropertyDescriptor<>(Interface::getNativeVlan, Schema.INTEGER))
          // skip ospf area
          .put(
              "Ospf_Area_Name",
              new PropertyDescriptor<>(Interface::getOspfAreaName, Schema.INTEGER))
          .put("Ospf_Cost", new PropertyDescriptor<>(Interface::getOspfCost, Schema.INTEGER))
          .put("Ospf_Enabled", new PropertyDescriptor<>(Interface::getOspfEnabled, Schema.BOOLEAN))
          .put(
              "Ospf_Hello_Multiplier",
              new PropertyDescriptor<>(Interface::getOspfHelloMultiplier, Schema.INTEGER))
          .put("Ospf_Passive", new PropertyDescriptor<>(Interface::getOspfPassive, Schema.BOOLEAN))
          .put(
              "Ospf_Point_To_Point",
              new PropertyDescriptor<>(Interface::getOspfPointToPoint, Schema.BOOLEAN))
          // skip outgoing filter
          .put(
              "Outgoing_Filter_Name",
              new PropertyDescriptor<>(Interface::getOutgoingFilterName, Schema.STRING))
          // skip getOwner
          .put("Primary_Address", new PropertyDescriptor<>(Interface::getAddress, Schema.STRING))
          .put("Proxy_Arp", new PropertyDescriptor<>(Interface::getProxyArp, Schema.BOOLEAN))
          .put("Rip_Enabled", new PropertyDescriptor<>(Interface::getRipEnabled, Schema.BOOLEAN))
          .put("Rip_Passive", new PropertyDescriptor<>(Interface::getRipPassive, Schema.BOOLEAN))
          // skip routing policy
          .put(
              "Routing_Policy_Name",
              new PropertyDescriptor<>(Interface::getRoutingPolicyName, Schema.STRING))
          .put(
              "Source_Nats",
              new PropertyDescriptor<>(Interface::getSourceNats, Schema.list(Schema.STRING)))
          .put(
              "Spanning_Tree_Portfast",
              new PropertyDescriptor<>(Interface::getSpanningTreePortfast, Schema.BOOLEAN))
          .put("Switchport", new PropertyDescriptor<>(Interface::getSwitchport, Schema.BOOLEAN))
          .put(
              "Switchport_Mode",
              new PropertyDescriptor<>(Interface::getSwitchportMode, Schema.STRING))
          .put(
              "Switchport_Trunk_Encapsulation",
              new PropertyDescriptor<>(Interface::getSwitchportTrunkEncapsulation, Schema.STRING))
          .put("Vrf", new PropertyDescriptor<>(Interface::getVrf, Schema.STRING))
          .put(
              "Vrrp_Groups",
              new PropertyDescriptor<>(Interface::getVrrpGroups, Schema.list(Schema.INTEGER)))
          // skip zone
          .put("Zone_Name", new PropertyDescriptor<>(Interface::getZoneName, Schema.STRING))
          .build();

  public static final InterfacePropertySpecifier ALL = new InterfacePropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public InterfacePropertySpecifier(String expression) {
    _expression = expression;
    _pattern = Pattern.compile(_expression.trim().toLowerCase()); // canonicalize
  }

  /**
   * Returns a list of suggestions based on the query, based on {@link
   * PropertySpecifier#baseAutoComplete}.
   */
  public static List<AutocompleteSuggestion> autoComplete(String query) {
    return PropertySpecifier.baseAutoComplete(query, JAVA_MAP.keySet());
  }

  @Override
  public Set<String> getMatchingProperties() {
    return JAVA_MAP
        .keySet()
        .stream()
        .filter(prop -> _pattern.matcher(prop).matches())
        .collect(Collectors.toSet());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}

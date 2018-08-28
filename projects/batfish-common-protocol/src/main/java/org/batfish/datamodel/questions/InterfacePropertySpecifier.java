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
          .put("access-vlan", new PropertyDescriptor<>(Interface::getAccessVlan, Schema.INTEGER))
          .put("active", new PropertyDescriptor<>(Interface::getActive, Schema.BOOLEAN))
          .put(
              "additional-arp-ips",
              new PropertyDescriptor<>(Interface::getAdditionalArpIps, Schema.list(Schema.IP)))
          .put(
              "allowed-vlans",
              new PropertyDescriptor<>(Interface::getAllowedVlans, Schema.list(Schema.STRING)))
          .put(
              "all-prefixes",
              new PropertyDescriptor<>(Interface::getAllAddresses, Schema.list(Schema.STRING)))
          .put("auto-state-vlan", new PropertyDescriptor<>(Interface::getAutoState, Schema.BOOLEAN))
          .put("bandwidth", new PropertyDescriptor<>(Interface::getBandwidth, Schema.DOUBLE))
          .put("blacklisted", new PropertyDescriptor<>(Interface::getBlacklisted, Schema.BOOLEAN))
          .put("channel-group", new PropertyDescriptor<>(Interface::getChannelGroup, Schema.STRING))
          .put(
              "channel-group-members",
              new PropertyDescriptor<>(
                  Interface::getChannelGroupMembers, Schema.list(Schema.STRING)))
          .put(
              "declared-names",
              new PropertyDescriptor<>(Interface::getDeclaredNames, Schema.list(Schema.STRING)))
          .put("description", new PropertyDescriptor<>(Interface::getDescription, Schema.STRING))
          .put(
              "dhcp-relay-addresses",
              new PropertyDescriptor<>(Interface::getDhcpRelayAddresses, Schema.list(Schema.IP)))
          .put(
              "hsrp-groups",
              new PropertyDescriptor<>(Interface::getHsrpGroups, Schema.set(Schema.STRING)))
          .put("hsrp-version", new PropertyDescriptor<>(Interface::getHsrpVersion, Schema.STRING))
          // skip incoming filter
          .put(
              "incoming-filter-name",
              new PropertyDescriptor<>(Interface::getIncomingFilter, Schema.STRING))
          .put(
              "interface-type",
              new PropertyDescriptor<>(Interface::getInterfaceType, Schema.STRING))
          .put("mtu", new PropertyDescriptor<>(Interface::getMtu, Schema.INTEGER))
          .put("native-vlan", new PropertyDescriptor<>(Interface::getNativeVlan, Schema.INTEGER))
          // skip ospf area
          .put(
              "ospf-area-name",
              new PropertyDescriptor<>(Interface::getOspfAreaName, Schema.INTEGER))
          .put("ospf-cost", new PropertyDescriptor<>(Interface::getOspfCost, Schema.INTEGER))
          .put("ospf-enabled", new PropertyDescriptor<>(Interface::getOspfEnabled, Schema.BOOLEAN))
          .put(
              "ospf-hello-multiplier",
              new PropertyDescriptor<>(Interface::getOspfHelloMultiplier, Schema.INTEGER))
          .put("ospf-passive", new PropertyDescriptor<>(Interface::getOspfPassive, Schema.BOOLEAN))
          .put(
              "ospf-point-to-point",
              new PropertyDescriptor<>(Interface::getOspfPointToPoint, Schema.BOOLEAN))
          // skip outgoing filter
          .put(
              "outgoing-filter-name",
              new PropertyDescriptor<>(Interface::getOutgoingFilterName, Schema.STRING))
          // skip getOwner
          .put("primary-address", new PropertyDescriptor<>(Interface::getAddress, Schema.STRING))
          .put("proxy-arp", new PropertyDescriptor<>(Interface::getProxyArp, Schema.BOOLEAN))
          .put("rip-enabled", new PropertyDescriptor<>(Interface::getRipEnabled, Schema.BOOLEAN))
          .put("rip-passive", new PropertyDescriptor<>(Interface::getRipPassive, Schema.BOOLEAN))
          // skip routing policy
          .put(
              "routing-policy-name",
              new PropertyDescriptor<>(Interface::getRoutingPolicyName, Schema.STRING))
          .put(
              "source-nats",
              new PropertyDescriptor<>(Interface::getSourceNats, Schema.list(Schema.STRING)))
          .put(
              "spanning-tree-portfast",
              new PropertyDescriptor<>(Interface::getSpanningTreePortfast, Schema.BOOLEAN))
          .put("switchport", new PropertyDescriptor<>(Interface::getSwitchport, Schema.BOOLEAN))
          .put(
              "switchport-mode",
              new PropertyDescriptor<>(Interface::getSwitchportMode, Schema.STRING))
          .put(
              "switchport-trunk-encapsulation",
              new PropertyDescriptor<>(Interface::getSwitchportTrunkEncapsulation, Schema.STRING))
          .put("vrf", new PropertyDescriptor<>(Interface::getVrf, Schema.STRING))
          .put(
              "vrrp-groups",
              new PropertyDescriptor<>(Interface::getVrrpGroups, Schema.list(Schema.INTEGER)))
          // skip zone
          .put("zone-name", new PropertyDescriptor<>(Interface::getZoneName, Schema.STRING))
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

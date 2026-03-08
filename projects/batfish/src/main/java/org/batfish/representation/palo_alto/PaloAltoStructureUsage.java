package org.batfish.representation.palo_alto;

import org.batfish.vendor.StructureUsage;

public enum PaloAltoStructureUsage implements StructureUsage {
  ADDRESS_GROUP_STATIC("address-group static"),
  APPLICATION_GROUP_MEMBERS("application-group members"),
  APPLICATION_OVERRIDE_RULE_APPLICATION("rulebase application-override rules application"),
  APPLICATION_OVERRIDE_RULE_DESTINATION("rulebase application-override rules destination"),
  APPLICATION_OVERRIDE_RULE_FROM_ZONE("rulebase application-override rules from"),
  APPLICATION_OVERRIDE_RULE_SELF_REF("rulebase application-override rules"),
  APPLICATION_OVERRIDE_RULE_SOURCE("rulebase application-override rules source"),
  APPLICATION_OVERRIDE_RULE_TO_ZONE("rulebase application-override rules to"),
  BGP_PEER_LOCAL_ADDRESS_INTERFACE("bgp peer local-address interface"),
  BGP_PEER_ADDRESS("bgp peer peer-address"),
  ETHERNET_AGGREGATE_GROUP("ethernet aggregate-group"),
  IMPORT_INTERFACE("import network interface"),
  LAYER2_INTERFACE_ZONE("zone network layer2"),
  LAYER3_INTERFACE_ADDRESS("interface ethernet layer3 ip"),
  LAYER3_INTERFACE_ZONE("zone network layer3"),
  LOOPBACK_INTERFACE_ADDRESS("interface loopback ip"),
  NAT_RULE_DESTINATION("rulebase nat rules destination"),
  NAT_RULE_DESTINATION_TRANSLATION("rulebase nat rules destination-translation"),
  NAT_RULE_FROM_ZONE("rulebase nat rules from"),
  NAT_RULE_SERVICE("rulebase nat rules service"),
  NAT_RULE_SOURCE("rulebase nat rules source"),
  NAT_RULE_SOURCE_TRANSLATION("rulebase nat rules source-translation"),
  NAT_RULE_SELF_REF("rulebase nat rules"),
  NAT_RULE_TO_ZONE("rulebase nat rules to"),
  REDIST_RULE_REDIST_PROFILE("redist-rule redist-profile"),
  SECURITY_RULE_APPLICATION("rulebase security rules application"),
  SECURITY_RULE_CATEGORY("rulebase security rules category"),
  SECURITY_RULE_DESTINATION("rulebase security rules destination"),
  SECURITY_RULE_FROM_ZONE("rulebase security rules from"),
  SECURITY_RULE_SOURCE("rulebase security rules source"),
  SECURITY_RULE_TO_ZONE("rulebase security rules to"),
  SECURITY_RULE_SERVICE("rulebase security rules service"),
  SECURITY_RULE_SELF_REF("rulebase security rules"),
  SERVICE_GROUP_MEMBER("service-group members"),
  STATIC_ROUTE_INTERFACE("static-route interface"),
  STATIC_ROUTE_NEXTHOP_IP("static-route nexthop ip-address"),
  STATIC_ROUTE_NEXT_VR("static-route nexthop next-vr"),
  TAP_INTERFACE_ZONE("zone network tap"),
  TEMPLATE_STACK_TEMPLATES("template-stack templates"),
  VIRTUAL_ROUTER_INTERFACE("virtual-router interface"),
  VIRTUAL_ROUTER_SELF_REFERENCE("virtual-router self-reference"),
  VIRTUAL_WIRE_INTERFACE_ZONE("zone network virtual-wire"),
  VSYS_IMPORT_INTERFACE("vsys import interface"),
  ZONE_INTERFACE("zone network layer3");

  private final String _description;

  PaloAltoStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}

package org.batfish.specifier.parboiled;

import org.parboiled.Rule;

/** Contains information on various expressions supported by this package */
public enum Grammar {
  APPLICATION_SPECIFIER(
      "applicationSpecifier", Parser.INSTANCE.ApplicationSpec(), "application-specifier"),
  FILTER_SPECIFIER("filterSpecifier", Parser.INSTANCE.FilterSpec(), "filter-specifier"),
  INTERFACE_SPECIFIER("interfaceSpecifier", Parser.INSTANCE.InterfaceSpec(), "interface-specifier"),
  IP_PROTOCOL_SPECIFIER(
      "ipProtocolSpecifier", Parser.INSTANCE.IpProtocolSpec(), "ip-protocol-specifier"),
  IP_SPACE_SPECIFIER("ipSpecifier", Parser.INSTANCE.IpSpaceSpec(), "ip-specifier"),
  LOCATION_SPECIFIER("locationSpecifier", Parser.INSTANCE.LocationSpec(), "location-specifier"),
  NODE_SPECIFIER("nodeSpecifier", Parser.INSTANCE.NodeSpec(), "node-specifier"),
  ROUTING_POLICY_SPECIFIER(
      "routingPolicySpecifier", Parser.INSTANCE.RoutingPolicySpec(), "routing-policy-specifier");

  static final String BASE_URL =
      "https://github.com/batfish/batfish/blob/master/questions/Parameters.md#";

  /** Which expression is the entry point of the grammar */
  private final Rule _expression;

  /** What we call the grammar in user-facing documentation */
  private final String _friendlyName;

  /** Where the grammar is documented related to {@link Grammar#BASE_URL} */
  private final String _urlTail;

  Grammar(String friendlyName, Rule expression, String urlTail) {
    _expression = expression;
    _friendlyName = friendlyName;
    _urlTail = urlTail;
  }

  String getFullUrl() {
    return BASE_URL + getUrlTail();
  }

  public Rule getExpression() {
    return _expression;
  }

  public String getFriendlyName() {
    return _friendlyName;
  }

  String getUrlTail() {
    return _urlTail;
  }
}

package org.batfish.specifier.parboiled;

import org.parboiled.Rule;

/** Contains information on various expressions supported by this package */
public enum Grammar {
  FILTER_SPECIFIER("filterSpecifier", Parser.INSTANCE.FilterExpression(), "filter-specifier"),
  INTERFACE_SPECIFIER(
      "interfaceSpecifier", Parser.INSTANCE.InterfaceExpression(), "interface-specifier"),
  IP_SPACE_SPECIFIER("ipSpecifier", Parser.INSTANCE.IpSpaceExpression(), "ip-specifier"),
  LOCATION_SPECIFIER(
      "locationSpecifier", Parser.INSTANCE.LocationExpression(), "location-specifier"),
  NODE_SPECIFIER("nodeSpecifier", Parser.INSTANCE.NodeExpression(), "node-specifier");

  static final String BASE_URL =
      "https://github.com/batfish/batfish/blob/master/questions/Parameters.md#";

  /** Which expression is the entry point of the grammar */
  private final Rule _expression;

  /** What we call the grammar in user-facing documentation */
  private final String _friendlyName;

  /** Where the grammar is documented related to {@link Grammar#_BASE_URL} */
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

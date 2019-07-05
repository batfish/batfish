package org.batfish.specifier.parboiled;

import java.util.Arrays;
import java.util.Collection;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;

/** Contains information on various expressions supported by this package */
public enum Grammar {
  APPLICATION_SPECIFIER("applicationSpecifier", "application-specifier"),
  FILTER_SPECIFIER("filterSpecifier", "filter-specifier"),
  INTERFACE_SPECIFIER("interfaceSpecifier", "interface-specifier"),
  IP_PROTOCOL_SPECIFIER("ipProtocolSpecifier", "ip-protocol-specifier"),
  IP_SPACE_SPECIFIER("ipSpecifier", "ip-specifier"),
  LOCATION_SPECIFIER("locationSpecifier", "location-specifier"),
  NAMED_STRUCTURE_SPECIFIER("namedStructureSpecifier", "named-structure-specifier"),
  NODE_SPECIFIER("nodeSpecifier", "node-specifier"),
  ROUTING_POLICY_SPECIFIER("routingPolicySpecifier", "routing-policy-specifier");

  static final String BASE_URL =
      "https://github.com/batfish/batfish/blob/master/questions/Parameters.md#";

  /** What we call the grammar in user-facing documentation */
  private final String _friendlyName;

  /** Where the grammar is documented related to {@link Grammar#BASE_URL} */
  private final String _urlTail;

  Grammar(String friendlyName, String urlTail) {
    _friendlyName = friendlyName;
    _urlTail = urlTail;
  }

  String getFullUrl() {
    return BASE_URL + getUrlTail();
  }

  public String getFriendlyName() {
    return _friendlyName;
  }

  String getUrlTail() {
    return _urlTail;
  }

  @Override
  public String toString() {
    return _friendlyName;
  }

  public static Collection<?> getEnumValues(Grammar grammar) {
    switch (grammar) {
      case APPLICATION_SPECIFIER:
        return Arrays.asList(Protocol.values());
      case NAMED_STRUCTURE_SPECIFIER:
        return NamedStructurePropertySpecifier.JAVA_MAP.keySet();
      default:
        throw new IllegalArgumentException(grammar + " is not an enum grammar");
    }
  }
}

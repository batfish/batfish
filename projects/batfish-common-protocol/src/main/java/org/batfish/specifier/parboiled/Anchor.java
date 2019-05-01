package org.batfish.specifier.parboiled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This annotation is applied to rules that we expect to be the basis of error reporting and auto
 * completion. The value of the annotation is the auto completion type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Anchor {

  @ParametersAreNonnullByDefault
  enum Type {
    ADDRESS_GROUP_NAME("ADDRESS_GROUP_NAME", "Address group name", "address-group"),
    CHAR_LITERAL("CHAR_LITERAL", "Character literal", "char-literal"),
    // grammar rules that are deprecated
    DEPRECATED("DEPRECATED", "Deprecated", "deprecated"),
    EOI("EOI", "EoI", "EoI"),
    FILTER_INTERFACE_IN("FILTER_INTERFACE_IN", "Incoming filter on interface", "<interface-spec>)"),
    FILTER_INTERFACE_OUT(
        "FILTER_INTERFACE_OUT", "Outgoing filter on interface", "<interface-spec>)"),
    FILTER_NAME("FILTER_NAME", "Filter name", "filter-name"),
    FILTER_NAME_REGEX("FILTER_NAME_REGEX", "Filter name regex", "filter-name-regex"),
    FILTER_PARENS("FILTER_PARENS", "Filter specifier", "<filter-specifier>)"),
    // grammar rules that shouldn't be the basis for autocompletion
    IGNORE("IGNORE", "Ignore", "ignore"),
    INTERFACE_CONNECTED_TO(
        "INTERFACE_CONNECTED_TO", "Interfaces connected to IP addresses", "<ip-specifier>)"),
    INTERFACE_GROUP_NAME("INTERFACE_GROUP_NAME", "Interface group name", "interface-group"),
    INTERFACE_NAME("INTERFACE_NAME", "Interface name", "interface-name"),
    INTERFACE_NAME_REGEX("INTERFACE_NAME_REGEX", "Interface name regex", "interface-name-regex"),
    INTERFACE_PARENS("INTERFACE_PARENS", "Interface specifier", "<interface-specifier>)"),
    INTERFACE_TYPE("INTERFACE_TYPE", "Interfaces of type", "<interface-type>)"),
    INTERFACE_VRF("INTERFACE_VRF", "Interfaces in VRF", "<vrf-name>)"),
    INTERFACE_ZONE("INTERFACE_ZONE", "Interfaces in zone", "<zone-name>)"),
    IP_ADDRESS("IP_ADDRESS", "IP address", "<ip-address>"),
    IP_ADDRESS_MASK("IP_ADDRESS_MASK", "IP address mask", "<ip-address:mask>"),
    IP_PREFIX("IP_PREFIX", "IP prefix", "<prefix-length>"),
    IP_PROTOCOL_NUMBER("IP_PROTOCOL_NUMBER", "IP protocol", "<ip-protocol>"),
    IP_RANGE("IP_RANGE", "IP range", "<ip-address-to>"),
    IP_WILDCARD("IP_WILDCARD", "IP wildcard", "<wildcard>"),
    LOCATION_PARENS("LOCATION_PARENS", "Location specifier", "<location-specifier>)"),
    NODE_AND_INTERFACE(
        "NODE_AND_INTERFACE", "Node and interface pair", "<node-specifier>[<interface-specifier>]"),
    NODE_NAME("NODE_NAME", "Node name", "node-name"),
    NODE_NAME_REGEX("NODE_NAME_REGEX", "Node name regex", "node-name-regex/"),
    NODE_PARENS("NODE_PARENS", "Node specifier", "<node-specifier>)"),
    NODE_ROLE_AND_DIMENSION(
        "NODE_ROLE_AND_DIMENSION", "Nodes in the role", "<role-name>, <dimension-name>)"),
    NODE_ROLE_DIMENSION_NAME(
        "NODE_ROLE_DIMENSION_NAME", "Node role dimension name", "node-role-dimension"),
    NODE_ROLE_NAME("NODE_ROLE_NAME", "Node role name", "node-role"),
    NODE_TYPE("DEVICE_TYPE", "Device type", "device-type"),
    REFERENCE_BOOK_AND_ADDRESS_GROUP(
        "REFERENCE_BOOK_AND_ADDRESS_GROUP",
        "IP address space in an address group",
        "<reference-book>, <address-group>)"),
    REFERENCE_BOOK_AND_INTERFACE_GROUP(
        "REFERENCE_BOOK_AND_INTERFACE_GROUP",
        "Interfaces in an interface group",
        "<reference-book>, <interface-group>)"),
    REFERENCE_BOOK_NAME("REFERENCE_BOOK_NAME", "Reference book name", "reference-book"),
    ROUTING_POLICY_NAME("ROUTING_POLICY_NAME", "Routing policy name", "routing-policy"),
    ROUTING_POLICY_NAME_REGEX(
        "ROUTING_POLICY_NAME_REGEX", "Routing policy name regex", "<routing-policy-name-regex>/"),
    ROUTING_POLICY_PARENS(
        "ROUTING_POLICY_PARENS", "Routing policy specifier", "<routing-policy-specifier>)"),
    STRING_LITERAL("STRING_LITERAL", "String literal", "string-literal"),
    VRF_NAME("VRF_NAME", "VRF name", "vrf-name"),
    WHITESPACE("WHITESPACE", "Whitespace", "whitespace"),
    ZONE_NAME("ZONE_NAME", "Zone name", "zone-name");

    @Nonnull private final String _description;

    @Nonnull private final String _hint;

    @Nonnull private final String _name;

    Type(String name, String description, String hint) {
      _name = name;
      _description = description;
      _hint = hint;
    }

    @Nonnull
    public String getDescription() {
      return _description;
    }

    @Nonnull
    public String getHint() {
      return _hint;
    }

    @Nonnull
    public String getName() {
      return _name;
    }
  }

  Type value();
}

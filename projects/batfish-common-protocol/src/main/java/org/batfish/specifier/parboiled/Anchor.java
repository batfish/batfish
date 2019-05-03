package org.batfish.specifier.parboiled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.AutocompleteSuggestion.SuggestionType;

/**
 * This annotation is applied to rules that we expect to be the basis of error reporting and auto
 * completion. The value of the annotation is the auto completion type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Anchor {

  @ParametersAreNonnullByDefault
  enum Type {
    ADDRESS_GROUP_NAME(
        "ADDRESS_GROUP_NAME", "Address group name", "addressGroup", SuggestionType.NAME_LITERAL),
    CHAR_LITERAL("CHAR_LITERAL", "Character literal", "character", SuggestionType.UNKNOWN),
    // grammar rules that are deprecated
    DEPRECATED("DEPRECATED", "Deprecated", "deprecated", SuggestionType.UNKNOWN),
    EOI("EOI", "EoI", "EoI", SuggestionType.UNKNOWN),
    FILTER_INTERFACE_IN(
        "FILTER_INTERFACE_IN",
        "Incoming filter on interface",
        "interfaceSpec)",
        SuggestionType.FUNCTION),
    FILTER_INTERFACE_OUT(
        "FILTER_INTERFACE_OUT",
        "Outgoing filter on interface",
        "interfaceSpec)",
        SuggestionType.FUNCTION),
    FILTER_NAME("FILTER_NAME", "Filter name", "filterName", SuggestionType.NAME_LITERAL),
    FILTER_NAME_REGEX(
        "FILTER_NAME_REGEX", "Filter name regex", "filterNameRegex", SuggestionType.REGEX),
    FILTER_PARENS("FILTER_PARENS", "Filter specifier", "filterSpec)", SuggestionType.PARENTHESIS),
    // grammar rules that shouldn't be the basis for autocompletion
    IGNORE("IGNORE", "Ignore", "ignore", SuggestionType.UNKNOWN),
    INTERFACE_CONNECTED_TO(
        "INTERFACE_CONNECTED_TO",
        "Interfaces connected to IP addresses",
        "ipSpec)",
        SuggestionType.FUNCTION),
    INTERFACE_GROUP_NAME(
        "INTERFACE_GROUP_NAME",
        "Interface group name",
        "interfaceGroup",
        SuggestionType.NAME_LITERAL),
    INTERFACE_NAME(
        "INTERFACE_NAME", "Interface name", "interfaceName", SuggestionType.NAME_LITERAL),
    INTERFACE_NAME_REGEX(
        "INTERFACE_NAME_REGEX", "Interface name regex", "interfaceNameRegex", SuggestionType.REGEX),
    INTERFACE_PARENS(
        "INTERFACE_PARENS", "Interface specifier", "interfaceSpec)", SuggestionType.PARENTHESIS),
    INTERFACE_TYPE(
        "INTERFACE_TYPE", "Interfaces of type", "interfaceType)", SuggestionType.FUNCTION),
    INTERFACE_VRF("INTERFACE_VRF", "Interfaces in VRF", "vrfName)", SuggestionType.FUNCTION),
    INTERFACE_ZONE("INTERFACE_ZONE", "Interfaces in zone", "zoneName)", SuggestionType.FUNCTION),
    IP_ADDRESS("IP_ADDRESS", "IP address", "ip-address", SuggestionType.ADDRESS_LITERAL),
    IP_ADDRESS_MASK(
        "IP_ADDRESS_MASK", "IP address mask", "ipAddressMask", SuggestionType.ADDRESS_LITERAL),
    IP_PREFIX("IP_PREFIX", "IP prefix", "prefix-length", SuggestionType.ADDRESS_LITERAL),
    IP_PROTOCOL_NUMBER("IP_PROTOCOL_NUMBER", "IP protocol", "ipProtocol", SuggestionType.CONSTANT),
    IP_RANGE("IP_RANGE", "IP range", "ipAddressEnd", SuggestionType.ADDRESS_LITERAL),
    IP_WILDCARD("IP_WILDCARD", "IP wildcard", "wildcard", SuggestionType.ADDRESS_LITERAL),
    LOCATION_ENTER(
        "LOCATION_ENTER",
        "Packets entering interface",
        "locationInterface)",
        SuggestionType.FUNCTION),
    LOCATION_PARENS(
        "LOCATION_PARENS", "Location specifier", "locationSpec)", SuggestionType.PARENTHESIS),
    NODE_AND_INTERFACE(
        "NODE_AND_INTERFACE",
        "Node and interface pair",
        "interfaceSpec]",
        SuggestionType.OPERATOR_WITH_RHS),
    NODE_NAME("NODE_NAME", "Node name", "nodeName", SuggestionType.NAME_LITERAL),
    NODE_NAME_REGEX("NODE_NAME_REGEX", "Node name regex", "nodeNameRegex/", SuggestionType.REGEX),
    NODE_PARENS("NODE_PARENS", "Node specifier", "nodeSpec)", SuggestionType.PARENTHESIS),
    NODE_ROLE_AND_DIMENSION(
        "NODE_ROLE_AND_DIMENSION",
        "Nodes in the role",
        "roleName, dimensionName)",
        SuggestionType.FUNCTION),
    NODE_ROLE_DIMENSION_NAME(
        "NODE_ROLE_DIMENSION_NAME",
        "Node role dimension name",
        "dimensionName",
        SuggestionType.NAME_LITERAL),
    NODE_ROLE_NAME("NODE_ROLE_NAME", "Node role name", "roleName", SuggestionType.NAME_LITERAL),
    NODE_TYPE("DEVICE_TYPE", "Device type", "deviceType)", SuggestionType.FUNCTION),
    REFERENCE_BOOK_AND_ADDRESS_GROUP(
        "REFERENCE_BOOK_AND_ADDRESS_GROUP",
        "IP address space in an address group",
        "referenceBook, addressGroup)",
        SuggestionType.FUNCTION),
    REFERENCE_BOOK_AND_INTERFACE_GROUP(
        "REFERENCE_BOOK_AND_INTERFACE_GROUP",
        "Interfaces in an interface group",
        "referenceBook, interfaceGroup)",
        SuggestionType.FUNCTION),
    REFERENCE_BOOK_NAME(
        "REFERENCE_BOOK_NAME", "Reference book name", "referenceBook", SuggestionType.NAME_LITERAL),
    ROUTING_POLICY_NAME(
        "ROUTING_POLICY_NAME",
        "Routing policy name",
        "routingPolicyName",
        SuggestionType.NAME_LITERAL),
    ROUTING_POLICY_NAME_REGEX(
        "ROUTING_POLICY_NAME_REGEX",
        "Routing policy name regex",
        "routingPolicyNameRegex/",
        SuggestionType.REGEX),
    ROUTING_POLICY_PARENS(
        "ROUTING_POLICY_PARENS",
        "Routing policy specifier",
        "routingPolicySpec)",
        SuggestionType.PARENTHESIS),
    STRING_LITERAL("STRING_LITERAL", "String literal", "string", SuggestionType.CONSTANT),
    UNKNOWN("UNKNOWN", "Unknown", "unknown", SuggestionType.UNKNOWN),
    VRF_NAME("VRF_NAME", "VRF name", "vrfName", SuggestionType.NAME_LITERAL),
    WHITESPACE("WHITESPACE", "Whitespace", "whitespace", SuggestionType.UNKNOWN),
    ZONE_NAME("ZONE_NAME", "Zone name", "zoneName", SuggestionType.NAME_LITERAL);

    @Nonnull private final String _description;

    @Nonnull private final String _hint;

    @Nonnull private final String _name;

    @Nonnull private final SuggestionType _suggestionType;

    Type(String name, String description, String hint, SuggestionType suggestionType) {
      _name = name;
      _description = description;
      _hint = hint;
      _suggestionType = suggestionType;
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

    @Nonnull
    public SuggestionType getSuggestionType() {
      return _suggestionType;
    }
  }

  Type value();
}

package org.batfish.specifier.parboiled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.AutocompleteSuggestion.SuggestionType;

/**
 * This annotation is applied to rules that we expect to be the basis of error reporting and auto
 * completion. The value of the annotation is the auto completion type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Anchor {

  /** An enum to describe different types of anchors */
  @ParametersAreNonnullByDefault
  enum Type {
    /** Names of address groups in a reference book */
    ADDRESS_GROUP_NAME(
        "ADDRESS_GROUP_NAME", "Address group name", null, SuggestionType.NAME_LITERAL),
    /** Names of applications, e.g., DNS */
    APPLICATION_NAME("APPLICATION_NAME", null, null, SuggestionType.CONSTANT),
    /**
     * Denotes a set operation for applicationSpec. The full description is filled in {@link
     * ParboiledAutoCompleteSuggestion#completeDescriptionIfNeeded}.
     */
    APPLICATION_SET_OP(
        "APPLICATION_SET_OP", " of applications", "applicationSpec", SuggestionType.SET_OPERATOR),
    /**
     * A character literal, e.g., set operators and parenthesis. It is an implicit anchor assigned
     * based on Parboiled path element labels.
     */
    CHAR_LITERAL("CHAR_LITERAL", null, null, SuggestionType.UNKNOWN),
    /** For grammar rules that are deprecated and we ignore for auto completion */
    DEPRECATED("DEPRECATED", null, null, SuggestionType.UNKNOWN),
    /** Impicit anchor to mark the end of input */
    EOI("EOI", null, null, SuggestionType.UNKNOWN),
    /** For @in() function of filterSpec */
    FILTER_INTERFACE_IN(
        "FILTER_INTERFACE_IN",
        "Incoming filter on interface",
        "interfaceSpec)",
        SuggestionType.FUNCTION),
    /** For @out() function of filterSpec */
    FILTER_INTERFACE_OUT(
        "FILTER_INTERFACE_OUT",
        "Outgoing filter on interface",
        "interfaceSpec)",
        SuggestionType.FUNCTION),
    /** Denotes the name of a filter */
    FILTER_NAME("FILTER_NAME", "Filter name", null, SuggestionType.NAME_LITERAL),
    /** Denotes the name regex for filters */
    FILTER_NAME_REGEX(
        "FILTER_NAME_REGEX", "Filter name regex", "filterNameRegex", SuggestionType.REGEX),
    /** For a rule that denotes filterSpec can be parenthetical */
    FILTER_PARENS("FILTER_PARENS", "Filter specifier", "filterSpec)", SuggestionType.OPEN_PARENS),
    /**
     * Denotes a set operation for filterSpec. The full description is filled in {@link
     * ParboiledAutoCompleteSuggestion#completeDescriptionIfNeeded}.
     */
    FILTER_SET_OP("FILTER_SET_OP", " of filters", "filterSpec", SuggestionType.SET_OPERATOR),
    /** Rules tagged as IGNORE and their children will not be used for autocompletion */
    IGNORE("IGNORE", null, null, SuggestionType.UNKNOWN),
    /** connectedTo() function for interfaces */
    INTERFACE_CONNECTED_TO(
        "INTERFACE_CONNECTED_TO",
        "Interfaces connected to IP addresses",
        "ipSpec)",
        SuggestionType.FUNCTION),
    /** Interface group names in a reference book */
    INTERFACE_GROUP_NAME(
        "INTERFACE_GROUP_NAME", "Interface group name", null, SuggestionType.NAME_LITERAL),
    /** Rule for interface name */
    INTERFACE_NAME("INTERFACE_NAME", "Interface name", null, SuggestionType.NAME_LITERAL),
    /** Rule for interface name regex */
    INTERFACE_NAME_REGEX(
        "INTERFACE_NAME_REGEX", "Interface name regex", "interfaceNameRegex", SuggestionType.REGEX),
    /** For a rule that denotes interfaceSpec can be paranthetical */
    INTERFACE_PARENS(
        "INTERFACE_PARENS", "Interface specifier", "interfaceSpec)", SuggestionType.OPEN_PARENS),
    /**
     * Denotes a set operation for interfaceSpec. The full description is filled in {@link
     * ParboiledAutoCompleteSuggestion#completeDescriptionIfNeeded}.
     */
    INTERFACE_SET_OP(
        "INTERFACE_SET_OP", " of interfaces", "interfaceSpec", SuggestionType.SET_OPERATOR),
    /** interfaceType() function */
    INTERFACE_TYPE(
        "INTERFACE_TYPE", "Interfaces of type", "interfaceType)", SuggestionType.FUNCTION),
    /** vrf() function to identify interfaces */
    INTERFACE_VRF("INTERFACE_VRF", "Interfaces in VRF", "vrfName)", SuggestionType.FUNCTION),
    /** zone() function to identify interfaces */
    INTERFACE_ZONE("INTERFACE_ZONE", "Interfaces in zone", "zoneName)", SuggestionType.FUNCTION),
    /** Name of an IP protocol such as TCP */
    IP_PROTOCOL_NAME("IP_PROTOCOL_NAME", null, null, SuggestionType.CONSTANT),
    /** Rule for excluding an IP protocol */
    IP_PROTOCOL_NOT(
        "IP_PROTOCOL_NOT", "Exclude IP protocol", "ipProtocol", SuggestionType.OPERATOR_NON_END),
    /** IPv4 address */
    IP_ADDRESS("IP_ADDRESS", "IP address", null, SuggestionType.ADDRESS_LITERAL),
    /** IPv4 address mask */
    IP_ADDRESS_MASK("IP_ADDRESS_MASK", "IP address mask", null, SuggestionType.ADDRESS_LITERAL),
    /** IPv4 address prefix */
    IP_PREFIX("IP_PREFIX", "IP prefix", null, SuggestionType.ADDRESS_LITERAL),
    /** IP protocol number such as 6 */
    IP_PROTOCOL_NUMBER("IP_PROTOCOL_NUMBER", "IP protocol", null, SuggestionType.CONSTANT),
    /**
     * Denotes a set operation for ipProtocolSpec. The full description is filled in {@link
     * ParboiledAutoCompleteSuggestion#completeDescriptionIfNeeded}.
     */
    IP_PROTOCOL_SET_OP(
        "IP_PROTOCOL_SET_OP", " of IP protocols", "ipProtocolSpec", SuggestionType.SET_OPERATOR),
    /** IPv4 address range ip1 - ip2 */
    IP_RANGE("IP_RANGE", "IP range", null, SuggestionType.ADDRESS_LITERAL),
    /**
     * Denotes a set operation for ipSpaceSpec. The full description is filled in {@link
     * ParboiledAutoCompleteSuggestion#completeDescriptionIfNeeded}.
     */
    IP_SPACE_PARENS("IP_SPACE_PARENS", "IP space", "ipSpec)", SuggestionType.OPEN_PARENS),
    IP_SPACE_SET_OP("IP_SPACE_SET_OP", " of IP spaces", "ipSpec", SuggestionType.SET_OPERATOR),
    /** IPv4 wildcard ip1:ip2 */
    IP_WILDCARD("IP_WILDCARD", "IP wildcard", null, SuggestionType.ADDRESS_LITERAL),
    /** enter() function for locationSpec */
    LOCATION_ENTER(
        "LOCATION_ENTER",
        "Packets entering interface",
        "locationInterface)",
        SuggestionType.FUNCTION),
    /** For a rule that denotes locationSpec can be parenthetical */
    LOCATION_PARENS(
        "LOCATION_PARENS", "Location specifier", "locationSpec)", SuggestionType.OPEN_PARENS),
    /**
     * Denotes a set operation for locationSpec. The full description is filled in {@link
     * ParboiledAutoCompleteSuggestion#completeDescriptionIfNeeded}.
     */
    LOCATION_SET_OP(
        "INTERFACE_SET_OP", " of locations", "locationSpec", SuggestionType.SET_OPERATOR),
    /**
     * Denotes a set operation for namedStructureSpec. The full description is filled in {@link
     * ParboiledAutoCompleteSuggestion#completeDescriptionIfNeeded}.
     */
    NAMED_STRUCTURE_SET_OP(
        "NAMED_STRUCTURE_SET_OP",
        " of named structure types",
        "namedStructureSpec",
        SuggestionType.SET_OPERATOR),
    /** Named structure type, e.g., ROUTE_FILTER_LIST */
    NAMED_STRUCTURE_TYPE("NAMED_STRUCTURE_TYPE", null, null, SuggestionType.CONSTANT),
    /** Regex for named structure types */
    NAMED_STRUCTURE_TYPE_REGEX(
        "NAMED_STRUCTURE_TYPE_REGEX",
        "Named structure type regex",
        "namedStructureTypeRegex/",
        SuggestionType.REGEX),
    /**
     * Rule for node[filter] style filterSpec. This anchor shouldn't be suggested as the rule has no
     * literal, and things will get delegated to sub rules.
     */
    NODE_AND_FILTER("NODE_AND_FILTER", "Node and filter pair", null, SuggestionType.UNKNOWN),
    /** Part of node[filter] after node */
    NODE_AND_FILTER_TAIL(
        "NODE_AND_FILTER_TAIL",
        "Filters of specified node(s)",
        "filterSpec]",
        SuggestionType.OPERATOR_NON_END),
    /**
     * Rule for node[interface] style interfaceSpec. This anchor shouldn't be suggested as the rule
     * has no literal, and things will get delegated to sub rules.
     */
    NODE_AND_INTERFACE(
        "NODE_AND_INTERFACE", "Node and interface pair", null, SuggestionType.UNKNOWN),
    /** Part of node[interface] after node */
    NODE_AND_INTERFACE_TAIL(
        "NODE_AND_INTERFACE_TAIL",
        "Interfaces of specified node(s)",
        "interfaceSpec]",
        SuggestionType.OPERATOR_NON_END),
    /** Names of devices */
    NODE_NAME("NODE_NAME", "Device name", null, SuggestionType.NAME_LITERAL),
    /** Name regex for devices */
    NODE_NAME_REGEX("NODE_NAME_REGEX", "Device name regex", "nodeNameRegex/", SuggestionType.REGEX),
    /** For a rule that denotes nodeSpec can be parenthetical */
    NODE_PARENS("NODE_PARENS", "Node specifier", "nodeSpec)", SuggestionType.OPEN_PARENS),
    /** Rule for @role(roleName, dimName) */
    NODE_ROLE_AND_DIMENSION(
        "NODE_ROLE_AND_DIMENSION",
        "Nodes in the role",
        "dimensionName, roleName)",
        SuggestionType.FUNCTION),
    NODE_ROLE_AND_DIMENSION_TAIL(
        "NODE_ROLE_AND_DIMENSION_TAIL",
        "Role name in the dimension",
        "roleName)",
        SuggestionType.OPERATOR_NON_END),
    /** Rule for node role dimension name */
    NODE_ROLE_DIMENSION_NAME(
        "NODE_ROLE_DIMENSION_NAME", "Node role dimension name", null, SuggestionType.NAME_LITERAL),
    /** Rule for node role name */
    NODE_ROLE_NAME("NODE_ROLE_NAME", "Node role name", null, SuggestionType.NAME_LITERAL),
    /**
     * Denotes a set operation for nodes. The full description is filled in {@link
     * ParboiledAutoCompleteSuggestion#completeDescriptionIfNeeded}.
     */
    NODE_SET_OP("NODE_SET_OP", " of nodes", "nodeSpec", SuggestionType.SET_OPERATOR),
    /** Rule for @deviceType() to pick nodes */
    NODE_TYPE("DEVICE_TYPE", "Device type", "deviceType)", SuggestionType.FUNCTION),
    /** Denotes an operator that ends an expression, e.g., ], ) */
    OPERATOR_END("OPERATOR_END", null, null, SuggestionType.OPERATOR_END),
    /** Rule for @addressGroup(book, group) */
    REFERENCE_BOOK_AND_ADDRESS_GROUP(
        "REFERENCE_BOOK_AND_ADDRESS_GROUP",
        "IP address space in an address group",
        "referenceBook, addressGroup)",
        SuggestionType.FUNCTION),
    /** Part of @addressGroup(book, group) after book */
    REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL(
        "REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL",
        "Address group in the reference book",
        "addressGroup)",
        SuggestionType.OPERATOR_NON_END),
    /** Rule for @interfaceGroup(book, group) */
    REFERENCE_BOOK_AND_INTERFACE_GROUP(
        "REFERENCE_BOOK_AND_INTERFACE_GROUP",
        "Interfaces in an interface group",
        "referenceBook, interfaceGroup)",
        SuggestionType.FUNCTION),
    /** Part of @interfaceGroup(book, group) after book */
    REFERENCE_BOOK_AND_INTERFACE_GROUP_TAIL(
        "REFERENCE_BOOK_AND_INTERFACE_GROUP_TAIL",
        "Interface group in the reference book",
        "interfaceGroup)",
        SuggestionType.OPERATOR_NON_END),
    /** Rule for reference book name */
    REFERENCE_BOOK_NAME(
        "REFERENCE_BOOK_NAME", "Reference book name", null, SuggestionType.NAME_LITERAL),
    /** Rule for routing policy name */
    ROUTING_POLICY_NAME(
        "ROUTING_POLICY_NAME", "Routing policy name", null, SuggestionType.NAME_LITERAL),
    /** Rule for routing policy name regex */
    ROUTING_POLICY_NAME_REGEX(
        "ROUTING_POLICY_NAME_REGEX",
        "Routing policy name regex",
        "routingPolicyNameRegex/",
        SuggestionType.REGEX),
    /** For a rule that denotes routingPolicySpec can be parenthetical */
    ROUTING_POLICY_PARENS(
        "ROUTING_POLICY_PARENS",
        "Routing policy specifier",
        "routingPolicySpec)",
        SuggestionType.OPEN_PARENS),
    /**
     * Denotes a set operation for routingPolicySpec. The full description is filled in {@link
     * ParboiledAutoCompleteSuggestion#completeDescriptionIfNeeded}.
     */
    ROUTING_POLICY_SET_OP(
        "ROUTING_POLICY_SET_OP",
        " of routing policies",
        "routingPolicySpec",
        SuggestionType.SET_OPERATOR),
    /**
     * A string literal, e.g., protocol and specifier names. It is an implicit anchor assigned based
     * on Parboiled path element labels.
     */
    STRING_LITERAL("STRING_LITERAL", "String literal", null, SuggestionType.CONSTANT),
    /** Used when we are lost */
    UNKNOWN("UNKNOWN", null, null, SuggestionType.UNKNOWN),
    /** Name of a VRF */
    VRF_NAME("VRF_NAME", "VRF name", null, SuggestionType.NAME_LITERAL),
    /** Implicit anchor generated by Parboiled */
    WHITESPACE("WHITESPACE", null, null, SuggestionType.UNKNOWN),
    /** Name of a zone */
    ZONE_NAME("ZONE_NAME", "Zone name", null, SuggestionType.NAME_LITERAL);

    @Nullable private final String _description;

    @Nullable private final String _hint;

    @Nonnull private final String _name;

    @Nonnull private final SuggestionType _suggestionType;

    Type(
        String name,
        @Nullable String description,
        @Nullable String hint,
        SuggestionType suggestionType) {
      _name = name;
      _description = description;
      _hint = hint;
      _suggestionType = suggestionType;
    }

    @Nullable
    public String getDescription() {
      return _description;
    }

    @Nullable
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

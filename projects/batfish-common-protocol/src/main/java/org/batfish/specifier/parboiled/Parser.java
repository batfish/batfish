package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_AND_BOOK;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.IGNORE;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_GROUP_AND_BOOK;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_TYPE;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS_MASK;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_PREFIX;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_PROTOCOL_NUMBER;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_RANGE;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_WILDCARD;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_ROLE_NAME_AND_DIMENSION;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_TYPE;
import static org.batfish.specifier.parboiled.Anchor.Type.VRF_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.ZONE_NAME;

import java.util.Map;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Protocol;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.support.Var;

/**
 * A parboiled-based parser for flexible specifiers. The rules for all types of expressions are in
 * this file (not sure how to put them in different files when they point to each other).
 *
 * <p>{@link Anchor} annotation: For each path from the top-level rule of an expression to leaf
 * values, there should be at least one rule with a Anchor annotation. This annotation is used for
 * error messages and generating auto completion suggestions. Character and string literals are
 * treated as implicit anchors. See findPathAnchor() in {@link ParserUtils}.
 */
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
public class Parser extends CommonParser {

  static final boolean SUPPORT_DEPRECATED_UNENCLOSED_REGEXES = true;

  static final Parser INSTANCE = Parboiled.createParser(Parser.class);

  static final Map<String, Anchor.Type> ANCHORS = initAnchors(Parser.class);

  /**
   * An array of Rules for matching enum values. They should have been private and static but
   * parboiled does not like those things in this context.
   */
  final Rule[] _applicationNameRules = initEnumRules(Protocol.values());

  final Rule[] _interfaceTypeRules = initEnumRules(InterfaceType.values());

  final Rule[] _ipProtocolNameRules = initIpProtocolNameRules();

  final Rule[] _deviceTypeRules = initEnumRules(DeviceType.values());

  /**
   * Application grammar
   *
   * <pre>
   *   applicationSpec := applicationTerm [, applicationTerm]*
   *
   *   applicationTerm := NAME  // one of {@link Protocol} enums values
   *
   * </pre>
   */

  /* A Filter expression is one or more intersection terms separated by , or \ */
  public Rule ApplicationSpec() {
    return Sequence(
        ApplicationTerm(),
        WhiteSpace(),
        ZeroOrMore(
            ", ",
            ApplicationTerm(),
            push(new UnionApplicationAstNode(pop(1), pop())),
            WhiteSpace()));
  }

  public Rule ApplicationTerm() {
    return Sequence(FirstOf(_applicationNameRules), push(new NameApplicationAstNode(match())));
  }

  /**
   * Filter grammar
   *
   * <pre>
   *   filterSpec := filterTerm [{@literal &} | , | \ filterTerm]*
   *
   *   filterTerm := @in(interfaceSpec)  // inFilterOf is also supported for back compat
   *               | @out(interfaceSpec) // outFilterOf is also supported
   *               | filterName
   *               | filterNameRegex
   *               | ( filterTerm )
   * </pre>
   */

  /* A Filter expression is one or more intersection terms separated by , or \ */
  public Rule FilterSpec() {
    Var<Character> op = new Var<>();
    return Sequence(
        FilterIntersection(),
        WhiteSpace(),
        ZeroOrMore(
            FirstOf(", ", "\\ "),
            op.set(matchedChar()),
            FilterIntersection(),
            push(SetOpFilterAstNode.create(op.get(), pop(1), pop())),
            WhiteSpace()));
  }

  public Rule FilterIntersection() {
    return Sequence(
        FilterTerm(),
        WhiteSpace(),
        ZeroOrMore(
            "& ", FilterTerm(), push(new IntersectionFilterAstNode(pop(1), pop())), WhiteSpace()));
  }

  public Rule FilterTerm() {
    return FirstOf(
        FilterDirection(),
        FilterDirectionDeprecated(),
        FilterNameRegexDeprecated(),
        FilterNameRegex(),
        FilterName(),
        FilterParens());
  }

  public Rule FilterDirection() {
    Var<String> direction = new Var<>();
    return Sequence(
        FirstOf(IgnoreCase("@in"), IgnoreCase("@out")),
        direction.set(match()),
        WhiteSpace(),
        "( ",
        InterfaceSpec(),
        WhiteSpace(),
        ") ",
        push(DirectionFilterAstNode.create(direction.get(), pop())));
  }

  @Anchor(IGNORE)
  public Rule FilterDirectionDeprecated() {
    Var<String> direction = new Var<>();
    return Sequence(
        FirstOf(IgnoreCase("inFilterOf"), IgnoreCase("outFilterOf")),
        direction.set(match()),
        WhiteSpace(),
        "( ",
        InterfaceSpec(),
        WhiteSpace(),
        ") ",
        push(DirectionFilterAstNode.create(direction.get(), pop())));
  }

  @Anchor(FILTER_NAME)
  public Rule FilterName() {
    return Sequence(NameLiteral(), push(new NameFilterAstNode(pop())));
  }

  @Anchor(FILTER_NAME_REGEX)
  public Rule FilterNameRegex() {
    return Sequence(Regex(), push(new NameRegexFilterAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule FilterNameRegexDeprecated() {
    return Sequence(RegexDeprecated(), push(new NameRegexFilterAstNode(pop())));
  }

  public Rule FilterParens() {
    // Leave the stack as is -- no need to remember that this was a parenthetical term
    return Sequence("( ", FilterSpec(), WhiteSpace(), ") ");
  }

  /**
   * Interface grammar
   *
   * <pre>
   *   interfaceSpec := interfaceTerm [{@literal &} | , | \ interfaceTerm]*
   *
   *   interfaceTerm := interfaceWithNode
   *                    | interfaceWithoutNode
   *                    | ( interfaceTerm )
   *
   *   interfaceWithNode := nodeTerm [interfaceWithoutNode]
   *
   *   interfaceWithoutNode := interfaceWithoutNodeTerm [{@literal &} | , | \ interfaceWithoutNodeTerm]*
   *
   *   interfaceWithoutNodeTerm
   *                        := @connectedTo(ipSpaceSpec)  // non-@ versions also supported for back compat
   *                        | @interfacegroup(a, b)
   *                        | @interfaceType(interfaceType)
   *                        | @vrf(vrfName)
   *                        | @zone(zoneName)
   *                        | interfaceName
   *                        | interfaceNameRegex
   *                        | ( interfaceTerm )
   * </pre>
   */

  /* An Interface expression is union and difference of one or more intersections */
  public Rule InterfaceSpec() {
    Var<Character> op = new Var<>();
    return Sequence(
        InterfaceIntersection(),
        WhiteSpace(),
        ZeroOrMore(
            FirstOf(", ", "\\ "),
            op.set(matchedChar()),
            InterfaceIntersection(),
            push(SetOpInterfaceAstNode.create(op.get(), pop(1), pop())),
            WhiteSpace()));
  }

  public Rule InterfaceIntersection() {
    return Sequence(
        InterfaceTerm(),
        WhiteSpace(),
        ZeroOrMore(
            "& ",
            InterfaceTerm(),
            push(new IntersectionInterfaceAstNode(pop(1), pop())),
            WhiteSpace()));
  }

  public Rule InterfaceTerm() {
    return FirstOf(InterfaceWithNode(), InterfaceWithoutNode(), InterfaceParens());
  }

  public Rule InterfaceWithNode() {
    return Sequence(
        NodeTerm(),
        WhiteSpace(),
        "[ ",
        InterfaceWithoutNode(),
        WhiteSpace(),
        "] ",
        push(new InterfaceWithNodeInterfaceAstNode(pop(1), pop())));
  }

  public Rule InterfaceWithoutNode() {
    Var<Character> op = new Var<>();
    return Sequence(
        InterfaceWithoutNodeIntersection(),
        WhiteSpace(),
        ZeroOrMore(
            FirstOf(", ", "\\ "),
            op.set(matchedChar()),
            InterfaceWithoutNodeIntersection(),
            push(SetOpInterfaceAstNode.create(op.get(), pop(1), pop())),
            WhiteSpace()));
  }

  public Rule InterfaceWithoutNodeIntersection() {
    return Sequence(
        InterfaceWithoutNodeTerm(),
        WhiteSpace(),
        ZeroOrMore(
            "& ",
            InterfaceWithoutNodeTerm(),
            push(new IntersectionInterfaceAstNode(pop(1), pop())),
            WhiteSpace()));
  }

  public Rule InterfaceWithoutNodeTerm() {
    return FirstOf(
        InterfaceFunc(),
        InterfaceNameRegexDeprecated(),
        InterfaceNameRegex(),
        InterfaceName(),
        InterfaceWithoutNodeParens());
  }

  /**
   * To avoid ambiguity in location specification, interface and node functions should be distinct
   */
  public Rule InterfaceFunc() {
    return FirstOf(
        InterfaceConnectedTo(),
        InterfaceConnectedToDeprecated(),
        InterfaceInterfaceGroup(),
        InterfaceInterfaceGroupDeprecated(),
        InterfaceType(),
        InterfaceTypeDeprecated(),
        InterfaceVrf(),
        InterfaceVrfDeprecated(),
        InterfaceZone(),
        InterfaceZoneDeprecated());
  }

  public Rule InterfaceConnectedTo() {
    return Sequence(
        IgnoreCase("@connectedTo"),
        WhiteSpace(),
        "( ",
        IpSpaceSpec(),
        WhiteSpace(),
        ") ",
        push(new ConnectedToInterfaceAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule InterfaceConnectedToDeprecated() {
    return Sequence(
        IgnoreCase("connectedTo"),
        WhiteSpace(),
        "( ",
        IpSpaceSpec(),
        WhiteSpace(),
        ") ",
        push(new ConnectedToInterfaceAstNode(pop())));
  }

  public Rule InterfaceInterfaceGroup() {
    return Sequence(
        IgnoreCase("@interfaceGroup"),
        WhiteSpace(),
        "( ",
        InterfaceGroupAndBook(),
        ") ",
        push(new InterfaceGroupInterfaceAstNode(pop(1), pop())));
  }

  @Anchor(IGNORE)
  public Rule InterfaceInterfaceGroupDeprecated() {
    return Sequence(
        IgnoreCase("ref.interfaceGroup"),
        WhiteSpace(),
        "( ",
        InterfaceGroupAndBook(),
        ") ",
        push(new InterfaceGroupInterfaceAstNode(pop(1), pop())));
  }

  /** Matches AddressGroup, ReferenceBook pair. Puts two values on stack */
  @Anchor(INTERFACE_GROUP_AND_BOOK)
  public Rule InterfaceGroupAndBook() {
    return Sequence(
        ReferenceObjectNameLiteral(),
        push(new StringAstNode(match())),
        WhiteSpace(),
        ", ",
        ReferenceObjectNameLiteral(),
        push(new StringAstNode(match())),
        WhiteSpace());
  }

  public Rule InterfaceType() {
    return Sequence(
        IgnoreCase("@interfaceType"),
        WhiteSpace(),
        "( ",
        InterfaceTypeSpec(),
        WhiteSpace(),
        ") ",
        push(new TypeInterfaceAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule InterfaceTypeDeprecated() {
    return Sequence(
        IgnoreCase("type"),
        WhiteSpace(),
        "( ",
        InterfaceTypeSpec(),
        WhiteSpace(),
        ") ",
        push(new TypeInterfaceAstNode(pop())));
  }

  @Anchor(INTERFACE_TYPE)
  public Rule InterfaceTypeSpec() {
    return Sequence(FirstOf(_interfaceTypeRules), push(new StringAstNode(match())));
  }

  public Rule InterfaceVrf() {
    return Sequence(
        IgnoreCase("@vrf"),
        WhiteSpace(),
        "( ",
        VrfName(),
        WhiteSpace(),
        ") ",
        push(new VrfInterfaceAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule InterfaceVrfDeprecated() {
    return Sequence(
        IgnoreCase("vrf"),
        WhiteSpace(),
        "( ",
        VrfName(),
        WhiteSpace(),
        ") ",
        push(new VrfInterfaceAstNode(pop())));
  }

  @Anchor(VRF_NAME)
  public Rule VrfName() {
    return NameLiteral();
  }

  public Rule InterfaceZone() {
    return Sequence(
        IgnoreCase("@zone"),
        WhiteSpace(),
        "( ",
        ZoneName(),
        WhiteSpace(),
        ") ",
        push(new ZoneInterfaceAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule InterfaceZoneDeprecated() {
    return Sequence(
        IgnoreCase("zone"),
        WhiteSpace(),
        "( ",
        ZoneName(),
        WhiteSpace(),
        ") ",
        push(new ZoneInterfaceAstNode(pop())));
  }

  @Anchor(ZONE_NAME)
  public Rule ZoneName() {
    return NameLiteral();
  }

  @Anchor(INTERFACE_NAME)
  public Rule InterfaceName() {
    return Sequence(NameLiteral(), push(new NameInterfaceAstNode(pop())));
  }

  @Anchor(INTERFACE_NAME_REGEX)
  public Rule InterfaceNameRegex() {
    return Sequence(Regex(), push(new NameRegexInterfaceAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule InterfaceNameRegexDeprecated() {
    return Sequence(RegexDeprecated(), push(new NameRegexInterfaceAstNode(pop())));
  }

  public Rule InterfaceParens() {
    // Leave the stack as is -- no need to remember that this was a parenthetical term
    return Sequence("( ", InterfaceSpec(), WhiteSpace(), ") ");
  }

  public Rule InterfaceWithoutNodeParens() {
    // Leave the stack as is -- no need to remember that this was a parenthetical term
    return Sequence("( ", InterfaceWithoutNode(), WhiteSpace(), ") ");
  }

  /**
   * IpProtocol grammar
   *
   * <pre>
   *   ipProtocolSpec := ipProtocolTerm [, ipProtocolTerm]*
   *
   *   ipProtocolTerm := ipProtocol
   *                     | ! ipProtocol
   *
   *   ipProtocol :=     Name      // e.g., TCP
   *                     | Number  // e.g., 51
   * </pre>
   */

  /* A Filter expression is one or more intersection terms separated by , or \ */
  public Rule IpProtocolSpec() {
    return Sequence(
        IpProtocolTerm(),
        WhiteSpace(),
        ZeroOrMore(
            ", ", IpProtocolTerm(), push(new UnionIpProtocolAstNode(pop(1), pop())), WhiteSpace()));
  }

  public Rule IpProtocolTerm() {
    return FirstOf(
        IpProtocol(), Sequence("! ", IpProtocol(), push(new NotIpProtocolAstNode(pop()))));
  }

  public Rule IpProtocol() {
    return FirstOf(IpProtocolName(), IpProtocolNumber());
  }

  public Rule IpProtocolName() {
    return Sequence(FirstOf(_ipProtocolNameRules), push(new IpProtocolIpProtocolAstNode(match())));
  }

  @Anchor(IP_PROTOCOL_NUMBER)
  public Rule IpProtocolNumber() {
    return Sequence(Number(), push(new IpProtocolIpProtocolAstNode(match())));
  }

  /**
   * IpSpace grammar
   *
   * <pre>
   * ipSpaceSpec := ipSpecTerm [, ipSpecTerm]*
   *
   * ipSpecTerm := @addgressgroup(groupname, bookname)  //ref.addressgroup for back compat
   *               | locationSpec
   *               | ofLocation(locationSpec)           // back compat
   *               | ipPrefix (e.g., 1.1.1.0/24)
   *               | ipWildcard (e.g., 1.1.1.1:255.255.255.0)
   *               | ipRange (e.g., 1.1.1.1-1.1.1.2)
   *               | ipAddress (e.g., 1.1.1.1)
   * </pre>
   */

  /* An IpSpace expression is a comma-separated list of IpSpaceTerms */
  public Rule IpSpaceSpec() {
    return Sequence(
        IpSpaceTerm(),
        WhiteSpace(),
        ZeroOrMore(
            ", ", IpSpaceTerm(), push(new UnionIpSpaceAstNode(pop(1), pop())), WhiteSpace()));
  }

  /* An IpSpace term is one of these things */
  public Rule IpSpaceTerm() {
    return FirstOf(
        IpPrefix(),
        IpWildcard(),
        IpRange(),
        IpAddress(),
        IpSpaceAddressGroup(),
        IpSpaceAddressGroupDeprecated(),
        IpSpaceLocationDeprecated(),
        IpSpaceLocation());
  }

  public Rule IpSpaceAddressGroup() {
    return Sequence(
        IgnoreCase("@addressgroup"),
        WhiteSpace(),
        "( ",
        AddressGroupAndBook(),
        ") ",
        push(new AddressGroupIpSpaceAstNode(pop(1), pop())));
  }

  @Anchor(IGNORE)
  public Rule IpSpaceAddressGroupDeprecated() {
    return Sequence(
        IgnoreCase("ref.addressgroup"),
        WhiteSpace(),
        "( ",
        AddressGroupAndBook(),
        ") ",
        push(new AddressGroupIpSpaceAstNode(pop(1), pop())));
  }

  /** Matches AddressGroup, ReferenceBook pair. Puts two values on stack */
  @Anchor(ADDRESS_GROUP_AND_BOOK)
  public Rule AddressGroupAndBook() {
    return Sequence(
        ReferenceObjectNameLiteral(),
        push(new StringAstNode(match())),
        WhiteSpace(),
        ", ",
        ReferenceObjectNameLiteral(),
        push(new StringAstNode(match())),
        WhiteSpace());
  }

  public Rule IpSpaceLocation() {
    return Sequence(LocationSpec(), push(new LocationIpSpaceAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule IpSpaceLocationDeprecated() {
    return Sequence(
        IgnoreCase("ofLocation"),
        WhiteSpace(),
        "( ",
        LocationSpec(),
        WhiteSpace(),
        ") ",
        push(new LocationIpSpaceAstNode(pop())));
  }

  /**
   * Matches IP addresses. Throws an exception if something matches syntactically but is invalid
   * semantically (e.g., 1.1.1.256)
   */
  @Anchor(IP_ADDRESS)
  public Rule IpAddress() {
    return Sequence(IpAddressUnchecked(), push(new IpAstNode(match())));
  }

  /**
   * Matches IP address mask (e.g., 255.255.255.0). It is syntactically similar to IP addresses but
   * we have a separate rule that helps with error messages and auto completion.
   */
  @Anchor(IP_ADDRESS_MASK)
  public Rule IpAddressMask() {
    return Sequence(IpAddressUnchecked(), push(new IpAstNode(match())));
  }

  /**
   * Matches IP prefixes. Throws an exception if something matches syntactically but is invalid
   * semantically (e.g., 1.1.1.2/33)
   */
  @Anchor(IP_PREFIX)
  public Rule IpPrefix() {
    return Sequence(IpPrefixUnchecked(), push(new PrefixAstNode(match())));
  }

  /** Matches Ip ranges (e.g., 1.1.1.1 - 1.1.1.2) */
  @Anchor(IP_RANGE)
  public Rule IpRange() {
    return Sequence(
        IpAddress(),
        WhiteSpace(),
        "- ",
        IpAddress(),
        push(new IpRangeAstNode(pop(1), pop())),
        WhiteSpace());
  }

  /** Matches Ip wildcards (e.g., 1.1.1.1:255.255.255.255) */
  @Anchor(IP_WILDCARD)
  public Rule IpWildcard() {
    return Sequence(IpAddress(), ':', IpAddressMask(), push(new IpWildcardAstNode(pop(1), pop())));
  }

  /**
   * Location grammar
   *
   * <pre>
   *   locationSpec := locationTerm [{@literal &} | , | \ locationTerm]*
   *
   *   locationTerm := locationInterface
   *               | @enter(locationInterface)   // non-@ versions also supported
   *               | @exit(locationInteface)
   *               | ( locationTerm )
   *
   *   locationInterface := nodeTerm[interfaceTerm]
   *                        | nodeTerm
   *                        | interfaceFunc
   * </pre>
   */

  /* A Node expression is one or more intersection terms separated by + or - */
  public Rule LocationSpec() {
    Var<Character> op = new Var<>();
    return Sequence(
        LocationIntersection(),
        WhiteSpace(),
        ZeroOrMore(
            FirstOf(", ", "\\ "),
            op.set(matchedChar()),
            LocationIntersection(),
            push(SetOpLocationAstNode.create(op.get(), pop(1), pop())),
            WhiteSpace()));
  }

  public Rule LocationIntersection() {
    return Sequence(
        LocationTerm(),
        WhiteSpace(),
        ZeroOrMore(
            "& ",
            LocationTerm(),
            push(new IntersectionLocationAstNode(pop(1), pop())),
            WhiteSpace()));
  }

  public Rule LocationTerm() {
    return FirstOf(
        LocationEnterDeprecated(),
        LocationEnter(),
        LocationInterfaceDeprecated(),
        LocationInterface(),
        LocationParens());
  }

  public Rule LocationEnter() {
    return Sequence(
        IgnoreCase("@enter"),
        WhiteSpace(),
        "( ",
        LocationInterface(),
        ") ",
        push(new EnterLocationAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule LocationEnterDeprecated() {
    return Sequence(
        IgnoreCase("enter"),
        WhiteSpace(),
        "( ",
        FirstOf(LocationInterfaceDeprecated(), LocationInterface()),
        ") ",
        push(new EnterLocationAstNode(pop())));
  }

  /**
   * LocationInterface specifies interfaces to use as locations. It is not simply pointing to
   * interfaceSpec because of how we want to treat a simple string like "foo". We want to treat that
   * string as a node name, not an interface name which interfaceSpec or interfaceWithoutNode will
   * do. For that reason, valid inputs for LocationInterface are InterfaceWithNode, NodeTerm, and
   * InterfaceFunc. To avoid confusion, it is a requirement that functions in NodeTerm (e.g., @role)
   * and InterfaceFunc (e.g., @vrf) be distinct.
   */
  public Rule LocationInterface() {
    return FirstOf(
        Sequence(
            InterfaceWithNode(), push(InterfaceLocationAstNode.createFromInterfaceWithNode(pop()))),
        Sequence(NodeTerm(), WhiteSpace(), push(InterfaceLocationAstNode.createFromNode(pop()))),
        Sequence(
            InterfaceFunc(),
            WhiteSpace(),
            push(InterfaceLocationAstNode.createFromInterface(pop()))));
  }

  @Anchor(IGNORE)
  public Rule LocationInterfaceDeprecated() {
    return Sequence(
        // brackets without node expression
        "[ ",
        InterfaceSpec(),
        WhiteSpace(),
        "] ",
        push(InterfaceLocationAstNode.createFromInterface(pop())));
  }

  public Rule LocationParens() {
    // Leave the stack as is -- no need to remember that this was a parenthetical term
    return Sequence("( ", LocationSpec(), WhiteSpace(), ") ");
  }

  /**
   * Node grammar
   *
   * <pre>
   *   nodeSpec := nodeTerm [{@literal &} | , | \ nodeTerm]*
   *
   *   nodeTerm := @role(a, b) // ref.noderole is also supported for back compat
   *               | @deviceType(a)
   *               | nodeName
   *               | nodeNameRegex
   *               | ( nodeTerm )
   * </pre>
   */

  /* A Node expression is one or more intersection terms separated by + or - */
  public Rule NodeSpec() {
    Var<Character> op = new Var<>();
    return Sequence(
        NodeIntersection(),
        WhiteSpace(),
        ZeroOrMore(
            FirstOf(", ", "\\ "),
            op.set(matchedChar()),
            NodeIntersection(),
            push(SetOpNodeAstNode.create(op.get(), pop(1), pop())),
            WhiteSpace()));
  }

  public Rule NodeIntersection() {
    return Sequence(
        NodeTerm(),
        WhiteSpace(),
        ZeroOrMore(
            "& ", NodeTerm(), push(new IntersectionNodeAstNode(pop(1), pop())), WhiteSpace()));
  }

  public Rule NodeTerm() {
    return FirstOf(
        NodeRole(),
        NodeRoleDeprecated(),
        NodeType(),
        NodeNameRegexDeprecated(),
        NodeNameRegex(),
        NodeName(),
        NodeParens());
  }

  public Rule NodeRole() {
    return Sequence(
        IgnoreCase("@role"),
        WhiteSpace(),
        "( ",
        NodeRoleNameAndDimension(),
        ") ",
        push(new RoleNodeAstNode(pop(1), pop())));
  }

  @Anchor(IGNORE)
  public Rule NodeRoleDeprecated() {
    return Sequence(
        IgnoreCase("ref.noderole"),
        WhiteSpace(),
        "( ",
        NodeRoleNameAndDimension(),
        ") ",
        push(new RoleNodeAstNode(pop(1), pop())));
  }

  /** Matches RoleName, DimensionName pair. Puts two values on stack */
  @Anchor(NODE_ROLE_NAME_AND_DIMENSION)
  public Rule NodeRoleNameAndDimension() {
    return Sequence(
        NodeRoleNameLiteral(),
        push(new StringAstNode(match())),
        WhiteSpace(),
        ", ",
        ReferenceObjectNameLiteral(),
        push(new StringAstNode(match())),
        WhiteSpace());
  }

  public Rule NodeType() {
    return Sequence(
        IgnoreCase("@deviceType"),
        WhiteSpace(),
        "( ",
        NodeTypeSpec(),
        WhiteSpace(),
        ") ",
        push(new TypeNodeAstNode(pop())));
  }

  @Anchor(NODE_TYPE)
  public Rule NodeTypeSpec() {
    return Sequence(FirstOf(_deviceTypeRules), push(new StringAstNode(match())));
  }

  @Anchor(NODE_NAME)
  public Rule NodeName() {
    return Sequence(NameLiteral(), push(new NameNodeAstNode(pop())));
  }

  @Anchor(NODE_NAME_REGEX)
  public Rule NodeNameRegex() {
    return Sequence(Regex(), push(new NameRegexNodeAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule NodeNameRegexDeprecated() {
    return Sequence(RegexDeprecated(), push(new NameRegexNodeAstNode(pop())), WhiteSpace());
  }

  public Rule NodeParens() {
    // Leave the stack as is -- no need to remember that this was a parenthetical term
    return Sequence("( ", NodeSpec(), WhiteSpace(), ") ");
  }
}

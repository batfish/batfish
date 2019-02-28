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
  final Rule[] _interfaceTypeRules = initEnumRules(InterfaceType.values());

  final Rule[] _deviceTypeRules = initEnumRules(DeviceType.values());

  /**
   * Filter grammar
   *
   * <pre>
   *   filterExpr := filterTerm [{@literal &} | , | \ filterTerm]*
   *
   *   filterTerm := @in(interfaceExpr)  // inFilterOf is also supported for back compat
   *               | @out(interfaceExpr) // outFilterOf is also supported
   *               | filterName
   *               | filterNameRegex
   *               | ( filterTerm )
   * </pre>
   */

  /* A Filter expression is one or more intersection terms separated by , or \ */
  public Rule FilterExpression() {
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
        InterfaceExpression(),
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
        InterfaceExpression(),
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
    return Sequence("( ", FilterExpression(), WhiteSpace(), ") ");
  }

  /**
   * Interface grammar
   *
   * <pre>
   *   interfaceExpr := interfaceTerm [{@literal &} | , | \ interfaceTerm]*
   *
   *   interfaceTerm := @connectedTo(ipSpaceExpr)  // non-@ versions also supported for back compat
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
  public Rule InterfaceExpression() {
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
    return FirstOf(
        InterfaceSpecifier(),
        InterfaceNameRegexDeprecated(),
        InterfaceNameRegex(),
        InterfaceName(),
        InterfaceParens());
  }

  /**
   * To avoid ambiguity in location specification, interface and node specifiers should be distinct
   */
  public Rule InterfaceSpecifier() {
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
        IpSpaceExpression(),
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
        IpSpaceExpression(),
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
        InterfaceTypeExpr(),
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
        InterfaceTypeExpr(),
        WhiteSpace(),
        ") ",
        push(new TypeInterfaceAstNode(pop())));
  }

  @Anchor(INTERFACE_TYPE)
  public Rule InterfaceTypeExpr() {
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
    return Sequence("( ", InterfaceExpression(), WhiteSpace(), ") ");
  }

  /**
   * IpSpace grammar
   *
   * <pre>
   * ipSpaceSpec := ipSpecTerm [, ipSpecTerm]*
   *
   * ipSpecTerm := @addgressgroup(groupname, bookname)  //ref.addressgroup for back compat
   *               | locationExpr
   *               | ofLocation(locationExpr)           // back compat
   *               | ipPrefix (e.g., 1.1.1.0/24)
   *               | ipWildcard (e.g., 1.1.1.1:255.255.255.0)
   *               | ipRange (e.g., 1.1.1.1-1.1.1.2)
   *               | ipAddress (e.g., 1.1.1.1)
   * </pre>
   */

  /* An IpSpace expression is a comma-separated list of IpSpaceTerms */
  public Rule IpSpaceExpression() {
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
    return Sequence(LocationExpression(), push(new LocationIpSpaceAstNode(pop())));
  }

  @Anchor(IGNORE)
  public Rule IpSpaceLocationDeprecated() {
    return Sequence(
        IgnoreCase("ofLocation"),
        WhiteSpace(),
        "( ",
        LocationExpression(),
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
   *   locationExpr := locationTerm [{@literal &} | , | \ locationTerm]*
   *
   *   locationTerm := locationInterface
   *               | @enter(locationInterface)   // non-@ versions also supported
   *               | @exit(locationInteface)
   *               | ( locationTerm )
   *
   *   locationInterface := nodeTerm[interfaceTerm]
   *                        | nodeTerm
   *                        | interfaceSpecifier
   * </pre>
   */

  /* A Node expression is one or more intersection terms separated by + or - */
  public Rule LocationExpression() {
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

  public Rule LocationInterface() {
    return FirstOf(
        Sequence(
            NodeTerm(),
            WhiteSpace(),
            "[ ",
            InterfaceExpression(),
            WhiteSpace(),
            "] ",
            push(InterfaceLocationAstNode.createFromNodeInterface(pop(1), pop()))),
        Sequence(NodeTerm(), WhiteSpace(), push(InterfaceLocationAstNode.createFromNode(pop()))),
        Sequence(
            InterfaceSpecifier(),
            WhiteSpace(),
            push(InterfaceLocationAstNode.createFromInterface(pop()))));
  }

  @Anchor(IGNORE)
  public Rule LocationInterfaceDeprecated() {
    return Sequence(
        // brackets without node expression
        "[ ",
        InterfaceExpression(),
        WhiteSpace(),
        "] ",
        push(InterfaceLocationAstNode.createFromInterface(pop())));
  }

  public Rule LocationParens() {
    // Leave the stack as is -- no need to remember that this was a parenthetical term
    return Sequence("( ", LocationExpression(), WhiteSpace(), ") ");
  }

  /**
   * Node grammar
   *
   * <pre>
   *   nodeExpr := nodeTerm [{@literal &} | , | \ nodeTerm]*
   *
   *   nodeTerm := @role(a, b) // ref.noderole is also supported for back compat
   *               | @deviceType(a)
   *               | nodeName
   *               | nodeNameRegex
   *               | ( nodeTerm )
   * </pre>
   */

  /* A Node expression is one or more intersection terms separated by + or - */
  public Rule NodeExpression() {
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
        NodeTypeExpr(),
        WhiteSpace(),
        ") ",
        push(new TypeNodeAstNode(pop())));
  }

  @Anchor(NODE_TYPE)
  public Rule NodeTypeExpr() {
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
    return Sequence("( ", NodeExpression(), WhiteSpace(), ") ");
  }
}

package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_AND_BOOK;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_GROUP_AND_BOOK;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_TYPE;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS_MASK;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_PREFIX;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_RANGE;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_WILDCARD;
import static org.batfish.specifier.parboiled.Anchor.Type.VRF;
import static org.batfish.specifier.parboiled.Anchor.Type.ZONE;

import java.util.Map;
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
 * treated as implicit anchors see findPathAnchor() in {@link ParserUtils}.
 */
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
public class Parser extends CommonParser {

  static final Parser INSTANCE = Parboiled.createParser(Parser.class);

  static final Map<String, Anchor.Type> ANCHORS = initAnchors(Parser.class);

  /**
   * An array of Rules for matching interface type values. It is supposed to be private with
   * parboiled does not like private rules
   */
  final Rule[] INTERFACE_TYPE_RULES = initEnumRules(InterfaceType.values());

  /**
   * Interface grammar
   *
   * <pre>
   *   interfaceExpr := interfaceTerm [&/+/- interfaceTerm]*
   *
   *   interfaceTerm := @connectedTo(ipSpaceExpr)  // also, non-@ versions supported for back compat
   *                        | @interfacegroup(a, b)
   *                        | @type(interfaceType)
   *                        | @vrf(vrfName)
   *                        | @zone(zoneName)
   *                        | interfaceName | interfaceRegex
   *                        | ( interfaceTerm )
   * </pre>
   */

  /* An Interface expression a list of of Interface separated by + or - */
  public Rule InterfaceExpression() {
    Var<Character> op = new Var<>();
    return Sequence(
        InterfaceIntersection(),
        WhiteSpace(),
        ZeroOrMore(
            FirstOf("+ ", "- "),
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
        InterfaceConnectedTo(),
        InterfaceInterfaceGroup(),
        InterfaceType(),
        InterfaceVrf(),
        InterfaceZone(),
        InterfaceNameRegex(),
        InterfaceName(),
        InterfaceParens());
  }

  public Rule InterfaceConnectedTo() {
    return Sequence(
        FirstOf(IgnoreCase("@connectedTo"), IgnoreCase("connectedTo")),
        WhiteSpace(),
        "( ",
        IpSpaceExpression(),
        WhiteSpace(),
        ") ",
        push(new ConnectedToInterfaceAstNode(pop())));
  }

  public Rule InterfaceInterfaceGroup() {
    return Sequence(
        FirstOf(IgnoreCase("@interfacegroup"), IgnoreCase("ref.interfacegroup")),
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
        FirstOf(IgnoreCase("@type"), IgnoreCase("type")),
        WhiteSpace(),
        "( ",
        InterfaceTypeExpr(),
        WhiteSpace(),
        ") ",
        push(new TypeInterfaceAstNode(pop())));
  }

  @Anchor(INTERFACE_TYPE)
  public Rule InterfaceTypeExpr() {
    return Sequence(FirstOf(INTERFACE_TYPE_RULES), push(new StringAstNode(match())));
  }

  public Rule InterfaceVrf() {
    return Sequence(
        FirstOf(IgnoreCase("@vrf"), IgnoreCase("vrf")),
        WhiteSpace(),
        "( ",
        VrfName(),
        WhiteSpace(),
        ") ",
        push(new VrfInterfaceAstNode(pop())));
  }

  @Anchor(VRF)
  public Rule VrfName() {
    return Sequence(OneOrMore(NameCharsAndDash()), push(new StringAstNode(match())));
  }

  public Rule InterfaceZone() {
    return Sequence(
        FirstOf(IgnoreCase("@zone"), IgnoreCase("zone")),
        WhiteSpace(),
        "( ",
        ZoneName(),
        WhiteSpace(),
        ") ",
        push(new ZoneInterfaceAstNode(pop())));
  }

  @Anchor(ZONE)
  public Rule ZoneName() {
    return Sequence(OneOrMore(NameCharsAndDash()), push(new StringAstNode(match())));
  }

  @Anchor(INTERFACE_NAME)
  public Rule InterfaceName() {
    return Sequence(
        OneOrMore(FirstOf(NameChars(), Colon(), Slash())), push(new NameInterfaceAstNode(match())));
  }

  @Anchor(INTERFACE_NAME_REGEX)
  public Rule InterfaceNameRegex() {
    return Sequence('/', Regex(), push(new NameRegexInterfaceAstNode(match())), '/');
  }

  public Rule InterfaceParens() {
    // Leave the stack as is -- no need to remember that this was a parenthetical term
    return Sequence("( ", InterfaceTerm(), WhiteSpace(), ") ");
  }

  /**
   * IpSpace grammar
   *
   * <pre>
   * ipSpaceSpec := ipSpecTerm [, ipSpecTerm]*
   *
   * ipSpecTerm := @addgressgroup(groupname, bookname)  //ref.addressgroup for back compat
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
            ", ", IpSpaceTerm(), push(new CommaIpSpaceAstNode(pop(1), pop())), WhiteSpace()));
  }

  /* An IpSpace term is one of these things */
  public Rule IpSpaceTerm() {
    return FirstOf(IpSpaceAddressGroup(), IpPrefix(), IpWildcard(), IpRange(), IpAddress());
  }

  /** Includes ref.addgressgroup for backward compatibility. Should be removed later */
  public Rule IpSpaceAddressGroup() {
    return Sequence(
        FirstOf(IgnoreCase("@addressgroup"), IgnoreCase("ref.addressgroup")),
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

  /**
   * Matched IP addresses. Throws an exception if something matches syntactically but is invalid
   * (e.g., 1.1.1.256)
   */
  @Anchor(IP_ADDRESS)
  public Rule IpAddress() {
    return Sequence(IpAddressUnchecked(), push(new IpAstNode(match())));
  }

  /**
   * Matched IP address mask (e.g., 255.255.255.0). It is syntactically similar to IP addresses but
   * we have a separate rule that helps with error messages and auto completion.
   */
  @Anchor(IP_ADDRESS_MASK)
  public Rule IpAddressMask() {
    return Sequence(IpAddressUnchecked(), push(new IpAstNode(match())));
  }

  /**
   * Matched IP prefixes. Throws an exception if something matches syntactically but is invalid
   * (e.g., 1.1.1.2/33)
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
}

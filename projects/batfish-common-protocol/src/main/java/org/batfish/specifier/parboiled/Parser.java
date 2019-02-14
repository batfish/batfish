package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_AND_BOOK;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_ADDRESS_MASK;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_PREFIX;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_RANGE;
import static org.batfish.specifier.parboiled.Anchor.Type.IP_WILDCARD;

import java.util.Map;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.support.MatcherPath;

/**
 * A parboiled-based parser for flexible specifiers. The rules for all types of expressions are in
 * this file (not sure how to put them in different files when they point to each other).
 *
 * <p>{@link Anchor} annotation: For each path from the top-level rule of an expression to leaf
 * values, there should be at least one rule with a Anchor annotation. This annotation is used for
 * error messages and generating auto completion suggestions. Character and string literals are
 * treated as implicit anchors (see {@link ParserUtils#findPathAnchor(MatcherPath, int, Map,
 * boolean)}
 */
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
class Parser extends CommonParser {

  static final Parser INSTANCE = Parboiled.createParser(Parser.class);

  static final Map<String, Anchor.Type> ANCHORS = initAnchors(Parser.class);

  /**
   * IpSpace grammar
   *
   * <pre>
   * ipSpec := ipSpecTerm [, ipSpecTerm]*
   *
   * ipSpecTerm := addgressgroup(groupname, bookname)
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
        push(new AddressGroupAstNode(pop(1), pop())));
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

package org.batfish.specifier.parboiled;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.specifier.parboiled.IpSpaceAstNode.Type;
import org.parboiled.Parboiled;
import org.parboiled.Rule;

/**
 * A parboiled-based parser for our flexible specifiers. The rules for all types of specifiers are
 * in this file (not sure how to put them in different files).
 */
@SuppressWarnings({
  "InfiniteRecursion",
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
class Parser extends CommonParser {

  static Parser INSTANCE = Parboiled.createParser(Parser.class);

  /**
   * Shared entry point for all expressions.
   *
   * <p>The parameter {@code expression} specifies the type of expression we want to parse.
   */
  public Rule input(Rule expression) {
    return Sequence(WhiteSpace(), expression, WhiteSpace(), EOI);
  }

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
            ", ",
            IpSpaceTerm(),
            push(new IpSpaceAstNode(Type.COMMA, (AstNode) pop(1), (AstNode) pop())),
            WhiteSpace()));
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
        push(new IpSpaceAstNode(Type.ADDRESS_GROUP, (AstNode) pop(1), (AstNode) pop())));
  }

  /** Matches AddressGroup, ReferenceBook pair. Puts two values on stack */
  public Rule AddressGroupAndBook() {
    return Sequence(
        ReferenceObjectNameLiteral(),
        push(new LeafAstNode(matchOrDefault("Error"))),
        WhiteSpace(),
        ", ",
        ReferenceObjectNameLiteral(),
        push(new LeafAstNode(matchOrDefault("Error"))),
        WhiteSpace());
  }

  /**
   * Matched IP addresses. Throws an exception if something matches syntactically but is invalid
   * (e.g., 1.1.1.256)
   */
  public Rule IpAddress() {
    return Sequence(IpAddressUnchecked(), push(new LeafAstNode(Ip.parse(matchOrDefault("Error")))));
  }

  /**
   * Matched IP prefixes. Throws an exception if something matches syntactically but is invalid
   * (e.g., 1.1.1.2/33)
   */
  public Rule IpPrefix() {
    return Sequence(
        IpPrefixUnchecked(), push(new LeafAstNode(Prefix.parse(matchOrDefault("Error")))));
  }

  /** Matches Ip ranges (e.g., 1.1.1.1 - 1.1.1.2) */
  public Rule IpRange() {
    return Sequence(
        IpAddress(),
        WhiteSpace(),
        "- ",
        IpAddress(),
        push(new IpSpaceAstNode(Type.RANGE, (AstNode) pop(1), (AstNode) pop(0))),
        WhiteSpace());
  }

  /** Matches Ip wildcards (e.g., 1.1.1.1:255.255.255.255) */
  public Rule IpWildcard() {
    return Sequence(
        Sequence(IpAddressUnchecked(), ':', IpAddressUnchecked()),
        push(new LeafAstNode(new IpWildcard(matchOrDefault("Error")))));
  }
}

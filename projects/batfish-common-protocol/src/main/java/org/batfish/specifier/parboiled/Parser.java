package org.batfish.specifier.parboiled;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.specifier.parboiled.IpSpaceAstNode.Type;
import org.parboiled.BaseParser;
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
public class Parser extends BaseParser<Object> {

  /**
   * This is the common entry point for all types of expressions. The type of expression you want to
   * parse is specified by the top-level rule for the expression
   */
  public Rule input(Rule expression) {
    return Sequence(expression, EOI);
  }

  /**
   * IpSpace grammar
   *
   * <pre>
   * ipSpec := ipSpecTerm [, ipSpecTerm]*
   *
   * ipSpecTerm := addgressgroup(groupname, bookname)
   *               | ! ipSpecTerm
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
    return FirstOf(
        IpSpaceAddressGroup(), IpSpaceNot(), IpPrefix(), IpWildcard(), IpRange(), IpAddress());
  }

  /** Includes ref.addgressgroup for backward compatibility. Should be removed later */
  public Rule IpSpaceAddressGroup() {
    return Sequence(
        FirstOf(IgnoreCase("addressgroup"), IgnoreCase("ref.addressgroup")),
        WhiteSpace(),
        "( ",
        AddressGroupName(),
        ", ",
        ReferenceBookName(),
        ") ",
        push(new IpSpaceAstNode(Type.ADDRESS_GROUP, (AstNode) pop(1), (AstNode) pop())));
  }

  /** Not of the IpSpaceTerm, which means exclusion */
  public Rule IpSpaceNot() {
    return Sequence("! ", IpSpaceTerm(), push(new IpSpaceAstNode(Type.NOT, (AstNode) pop(), null)));
  }

  // Common helpers follow

  /** Matches names of AddressGroup */
  public Rule AddressGroupName() {
    return Sequence(
        ReferenceObjectNameLiteral(), push(new LeafAstNode(matchOrDefault("Error"))), WhiteSpace());
  }

  /**
   * Matched IP addresses. Throws an exception if something matches syntactically but is invalid
   * (e.g., 1.1.1.256)
   */
  public Rule IpAddress() {
    return Sequence(IpAddressUnchecked(), push(new LeafAstNode(Ip.parse(matchOrDefault("Error")))));
  }

  public Rule IpAddressUnchecked() {
    return Sequence(Number(), '.', Number(), '.', Number(), '.', Number());
  }

  /**
   * Matched IP prefixes. Throws an exception if something matches syntactically but is invalid
   * (e.g., 1.1.1.2/33)
   */
  public Rule IpPrefix() {
    return Sequence(
        IpPrefixUnchecked(), push(new LeafAstNode(Prefix.parse(matchOrDefault("Error")))));
  }

  public Rule IpPrefixUnchecked() {
    return Sequence(IpAddressUnchecked(), '/', Number());
  }

  /** Matches Ip ranges (e.g., 1.1.1.1 - 1.1.1.2) */
  public Rule IpRange() {
    return Sequence(
        IpAddress(),
        WhiteSpace(),
        "- ",
        IpAddress(),
        push(new IpSpaceAstNode(Type.DASH, (AstNode) pop(1), (AstNode) pop(0))),
        WhiteSpace());
  }

  /** Matches Ip ranges (e.g., 1.1.1.1 - 1.1.1.2) */
  public Rule IpWildcard() {
    return Sequence(
        Sequence(IpAddressUnchecked(), ':', IpAddressUnchecked()),
        push(new LeafAstNode(new IpWildcard(matchOrDefault("Error")))));
  }

  /** Matches names of reference books */
  public Rule ReferenceBookName() {
    return Sequence(
        ReferenceObjectNameLiteral(), push(new LeafAstNode(matchOrDefault("Error"))), WhiteSpace());
  }

  /**
   * Redefine rule creation for string literals.
   *
   * <p>We automatically match trailing whitespace, so we don't have to insert extra Whitespace()
   * rules after each character or string literal
   */
  @Override
  protected Rule fromStringLiteral(String string) {
    return string.endsWith(" ")
        ? Sequence(String(string.substring(0, string.length() - 1)), WhiteSpace())
        : String(string);
  }

  /** [a-z] + [A-Z] */
  public Rule AlphabetChar() {
    return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
  }

  /** [0-9] */
  public Rule Digit() {
    return CharRange('0', '9');
  }

  /** [0-9]+ */
  public Rule Number() {
    return OneOrMore(Digit());
  }

  /**
   * [a-zA-Z_][-a-zA-Z0-9_]*
   *
   * <p>This spec is based on {@link org.batfish.referencelibrary.ReferenceLibrary#NAME_PATTERN}
   */
  public Rule ReferenceObjectNameLiteral() {
    return Sequence(
        FirstOf(AlphabetChar(), Ch('_')),
        ZeroOrMore(FirstOf(AlphabetChar(), Ch('_'), Digit(), Ch('-'))));
  }

  public Rule WhiteSpace() {
    return ZeroOrMore(AnyOf(" \t\f"));
  }
}

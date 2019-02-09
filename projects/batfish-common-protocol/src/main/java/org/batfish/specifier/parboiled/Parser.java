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
@SuppressWarnings({"InfiniteRecursion"})
public class Parser extends BaseParser<Object> {

  /**
   * This is the common entry point for all types of expressions. The type of expression you want to
   * parse is specified by the top-level rule for the expression
   */
  public Rule input(Rule expression) {
    return Sequence(expression, EOI);
  }

  /**
   * IpSpace
   *
   * <pre>
   * ipSpec =
   * addressgroup(<group=string>,<book=string>)
   * | ofLocation(<locationSpec>)
   * | <ipv4spec>
   * | <ipv4spec> - <ipv4spec>
   *
   * <ipv4spec> =
   * <IPv4 address in A.B.C.D form>
   * | <IPv4 prefix in A.B.C.D/L form>
   * | <IPv4 wildcard in A.B.C.D:M.N.O.P form>
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
        IpSpaceAddressGroup(), IpSpaceNot(), Prefix(), IpWildcard(), IpRange(), IpAddress());
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

  public Rule IpSpaceNot() {
    return Sequence("! ", IpSpaceTerm(), push(new IpSpaceAstNode(Type.NOT, (AstNode) pop(), null)));
  }

  // Common helpers follow

  public Rule AddressGroupName() {
    return Sequence(
        ReferenceObjectNameLiteral(), push(new LeafAstNode(matchOrDefault("Error"))), WhiteSpace());
  }

  public Rule IpAddress() {
    return Sequence(IpAddressUnchecked(), push(new LeafAstNode(Ip.parse(matchOrDefault("Error")))));
  }

  public Rule IpAddressUnchecked() {
    return Sequence(Number(), '.', Number(), '.', Number(), '.', Number());
  }

  public Rule IpRange() {
    return Sequence(
        IpAddress(),
        WhiteSpace(),
        "- ",
        IpAddress(),
        push(new IpSpaceAstNode(Type.DASH, (AstNode) pop(1), (AstNode) pop(0))),
        WhiteSpace());
  }

  public Rule IpWildcard() {
    return Sequence(
        Sequence(IpAddressUnchecked(), ':', IpAddressUnchecked()),
        push(new LeafAstNode(new IpWildcard(matchOrDefault("Error")))));
  }

  public Rule Prefix() {
    return Sequence(
        PrefixUnchecked(), push(new LeafAstNode(Prefix.parse(matchOrDefault("Error")))));
  }

  public Rule PrefixUnchecked() {
    return Sequence(IpAddressUnchecked(), '/', Number());
  }

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

  public Rule AlphabetChar() {
    return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
  }

  public Rule Digit() {
    return CharRange('0', '9');
  }

  /** This spec is based on {@link org.batfish.referencelibrary.ReferenceLibrary#NAME_PATTERN} */
  public Rule ReferenceObjectNameLiteral() {
    return Sequence(
        FirstOf(AlphabetChar(), Ch('_')),
        ZeroOrMore(FirstOf(AlphabetChar(), Ch('_'), Digit(), Ch('-'))));
  }

  public Rule Number() {
    return OneOrMore(Digit());
  }

  public Rule WhiteSpace() {
    return ZeroOrMore(AnyOf(" \t\f"));
  }
}

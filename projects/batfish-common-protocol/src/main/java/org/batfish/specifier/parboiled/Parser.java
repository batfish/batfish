package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Completion.Type.ADDRESS_GROUP_AND_BOOK;
import static org.batfish.specifier.parboiled.Completion.Type.IP_ADDRESS;
import static org.batfish.specifier.parboiled.Completion.Type.IP_PREFIX;
import static org.batfish.specifier.parboiled.Completion.Type.IP_RANGE;
import static org.batfish.specifier.parboiled.Completion.Type.IP_WILDCARD;

import java.util.Map;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.specifier.parboiled.IpSpaceAstNode.Type;
import org.parboiled.Parboiled;
import org.parboiled.Rule;

/**
 * A parboiled-based parser for flexible specifiers. The rules for all types of expressions are in
 * this file (not sure how to put them in different files when they point to each other).
 *
 * <p>Completion annotation: For each path from the top-level rule of an expression to leaf values,
 * there should be at least one rule with a Completion annotation. This annotation is used for both
 * error messages and generating auto completion suggestions.
 */
@SuppressWarnings({
  "InfiniteRecursion",
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
class Parser extends CommonParser {

  static final Parser INSTANCE = Parboiled.createParser(Parser.class);

  static final Map<String, Completion.Type> COMPLETION_TYPES = initCompletionTypes(Parser.class);

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
  @Completion(ADDRESS_GROUP_AND_BOOK)
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
  @Completion(IP_ADDRESS)
  public Rule IpAddress() {
    return Sequence(IpAddressUnchecked(), push(new LeafAstNode(Ip.parse(matchOrDefault("Error")))));
  }

  /**
   * Matched IP prefixes. Throws an exception if something matches syntactically but is invalid
   * (e.g., 1.1.1.2/33)
   */
  @Completion(IP_PREFIX)
  public Rule IpPrefix() {
    return Sequence(
        IpPrefixUnchecked(), push(new LeafAstNode(Prefix.parse(matchOrDefault("Error")))));
  }

  /** Matches Ip ranges (e.g., 1.1.1.1 - 1.1.1.2) */
  @Completion(IP_RANGE)
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
  @Completion(IP_WILDCARD)
  public Rule IpWildcard() {
    return Sequence(
        Sequence(IpAddressUnchecked(), ':', IpAddressUnchecked()),
        push(new LeafAstNode(new IpWildcard(matchOrDefault("Error")))));
  }
}

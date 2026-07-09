package org.batfish.specifier.parboiled;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.batfish.common.util.antlr4c3.CodeCompletionCore;
import org.batfish.common.util.antlr4c3.CodeCompletionCore.CandidateRule;
import org.batfish.common.util.antlr4c3.CodeCompletionCore.CandidatesCollection;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.parboiled.grammar.SpecifierLexer;
import org.batfish.specifier.parboiled.grammar.SpecifierParser;

/**
 * Produces {@link PotentialMatch}es for autocompletion using the antlr4-c3 {@link
 * CodeCompletionCore} instead of parboiled's failed-matcher-path introspection.
 *
 * <p>The output is consumed by {@link ParboiledAutoComplete}, which is unchanged: it switches on
 * each match's {@link Anchor.Type} to produce suggestions. Two kinds of matches are produced,
 * mirroring parboiled:
 *
 * <ul>
 *   <li>Candidate rules (preferred rules valid at the caret) map to their {@link Anchor.Type}; the
 *       match prefix is the text from the rule's start token to the caret.
 *   <li>Candidate tokens (literals such as {@code ,}, {@code &}, {@code (}, {@code @role}) map to a
 *       {@link Anchor.Type#STRING_LITERAL} anchor whose "match" is the literal text; their path is
 *       the enclosing rule stack, which {@link ParboiledAutoComplete} walks to find the ancestor
 *       anchor (e.g. {@code ,} inside {@code nodeSpec} yields {@code NODE_SET_OP}).
 * </ul>
 */
final class C3PotentialMatches {

  private C3PotentialMatches() {}

  /**
   * The preferred rules whose presence at the caret we collect as rule candidates: the
   * value-bearing leaf rules (names, regexes, enum/type values, IP literals). Container rules
   * (set-op specs, intersections, parens, node-qualified terms, tails) are intentionally excluded;
   * their significant literals (',', '&', '(', '[', ...) surface as token candidates instead, and
   * are classified by walking up to the enclosing rule's anchor -- mirroring how parboiled treats
   * those literals as CHAR/STRING_LITERAL anchors whose ancestor is the container rule.
   */
  private static final Set<Integer> PREFERRED_RULES =
      ImmutableSet.of(
          SpecifierParser.RULE_referenceBook,
          SpecifierParser.RULE_appName,
          SpecifierParser.RULE_appPort,
          SpecifierParser.RULE_appPortRange,
          SpecifierParser.RULE_enumSetValue,
          SpecifierParser.RULE_filterName,
          SpecifierParser.RULE_filterNameRegex,
          SpecifierParser.RULE_interfaceGroup,
          SpecifierParser.RULE_interfaceName,
          SpecifierParser.RULE_interfaceNameRegex,
          SpecifierParser.RULE_vrfName,
          SpecifierParser.RULE_zoneName,
          SpecifierParser.RULE_ipProtocolName,
          SpecifierParser.RULE_ipProtocolNumber,
          SpecifierParser.RULE_addressGroup,
          SpecifierParser.RULE_ipAddress,
          SpecifierParser.RULE_ipAddressMask,
          SpecifierParser.RULE_ipPrefix,
          SpecifierParser.RULE_nameSetName,
          SpecifierParser.RULE_nameSetRegex,
          SpecifierParser.RULE_nodeRoleDimensionName,
          SpecifierParser.RULE_nodeRoleName,
          SpecifierParser.RULE_nodeName,
          SpecifierParser.RULE_nodeNameRegex,
          SpecifierParser.RULE_routingPolicyName,
          SpecifierParser.RULE_routingPolicyNameRegex);

  static Set<PotentialMatch> getPotentialMatches(Grammar grammar, String query) {
    CommonTokenStream tokens = lex(grammar, query);
    List<Token> allTokens = tokens.getTokens();
    int eofIndex = allTokens.size() - 1;

    List<PotentialMatch> matches = new ArrayList<>();

    // Position 1: caret at EOF. Yields what may follow a complete term (operators, close-parens,
    // and rule candidates that have not begun).
    collectAt(grammar, query, eofIndex, matches, /* rulesOnly= */ false);

    // Position 2: if the query ends in a partial token (a name/regex/number/IP with no trailing
    // whitespace), also collect with the caret on that token, so an in-progress name/regex is
    // offered for extension at its own start offset. This reproduces parboiled, whose forced-error
    // trick offered both the extension of the last token and the following operators at once. Only
    // rule (name/regex/value) extensions are taken from this position; operator/paren tokens that
    // could only follow a *completed* term come from the EOF position above.
    if (eofIndex > 0) {
      Token last = allTokens.get(eofIndex - 1);
      if (isPartialToken(last, query)) {
        collectAt(grammar, query, eofIndex - 1, matches, /* rulesOnly= */ true);
      }
    }

    return ImmutableSet.copyOf(matches);
  }

  private static CommonTokenStream lex(Grammar grammar, String query) {
    SpecifierLexer lexer = new SpecifierLexer(CharStreams.fromString(query));
    lexer.slashInNames = SpecifierAstBuilder.slashInNames(grammar);
    lexer.removeErrorListeners();
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    tokens.fill();
    return tokens;
  }

  private static void collectAt(
      Grammar grammar,
      String query,
      int caretTokenIndex,
      List<PotentialMatch> matches,
      boolean rulesOnly) {
    CommonTokenStream tokens = lex(grammar, query);
    SpecifierParser parser = new SpecifierParser(tokens);
    parser.removeErrorListeners();
    CodeCompletionCore core = new CodeCompletionCore(parser);
    core.preferredRules().addAll(PREFERRED_RULES);
    entryRule(parser, grammar);
    CandidatesCollection candidates = core.collectCandidates(caretTokenIndex, null);
    addRuleMatches(candidates, tokens, query, matches);
    if (!rulesOnly) {
      addTokenMatches(candidates, parser.getVocabulary(), query, matches);
    }
  }

  /** A token is "partial" if it abuts the end of the query (no trailing whitespace after it). */
  private static boolean isPartialToken(Token token, String query) {
    if (token.getType() == Token.EOF) {
      return false;
    }
    return token.getStopIndex() == query.length() - 1;
  }

  private static void entryRule(SpecifierParser parser, Grammar grammar) {
    switch (grammar) {
      case APPLICATION_SPECIFIER -> parser.appSpecInput();
      case FILTER_SPECIFIER -> parser.filterSpecInput();
      case INTERFACE_SPECIFIER -> parser.interfaceSpecInput();
      case IP_PROTOCOL_SPECIFIER -> parser.ipProtocolSpecInput();
      case IP_SPACE_SPECIFIER -> parser.ipSpaceSpecInput();
      case LOCATION_SPECIFIER -> parser.locationSpecInput();
      case NODE_SPECIFIER -> parser.nodeSpecInput();
      case ROUTING_POLICY_SPECIFIER -> parser.routingPolicySpecInput();
      case SINGLE_APPLICATION_SPECIFIER -> parser.oneAppSpecInput();
      case MLAG_ID_SPECIFIER -> parser.nameSetSpecInput();
      default -> parser.enumSetSpecInput();
    }
  }

  private static void addRuleMatches(
      CandidatesCollection candidates,
      CommonTokenStream tokens,
      String query,
      List<PotentialMatch> matches) {
    for (Map.Entry<Integer, CandidateRule> entry : candidates.rules().entrySet()) {
      Anchor.Type anchorType = SpecifierRuleAnchors.ANCHORS.get(entry.getKey());
      if (anchorType == null || anchorType == Anchor.Type.DEPRECATED) {
        continue;
      }
      int startTokenIndex = entry.getValue().startTokenIndex();
      int startCharIndex = charIndexOf(tokens, startTokenIndex);
      List<PathElement> path =
          ruleStackToPath(entry.getValue().ruleList(), anchorType, startCharIndex);
      if (isRegexAnchor(anchorType)) {
        // A /-delimited regex surfaces via its opening '/' (a CHAR_LITERAL whose ancestor is the
        // regex rule), mirroring parboiled. The regex rule itself is not emitted as a match.
        PathElement opener =
            new PathElement(Anchor.Type.CHAR_LITERAL, "'/'", path.size(), startCharIndex);
        List<PathElement> regexPath = new ArrayList<>(path);
        regexPath.add(opener);
        matches.add(new PotentialMatch(opener, "", regexPath));
        continue;
      }
      String matchPrefix = query.substring(Math.min(startCharIndex, query.length()));
      PathElement anchor = path.get(path.size() - 1);
      matches.add(new PotentialMatch(anchor, matchPrefix, path));
    }
  }

  private static boolean isRegexAnchor(Anchor.Type type) {
    return switch (type) {
      case NODE_NAME_REGEX,
          INTERFACE_NAME_REGEX,
          FILTER_NAME_REGEX,
          ROUTING_POLICY_NAME_REGEX,
          NAME_SET_REGEX,
          ENUM_SET_REGEX ->
          true;
      default -> false;
    };
  }

  private static void addTokenMatches(
      CandidatesCollection candidates,
      Vocabulary vocabulary,
      String query,
      List<PotentialMatch> matches) {
    for (Map.Entry<Integer, List<List<Integer>>> entry : candidates.tokenRuleStacks().entrySet()) {
      int tokenType = entry.getKey();
      if (tokenType == Token.EOF) {
        continue;
      }
      String literal = literalText(vocabulary, tokenType);
      if (literal == null) {
        continue;
      }
      for (List<Integer> stack : entry.getValue()) {
        List<PathElement> path = new ArrayList<>();
        int level = 0;
        for (int ruleIndex : stack) {
          Anchor.Type type = SpecifierRuleAnchors.ANCHORS.get(ruleIndex);
          path.add(
              new PathElement(type, SpecifierParser.ruleNames[ruleIndex], level, query.length()));
          level++;
        }
        // The literal is the anchor; its label carries the token text in quotes so that
        // PotentialMatch.getMatch() returns the literal (mirroring parboiled).
        PathElement literalAnchor =
            new PathElement(
                Anchor.Type.STRING_LITERAL, "\"" + literal + "\"", level, query.length());
        path.add(literalAnchor);
        matches.add(new PotentialMatch(literalAnchor, "", path));
      }
    }
  }

  /** Builds a path whose last element is the anchor rule; parents carry their own anchor types. */
  private static List<PathElement> ruleStackToPath(
      List<Integer> ruleStack, Anchor.Type anchorType, int startCharIndex) {
    List<PathElement> path = new ArrayList<>();
    int level = 0;
    for (int ruleIndex : ruleStack) {
      Anchor.Type type = SpecifierRuleAnchors.ANCHORS.get(ruleIndex);
      path.add(new PathElement(type, SpecifierParser.ruleNames[ruleIndex], level, startCharIndex));
      level++;
    }
    // Append the anchor rule itself (the candidate rule is not included in its own ruleList).
    path.add(new PathElement(anchorType, "anchor", level, startCharIndex));
    return path;
  }

  private static int charIndexOf(CommonTokenStream tokens, int tokenIndex) {
    Token token = tokens.get(tokenIndex);
    return token.getType() == Token.EOF ? token.getStartIndex() : token.getStartIndex();
  }

  /**
   * The @-function keyword tokens are defined with case-insensitive fragment sequences, so ANTLR
   * records no literal name for them. Map them to their canonical completion text.
   */
  private static final Map<Integer, String> KEYWORD_TEXT =
      ImmutableMap.<Integer, String>builder()
          .put(SpecifierParser.AT_ROLE, "@role")
          .put(SpecifierParser.AT_DEVICE_TYPE, "@deviceType")
          .put(SpecifierParser.AT_IN, "@in")
          .put(SpecifierParser.AT_OUT, "@out")
          .put(SpecifierParser.AT_CONNECTED_TO, "@connectedTo")
          .put(SpecifierParser.AT_INTERFACE_GROUP, "@interfaceGroup")
          .put(SpecifierParser.AT_INTERFACE_TYPE, "@interfaceType")
          .put(SpecifierParser.AT_VRF, "@vrf")
          .put(SpecifierParser.AT_ZONE, "@zone")
          .put(SpecifierParser.AT_ADDRESS_GROUP, "@addressgroup")
          .put(SpecifierParser.AT_ENTER, "@enter")
          .build();

  /** Returns the literal text for a token type that is a fixed literal, or null otherwise. */
  private static String literalText(Vocabulary vocabulary, int tokenType) {
    String keyword = KEYWORD_TEXT.get(tokenType);
    if (keyword != null) {
      return keyword;
    }
    String literal = vocabulary.getLiteralName(tokenType);
    if (literal == null) {
      return null;
    }
    // ANTLR literal names are single-quoted, e.g. "','".
    return literal.substring(1, literal.length() - 1);
  }
}

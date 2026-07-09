package org.batfish.specifier.parboiled;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
          SpecifierParser.RULE_enumSetRegex,
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
    // and rule candidates that have not begun). Skipped when the query ends in a value token that
    // is
    // not a valid complete value (e.g. "TC" as an ip-protocol): parboiled's parse would fail inside
    // the value rule, offering only extensions (from the partial-token pass), not operators.
    boolean trailingValueInvalid =
        eofIndex > 0 && trailingValueIsInvalid(grammar, allTokens.get(eofIndex - 1), query);
    if (!trailingValueInvalid) {
      collectAt(grammar, query, eofIndex, matches, /* rulesOnly= */ false, Token.INVALID_TYPE);
    }

    // Position 2: if the query ends in a partial token (a name/regex/number/IP with no trailing
    // whitespace), also collect with the caret on that token, so an in-progress name/regex is
    // offered for extension at its own start offset. This reproduces parboiled, whose forced-error
    // trick offered both the extension of the last token and the following operators at once. Only
    // rule (name/regex/value) extensions are taken from this position; operator/paren tokens that
    // could only follow a *completed* term come from the EOF position above.
    if (eofIndex > 0) {
      Token last = allTokens.get(eofIndex - 1);
      if (isPartialToken(last, query)) {
        collectAt(grammar, query, eofIndex - 1, matches, /* rulesOnly= */ true, last.getType());
        addAppKeywordPrefixMatches(grammar, allTokens, eofIndex, last, matches);
        addAppPortRangeDash(grammar, allTokens, eofIndex, last, query, matches);
      } else if (grammar == Grammar.SINGLE_APPLICATION_SPECIFIER
          && isAppKeywordToken(last)
          && last.getStopIndex() == query.length() - 1) {
        // A trailing complete icmp/tcp/udp keyword in the single-application grammar must be
        // followed
        // by '/...'. Replace the standalone '/' match (anchored at the keyword's ONE_APP_* rule)
        // with the combined "icmp/" anchored at the keyword start, matching parboiled.
        matches.removeIf(C3PotentialMatches::isSlashLiteralMatch);
        Anchor.Type anchor = oneAppKeywordAnchor(last.getType());
        int startChar = last.getStartIndex();
        matches.add(oneAppKeywordSlashMatch(anchor, last.getText(), startChar));
      }
    }

    if (grammar == Grammar.SINGLE_APPLICATION_SPECIFIER) {
      // In the single-application grammar icmp/tcp/udp are only valid as "icmp/...", so rewrite any
      // bare keyword suggestion (e.g. from the empty-input token candidates) to "icmp/". Doing it
      // here (rather than via literal extension) avoids the extension machinery double-appending.
      rewriteOneAppBareKeywords(matches);
    }

    return ImmutableSet.copyOf(matches);
  }

  /**
   * True if the query ends (no trailing whitespace) in a NAME/NUM that is not a valid complete
   * value for a closed-set grammar (application, ip-protocol, enum set). For such grammars an
   * invalid-but-name-shaped trailing token means the term did not complete, so following operators
   * should not be offered. For open grammars (node/interface/... names are arbitrary) this is
   * always false.
   */
  private static boolean trailingValueIsInvalid(Grammar grammar, Token last, String query) {
    if (last.getStopIndex() != query.length() - 1) {
      return false; // trailing whitespace: the term is complete
    }
    int type = last.getType();
    if (type != SpecifierParser.NAME && type != SpecifierParser.NUM) {
      return false;
    }
    String text = last.getText();
    return switch (grammar) {
      case APPLICATION_SPECIFIER, SINGLE_APPLICATION_SPECIFIER ->
          type == SpecifierParser.NAME
              && !CommonParser.namedApplications.contains(text.toUpperCase())
              && !isAppKeyword(text);
      case IP_PROTOCOL_SPECIFIER ->
          type == SpecifierParser.NAME && !IpProtocolIpProtocolAstNode.isValidName(text);
      case NODE_SPECIFIER,
          INTERFACE_SPECIFIER,
          FILTER_SPECIFIER,
          IP_SPACE_SPECIFIER,
          LOCATION_SPECIFIER,
          ROUTING_POLICY_SPECIFIER,
          MLAG_ID_SPECIFIER ->
          false;
      default ->
          // Enum-set grammars: value must be one of the closed enum set.
          type == SpecifierParser.NAME
              && !ValueEnumSetAstNode.isValidValue(text, Grammar.getEnumValues(grammar));
    };
  }

  /** The type of the nearest default-channel token strictly before index {@code i} (skips WS). */
  private static int prevDefaultChannelType(List<Token> allTokens, int i) {
    for (int j = i - 1; j >= 0; j--) {
      if (allTokens.get(j).getChannel() == Token.DEFAULT_CHANNEL) {
        return allTokens.get(j).getType();
      }
    }
    return -1;
  }

  private static boolean isAppKeywordToken(Token token) {
    int t = token.getType();
    return t == SpecifierParser.ICMP || t == SpecifierParser.TCP || t == SpecifierParser.UDP;
  }

  private static boolean isSlashLiteralMatch(PotentialMatch m) {
    return m.getAnchorType() == Anchor.Type.STRING_LITERAL && "/".equals(m.getMatch());
  }

  /** A "icmp/" style match anchored at {@code startChar} under the given ONE_APP_* anchor. */
  private static PotentialMatch oneAppKeywordSlashMatch(
      Anchor.Type anchor, String keyword, int startChar) {
    PathElement ancestor = new PathElement(anchor, "oneApp", 0, startChar);
    PathElement literal =
        new PathElement(Anchor.Type.STRING_LITERAL, "\"" + keyword + "/\"", 1, startChar);
    return new PotentialMatch(literal, "", List.of(ancestor, literal));
  }

  /** Rewrites bare icmp/tcp/udp literal matches to their "icmp/" form for the one-app grammar. */
  private static void rewriteOneAppBareKeywords(List<PotentialMatch> matches) {
    for (int i = 0; i < matches.size(); i++) {
      PotentialMatch m = matches.get(i);
      String match = m.getMatch();
      if (m.getAnchorType() == Anchor.Type.STRING_LITERAL && match != null && isAppKeyword(match)) {
        Anchor.Type anchor =
            match.equalsIgnoreCase("icmp")
                ? Anchor.Type.ONE_APP_ICMP
                : match.equalsIgnoreCase("tcp") ? Anchor.Type.ONE_APP_TCP : Anchor.Type.ONE_APP_UDP;
        matches.set(i, oneAppKeywordSlashMatch(anchor, match, m.getMatchStartIndex()));
      }
    }
  }

  private static Anchor.Type oneAppKeywordAnchor(int tokenType) {
    if (tokenType == SpecifierParser.ICMP) {
      return Anchor.Type.ONE_APP_ICMP;
    }
    if (tokenType == SpecifierParser.TCP) {
      return Anchor.Type.ONE_APP_TCP;
    }
    return Anchor.Type.ONE_APP_UDP;
  }

  private static boolean isAppKeyword(String text) {
    return text.equalsIgnoreCase("icmp")
        || text.equalsIgnoreCase("tcp")
        || text.equalsIgnoreCase("udp");
  }

  /**
   * In the application specifiers, icmp/tcp/udp are keyword tokens, so a partial name that is a
   * strict prefix of one (e.g. "ud" -> "udp") does not surface as a keyword candidate from c3.
   * Offer such keyword completions explicitly, but only at a term-start position (start of input or
   * right after a ',' term separator), anchored at the partial token's start.
   */
  private static void addAppKeywordPrefixMatches(
      Grammar grammar,
      List<Token> allTokens,
      int eofIndex,
      Token partial,
      List<PotentialMatch> matches) {
    if (grammar != Grammar.APPLICATION_SPECIFIER
        && grammar != Grammar.SINGLE_APPLICATION_SPECIFIER) {
      return;
    }
    if (partial.getType() != SpecifierParser.NAME) {
      return;
    }
    // Term-start: the partial token is the first real token, or the previous real token is a ','.
    int prevType = prevDefaultChannelType(allTokens, eofIndex - 1);
    boolean atTermStart = prevType == -1 || prevType == SpecifierParser.COMMA;
    if (!atTermStart) {
      return;
    }
    String prefix = partial.getText().toLowerCase();
    int start = partial.getStartIndex();
    addKeywordPrefixMatch(prefix, "icmp", Anchor.Type.APP_ICMP, start, matches);
    addKeywordPrefixMatch(prefix, "tcp", Anchor.Type.APP_TCP, start, matches);
    addKeywordPrefixMatch(prefix, "udp", Anchor.Type.APP_UDP, start, matches);
  }

  /**
   * A partial port number in an application port spec (tcp/udp {@code /N}) can begin a range, so
   * offer '-' (APP_PORT_RANGE) at the caret. c3 does not surface this because the range tail is an
   * optional continuation the caret-on-token position does not expand.
   */
  private static void addAppPortRangeDash(
      Grammar grammar,
      List<Token> allTokens,
      int eofIndex,
      Token partial,
      String query,
      List<PotentialMatch> matches) {
    if (grammar != Grammar.APPLICATION_SPECIFIER || partial.getType() != SpecifierParser.NUM) {
      return;
    }
    // A '-' (range) may follow only the *first* number of a tcp/udp port term: the number must be
    // immediately preceded by the '/' that starts the port spec or a ',' between port terms (not by
    // a '-', which would already be a range), and the term keyword must be tcp/udp (icmp
    // types/codes
    // are not ranges).
    int prevType = prevDefaultChannelType(allTokens, eofIndex - 1);
    if (prevType != SpecifierParser.SLASH && prevType != SpecifierParser.COMMA) {
      return;
    }
    boolean tcpOrUdpTerm = false;
    for (int i = eofIndex - 2; i >= 0; i--) {
      int t = allTokens.get(i).getType();
      if (t == SpecifierParser.TCP || t == SpecifierParser.UDP) {
        tcpOrUdpTerm = true;
        break;
      }
      if (t == SpecifierParser.ICMP) {
        break;
      }
    }
    if (!tcpOrUdpTerm) {
      return;
    }
    PathElement ancestor =
        new PathElement(Anchor.Type.APP_PORT_RANGE, "appPortRange", 0, query.length());
    PathElement literal = new PathElement(Anchor.Type.STRING_LITERAL, "\"-\"", 1, query.length());
    matches.add(new PotentialMatch(literal, "", List.of(ancestor, literal)));
  }

  private static void addKeywordPrefixMatch(
      String prefix,
      String keyword,
      Anchor.Type anchor,
      int startChar,
      List<PotentialMatch> matches) {
    if (!keyword.startsWith(prefix) || keyword.equals(prefix)) {
      return;
    }
    PathElement ancestor = new PathElement(anchor, "app", 0, startChar);
    PathElement literal =
        new PathElement(Anchor.Type.STRING_LITERAL, "\"" + keyword + "\"", 1, startChar);
    matches.add(new PotentialMatch(literal, "", List.of(ancestor, literal)));
  }

  private static CommonTokenStream lex(Grammar grammar, String query) {
    SpecifierLexer lexer = SpecifierAstBuilder.newLexer(grammar, query);
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
      boolean rulesOnly,
      int partialTokenType) {
    CommonTokenStream tokens = lex(grammar, query);
    SpecifierParser parser = new SpecifierParser(tokens);
    parser.removeErrorListeners();
    CodeCompletionCore core = new CodeCompletionCore(parser);
    core.preferredRules().addAll(PREFERRED_RULES);
    // Parse from the grammar's entry rule and pass that context to c3 so its ATN walk starts at the
    // right rule (with context == null, c3 would start at rule index 0, which is nodeSpecInput).
    org.antlr.v4.runtime.ParserRuleContext entry = entryRule(parser, grammar);
    CandidatesCollection candidates = core.collectCandidates(caretTokenIndex, entry);
    // In the partial-token pass (rulesOnly), the caret is inside an existing value token, so only
    // same-kind extensions make sense: emit rule (name/value) matches but not the regex '/'-opener
    // (a regex cannot continue a partial name) and not follow-on operator/paren tokens. Also filter
    // rule matches to those whose leaf-value kind is compatible with the partial token (a NUM
    // cannot extend a name-only rule, and vice versa), since names cannot start with a digit.
    addRuleMatches(
        candidates, tokens, query, matches, /* includeRegexOpener= */ !rulesOnly, partialTokenType);
    if (!rulesOnly) {
      addTokenMatches(candidates, parser.getVocabulary(), tokens, query, matches, false);
    } else {
      // In the partial-token pass, also surface token candidates that continue the *current* value
      // term (e.g. '-' to turn a port into a port range), but not operators/parens that require the
      // term to be complete.
      addTokenMatches(candidates, parser.getVocabulary(), tokens, query, matches, true);
    }
  }

  /**
   * Whether a rule with the given anchor can extend a partial token of {@code partialTokenType}.
   */
  private static boolean compatibleWithPartial(Anchor.Type anchorType, int partialTokenType) {
    if (partialTokenType == Token.INVALID_TYPE) {
      return true; // Not a partial-token pass.
    }
    boolean numericAnchor =
        anchorType == Anchor.Type.IP_PROTOCOL_NUMBER
            || anchorType == Anchor.Type.APP_PORT
            || anchorType == Anchor.Type.APP_PORT_RANGE
            || anchorType == Anchor.Type.IP_ADDRESS
            || anchorType == Anchor.Type.IP_ADDRESS_MASK
            || anchorType == Anchor.Type.IP_PREFIX;
    boolean numericToken =
        partialTokenType == SpecifierParser.NUM
            || partialTokenType == SpecifierParser.IP_ADDRESS
            || partialTokenType == SpecifierParser.IP_PREFIX;
    // A digit-led token extends only numeric-value anchors; a letter-led token only non-numeric.
    return numericToken == numericAnchor;
  }

  /**
   * A token is "partial" (extendable in-place as a name/regex/value) if it abuts the end of the
   * query (no trailing whitespace) and is a value-shaped token -- a NAME, quoted name, number, IP,
   * or regex. Keyword tokens (the @-functions) and punctuation are not extended: after them the
   * grammar dictates what follows, exactly as parboiled's forced-error trick would fail inside the
   * keyword's rule rather than re-offer a fresh term.
   */
  private static boolean isPartialToken(Token token, String query) {
    if (token.getStopIndex() != query.length() - 1) {
      return false;
    }
    return switch (token.getType()) {
      case SpecifierParser.NAME,
          SpecifierParser.QUOTED_NAME,
          SpecifierParser.NUM,
          SpecifierParser.IP_ADDRESS,
          SpecifierParser.IP_PREFIX,
          SpecifierParser.DEPRECATED_REGEX,
          SpecifierParser.REGEX ->
          true;
      default -> false;
    };
  }

  private static org.antlr.v4.runtime.ParserRuleContext entryRule(
      SpecifierParser parser, Grammar grammar) {
    return switch (grammar) {
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
    };
  }

  private static void addRuleMatches(
      CandidatesCollection candidates,
      CommonTokenStream tokens,
      String query,
      List<PotentialMatch> matches,
      boolean includeRegexOpener,
      int partialTokenType) {
    for (Map.Entry<Integer, CandidateRule> entry : candidates.rules().entrySet()) {
      Anchor.Type anchorType = SpecifierRuleAnchors.ANCHORS.get(entry.getKey());
      if (anchorType == null || anchorType == Anchor.Type.DEPRECATED) {
        continue;
      }
      if (!compatibleWithPartial(anchorType, partialTokenType)) {
        continue;
      }
      int startTokenIndex = entry.getValue().startTokenIndex();
      int startCharIndex = charIndexOf(tokens, startTokenIndex);
      List<PathElement> path =
          ruleStackToPath(
              entry.getValue().ruleList(),
              entry.getValue().ruleStartTokens(),
              anchorType,
              startCharIndex,
              tokens);
      if (isRegexAnchor(anchorType)) {
        if (!includeRegexOpener) {
          continue;
        }
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
      CommonTokenStream tokens,
      String query,
      List<PotentialMatch> matches,
      boolean valueContinuationOnly) {
    for (Map.Entry<Integer, List<CodeCompletionCore.RuleStack>> entry :
        candidates.tokenRuleStacks().entrySet()) {
      int tokenType = entry.getKey();
      if (tokenType == Token.EOF) {
        continue;
      }
      String literal = literalText(vocabulary, tokenType);
      if (literal == null) {
        continue;
      }
      for (CodeCompletionCore.RuleStack stack : entry.getValue()) {
        List<PathElement> path = new ArrayList<>();
        List<Integer> ruleList = stack.ruleList();
        List<Integer> starts = stack.ruleStartTokens();
        Anchor.Type nearestAnchor = null;
        for (int i = 0; i < ruleList.size(); i++) {
          int ruleIndex = ruleList.get(i);
          Anchor.Type type = SpecifierRuleAnchors.ANCHORS.get(ruleIndex);
          if (type != null) {
            nearestAnchor = type;
          }
          int startChar = charIndexOf(tokens, starts.get(i));
          path.add(new PathElement(type, SpecifierParser.ruleNames[ruleIndex], i, startChar));
        }
        // In the partial-token pass only keep tokens that continue the current value term (e.g. the
        // '-' that turns a port into a port range), not operators/parens requiring a complete term.
        if (valueContinuationOnly && !isValueContinuationAnchor(nearestAnchor)) {
          continue;
        }
        // The literal is the anchor; its label carries the token text in quotes so that
        // PotentialMatch.getMatch() returns the literal (mirroring parboiled). It anchors at the
        // caret (end of query).
        PathElement literalAnchor =
            new PathElement(
                Anchor.Type.STRING_LITERAL, "\"" + literal + "\"", ruleList.size(), query.length());
        path.add(literalAnchor);
        matches.add(new PotentialMatch(literalAnchor, "", path));
      }
    }
  }

  /** Anchors whose in-rule tokens continue the current value term (not term separators). */
  private static boolean isValueContinuationAnchor(Anchor.Type anchor) {
    return anchor == Anchor.Type.APP_PORT_RANGE
        || anchor == Anchor.Type.APP_PORTS
        || anchor == Anchor.Type.APP_ICMP_TYPE
        || anchor == Anchor.Type.APP_ICMP_TYPE_CODE;
  }

  /**
   * Builds a path whose last element is the anchor rule; each element carries its rule's anchor
   * type and the char index at which that rule started (from the parallel {@code ruleStartTokens}),
   * so that {@link ParboiledAutoComplete#findPrecedingInput} can recover input spans.
   */
  private static List<PathElement> ruleStackToPath(
      List<Integer> ruleStack,
      List<Integer> ruleStartTokens,
      Anchor.Type anchorType,
      int anchorStartCharIndex,
      CommonTokenStream tokens) {
    List<PathElement> path = new ArrayList<>();
    for (int i = 0; i < ruleStack.size(); i++) {
      int ruleIndex = ruleStack.get(i);
      Anchor.Type type = SpecifierRuleAnchors.ANCHORS.get(ruleIndex);
      int startChar = charIndexOf(tokens, ruleStartTokens.get(i));
      path.add(new PathElement(type, SpecifierParser.ruleNames[ruleIndex], i, startChar));
    }
    // Append the anchor rule itself (the candidate rule is not included in its own ruleList).
    path.add(new PathElement(anchorType, "anchor", ruleStack.size(), anchorStartCharIndex));
    return path;
  }

  private static int charIndexOf(CommonTokenStream tokens, int tokenIndex) {
    return tokens.get(tokenIndex).getStartIndex();
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
          .put(SpecifierParser.ICMP, "icmp")
          .put(SpecifierParser.TCP, "tcp")
          .put(SpecifierParser.UDP, "udp")
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

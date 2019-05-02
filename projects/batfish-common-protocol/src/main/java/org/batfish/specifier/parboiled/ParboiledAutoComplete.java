package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Names.ESCAPE_CHAR;
import static org.batfish.datamodel.Names.nameNeedsEscaping;
import static org.batfish.datamodel.answers.AutocompleteSuggestion.DEFAULT_RANK;
import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.CHAR_LITERAL;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;
import static org.batfish.specifier.parboiled.CommonParser.isEscapableNameAnchor;
import static org.batfish.specifier.parboiled.CommonParser.isOperatorWithRhs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.answers.AutoCompleteUtils;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.Variable;
import org.batfish.datamodel.questions.Variable.Type;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.InterfaceGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** A helper class that provides auto complete suggestions */
@ParametersAreNonnullByDefault
public final class ParboiledAutoComplete {

  static final char ILLEGAL_CHAR = (char) 0x26bd;

  public static final int RANK_STRING_LITERAL = 1;

  private final CommonParser _parser;
  private final Rule _expression;
  private final Map<String, Anchor.Type> _completionTypes;

  private final String _network;
  private final String _snapshot;
  private final String _query;
  private final int _maxSuggestions;
  private final CompletionMetadata _completionMetadata;
  private final NodeRolesData _nodeRolesData;
  private final ReferenceLibrary _referenceLibrary;

  ParboiledAutoComplete(
      CommonParser parser,
      Rule expression,
      Map<String, Anchor.Type> completionTypes,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    _parser = parser;
    _expression = expression;
    _completionTypes = completionTypes;
    _network = network;
    _snapshot = snapshot;
    _query = query;
    _maxSuggestions = maxSuggestions;
    _completionMetadata = completionMetadata;
    _nodeRolesData = nodeRolesData;
    _referenceLibrary = referenceLibrary;
  }

  public static List<AutocompleteSuggestion> autoComplete(
      Grammar grammar,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    Parser parser = Parser.instance();
    return new ParboiledAutoComplete(
            parser,
            parser.getInputRule(grammar),
            Parser.ANCHORS,
            network,
            snapshot,
            query,
            maxSuggestions,
            completionMetadata,
            nodeRolesData,
            referenceLibrary)
        .run();
  }

  /** This is the entry point for all auto completions */
  List<ParboiledAutoCompleteSuggestion> run() {
    Set<PotentialMatch> potentialMatches = getPotentialMatches(_query);

    Set<AutocompleteSuggestion> allSuggestions =
        potentialMatches.stream()
            .map(this::autoCompletePotentialMatch)
            .flatMap(Collection::stream)
            .collect(ImmutableSet.toImmutableSet());

    return allSuggestions.stream()
        .sorted(
            Comparator.comparing(AutocompleteSuggestion::getRank)
                .thenComparing(s -> s.getText().length()))
        .collect(ImmutableList.toImmutableList());
  }

  private Set<PotentialMatch> getPotentialMatches(String query) {
    /**
     * Before passing the query to the parser, we make it illegal by adding a funny, non-ascii
     * character (soccer ball :)). We will not get any errors backs if the string is legal.
     */
    String testQuery = query + new String(Character.toChars(ILLEGAL_CHAR));

    ParsingResult<AstNode> result = new ReportingParseRunner<AstNode>(_expression).run(testQuery);
    if (result.parseErrors.isEmpty()) {
      throw new IllegalStateException("Failed to force erroneous input");
    }

    InvalidInputError error = (InvalidInputError) result.parseErrors.get(0);

    return ParserUtils.getPotentialMatches(error, _completionTypes, false);
  }

  @VisibleForTesting
  List<ParboiledAutoCompleteSuggestion> autoCompletePotentialMatch(PotentialMatch pm) {
    switch (pm.getAnchorType()) {
      case ADDRESS_GROUP_NAME:
        return autoCompleteReferenceBookEntity(pm, DEFAULT_RANK);
      case CHAR_LITERAL:
        /*
         Char and String literals get a lower rank so that the possibly many suggestions for dynamic values
         (e.g., all nodes in the snapshot) do not drown everything else
        */
        return autoCompleteLiteral(pm, RANK_STRING_LITERAL);
      case EOI:
        return ImmutableList.of();
      case FILTER_INTERFACE_IN:
      case FILTER_INTERFACE_OUT:
        // Should delegate to interface spec
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case FILTER_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case FILTER_NAME_REGEX:
        // can't help with regexes
        return ImmutableList.of();
      case FILTER_PARENS:
        // Other filter rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case INTERFACE_GROUP_NAME:
        return autoCompleteReferenceBookEntity(pm, DEFAULT_RANK);
      case INTERFACE_NAME:
        return autoCompleteInterfaceName(pm, DEFAULT_RANK);
      case INTERFACE_NAME_REGEX:
        // can't help with regexes
        return ImmutableList.of();
      case INTERFACE_PARENS:
        // Other interface rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case INTERFACE_TYPE:
      case INTERFACE_VRF:
      case INTERFACE_ZONE:
        // These rely on type, vrf, or zone completion that appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case IP_ADDRESS:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case IP_ADDRESS_MASK:
        // can't help with masks
        return ImmutableList.of();
      case IP_PROTOCOL_NUMBER:
        // don't help with numbers
        return ImmutableList.of();
      case IP_PREFIX:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case IP_RANGE:
        // Relies on IP_ADDRESS completion as it appears later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case IP_WILDCARD:
        // Relies on IP_ADDRESS and IP_ADDRESS_MASK completions as they appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case LOCATION_PARENS:
        // Other location rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case NODE_AND_INTERFACE:
        // Node or Interface based anchors should appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case NODE_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case NODE_NAME_REGEX:
        // can't help with regexes
        return ImmutableList.of();
      case NODE_PARENS:
        // Other node rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case NODE_ROLE_AND_DIMENSION:
        // Role and dimension name rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case NODE_ROLE_DIMENSION_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case NODE_ROLE_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case NODE_TYPE:
        // Relies on STRING_LITERAL completion as it appears later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case REFERENCE_BOOK_AND_ADDRESS_GROUP:
      case REFERENCE_BOOK_AND_INTERFACE_GROUP:
        // Reference book name and address/interface group name should appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case REFERENCE_BOOK_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case ROUTING_POLICY_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case ROUTING_POLICY_NAME_REGEX:
        // can't help with regexes
        return ImmutableList.of();
      case ROUTING_POLICY_PARENS:
        // Other routing policy rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case STRING_LITERAL:
        /*
         Char and String literals get a lower rank so that the possibly many suggestions for dynamic values
         (e.g., all nodes in the snapshot) do not drown everything else
        */
        return autoCompleteLiteral(pm, RANK_STRING_LITERAL);
      case VRF_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      case WHITESPACE:
        // nothing useful to suggest for these completion types
        return ImmutableList.of();
      case ZONE_NAME:
        return autoCompletePotentialMatch(pm, DEFAULT_RANK);
      default:
        throw new IllegalArgumentException("Unhandled completion type " + pm.getAnchorType());
    }
  }

  @VisibleForTesting
  List<AutocompleteSuggestion> autoCompleteLiteral(PotentialMatch pm, int rank) {
    Optional<PotentialMatch> extendedMatch = extendLiteralMatch(_query, pm);

    PotentialMatch pmToConsider = extendedMatch.orElse(pm);

    Optional<Anchor.Type> ancestorAnchor = Optional.empty();

    if (isOperatorWithRhs(pmToConsider.getMatch())) {
      int anchorIndex = pmToConsider.getPath().indexOf(pmToConsider.getAnchor());
      checkArgument(anchorIndex != -1, "Anchor is not present in the path.");

      ancestorAnchor =
          IntStream.range(0, anchorIndex)
              .mapToObj(i -> pmToConsider.getPath().get(anchorIndex - i - 1).getAnchorType())
              .filter(Objects::nonNull)
              .findFirst();
    }

    return ImmutableList.of(
        new AutocompleteSuggestion(
            pm.getMatch() + extendedMatch.map(PotentialMatch::getMatch).orElse(""),
            true,
            ancestorAnchor.map(Anchor.Type::getDescription).orElse(null),
            rank,
            pm.getMatchStartIndex(),
            ancestorAnchor.map(Anchor.Type::getHint).orElse(null)));
  }

  /**
   * For literal matches, we check if there is a unique extension that is also a literal. This is
   * most helpful for specifiers, so we can suggest '@connectedTo(' (with open parenthesis) instead
   * of first suggesting '@connectedTo' and then suggesting '('.
   */
  private Optional<PotentialMatch> extendLiteralMatch(String query, PotentialMatch pm) {
    String extendedQuery = query.substring(0, pm.getMatchStartIndex()) + pm.getMatch();

    // this call reuses the parser object, which is OK since the previous iteration is over
    Set<PotentialMatch> extendedMatches = getPotentialMatches(extendedQuery);

    // if we get a unique extension, extend based on one of those matches
    if (extendedMatches.stream().map(PotentialMatch::getMatch).distinct().count() == 1) {
      PotentialMatch someMatch = Iterables.getFirst(extendedMatches, null);
      if (someMatch.getAnchorType() == CHAR_LITERAL
          || someMatch.getAnchorType() == STRING_LITERAL) {
        return Optional.of(someMatch);
      }
    }
    return Optional.empty();
  }

  private List<AutocompleteSuggestion> autoCompletePotentialMatch(PotentialMatch pm, int rank) {
    String matchPrefix = unescapeIfNeeded(pm.getMatchPrefix(), pm.getAnchorType());
    List<AutocompleteSuggestion> suggestions =
        AutoCompleteUtils.autoComplete(
            _network,
            _snapshot,
            anchorTypeToVariableType(pm.getAnchorType()),
            matchPrefix,
            _maxSuggestions,
            _completionMetadata,
            _nodeRolesData,
            _referenceLibrary);
    return updateSuggestions(
        suggestions,
        !matchPrefix.equals(pm.getMatchPrefix()),
        pm.getAnchorType(),
        rank,
        pm.getMatchStartIndex());
  }

  /**
   * Converts completion type to variable type for cases. Throws an exception when the mapping does
   * not exist
   */
  private static Variable.Type anchorTypeToVariableType(Anchor.Type anchorType) {
    switch (anchorType) {
      case ADDRESS_GROUP_NAME:
        return Variable.Type.ADDRESS_GROUP_NAME;
      case FILTER_NAME:
        return Variable.Type.FILTER_NAME;
      case INTERFACE_GROUP_NAME:
        return Variable.Type.INTERFACE_GROUP_NAME;
      case INTERFACE_NAME:
        return Type.INTERFACE_NAME;
      case IP_ADDRESS:
        return Variable.Type.IP;
      case IP_PREFIX:
        return Variable.Type.PREFIX;
      case NODE_NAME:
        return Variable.Type.NODE_NAME;
      case NODE_ROLE_NAME:
        return Variable.Type.NODE_ROLE_NAME;
      case NODE_ROLE_DIMENSION_NAME:
        return Variable.Type.NODE_ROLE_DIMENSION_NAME;
      case REFERENCE_BOOK_NAME:
        return Variable.Type.REFERENCE_BOOK_NAME;
      case ROUTING_POLICY_NAME:
        return Variable.Type.ROUTING_POLICY_NAME;
      case VRF_NAME:
        return Variable.Type.VRF;
      case ZONE_NAME:
        return Variable.Type.ZONE;
      default:
        throw new IllegalArgumentException("No valid Variable type for Anchor type" + anchorType);
    }
  }

  @VisibleForTesting
  List<AutocompleteSuggestion> autoCompleteInterfaceName(PotentialMatch pm, int rank) {
    int anchorIndex = pm.getPath().indexOf(pm.getAnchor());
    checkArgument(anchorIndex != -1, "Anchor is not present in the path.");

    Anchor.Type parentAnchorType =
        (anchorIndex == 0) ? null : pm.getPath().get(anchorIndex - 1).getAnchorType();

    if (parentAnchorType == null) {
      return autoCompletePotentialMatch(pm, rank);
    }

    switch (parentAnchorType) {
      case NODE_AND_INTERFACE:
        String interfaceNamePrefix = pm.getMatchPrefix();
        // node information is at the head if nothing about the interface name was entered;
        // otherwise, it is second from top
        NodeAstNode nodeAst =
            (NodeAstNode)
                _parser
                    .getShadowStack()
                    .getValueStack()
                    .peek(interfaceNamePrefix.isEmpty() ? 0 : 1);

        // do context sensitive auto completion input is a node name or regex
        if (!(nodeAst instanceof NameNodeAstNode) && !(nodeAst instanceof NameRegexNodeAstNode)) {
          return autoCompletePotentialMatch(pm, rank);
        }

        Set<String> candidateInterfaces =
            _completionMetadata.getInterfaces().stream()
                .filter(i -> nodeNameMatches(i.getHostname(), nodeAst))
                .map(NodeInterfacePair::getInterface)
                .collect(ImmutableSet.toImmutableSet());
        return updateSuggestions(
            AutoCompleteUtils.stringAutoComplete(interfaceNamePrefix, candidateInterfaces),
            false,
            Anchor.Type.INTERFACE_NAME,
            rank,
            pm.getMatchStartIndex());

      default:
        return autoCompletePotentialMatch(pm, rank);
    }
  }

  @VisibleForTesting
  static boolean nodeNameMatches(String nodeName, NodeAstNode nodeAst) {
    if (nodeAst instanceof NameNodeAstNode) {
      return nodeName.equalsIgnoreCase(((NameNodeAstNode) nodeAst).getName());
    } else if (nodeAst instanceof NameRegexNodeAstNode) {
      return ((NameRegexNodeAstNode) nodeAst).getPattern().matcher(nodeName).find();
    } else {
      throw new IllegalArgumentException("Can only match node names or regexes");
    }
  }

  /**
   * Auto completes reference book names. The completion is context sensitive if the parent {@link
   * PathElement} exists and is one that supports such completion. Otherwise, non context-sensitive
   * completion is used
   */
  @VisibleForTesting
  List<ParboiledAutoCompleteSuggestion> autoCompleteReferenceBookEntity(
      PotentialMatch pm, int rank) {
    int anchorIndex = pm.getPath().indexOf(pm.getAnchor());
    checkArgument(anchorIndex != -1, "Anchor is not present in the path.");

    Anchor.Type parentAnchorType =
        (anchorIndex == 0) ? null : pm.getPath().get(anchorIndex - 1).getAnchorType();

    if (parentAnchorType == null) {
      return autoCompletePotentialMatch(pm, rank);
    }

    switch (parentAnchorType) {
      case REFERENCE_BOOK_AND_ADDRESS_GROUP:
        checkArgument(
            pm.getAnchorType() == ADDRESS_GROUP_NAME,
            "Unexpected anchor for auto completing reference book entity. Expected %s. Got %s.",
            ADDRESS_GROUP_NAME,
            pm.getAnchorType());
        Function<ReferenceBook, Set<String>> addressGroupGetter =
            book ->
                book.getAddressGroups().stream()
                    .map(AddressGroup::getName)
                    .collect(ImmutableSet.toImmutableSet());
        return autoCompleteReferenceBookEntity(pm, addressGroupGetter, rank);
      case REFERENCE_BOOK_AND_INTERFACE_GROUP:
        checkArgument(
            pm.getAnchorType() == INTERFACE_GROUP_NAME,
            "Unexpected anchor for auto completing reference book entity. Expected %s. Got %s.",
            INTERFACE_GROUP_NAME,
            pm.getAnchorType());
        Function<ReferenceBook, Set<String>> interfaceGroupGetter =
            book ->
                book.getInterfaceGroups().stream()
                    .map(InterfaceGroup::getName)
                    .collect(ImmutableSet.toImmutableSet());
        return autoCompleteReferenceBookEntity(pm, interfaceGroupGetter, rank);
      default:
        return autoCompletePotentialMatch(pm, rank);
    }
  }

  private List<ParboiledAutoCompleteSuggestion> autoCompleteReferenceBookEntity(
      PotentialMatch pm, Function<ReferenceBook, Set<String>> entityNameGetter, int rank) {
    String matchPrefix = pm.getMatchPrefix();
    // book name is at the head if nothing about the reference book was entered;
    // otherwise, it is second from top
    String bookName =
        ((StringAstNode)
                _parser.getShadowStack().getValueStack().peek(matchPrefix.isEmpty() ? 0 : 1))
            .getStr();
    Set<String> candidateEntityNames =
        entityNameGetter.apply(
            _referenceLibrary
                .getReferenceBook(bookName)
                .orElse(ReferenceBook.builder("empty").build()));
    return updateSuggestions(
        AutoCompleteUtils.stringAutoComplete(matchPrefix, candidateEntityNames),
        false,
        pm.getAnchorType(),
        rank,
        pm.getMatchStartIndex());
  }

  /**
   * Update suggestions obtained through {@link AutoCompleteUtils} to escape names if needed and
   * assign rank and start index
   */
  @Nonnull
  private static List<ParboiledAutoCompleteSuggestion> updateSuggestions(
      List<AutocompleteSuggestion> suggestions,
      boolean escape,
      Anchor.Type anchorType,
      int rank,
      int startIndex) {
    return suggestions.stream()
        .map(
            s ->
                new AutocompleteSuggestion(
                    // escape if needed
                    escape || (isEscapableNameAnchor(anchorType) && nameNeedsEscaping(s.getText()))
                        ? ESCAPE_CHAR + s.getText() + ESCAPE_CHAR
                        : s.getText(),
                    true,
                    s.getDescription(),
                    rank,
                    startIndex))
        .collect(ImmutableList.toImmutableList());
  }

  /** Unescapes {@code originalMatch} if it is of escapable type and is already escaped */
  @Nonnull
  private static String unescapeIfNeeded(String originalMatch, Anchor.Type anchorType) {
    if (isEscapableNameAnchor(anchorType) && originalMatch.startsWith(ESCAPE_CHAR)) {
      return originalMatch.substring(1);
    }
    return originalMatch;
  }
}

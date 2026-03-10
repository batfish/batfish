package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Names.ESCAPE_CHAR;
import static org.batfish.datamodel.Names.nameNeedsEscaping;
import static org.batfish.specifier.parboiled.Anchor.Type.CHAR_LITERAL;
import static org.batfish.specifier.parboiled.Anchor.Type.FILTER_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.INTERFACE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_AND_INTERFACE;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_AND_INTERFACE_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_ROLE_AND_DIMENSION;
import static org.batfish.specifier.parboiled.Anchor.Type.NODE_ROLE_AND_DIMENSION_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_INTERFACE_GROUP;
import static org.batfish.specifier.parboiled.Anchor.Type.REFERENCE_BOOK_AND_INTERFACE_GROUP_TAIL;
import static org.batfish.specifier.parboiled.Anchor.Type.ROUTING_POLICY_NAME_REGEX;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;
import static org.batfish.specifier.parboiled.CommonParser.isEscapableNameAnchor;
import static org.batfish.specifier.parboiled.ParboiledAutoCompleteSuggestion.toAutoCompleteSuggestions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collection;
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
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.answers.AutoCompleteUtils;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.Variable;
import org.batfish.datamodel.questions.Variable.Type;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.InterfaceGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** A helper class that provides auto complete suggestions */
@ParametersAreNonnullByDefault
public final class ParboiledAutoComplete {

  static final char ILLEGAL_CHAR = (char) 0x26bd;

  @VisibleForTesting
  static Set<String> addressGroupGetter(ReferenceBook book) {
    return book.getAddressGroups().stream()
        .map(AddressGroup::getName)
        .collect(ImmutableSet.toImmutableSet());
  }

  private static Set<String> interfaceGroupGetter(ReferenceBook book) {
    return book.getInterfaceGroups().stream()
        .map(InterfaceGroup::getName)
        .collect(ImmutableSet.toImmutableSet());
  }

  private final Grammar _grammar;
  private final Rule _inputRule;
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
      Grammar grammar,
      Map<String, Anchor.Type> completionTypes,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    this(
        grammar,
        parser.getInputRule(grammar),
        completionTypes,
        network,
        snapshot,
        query,
        maxSuggestions,
        completionMetadata,
        nodeRolesData,
        referenceLibrary);
  }

  ParboiledAutoComplete(
      Grammar grammar,
      Rule inputRule,
      Map<String, Anchor.Type> completionTypes,
      String network,
      String snapshot,
      String query,
      int maxSuggestions,
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    _grammar = grammar;
    _inputRule = inputRule;
    _completionTypes = completionTypes;
    _network = network;
    _snapshot = snapshot;
    _query = query;
    _maxSuggestions = maxSuggestions;
    _completionMetadata = completionMetadata;
    _nodeRolesData = nodeRolesData;
    _referenceLibrary = referenceLibrary;
  }

  /**
   * The entry point for auto completion. Given the {@code grammar} and {@code query}, this function
   * will produce at most {@code maxSuggestions} suggestions based on other supplied details of the
   * network
   */
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
    return toAutoCompleteSuggestions(
        new ParboiledAutoComplete(
                grammar,
                parser.getInputRule(grammar),
                Parser.ANCHORS,
                network,
                snapshot,
                query,
                maxSuggestions,
                completionMetadata,
                nodeRolesData,
                referenceLibrary)
            .run());
  }

  Set<ParboiledAutoCompleteSuggestion> run() {
    Set<PotentialMatch> potentialMatches = getPotentialMatches(_query);

    return potentialMatches.stream()
        .map(this::autoCompletePotentialMatch)
        .flatMap(Collection::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  private Set<PotentialMatch> getPotentialMatches(String query) {
    /**
     * Before passing the query to the parser, we make it illegal by adding a funny, non-ascii
     * character (soccer ball :)). We will not get any errors backs if the string is legal.
     */
    String testQuery = query + new String(Character.toChars(ILLEGAL_CHAR));

    ParsingResult<AstNode> result = new ReportingParseRunner<AstNode>(_inputRule).run(testQuery);
    if (result.parseErrors.isEmpty()) {
      throw new IllegalStateException("Failed to force erroneous input");
    }

    InvalidInputError error = (InvalidInputError) result.parseErrors.get(0);

    return ParserUtils.getPotentialMatches(error, _completionTypes, false);
  }

  @VisibleForTesting
  Set<ParboiledAutoCompleteSuggestion> autoCompletePotentialMatch(PotentialMatch pm) {
    switch (pm.getAnchorType()) {
      case ADDRESS_GROUP_NAME:
        return autoCompleteReferenceBookEntity(pm);
      case ONE_APP_ICMP:
      case ONE_APP_ICMP_TYPE:
      case APP_ICMP_TYPE:
      case APP_ICMP_TYPE_CODE:
        // don't help with numbers
        return ImmutableSet.of();
      case APP_NAME:
        return autoCompleteAppName(pm);
      case APP_PORT:
      case APP_PORT_RANGE:
        // don't help with numbers
        return ImmutableSet.of();
      case CHAR_LITERAL:
        return autoCompleteLiteral(pm);
      case EOI:
        return ImmutableSet.of();
      case ENUM_SET_VALUE:
        return autoCompleteEnumSetValue(pm);
      case FILTER_INTERFACE_IN:
      case FILTER_INTERFACE_OUT:
        // Should delegate to interface spec
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case FILTER_NAME:
        return autoCompleteGeneric(pm, _grammar);
      case FILTER_NAME_REGEX:
        return ImmutableSet.of(
            new ParboiledAutoCompleteSuggestion(
                "", pm.getMatchPrefix().length() + pm.getMatchStartIndex(), FILTER_NAME_REGEX));
      case FILTER_PARENS:
        // Other filter rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case INTERFACE_GROUP_NAME:
        return autoCompleteReferenceBookEntity(pm);
      case INTERFACE_NAME:
        return autoCompleteInterfaceName(pm);
      case INTERFACE_NAME_REGEX:
        return ImmutableSet.of(
            new ParboiledAutoCompleteSuggestion(
                "", pm.getMatchPrefix().length() + pm.getMatchStartIndex(), INTERFACE_NAME_REGEX));
      case INTERFACE_PARENS:
        // Other interface rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case INTERFACE_TYPE:
      case INTERFACE_VRF:
      case INTERFACE_ZONE:
        // These rely on type, vrf, or zone completion that appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case IP_ADDRESS:
        return autoCompleteGeneric(pm, _grammar);
      case IP_ADDRESS_MASK:
        // can't help with masks
        return ImmutableSet.of();
      case IP_PROTOCOL_NAME:
        return autoCompleteIpProtocolName(pm);
      case IP_PROTOCOL_NUMBER:
        // don't help with numbers
        return ImmutableSet.of();
      case IP_PREFIX:
        return autoCompleteGeneric(pm, _grammar);
      case IP_RANGE:
        // Relies on IP_ADDRESS completion as it appears later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case IP_WILDCARD:
        // Relies on IP_ADDRESS and IP_ADDRESS_MASK completions as they appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case LOCATION_PARENS:
        // Other location rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case NAME_SET_NAME:
        return autoCompleteGeneric(pm, _grammar);
      case NODE_AND_INTERFACE:
        // Node or Interface based anchors should appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case NODE_NAME:
        return autoCompleteGeneric(pm, _grammar);
      case NODE_NAME_REGEX:
        return ImmutableSet.of(
            new ParboiledAutoCompleteSuggestion(
                "", pm.getMatchPrefix().length() + pm.getMatchStartIndex(), NODE_NAME_REGEX));
      case NODE_PARENS:
        // Other node rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case NODE_ROLE_AND_DIMENSION:
        // Role and dimension name rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case NODE_ROLE_DIMENSION_NAME:
        return autoCompleteGeneric(pm, _grammar);
      case NODE_ROLE_NAME:
        return autoCompleteNodeRoleName(pm);
      case NODE_TYPE:
        // Relies on STRING_LITERAL completion as it appears later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case REFERENCE_BOOK_AND_ADDRESS_GROUP:
      case REFERENCE_BOOK_AND_INTERFACE_GROUP:
        // Reference book name and address/interface group name should appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case REFERENCE_BOOK_NAME:
        return autoCompleteGeneric(pm, _grammar);
      case ROUTING_POLICY_NAME:
        return autoCompleteGeneric(pm, _grammar);
      case ROUTING_POLICY_NAME_REGEX:
        return ImmutableSet.of(
            new ParboiledAutoCompleteSuggestion(
                "",
                pm.getMatchPrefix().length() + pm.getMatchStartIndex(),
                ROUTING_POLICY_NAME_REGEX));
      case ROUTING_POLICY_PARENS:
        // Other routing policy rules appear later in the path
        throw new IllegalStateException(String.format("Unexpected auto completion for %s", pm));
      case STRING_LITERAL:
        return autoCompleteLiteral(pm);
      case VRF_NAME:
        return autoCompleteGeneric(pm, _grammar);
      case WHITESPACE:
        // nothing useful to suggest for these completion types
        return ImmutableSet.of();
      case ZONE_NAME:
        return autoCompleteGeneric(pm, _grammar);
      default:
        throw new IllegalArgumentException("Unhandled completion type " + pm.getAnchorType());
    }
  }

  /** Auto completes app name. */
  private Set<ParboiledAutoCompleteSuggestion> autoCompleteAppName(PotentialMatch pm) {
    return updateSuggestions(
        AutoCompleteUtils.stringAutoComplete(pm.getMatchPrefix(), CommonParser.namedApplications),
        false,
        Anchor.Type.APP_NAME,
        pm.getMatchStartIndex());
  }

  /** Auto completes enum set values. */
  private Set<ParboiledAutoCompleteSuggestion> autoCompleteEnumSetValue(PotentialMatch pm) {
    return updateSuggestions(
        AutoCompleteUtils.stringAutoComplete(
            pm.getMatchPrefix(),
            Grammar.getEnumValues(_grammar).stream()
                .map(Object::toString)
                .collect(ImmutableSet.toImmutableSet())),
        false,
        Anchor.Type.ENUM_SET_VALUE,
        pm.getMatchStartIndex());
  }

  /** Auto completes ip protocol names. */
  private Set<ParboiledAutoCompleteSuggestion> autoCompleteIpProtocolName(PotentialMatch pm) {
    return updateSuggestions(
        AutoCompleteUtils.stringAutoComplete(
            pm.getMatchPrefix(),
            Arrays.stream(IpProtocol.values())
                .map(Object::toString)
                .collect(ImmutableSet.toImmutableSet())),
        false,
        Anchor.Type.IP_PROTOCOL_NAME,
        pm.getMatchStartIndex());
  }

  private Set<ParboiledAutoCompleteSuggestion> autoCompleteLiteral(PotentialMatch pm) {
    Optional<PotentialMatch> extendedMatch = extendLiteralMatch(_query, pm);

    PotentialMatch pmToConsider = extendedMatch.orElse(pm);

    int anchorIndex = pmToConsider.getPath().indexOf(pmToConsider.getAnchor());
    checkArgument(anchorIndex != -1, "Anchor is not present in the path.");

    Optional<Anchor.Type> ancestorAnchor =
        IntStream.range(0, anchorIndex)
            .mapToObj(i -> pmToConsider.getPath().get(anchorIndex - i - 1).getAnchorType())
            .filter(Objects::nonNull)
            .findFirst();

    return ImmutableSet.of(
        new ParboiledAutoCompleteSuggestion(
            pm.getMatch() + extendedMatch.map(PotentialMatch::getMatch).orElse(""),
            pm.getMatchStartIndex(),
            ancestorAnchor.orElse(Anchor.Type.UNKNOWN)));
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

  private Set<ParboiledAutoCompleteSuggestion> autoCompleteGeneric(
      PotentialMatch pm, Grammar grammar) {
    String matchPrefix = unescapeIfNeeded(pm.getMatchPrefix(), pm.getAnchorType());
    List<AutocompleteSuggestion> suggestions =
        AutoCompleteUtils.autoComplete(
            _network,
            _snapshot,
            anchorTypeToVariableType(pm.getAnchorType(), grammar),
            matchPrefix,
            _maxSuggestions,
            _completionMetadata,
            _nodeRolesData,
            _referenceLibrary,
            false);
    return updateSuggestions(
        suggestions,
        !matchPrefix.equals(pm.getMatchPrefix()),
        pm.getAnchorType(),
        pm.getMatchStartIndex());
  }

  /**
   * Converts completion type to variable type for cases. Throws an exception when the mapping does
   * not exist
   */
  private static Variable.Type anchorTypeToVariableType(Anchor.Type anchorType, Grammar grammar) {
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
      case NAME_SET_NAME:
        return grammarToNameVariableType(grammar);
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

  /**
   * Returns the specific name variable type corresponding the grammar. The grammar is expected to
   * be one of the ones that use NameSetSpec as the entry point.
   */
  private static Variable.Type grammarToNameVariableType(Grammar grammar) {
    switch (grammar) {
      case MLAG_ID_SPECIFIER:
        return Variable.Type.MLAG_ID;
      default:
        throw new IllegalArgumentException(
            "Cannot determine name variable type for grammar " + grammar);
    }
  }

  /**
   * Auto completes names for interfaces. The completion is context sensitive if an ancestor {@link
   * PathElement} indicates that nodes appeared earlier in the path. Otherwise, context-independent
   * completion is used
   */
  @VisibleForTesting
  Set<ParboiledAutoCompleteSuggestion> autoCompleteInterfaceName(PotentialMatch pm) {

    Optional<String> nodeInput =
        findPrecedingInput(pm, _query, NODE_AND_INTERFACE, NODE_AND_INTERFACE_TAIL);

    return nodeInput.isPresent()
        ? autoCompleteInterfaceName(pm, nodeInput.get())
        : autoCompleteGeneric(pm, _grammar);
  }

  @VisibleForTesting
  Set<ParboiledAutoCompleteSuggestion> autoCompleteInterfaceName(
      PotentialMatch pm, String nodeInput) {
    NodeAstNode nodeAst = ParboiledNodeSpecifier.getAst(nodeInput);

    // do context sensitive auto completion only if input is a node name or regex
    if (!(nodeAst instanceof NameNodeAstNode) && !(nodeAst instanceof NameRegexNodeAstNode)) {
      return autoCompleteGeneric(pm, _grammar);
    }

    String interfaceNamePrefix = unescapeIfNeeded(pm.getMatchPrefix(), pm.getAnchorType());

    Set<String> candidateInterfaces =
        _completionMetadata.getInterfaces().stream()
            .filter(i -> nodeNameMatches(i.getHostname(), nodeAst))
            .map(NodeInterfacePair::getInterface)
            .collect(ImmutableSet.toImmutableSet());
    return updateSuggestions(
        AutoCompleteUtils.stringAutoComplete(interfaceNamePrefix, candidateInterfaces),
        !interfaceNamePrefix.equals(pm.getMatchPrefix()),
        Anchor.Type.INTERFACE_NAME,
        pm.getMatchStartIndex());
  }

  private static boolean nodeNameMatches(String nodeName, NodeAstNode nodeAst) {
    if (nodeAst instanceof NameNodeAstNode) {
      return nodeName.equalsIgnoreCase(((NameNodeAstNode) nodeAst).getName());
    } else if (nodeAst instanceof NameRegexNodeAstNode) {
      return ((NameRegexNodeAstNode) nodeAst).getPattern().matcher(nodeName).find();
    } else {
      throw new IllegalArgumentException("Can only match node names or regexes");
    }
  }

  /**
   * Auto completes node role names in a context sensitive manner if an ancestor {@link PathElement}
   * indicates that dimension name appeared earlier in the path. Otherwise, context-independent
   * completion is used
   */
  @VisibleForTesting
  Set<ParboiledAutoCompleteSuggestion> autoCompleteNodeRoleName(PotentialMatch pm) {
    Optional<String> roleDimensionInput =
        findPrecedingInput(pm, _query, NODE_ROLE_AND_DIMENSION, NODE_ROLE_AND_DIMENSION_TAIL);

    return roleDimensionInput
        .map(s -> autoCompleteNodeRoleName(pm, s))
        .orElseGet(() -> autoCompleteGeneric(pm, _grammar));
  }

  @VisibleForTesting
  Set<ParboiledAutoCompleteSuggestion> autoCompleteNodeRoleName(
      PotentialMatch pm, String roleDimensionInput) {
    NodeRoleDimension nodeRoleDimension =
        _nodeRolesData
            .nodeRoleDimensionFor(extractGroupingName(pm, roleDimensionInput))
            .orElse(NodeRoleDimension.builder("dummy").build());

    String matchPrefix = unescapeIfNeeded(pm.getMatchPrefix(), pm.getAnchorType());
    return updateSuggestions(
        AutoCompleteUtils.stringAutoComplete(
            matchPrefix, nodeRoleDimension.roleNamesFor(_completionMetadata.getNodes().keySet())),
        !matchPrefix.equals(pm.getMatchPrefix()),
        pm.getAnchorType(),
        pm.getMatchStartIndex());
  }

  /**
   * Auto completes names for reference book entities like address groups. The completion is context
   * sensitive if an ancestor {@link PathElement} indicates that reference book appeared earlier in
   * the path. Otherwise, context-independent completion is used
   */
  @VisibleForTesting
  Set<ParboiledAutoCompleteSuggestion> autoCompleteReferenceBookEntity(PotentialMatch pm) {
    Optional<String> refBookInput = findReferenceBookInput(pm, _query);

    Function<ReferenceBook, Set<String>> entityNameGetter = getEntityNameGetter(pm.getAnchorType());

    return refBookInput
        .map(r -> autoCompleteReferenceBookEntity(pm, r, entityNameGetter))
        .orElse(autoCompleteGeneric(pm, _grammar));
  }

  private static Optional<String> findReferenceBookInput(PotentialMatch pm, String query) {
    switch (pm.getAnchorType()) {
      case ADDRESS_GROUP_NAME:
        return findPrecedingInput(
            pm, query, REFERENCE_BOOK_AND_ADDRESS_GROUP, REFERENCE_BOOK_AND_ADDRESS_GROUP_TAIL);
      case INTERFACE_GROUP_NAME:
        return findPrecedingInput(
            pm, query, REFERENCE_BOOK_AND_INTERFACE_GROUP, REFERENCE_BOOK_AND_INTERFACE_GROUP_TAIL);
      default:
        throw new IllegalArgumentException("Unexpected anchor type " + pm.getAnchorType());
    }
  }

  private static Function<ReferenceBook, Set<String>> getEntityNameGetter(Anchor.Type anchorType) {
    switch (anchorType) {
      case ADDRESS_GROUP_NAME:
        return ParboiledAutoComplete::addressGroupGetter;
      case INTERFACE_GROUP_NAME:
        return ParboiledAutoComplete::interfaceGroupGetter;
      default:
        throw new IllegalArgumentException("Unexpected anchor type " + anchorType);
    }
  }

  @VisibleForTesting
  Set<ParboiledAutoCompleteSuggestion> autoCompleteReferenceBookEntity(
      PotentialMatch pm,
      String refBookInput,
      Function<ReferenceBook, Set<String>> entityNameGetter) {
    ReferenceBook refBook =
        _referenceLibrary
            .getReferenceBook(extractGroupingName(pm, refBookInput))
            .orElse(ReferenceBook.builder("empty").build());

    Set<String> candidateEntityNames = entityNameGetter.apply(refBook);

    String matchPrefix = unescapeIfNeeded(pm.getMatchPrefix(), pm.getAnchorType());
    return updateSuggestions(
        AutoCompleteUtils.stringAutoComplete(matchPrefix, candidateEntityNames),
        !matchPrefix.equals(pm.getMatchPrefix()),
        pm.getAnchorType(),
        pm.getMatchStartIndex());
  }

  /**
   * Finds the input between head and tail anchor types, walking walks backwards from the anchor.
   * The returned Optional is empty if the path does not contain the head anchor type.
   *
   * @throws IllegalArgumentException if anchor is not found on the path, or if headAnchorType is
   *     present but tailAnchorType is absent
   */
  @VisibleForTesting
  static Optional<String> findPrecedingInput(
      PotentialMatch pm, String query, Anchor.Type headAnchorType, Anchor.Type tailAnchorType) {
    int anchorIndex = pm.getPath().indexOf(pm.getAnchor());
    checkArgument(anchorIndex != -1, "Anchor is not present in the path.");

    Optional<PathElement> nodeStartElement =
        findFirstMatchingPathElement(pm, anchorIndex, headAnchorType);

    if (!nodeStartElement.isPresent()) {
      return Optional.empty();
    }

    Optional<PathElement> nodeEndElement =
        findFirstMatchingPathElement(pm, anchorIndex, tailAnchorType);
    checkArgument(
        nodeEndElement.isPresent(),
        "Tail anchor type '%s' not found for input '%s'",
        tailAnchorType,
        query);

    return Optional.of(
        query.substring(
            nodeStartElement.get().getStartIndex(), nodeEndElement.get().getStartIndex()));
  }

  /**
   * Scans the PotentialMatch path backwards, starting from {@code anchorIndex}, and returns the
   * first element that matches {@code anchorType}.
   */
  static Optional<PathElement> findFirstMatchingPathElement(
      PotentialMatch pm, int anchorIndex, Anchor.Type anchorType) {
    return IntStream.range(0, anchorIndex)
        .mapToObj(i -> pm.getPath().get(anchorIndex - i - 1))
        .filter(pe -> pe.getAnchorType() == anchorType)
        .findFirst();
  }

  /**
   * Extracts the dimension or reference book name from input that matches between their head and
   * tail anchors. For node dimension, the head and tail are NODE_ROLE_AND_DIMENSION(_TAIL). For
   * reference book, the head and tail REFERENCE_BOOK_AND_ADDRESS_GROUP(_TAIL), etc.
   *
   * <p>Based on our grammar structure, the function removes leading '(' and then trims whitepsace.
   */
  @VisibleForTesting
  private static String extractGroupingName(PotentialMatch pm, String groupInput) {
    checkArgument(
        groupInput.startsWith("("),
        "Open parens not found in input '%s' for '%s'",
        groupInput,
        pm.getAnchorType());
    return groupInput.substring(1).trim();
  }

  /**
   * Update suggestions obtained through {@link AutoCompleteUtils} to escape names if needed and
   * assign start index
   */
  @VisibleForTesting
  static @Nonnull Set<ParboiledAutoCompleteSuggestion> updateSuggestions(
      List<AutocompleteSuggestion> suggestions,
      boolean escape,
      Anchor.Type anchorType,
      int startIndex) {
    return suggestions.stream()
        .map(
            s ->
                new ParboiledAutoCompleteSuggestion(
                    escape || (isEscapableNameAnchor(anchorType) && nameNeedsEscaping(s.getText()))
                        ? ESCAPE_CHAR + s.getText() + ESCAPE_CHAR
                        : s.getText(),
                    Optional.ofNullable(s.getHint()).orElseGet(anchorType::getHint),
                    startIndex,
                    anchorType,
                    Optional.ofNullable(s.getDescription()).orElse(null)))
        .collect(ImmutableSet.toImmutableSet());
  }

  /** Unescapes {@code originalMatch} if it is of escapable type and is already escaped */
  private static @Nonnull String unescapeIfNeeded(String originalMatch, Anchor.Type anchorType) {
    if (isEscapableNameAnchor(anchorType) && originalMatch.startsWith(ESCAPE_CHAR)) {
      return originalMatch.substring(1);
    }
    return originalMatch;
  }
}

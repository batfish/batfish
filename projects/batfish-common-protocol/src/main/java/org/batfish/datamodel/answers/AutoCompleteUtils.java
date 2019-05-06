package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.batfish.common.CompletionMetadata;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.FlowState;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.BgpPeerPropertySpecifier;
import org.batfish.datamodel.questions.BgpProcessPropertySpecifier;
import org.batfish.datamodel.questions.ConfiguredSessionStatus;
import org.batfish.datamodel.questions.InterfacePropertySpecifier;
import org.batfish.datamodel.questions.IpsecSessionStatus;
import org.batfish.datamodel.questions.NamedStructureSpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.OspfPropertySpecifier;
import org.batfish.datamodel.questions.Variable;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.DispositionSpecifier;
import org.batfish.specifier.RoutingProtocolSpecifier;
import org.batfish.specifier.parboiled.Grammar;
import org.batfish.specifier.parboiled.ParboiledAutoComplete;

/** A utility class to generate auto complete suggestions for user input */
@ParametersAreNonnullByDefault
public final class AutoCompleteUtils {

  @Nonnull
  public static List<AutocompleteSuggestion> autoComplete(
      Variable.Type completionType, String query, int maxSuggestions) {
    return autoComplete(null, null, completionType, query, maxSuggestions, null, null, null);
  }

  @Nonnull
  public static List<AutocompleteSuggestion> autoComplete(
      @Nullable String network,
      @Nullable String snapshot,
      Variable.Type completionType,
      String query,
      int maxSuggestions,
      @Nullable CompletionMetadata completionMetadata,
      @Nullable NodeRolesData nodeRolesData,
      @Nullable ReferenceLibrary referenceLibrary) {
    return autoComplete(
        network,
        snapshot,
        completionType,
        query,
        maxSuggestions,
        completionMetadata,
        nodeRolesData,
        referenceLibrary,
        true);
  }

  /**
   * @param network name of network
   * @param snapshot name of snapshot
   * @param completionType completion type
   * @param query input query
   * @param maxSuggestions maximum number of suggestions returned
   * @param completionMetadata completion metadata
   * @param nodeRolesData node roles data
   * @param referenceLibrary reference library
   * @param fuzzyMatching if true will relax the input query to guarantee that suggestions are
   *     returned
   * @return a list of AutocompleteSuggestion
   */
  @Nonnull
  public static List<AutocompleteSuggestion> autoComplete(
      @Nullable String network,
      @Nullable String snapshot,
      Variable.Type completionType,
      String query,
      int maxSuggestions,
      @Nullable CompletionMetadata completionMetadata,
      @Nullable NodeRolesData nodeRolesData,
      @Nullable ReferenceLibrary referenceLibrary,
      boolean fuzzyMatching) {

    List<AutocompleteSuggestion> suggestions =
        getPotentialMatches(
            network,
            snapshot,
            completionType,
            query,
            maxSuggestions,
            completionMetadata,
            nodeRolesData,
            referenceLibrary);

    if (fuzzyMatching) {
      // If there are no suggestions, remove characters from the end of the query until there are
      // suggestions or the query is the empty string. This logic is done here to ensure that all
      // possible suggestions types have been considered before relaxing the query
      String relaxedQuery = query;
      while (relaxedQuery.length() > 0 && suggestions.isEmpty()) {
        relaxedQuery = relaxedQuery.substring(0, relaxedQuery.length() - 1);
        suggestions =
            getPotentialMatches(
                network,
                snapshot,
                completionType,
                relaxedQuery,
                maxSuggestions,
                completionMetadata,
                nodeRolesData,
                referenceLibrary);
      }
    }

    return orderSuggestions(query, suggestions);
  }

  /** Basic ordering logic, by suggestion type and then by suggestion text */
  @VisibleForTesting
  static List<AutocompleteSuggestion> orderSuggestions(
      String query, List<AutocompleteSuggestion> suggestions) {
    final LevenshteinDistance distance = new LevenshteinDistance();
    return suggestions.stream()
        .sorted(
            // first order by suggestion type
            Comparator.comparing(AutocompleteSuggestion::getSuggestionType)
                // then by (inverse of) common prefix length
                .thenComparing(
                    s ->
                        -1
                            * StringUtils.getCommonPrefix(
                                    query.toLowerCase(),
                                    (query.substring(0, s.getInsertionIndex()) + s.getText()))
                                .toLowerCase()
                                .length())
                // then by edit distance
                .thenComparing(
                    s ->
                        distance.apply(
                            query.toLowerCase(),
                            (query.substring(0, s.getInsertionIndex()) + s.getText())
                                .toLowerCase())))
        .collect(ImmutableList.toImmutableList());
  }

  @Nonnull
  private static List<AutocompleteSuggestion> getPotentialMatches(
      @Nullable String network,
      @Nullable String snapshot,
      Variable.Type completionType,
      String query,
      int maxSuggestions,
      @Nullable CompletionMetadata completionMetadata,
      @Nullable NodeRolesData nodeRolesData,
      @Nullable ReferenceLibrary referenceLibrary) {
    List<AutocompleteSuggestion> suggestions;

    try {
      switch (completionType) {
        case ADDRESS_GROUP_AND_BOOK:
          // deprecated -- left for now for backward compatibility
          suggestions = ImmutableList.of();
          break;
        case ADDRESS_GROUP_NAME:
          {
            checkReferenceLibrary(referenceLibrary, network);
            ImmutableSet<String> groups =
                referenceLibrary.getReferenceBooks().stream()
                    .map(
                        b ->
                            b.getAddressGroups().stream()
                                .map(ag -> ag.getName())
                                .collect(ImmutableSet.toImmutableSet()))
                    .flatMap(Collection::stream)
                    .collect(ImmutableSet.toImmutableSet());
            suggestions = stringAutoComplete(query, groups);
            break;
          }
        case APPLICATION_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.APPLICATION_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case BGP_PEER_PROPERTY_SPEC:
          {
            suggestions = baseAutoComplete(query, BgpPeerPropertySpecifier.JAVA_MAP.keySet());
            break;
          }
        case BGP_PROCESS_PROPERTY_SPEC:
          {
            suggestions = baseAutoComplete(query, BgpProcessPropertySpecifier.JAVA_MAP.keySet());
            break;
          }
        case BGP_SESSION_STATUS:
          {
            suggestions =
                baseAutoComplete(
                    query,
                    Stream.of(ConfiguredSessionStatus.values())
                        .map(ConfiguredSessionStatus::name)
                        .collect(Collectors.toSet()));
            break;
          }
        case BGP_SESSION_TYPE:
          {
            suggestions =
                baseAutoComplete(
                    query,
                    Stream.of(SessionType.values())
                        .map(SessionType::name)
                        .collect(Collectors.toSet()));

            break;
          }
        case DISPOSITION_SPEC:
          {
            suggestions = DispositionSpecifier.autoComplete(query);
            break;
          }
        case FILTER_NAME:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = stringAutoComplete(query, completionMetadata.getFilterNames());
            break;
          }
        case FILTER:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = baseAutoComplete(query, completionMetadata.getFilterNames());
            break;
          }
        case FILTER_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.FILTER_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case FLOW_STATE:
          {
            suggestions =
                baseAutoComplete(
                    query,
                    Stream.of(FlowState.values()).map(FlowState::name).collect(Collectors.toSet()));
            break;
          }
        case INTERFACE:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions =
                baseAutoComplete(
                    query,
                    completionMetadata.getInterfaces().stream()
                        .map(NodeInterfacePair::toString)
                        .collect(ImmutableSet.toImmutableSet()));
            break;
          }
        case INTERFACE_GROUP_AND_BOOK:
          // deprecated -- left for now for backward compatibility
          suggestions = ImmutableList.of();
          break;
        case INTERFACE_GROUP_NAME:
          {
            checkReferenceLibrary(referenceLibrary, network);
            ImmutableSet<String> groups =
                referenceLibrary.getReferenceBooks().stream()
                    .flatMap(b -> b.getInterfaceGroups().stream().map(g -> g.getName()))
                    .collect(ImmutableSet.toImmutableSet());
            suggestions = stringAutoComplete(query, groups);
            break;
          }
        case INTERFACE_NAME:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions =
                stringAutoComplete(
                    query,
                    completionMetadata.getInterfaces().stream()
                        .map(NodeInterfacePair::getInterface)
                        .collect(Collectors.toSet()));
            break;
          }
        case INTERFACE_TYPE:
          {
            suggestions =
                stringAutoComplete(
                    query,
                    Arrays.stream(InterfaceType.values())
                        .map(type -> type.toString())
                        .collect(ImmutableSet.toImmutableSet()));
            break;
          }
        case INTERFACES_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.INTERFACE_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case INTERFACE_PROPERTY_SPEC:
          {
            suggestions = baseAutoComplete(query, InterfacePropertySpecifier.JAVA_MAP.keySet());
            break;
          }
        case IP:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = stringAutoComplete(query, completionMetadata.getIps());
            break;
          }
        case IP_PROTOCOL_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.IP_PROTOCOL_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case IP_SPACE_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.IP_SPACE_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case IPSEC_SESSION_STATUS:
          {
            suggestions =
                baseAutoComplete(
                    query,
                    Stream.of(IpsecSessionStatus.values())
                        .map(IpsecSessionStatus::name)
                        .collect(Collectors.toSet()));
            break;
          }
        case LOCATION_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.LOCATION_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case NAMED_STRUCTURE_SPEC:
          {
            suggestions = baseAutoComplete(query, NamedStructureSpecifier.JAVA_MAP.keySet());
            break;
          }
        case NODE_NAME:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = stringAutoComplete(query, completionMetadata.getNodes());
            break;
          }
        case NODE_PROPERTY_SPEC:
          {
            suggestions = baseAutoComplete(query, NodePropertySpecifier.JAVA_MAP.keySet());
            break;
          }
        case NODE_ROLE_AND_DIMENSION:
          // deprecated -- left for now for backward compatibility
          suggestions = ImmutableList.of();
          break;
        case NODE_ROLE_DIMENSION:
          // deprecated -- left for now for backward compatibility
          suggestions = ImmutableList.of();
          break;
        case NODE_ROLE_DIMENSION_NAME:
          {
            checkNodeRolesData(nodeRolesData, network);
            suggestions =
                stringAutoComplete(
                    query,
                    nodeRolesData.getNodeRoleDimensions().stream()
                        .map(NodeRoleDimension::getName)
                        .collect(Collectors.toSet()));
            break;
          }
        case NODE_ROLE_NAME:
          {
            checkNodeRolesData(nodeRolesData, network);
            ImmutableSet<String> roles =
                nodeRolesData.getNodeRoleDimensions().stream()
                    .flatMap(d -> d.getRoles().stream().map(r -> r.getName()))
                    .collect(ImmutableSet.toImmutableSet());
            suggestions = stringAutoComplete(query, roles);
            break;
          }
        case NODE_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.NODE_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case OSPF_PROPERTY_SPEC:
          {
            suggestions = baseAutoComplete(query, OspfPropertySpecifier.JAVA_MAP.keySet());
            break;
          }
        case PREFIX:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = stringAutoComplete(query, completionMetadata.getPrefixes());
            break;
          }
        case PROTOCOL:
          {
            suggestions =
                baseAutoComplete(
                    query,
                    Stream.of(Protocol.values()).map(Protocol::name).collect(Collectors.toSet()));
            break;
          }
        case REFERENCE_BOOK_NAME:
          {
            checkReferenceLibrary(referenceLibrary, network);
            suggestions =
                stringAutoComplete(
                    query,
                    referenceLibrary.getReferenceBooks().stream()
                        .map(ReferenceBook::getName)
                        .collect(Collectors.toSet()));
            break;
          }
        case ROUTING_POLICY_NAME:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = stringAutoComplete(query, completionMetadata.getRoutingPolicyNames());
            break;
          }
        case ROUTING_POLICY_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.ROUTING_POLICY_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case ROUTING_PROTOCOL_SPEC:
          {
            suggestions = RoutingProtocolSpecifier.autoComplete(query);
            break;
          }
        case STRUCTURE_NAME:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = baseAutoComplete(query, completionMetadata.getStructureNames());
            break;
          }
        case VRF:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = baseAutoComplete(query, completionMetadata.getVrfs());
            break;
          }
        case ZONE:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = baseAutoComplete(query, completionMetadata.getZones());
            break;
          }
        default:
          throw new IllegalArgumentException("Unsupported completion type: " + completionType);
      }
    } catch (Exception e) {
      // if any error occurs, just return an empty list
      return ImmutableList.of();
    }

    return suggestions.subList(0, Integer.min(suggestions.size(), maxSuggestions));
  }

  /**
   * Returns a list of suggestions based on the query. The current implementation treats the query
   * as a substring of the property string.
   *
   * <p>TODO: Get rid of this method in favor of methods below. Stop doing implicit regexes.
   */
  @Nonnull
  public static List<AutocompleteSuggestion> baseAutoComplete(
      @Nullable String query, Set<String> allProperties) {

    String finalQuery = firstNonNull(query, "").toLowerCase();
    ImmutableList.Builder<AutocompleteSuggestion> suggestions = new ImmutableList.Builder<>();
    String queryWithStars = ".*" + (finalQuery.isEmpty() ? "" : finalQuery + ".*");
    Pattern queryPattern = safeGetPattern(queryWithStars);

    /*
     * if queryWithStars is not a valid Pattern, finalQuery must be a funky string that will not
     * match anything as string.contains or regex.matches; so we skip formalities altogether
     */
    if (queryPattern != null) {
      suggestions.addAll(
          allProperties.stream()
              .filter(prop -> queryPattern.matcher(prop.toLowerCase()).matches())
              .map(prop -> new AutocompleteSuggestion(prop, false))
              .collect(Collectors.toList()));
    }
    return suggestions.build();
  }

  /**
   * Returns a list of suggestions based on query strings.
   *
   * <p>The search is case-insensitive and looks for a substring match.
   */
  @Nonnull
  public static List<AutocompleteSuggestion> stringAutoComplete(
      @Nullable String query, Set<String> strings) {

    String testQuery = query == null ? "" : query.toLowerCase();

    return strings.stream()
        .filter(s -> s.toLowerCase().contains(testQuery))
        .map(s -> new AutocompleteSuggestion(s, false))
        .collect(ImmutableList.toImmutableList());
  }

  private static void checkCompletionMetadata(
      @Nullable CompletionMetadata completionMetadata,
      @Nullable String network,
      @Nullable String snapshot) {
    checkArgument(
        completionMetadata != null,
        "Cannot autocomplete because completion metadata not found for %s / %s",
        network,
        snapshot);
  }

  private static void checkNodeRolesData(
      @Nullable NodeRolesData nodeRolesData, @Nullable String network) {
    checkArgument(
        nodeRolesData != null,
        "Cannot autocomplete because node roles data not found for %s",
        network);
  }

  private static void checkReferenceLibrary(
      @Nullable ReferenceLibrary referenceLibrary, @Nullable String network) {
    checkArgument(
        referenceLibrary != null,
        "Cannot autocomplete because reference library not found for %s",
        network);
  }

  /** Returns the Pattern if {@code candidateRegex} is a valid regex, and null otherwise */
  @Nullable
  private static Pattern safeGetPattern(String candidateRegex) {
    try {
      return Pattern.compile(candidateRegex);
    } catch (PatternSyntaxException e) {
      return null;
    }
  }
}

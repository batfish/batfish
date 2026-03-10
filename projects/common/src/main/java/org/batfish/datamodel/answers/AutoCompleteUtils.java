package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Streams;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.batfish.common.CompletionMetadata;
import org.batfish.common.autocomplete.IpCompletionMetadata;
import org.batfish.common.autocomplete.IpCompletionRelevance;
import org.batfish.common.autocomplete.LocationCompletionMetadata;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixTrieMultiMap;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.answers.AutocompleteSuggestion.SuggestionType;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.Variable;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.DispositionSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.ToSpecifierString;
import org.batfish.specifier.parboiled.ParboiledAutoComplete;

/** A utility class to generate auto complete suggestions for user input */
@ParametersAreNonnullByDefault
public final class AutoCompleteUtils {

  private static final int MAX_SUGGESTIONS_PER_TYPE = 5;

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
  public static @Nonnull List<AutocompleteSuggestion> autoComplete(
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
      while (!relaxedQuery.isEmpty() && suggestions.isEmpty()) {
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

    return limitSuggestionsByType(
        orderSuggestions(query, suggestions), maxSuggestions, MAX_SUGGESTIONS_PER_TYPE);
  }

  /** Basic ordering logic, by suggestion type, rank, and then by suggestion text */
  @VisibleForTesting
  static List<AutocompleteSuggestion> orderSuggestions(
      String query, List<AutocompleteSuggestion> suggestions) {
    final LevenshteinDistance distance = new LevenshteinDistance();
    return suggestions.stream()
        .sorted(
            // first order by suggestion type
            Comparator.comparing(AutocompleteSuggestion::getSuggestionType)
                // then rank within the same type
                .thenComparing(AutocompleteSuggestion::getRank)
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

  /**
   * Limits the number of suggestions per type.
   *
   * @param orderedSuggestions original ordered list of suggestions
   * @param maxSuggestions the maximum number of suggestions
   * @param maxSuggestionsPerType the maximum number of suggestions for a given type
   */
  @VisibleForTesting
  static List<AutocompleteSuggestion> limitSuggestionsByType(
      List<AutocompleteSuggestion> orderedSuggestions,
      int maxSuggestions,
      int maxSuggestionsPerType) {
    if (orderedSuggestions.size() <= maxSuggestions) {
      return orderedSuggestions;
    }

    Map<SuggestionType, Integer> suggestionTypeCounts = new TreeMap<>();

    List<AutocompleteSuggestion> limitedSuggestions = new ArrayList<>();
    List<AutocompleteSuggestion> leftOverSuggestions = new ArrayList<>();

    for (AutocompleteSuggestion suggestion : orderedSuggestions) {
      if (limitedSuggestions.size() == maxSuggestions) {
        return limitedSuggestions;
      }

      Integer count = suggestionTypeCounts.getOrDefault(suggestion.getSuggestionType(), 0);

      if (count < maxSuggestionsPerType) {
        limitedSuggestions.add(suggestion);
        suggestionTypeCounts.put(suggestion.getSuggestionType(), count + 1);
      } else {
        leftOverSuggestions.add(suggestion);
      }
    }

    int suggestionsToAdd = maxSuggestions - limitedSuggestions.size();

    if (suggestionsToAdd > 0) {
      // add remaining suggestions from leftovers
      limitedSuggestions.addAll(
          leftOverSuggestions.subList(
              0, Integer.min(suggestionsToAdd, leftOverSuggestions.size())));
    }

    return limitedSuggestions;
  }

  private static @Nonnull List<AutocompleteSuggestion> getPotentialMatches(
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
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.BGP_PEER_PROPERTY_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case BGP_PROCESS_PROPERTY_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.BGP_PROCESS_PROPERTY_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case BGP_ROUTE_STATUS_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.BGP_ROUTE_STATUS_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case BGP_SESSION_COMPAT_STATUS_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.BGP_SESSION_COMPAT_STATUS_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case BGP_SESSION_STATUS_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.BGP_SESSION_STATUS_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case BGP_SESSION_TYPE_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.BGP_SESSION_TYPE_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case DEPRECATED_FLOW_STATE:
          {
            suggestions = baseAutoComplete(query, ImmutableSet.of("NEW", "ESTABLISHED", "RELATED"));
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
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.INTERFACE_PROPERTY_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case IP:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = ipStringAutoComplete(query, completionMetadata.getIps());
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
            checkCompletionMetadata(completionMetadata, network, snapshot);
            // first, get the suggestions based on IP metadata
            List<AutocompleteSuggestion> metadataSuggestions =
                ipStringAutoComplete(query, completionMetadata.getIps());
            Set<String> metadataSuggestionTexts =
                metadataSuggestions.stream()
                    .map(s -> s.getText())
                    .collect(ImmutableSet.toImmutableSet());

            // then, get grammar-based suggestions
            List<AutocompleteSuggestion> grammarSuggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.IP_SPACE_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);

            // merge the suggestions
            suggestions =
                Streams.concat(
                        metadataSuggestions.stream(),
                        grammarSuggestions.stream()
                            .filter(s -> !metadataSuggestionTexts.contains(s.getText())))
                    .collect(ImmutableList.toImmutableList());
            break;
          }
        case IPSEC_SESSION_STATUS_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.IPSEC_SESSION_STATUS_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
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
        case MLAG_ID:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = stringAutoComplete(query, completionMetadata.getMlagIds());
            break;
          }
        case MLAG_ID_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.MLAG_ID_SPECIFIER,
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
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.NAMED_STRUCTURE_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case NODE_NAME:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            Map<String, Optional<String>> nodesWithDescriptions =
                toImmutableMap(
                    completionMetadata.getNodes(),
                    Entry::getKey,
                    entry -> Optional.ofNullable(entry.getValue().getHumanName()));
            suggestions = stringAutoComplete(query, nodesWithDescriptions);
            break;
          }
        case NODE_PROPERTY_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.NODE_PROPERTY_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
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
            suggestions = stringAutoComplete(query, nodeRolesData.toNodeRoleDimensions().keySet());
            break;
          }
        case NODE_ROLE_NAME:
          {
            checkNodeRolesData(nodeRolesData, network);
            ImmutableSet<String> roles =
                nodeRolesData.toNodeRoleDimensions().values().stream()
                    .flatMap(d -> d.roleNamesFor(completionMetadata.getNodes().keySet()).stream())
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
        case OSPF_INTERFACE_PROPERTY_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.OSPF_INTERFACE_PROPERTY_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case OSPF_PROCESS_PROPERTY_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.OSPF_PROCESS_PROPERTY_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case OSPF_SESSION_STATUS_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.OSPF_SESSION_STATUS_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
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
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.ROUTING_PROTOCOL_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case SINGLE_APPLICATION_SPEC:
          {
            suggestions =
                ParboiledAutoComplete.autoComplete(
                    Grammar.SINGLE_APPLICATION_SPECIFIER,
                    network,
                    snapshot,
                    query,
                    maxSuggestions,
                    completionMetadata,
                    nodeRolesData,
                    referenceLibrary);
            break;
          }
        case SOURCE_LOCATION:
          {
            suggestions = autoCompleteSourceLocation(query, false, completionMetadata);
            break;
          }
        case STRUCTURE_NAME:
          {
            checkCompletionMetadata(completionMetadata, network, snapshot);
            suggestions = baseAutoComplete(query, completionMetadata.getStructureNames());
            break;
          }
        case TRACEROUTE_SOURCE_LOCATION:
          {
            suggestions = autoCompleteSourceLocation(query, true, completionMetadata);
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
    return suggestions;
  }

  /**
   * Returns a list of loactions that the match the query. The query can match on either the
   * location (node, interface) or the human name of the node. When tracerouteSource is false,
   * "natural" source locations (per location info) with IPs are considered. Otherwise, traceroute
   * sources are considered. In the latter mode, natural sources are ranked higher.
   */
  public static @Nonnull List<AutocompleteSuggestion> autoCompleteSourceLocation(
      String query, boolean tracerouteSource, @Nullable CompletionMetadata completionMetadata) {
    checkNotNull(
        completionMetadata, "Cannot autocomplete source locations without completion metadata");
    checkNotNull(
        completionMetadata.getLocations(),
        "cannot autocomplete source locations without location metadata");
    List<AutocompleteSuggestion> sourceSuggestions =
        stringAutoComplete(query, getLocationsWithHumanNames(false, completionMetadata), 1);
    if (!tracerouteSource) {
      return sourceSuggestions;
    }
    List<AutocompleteSuggestion> tracerouteSourceSuggestions =
        stringAutoComplete(query, getLocationsWithHumanNames(true, completionMetadata), 2);
    return Streams.concat(sourceSuggestions.stream(), tracerouteSourceSuggestions.stream())
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns a map from location to the (optional) human name of its node. When tracerouteSource is
   * false, "natural" source locations (per location info) with IPs are considered. Otherwise,
   * traceroute sources that are not source location with IPs are considered.
   */
  private static Map<String, Optional<String>> getLocationsWithHumanNames(
      boolean tracerouteSource, CompletionMetadata completionMetadata) {
    return (tracerouteSource
            ? completionMetadata.getLocations().stream()
                .filter(loc -> loc.isTracerouteSource() && !loc.isSourceWithIps())
            : completionMetadata.getLocations().stream()
                .filter(LocationCompletionMetadata::isSourceWithIps))
        .map(LocationCompletionMetadata::getLocation)
        .collect(
            ImmutableMap.toImmutableMap(
                ToSpecifierString::toSpecifierString,
                location ->
                    Optional.ofNullable(
                        completionMetadata.getNodes().get(location.getNodeName()).getHumanName())));
  }

  /**
   * Returns a list of suggestions based on the query. The current implementation treats the query
   * as a substring of the property string.
   *
   * <p>TODO: Get rid of this method in favor of methods below. Stop doing implicit regexes.
   */
  public static @Nonnull List<AutocompleteSuggestion> baseAutoComplete(
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
  public static @Nonnull List<AutocompleteSuggestion> stringAutoComplete(
      @Nullable String query, Set<String> strings) {

    String testQuery = query == null ? "" : query.toLowerCase();

    return strings.stream()
        .filter(s -> s.toLowerCase().contains(testQuery))
        .map(s -> new AutocompleteSuggestion(s, false))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns a list of suggestions based on query strings.
   *
   * <p>The search is case-insensitive and looks for a substring match.
   */
  @VisibleForTesting
  static @Nonnull List<AutocompleteSuggestion> stringAutoComplete(
      @Nullable String query, Map<String, Optional<String>> stringsWithDescriptions) {
    return stringAutoComplete(query, stringsWithDescriptions, AutocompleteSuggestion.DEFAULT_RANK);
  }

  /**
   * Returns a list of suggestions based on query strings.
   *
   * <p>The search is case-insensitive and looks for a substring match.
   */
  private static @Nonnull List<AutocompleteSuggestion> stringAutoComplete(
      @Nullable String query, Map<String, Optional<String>> stringsWithDescriptions, int rank) {

    String testQuery = query == null ? "" : query.toLowerCase();

    return stringsWithDescriptions.entrySet().stream()
        .filter(
            s ->
                s.getKey().toLowerCase().contains(testQuery)
                    || s.getValue()
                        .map(hint -> hint.toLowerCase().contains(testQuery))
                        .orElse(false))
        .map(s -> new AutocompleteSuggestion(s.getKey(), false, s.getValue().orElse(null), rank))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns a list of suggestions based on a query for Ips.
   *
   * <p>The query string may be an IP (e.g., "10.") or another string altogether (e.g., "nod"). The
   * method first matches on IPs (and includes all of its {@link IpCompletionMetadata} as hint and
   * then matches on {@link IpCompletionMetadata} (and includes only matching {@link
   * IpCompletionRelevance} in hints).
   */
  public static @Nonnull ImmutableList<AutocompleteSuggestion> ipStringAutoComplete(
      @Nullable String query, PrefixTrieMultiMap<IpCompletionMetadata> ips) {

    String testQuery = query == null ? "" : query.toLowerCase();

    // when the query has multiple words, each of those words should match
    String[] subQueries = testQuery.split("\\s+");

    RangeSet<Ip> allIps =
        ImmutableRangeSet.of(Range.closed(Prefix.ZERO.getStartIp(), Prefix.ZERO.getEndIp()));

    // find matching IP entries
    Map<Ip, IpCompletionMetadata> ipMatches =
        ips.getOverlappingEntries(allIps)
            .filter(e -> e.getKey().getPrefixLength() == Prefix.MAX_PREFIX_LENGTH)
            .filter(
                e -> {
                  String ipStr = e.getKey().getStartIp().toString();
                  return Arrays.stream(subQueries).allMatch(ipStr::contains);
                })
            .collect(
                ImmutableMap.toImmutableMap(
                    e -> e.getKey().getStartIp(),
                    // invariant: value set is non-empty. could have multiple elements, but this
                    // code will only use one.
                    e -> e.getValue().iterator().next()));

    // find relevance matches
    List<AutocompleteSuggestion> relevanceMatches =
        ips.getOverlappingEntries(allIps)
            .filter(
                e ->
                    e.getKey().getPrefixLength() == Prefix.MAX_PREFIX_LENGTH
                        && !ipMatches.containsKey(e.getKey().getStartIp()))
            .map(
                e -> {
                  Ip ip = e.getKey().getStartIp();
                  return new SimpleEntry<>(
                      ip,
                      e.getValue().stream()
                          .flatMap(metadata -> metadata.getRelevances().stream())
                          .filter(r -> r.matches(subQueries, ip))
                          .collect(ImmutableList.toImmutableList()));
                })
            .filter(e -> !e.getValue().isEmpty())
            .map(e -> toAutocompleteSuggestion(e.getKey(), e.getValue()))
            .collect(ImmutableList.toImmutableList());

    return new ImmutableList.Builder<AutocompleteSuggestion>()
        .addAll(
            ipMatches.entrySet().stream()
                .map(
                    entry ->
                        toAutocompleteSuggestion(entry.getKey(), entry.getValue().getRelevances()))
                .collect(ImmutableList.toImmutableList()))
        .addAll(relevanceMatches)
        .build();
  }

  public static AutocompleteSuggestion toAutocompleteSuggestion(
      Ip ip, List<IpCompletionRelevance> relevances) {
    return AutocompleteSuggestion.builder()
        .setText(ip.toString())
        .setDescription(toDescription(relevances))
        .setSuggestionType(SuggestionType.ADDRESS_LITERAL)
        .build();
  }

  @VisibleForTesting
  static @Nullable String toDescription(List<IpCompletionRelevance> relevances) {
    if (relevances.isEmpty()) {
      return null;
    }
    if (relevances.size() == 1) {
      return relevances.get(0).getDisplay();
    }
    return String.format("%s ... (%d more)", relevances.get(0).getDisplay(), relevances.size() - 1);
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
  private static @Nullable Pattern safeGetPattern(String candidateRegex) {
    try {
      return Pattern.compile(candidateRegex);
    } catch (PatternSyntaxException e) {
      return null;
    }
  }
}

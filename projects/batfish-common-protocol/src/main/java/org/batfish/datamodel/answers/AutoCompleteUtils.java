package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.OspfPropertySpecifier;
import org.batfish.datamodel.questions.Variable;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.DispositionSpecifier;
import org.batfish.specifier.IpProtocolSpecifier;
import org.batfish.specifier.RoutingProtocolSpecifier;
import org.batfish.specifier.parboiled.ParboiledAutoComplete;

@ParametersAreNonnullByDefault
public final class AutoCompleteUtils {

  private static class StringPair {
    public final String s1;
    public final String s2;

    public StringPair(String s1, String s2) {
      this.s1 = s1;
      this.s2 = s2;
    }
  }

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
    List<AutocompleteSuggestion> suggestions;

    switch (completionType) {
      case ADDRESS_GROUP_AND_BOOK:
        {
          checkReferenceLibrary(referenceLibrary, network);
          ImmutableSet<StringPair> pairs =
              referenceLibrary.getReferenceBooks().stream()
                  .map(
                      b ->
                          b.getAddressGroups().stream()
                              .map(ag -> new StringPair(ag.getName(), b.getName()))
                              .collect(ImmutableSet.toImmutableSet()))
                  .flatMap(Collection::stream)
                  .collect(ImmutableSet.toImmutableSet());
          suggestions = stringPairAutoComplete(query, pairs);
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
                      .collect(Collectors.toSet()));
          break;
        }
      case INTERFACE_GROUP_AND_BOOK:
        {
          checkReferenceLibrary(referenceLibrary, network);
          ImmutableSet<StringPair> pairs =
              referenceLibrary.getReferenceBooks().stream()
                  .map(
                      b ->
                          b.getInterfaceGroups().stream()
                              .map(ag -> new StringPair(ag.getName(), b.getName()))
                              .collect(ImmutableSet.toImmutableSet()))
                  .flatMap(Collection::stream)
                  .collect(ImmutableSet.toImmutableSet());
          suggestions = stringPairAutoComplete(query, pairs);
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
              ParboiledAutoComplete.autoCompleteInterface(
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
          suggestions = IpProtocolSpecifier.autoComplete(query);
          break;
        }
      case IP_SPACE_SPEC:
        {
          suggestions =
              ParboiledAutoComplete.autoCompleteIpSpace(
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
        {
          checkNodeRolesData(nodeRolesData, network);
          ImmutableSet<StringPair> pairs =
              nodeRolesData.getNodeRoleDimensions().stream()
                  .map(
                      d ->
                          d.getRoles().stream()
                              .map(r -> new StringPair(r.getName(), d.getName()))
                              .collect(ImmutableSet.toImmutableSet()))
                  .flatMap(Collection::stream)
                  .collect(ImmutableSet.toImmutableSet());
          suggestions = stringPairAutoComplete(query, pairs);
          break;
        }
      case NODE_ROLE_DIMENSION:
        {
          checkNodeRolesData(nodeRolesData, network);
          suggestions =
              baseAutoComplete(
                  query,
                  nodeRolesData.getNodeRoleDimensions().stream()
                      .map(NodeRoleDimension::getName)
                      .collect(Collectors.toSet()));
          break;
        }
      case NODE_SPEC:
        {
          checkCompletionMetadata(completionMetadata, network, snapshot);
          checkNodeRolesData(nodeRolesData, network);
          suggestions =
              NodesSpecifier.autoComplete(query, completionMetadata.getNodes(), nodeRolesData);
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
   * <p>The search is case-insensitive
   *
   * <p>The returned list contains the tail of the match. If the query is "ab" and the matching
   * string is "abcd", "cd" is returned.
   */
  @Nonnull
  public static List<AutocompleteSuggestion> stringAutoComplete(
      @Nullable String query, Set<String> strings) {

    // remove whitespace from the query
    String testQuery = query == null ? "" : query.toLowerCase();

    return strings.stream()
        .filter(s -> s.toLowerCase().startsWith(testQuery))
        .map(s -> new AutocompleteSuggestion(s.substring(testQuery.length()), false))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns a list of suggestions based on query over pairs of names (strings).
   *
   * <p>The pairs are converted to "a,b" lowercase strings and the query is considered to be a
   * prefix over those strings. We assume that neither "a" not "b" contain whitespace, consistent
   * with valid names per {@link ReferenceLibrary#NAME_PATTERN}.
   *
   * <p>The returned list contains the tail of the matching pair. If the query is "aa" and a
   * matching pair is "aa,bb", ",bb" is returned.
   */
  @Nonnull
  private static List<AutocompleteSuggestion> stringPairAutoComplete(
      @Nullable String query, Set<StringPair> pairs) {

    // remove whitespace from the query
    String testQuery = query == null ? "" : query.replaceAll("\\s+", "").toLowerCase();

    return pairs.stream()
        .map(p -> String.join(",", p.s1, p.s2).toLowerCase())
        .filter(p -> p.startsWith(testQuery))
        .map(p -> new AutocompleteSuggestion(p.substring(testQuery.length()), false))
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

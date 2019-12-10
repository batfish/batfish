package org.batfish.question;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.Function;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AuthenticationKeyChain;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.questions.INodeRegexQuestion;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

@AutoService(Plugin.class)
public class CompareSameNameQuestionPlugin extends QuestionPlugin {

  public static final String DEBUG_FLAG_ASSUME_ALL_UNIQUE = "compareSameName.assumeAllUnique";

  public static class CompareSameNameAnswerElement extends AnswerElement {
    private static final String PROP_EQUIVALENCE_SETS_MAP = "equivalenceSetsMap";
    private static final String PROP_NODES = "nodes";

    /** Equivalence sets are keyed by classname */
    private SortedMap<String, NamedStructureEquivalenceSets<?>> _equivalenceSets;

    private Set<String> _nodes;

    @JsonCreator
    public CompareSameNameAnswerElement(
        @JsonProperty(PROP_EQUIVALENCE_SETS_MAP)
            SortedMap<String, NamedStructureEquivalenceSets<?>> equivalenceSets,
        @JsonProperty(PROP_NODES) Set<String> nodes) {
      _equivalenceSets = firstNonNull(equivalenceSets, new TreeMap<>());
      _nodes = nodes;
    }

    public void add(String className, NamedStructureEquivalenceSets<?> sets) {
      _equivalenceSets.put(className, sets);
    }

    @JsonProperty(PROP_EQUIVALENCE_SETS_MAP)
    public SortedMap<String, NamedStructureEquivalenceSets<?>> getEquivalenceSets() {
      return _equivalenceSets;
    }

    @JsonProperty(PROP_NODES)
    public Set<String> getNodes() {
      return _nodes;
    }
  }

  public static class CompareSameNameAnswerer extends Answerer {

    private CompareSameNameAnswerElement _answerElement;

    private Map<String, Configuration> _configurations;

    private CompareSameNameQuestion _csnQuestion;

    private Set<String> _nodes;

    private boolean _assumeAllUnique;

    public CompareSameNameAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    private <T> void add(
        Class<T> structureClass, Function<Configuration, Map<String, T>> structureMapRetriever) {
      String structType = structureClass.getSimpleName().toLowerCase();
      if ((_csnQuestion.getNamedStructTypes().isEmpty()
              && !(_csnQuestion.getExcludedNamedStructTypes().contains(structType)))
          || _csnQuestion.getNamedStructTypes().contains(structType)) {
        _answerElement.add(
            structureClass.getSimpleName(),
            processStructures(structureClass, _nodes, _configurations, structureMapRetriever));
      }
    }

    @Override
    public CompareSameNameAnswerElement answer(NetworkSnapshot snapshot) {
      _assumeAllUnique = _batfish.debugFlagEnabled(DEBUG_FLAG_ASSUME_ALL_UNIQUE);
      _configurations = _batfish.loadConfigurations(snapshot);
      _csnQuestion = (CompareSameNameQuestion) _question;
      _nodes = _csnQuestion.getNodeRegex().getMatchingNodes(_batfish, snapshot);
      _answerElement = new CompareSameNameAnswerElement(null, _nodes);

      add(AsPathAccessList.class, Configuration::getAsPathAccessLists);
      add(AuthenticationKeyChain.class, Configuration::getAuthenticationKeyChains);
      add(CommunityList.class, Configuration::getCommunityLists);
      add(IkePhase1Key.class, Configuration::getIkePhase1Keys);
      add(IkePhase1Policy.class, Configuration::getIkePhase1Policies);
      add(IkePhase1Proposal.class, Configuration::getIkePhase1Proposals);
      add(Interface.class, Configuration::getAllInterfaces);
      add(Ip6AccessList.class, Configuration::getIp6AccessLists);
      add(IpAccessList.class, Configuration::getIpAccessLists);
      add(IpsecPhase2Policy.class, Configuration::getIpsecPhase2Policies);
      add(IpsecPhase2Proposal.class, Configuration::getIpsecPhase2Proposals);
      add(IpsecPeerConfig.class, Configuration::getIpsecPeerConfigs);
      add(Route6FilterList.class, Configuration::getRoute6FilterLists);
      add(RouteFilterList.class, Configuration::getRouteFilterLists);
      add(RoutingPolicy.class, Configuration::getRoutingPolicies);
      add(Vrf.class, Configuration::getVrfs);
      add(Zone.class, Configuration::getZones);

      return _answerElement;
    }

    private boolean ignored(String structName) {
      return !_csnQuestion.getCompareGenerated() && structName.startsWith("~");
    }

    private <T> NamedStructureEquivalenceSets<T> processStructures(
        Class<T> structureClass,
        Set<String> hostnames,
        Map<String, Configuration> configurations,
        Function<Configuration, Map<String, T>> structureMapRetriever) {
      String structureClassName = structureClass.getSimpleName();
      // collect the set of all names for structures of type T, across all nodes
      Set<String> allNames =
          hostnames.stream()
              .map(configurations::get)
              .map(structureMapRetriever)
              .flatMap(structureMap -> structureMap.keySet().stream())
              .filter(structName -> !ignored(structName))
              .collect(ImmutableSet.toImmutableSet());
      NamedStructureEquivalenceSets.Builder<T> builder =
          NamedStructureEquivalenceSets.builder(structureClassName);
      for (String hostname : hostnames) {
        Configuration node = configurations.get(hostname);
        Map<String, T> structureMap = structureMapRetriever.apply(node);
        for (String structName : allNames) {
          T struct = structureMap.get(structName);
          if (struct != null || _csnQuestion.getMissing()) {
            builder.addEntry(structName, hostname, struct, _assumeAllUnique);
          }
        }
      }
      NamedStructureEquivalenceSets<T> ae = builder.build();
      if (!_csnQuestion.getSingletons()) {
        ae.clean();
      }
      return ae;
    }

    public void setAssumeAllUnique(boolean assumeAllUnique) {
      _assumeAllUnique = assumeAllUnique;
    }
  }

  /**
   * Compares named structures with identical names across multiple nodes.
   *
   * <p>Named structures refer to constructs like route-maps and access-control lists. Often,
   * identical functionality is needed on multiple routers and it is common to have the same name
   * for those structures across routers. We compare the contents of structures with the same name
   * across different routers. When the contents of a same-named structure differ across routers, it
   * usually indicates a configuration error. For instance, if the ACL named
   * ``\verb|block_non_http_ssh|'' has identical content on nine out of ten routers, but is
   * different in the tenth router, the ACL is likely misconfigured on the tenth router.
   */
  public static final class CompareSameNameQuestion extends Question implements INodeRegexQuestion {
    private static final String PROP_COMPARE_GENERATED = "compareGenerated";
    private static final String PROP_EXCLUDED_NAMED_STRUCT_TYPES = "excludedNamedStructTypes";
    private static final String PROP_MISSING = "missing";
    private static final String PROP_NAMED_STRUCT_TYPES = "namedStructTypes";
    private static final String PROP_NODE_REGEX = "nodeRegex";
    private static final String PROP_SINGLETONS = "singletons";

    /** Whether to also compare auto-generated structures */
    private boolean _compareGenerated;

    /**
     * Exclude structures of this type.
     *
     * <p>Default value is [Interface, Vrf] because these named structure types seem to be less
     * useful and have many entries that slow down the computation considerably.
     */
    private SortedSet<String> _excludedNamedStructTypes;

    /**
     * Whether to create an equivalence class for nodes that are missing a structure of a given
     * name.
     */
    private boolean _missing;

    /**
     * Set of structure types to analyze drawn from ( AsPathAccessList, * AuthenticationKeyChain,
     * CommunityList, IkePhase1Policy, IkePhase1Proposal, IkePhase1Key, Interface, * Ip6AccessList,
     * IpAccessList, IpsecPhase2Policy, IpsecPhase2Proposal, IpsecPeerConfig, Route6FilterList, *
     * RouteFilterList, RoutingPolicy, Vrf, Zone )
     *
     * <p>Default value is '[]', which denotes all types except those in excludedNamedStructTypes.
     */
    private SortedSet<String> _namedStructTypes;

    /** The set of nodes over which to run the analysis */
    private NodesSpecifier _nodeRegex;

    /** Whether to include named structures for which there is only one equivalence class. */
    private boolean _singletons;

    @JsonCreator
    public CompareSameNameQuestion(
        @JsonProperty(PROP_COMPARE_GENERATED) Boolean compareGenerated,
        @JsonProperty(PROP_EXCLUDED_NAMED_STRUCT_TYPES) SortedSet<String> excludedNamedStructTypes,
        @JsonProperty(PROP_MISSING) Boolean missing,
        @JsonProperty(PROP_NAMED_STRUCT_TYPES) SortedSet<String> namedStructTypes,
        @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex,
        @JsonProperty(PROP_SINGLETONS) Boolean singletons) {
      _compareGenerated = firstNonNull(compareGenerated, false);
      _excludedNamedStructTypes =
          toLowerCase(
              firstNonNull(
                  excludedNamedStructTypes,
                  Arrays.asList(Interface.class.getSimpleName(), Vrf.class.getSimpleName())));
      _missing = firstNonNull(missing, false);
      _namedStructTypes = toLowerCase(firstNonNull(namedStructTypes, ImmutableSortedSet.of()));
      _nodeRegex = firstNonNull(nodeRegex, NodesSpecifier.ALL);
      _singletons = firstNonNull(singletons, false);
    }

    @JsonProperty(PROP_COMPARE_GENERATED)
    public boolean getCompareGenerated() {
      return _compareGenerated;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @JsonProperty(PROP_EXCLUDED_NAMED_STRUCT_TYPES)
    public SortedSet<String> getExcludedNamedStructTypes() {
      return _excludedNamedStructTypes;
    }

    @JsonProperty(PROP_MISSING)
    public boolean getMissing() {
      return _missing;
    }

    @Override
    public String getName() {
      return "comparesamename";
    }

    @JsonProperty(PROP_NAMED_STRUCT_TYPES)
    public SortedSet<String> getNamedStructTypes() {
      return _namedStructTypes;
    }

    @Override
    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_SINGLETONS)
    public boolean getSingletons() {
      return _singletons;
    }

    @Override
    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier regex) {
      _nodeRegex = regex;
    }

    private SortedSet<String> toLowerCase(Collection<String> names) {
      return names.stream()
          .map(String::toLowerCase)
          .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
    }
  }

  @Override
  protected CompareSameNameAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new CompareSameNameAnswerer(question, batfish);
  }

  @Override
  protected CompareSameNameQuestion createQuestion() {
    return new CompareSameNameQuestion(null, null, null, null, null, null);
  }
}

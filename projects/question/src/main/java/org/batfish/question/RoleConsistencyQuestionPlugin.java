package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSet;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.collections.NamedStructureOutlierSet;
import org.batfish.datamodel.collections.OutlierSet;
import org.batfish.datamodel.questions.INodeRegexQuestion;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.role.RoleConsistencyHypothesis;

@AutoService(Plugin.class)
public class RoleConsistencyQuestionPlugin extends QuestionPlugin {

  public static class RoleConsistencyAnswerElement implements AnswerElement {

    private static final String PROP_ROLE_SPECIFIER = "roleSpecifier";

    private static final String PROP_NAME = "name";

    private SortedSet<OutlierSet<NavigableSet<String>>> _outliers;

    public RoleConsistencyAnswerElement() {
      _namedStructureRoleConsistency = new TreeSet<>();
      _serverRoleConsistency = new TreeSet<>();
    }

    @JsonProperty(PROP_NAMED_STRUCTURE_OUTLIERS)
    public SortedSet<NamedStructureOutlierSet<?>> getNamedStructureRoleConsistency() {
      return _namedStructureRoleConsistency;
    }

    @JsonProperty(PROP_SERVER_OUTLIERS)
    public SortedSet<OutlierSet<NavigableSet<String>>> getServerRoleConsistency() {
      return _serverRoleConsistency;
    }

    @Override
    public String prettyPrint() {
      if (_namedStructureRoleConsistency.size() == 0 && _serverRoleConsistency.size() == 0) {
        return "";
      }

      StringBuilder sb = new StringBuilder("Results for outliers\n");

      for (OutlierSet<?> outlier : _serverRoleConsistency) {
        sb.append("  Hypothesis: every node should have the following set of ");
        sb.append(outlier.getName() + ": " + outlier.getDefinition() + "\n");
        sb.append("  RoleConsistency: ");
        sb.append(outlier.getRoleConsistency() + "\n");
        sb.append("  Conformers: ");
        sb.append(outlier.getConformers() + "\n\n");
      }

      for (NamedStructureOutlierSet<?> outlier : _namedStructureRoleConsistency) {
        switch (outlier.getHypothesis()) {
          case SAME_DEFINITION:
            sb.append(
                "  Hypothesis: every "
                    + outlier.getStructType()
                    + " named "
                    + outlier.getName()
                    + " should have the same definition\n");
            break;
          case SAME_NAME:
            sb.append("  Hypothesis:");
            if (outlier.getNamedStructure() != null) {
              sb.append(" every ");
            } else {
              sb.append(" no ");
            }
            sb.append(
                "node should define a "
                    + outlier.getStructType()
                    + " named "
                    + outlier.getName()
                    + "\n");
            break;
          default:
            throw new BatfishException("Unexpected hypothesis" + outlier.getHypothesis());
        }
        sb.append("  RoleConsistency: ");
        sb.append(outlier.getRoleConsistency() + "\n");
        sb.append("  Conformers: ");
        sb.append(outlier.getConformers() + "\n\n");
      }
      return sb.toString();
    }

    @JsonProperty(PROP_NAMED_STRUCTURE_OUTLIERS)
    public void setNamedStructureRoleConsistency(
        SortedSet<NamedStructureOutlierSet<?>> namedStructureRoleConsistency) {
      _namedStructureRoleConsistency = namedStructureRoleConsistency;
    }

    @JsonProperty(PROP_SERVER_OUTLIERS)
    public void setServerRoleConsistency(SortedSet<OutlierSet<NavigableSet<String>>> serverRoleConsistency) {
      _serverRoleConsistency = serverRoleConsistency;
    }
  }

  public static class RoleConsistencyAnswerer extends Answerer {

    private RoleConsistencyAnswerElement _answerElement;

    private Map<String, Configuration> _configurations;

    // the node names that match the question's node regex
    private Set<String> _nodes;

    // only report outliers that represent this percentage or less of
    // the total number of nodes
    private static double OUTLIERS_THRESHOLD = 0.34;

    // if this flag is true, report all outliers, even those that exceed the threshold above,
    // and including situations when there are zero outliers
    private boolean _verbose;

    public RoleConsistencyAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    private <T> void addNamedStructureRoleConsistency(
        RoleConsistencyHypothesis hypothesis,
        NamedStructureEquivalenceSets<T> equivSet,
        SortedSet<NamedStructureOutlierSet<?>> rankedRoleConsistency) {
      String structType = equivSet.getStructureClassName();
      for (Map.Entry<String, SortedSet<NamedStructureEquivalenceSet<T>>> entry :
          equivSet.getSameNamedStructures().entrySet()) {
        String name = entry.getKey();
        SortedSet<NamedStructureEquivalenceSet<T>> eClasses = entry.getValue();
        NamedStructureEquivalenceSet<T> max =
            eClasses
                .stream()
                .max(Comparator.comparingInt(es -> es.getNodes().size()))
                .orElseThrow(
                    () ->
                        new BatfishException(
                            "Named structure " + name + " has no equivalence classes"));
        SortedSet<String> conformers = max.getNodes();

        SortedSet<String> outliers =
            eClasses
                .stream()
                .filter(eClass -> eClass != max)
                .flatMap(eClass -> eClass.getNodes().stream())
                .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
        rankedRoleConsistency.add(
            new NamedStructureOutlierSet<>(
                hypothesis, structType, name, max.getNamedStructure(), conformers, outliers));
      }
    }

    private <T> void addPropertyRoleConsistency(
        String name, Function<Configuration, T> accessor, SortedSet<OutlierSet<T>> rankedRoleConsistency) {

      // partition the nodes into equivalence classes based on their values for the
      // property of interest
      Map<T, SortedSet<String>> equivSets = new HashMap<>();
      for (String node : _nodes) {
        T definition = accessor.apply(_configurations.get(node));
        SortedSet<String> matchingNodes = equivSets.getOrDefault(definition, new TreeSet<>());
        matchingNodes.add(node);
        equivSets.put(definition, matchingNodes);
      }

      // the equivalence class of the largest size is treated as the one whose value is
      // hypothesized to be the correct one
      Map.Entry<T, SortedSet<String>> max =
          equivSets
              .entrySet()
              .stream()
              .max(Comparator.comparingInt(e -> e.getValue().size()))
              .orElseThrow(
                  () -> new BatfishException("Set " + name + " has no equivalence classes"));
      SortedSet<String> conformers = max.getValue();
      T definition = max.getKey();
      equivSets.remove(definition);
      SortedSet<String> outliers = new TreeSet<>();
      for (SortedSet<String> nodes : equivSets.values()) {
        outliers.addAll(nodes);
      }
      if (_verbose || isWithinThreshold(conformers, outliers)) {
        rankedRoleConsistency.add(new OutlierSet<T>(name, definition, conformers, outliers));
      }
    }

    @Override
    public RoleConsistencyAnswerElement answer() {

      RoleConsistencyQuestion question = (RoleConsistencyQuestion) _question;
      _answerElement = new RoleConsistencyAnswerElement();

      _configurations = _batfish.loadConfigurations();
      _nodes = question.getNodeRegex().getMatchingNodes(_configurations);
      _verbose = question.getVerbose();

      switch (question.getHypothesis()) {
        case SAME_DEFINITION:
        case SAME_NAME:
          SortedSet<NamedStructureOutlierSet<?>> outliers = namedStructureRoleConsistency(question);
          _answerElement.setNamedStructureRoleConsistency(outliers);
          break;
        case SAME_SERVERS:
          _answerElement.setServerRoleConsistency(serverRoleConsistency());
          break;
        default:
          throw new BatfishException(
              "Unexpected outlier detection hypothesis " + question.getHypothesis());
      }

      return _answerElement;
    }

    // check that there is at least one outlier, but also that the fraction of outliers
    // does not exceed our threshold for reporting
    private static boolean isWithinThreshold(
        SortedSet<String> conformers, SortedSet<String> outliers) {
      double cSize = conformers.size();
      double oSize = outliers.size();
      return oSize > 0 && (oSize / (cSize + oSize)) <= OUTLIERS_THRESHOLD;
    }

    private SortedSet<NamedStructureOutlierSet<?>> namedStructureRoleConsistency(
        RoleConsistencyQuestion question) {

      // first get the results of compareSameName
      CompareSameNameQuestionPlugin.CompareSameNameQuestion inner =
          new CompareSameNameQuestionPlugin.CompareSameNameQuestion();
      inner.setNodeRegex(question.getNodeRegex());
      inner.setNamedStructTypes(question.getNamedStructTypes());
      inner.setExcludedNamedStructTypes(new TreeSet<>());
      inner.setSingletons(true);
      CompareSameNameQuestionPlugin.CompareSameNameAnswerer innerAnswerer =
          new CompareSameNameQuestionPlugin().createAnswerer(inner, _batfish);
      CompareSameNameQuestionPlugin.CompareSameNameAnswerElement innerAnswer =
          innerAnswerer.answer();

      SortedMap<String, NamedStructureEquivalenceSets<?>> equivalenceSets =
          innerAnswer.getEquivalenceSets();

      RoleConsistencyHypothesis hypothesis = question.getHypothesis();
      switch (hypothesis) {
        case SAME_DEFINITION:
          // nothing to do before ranking outliers
          break;
        case SAME_NAME:
          // create at most two equivalence classes for each name:
          // one containing the nodes that have a structure of that name,
          // and one containing the nodes that don't have a structure of that name
          for (NamedStructureEquivalenceSets<?> eSets : equivalenceSets.values()) {
            toNameOnlyEquivalenceSets(eSets, innerAnswer.getNodes());
          }
          break;
        default:
          throw new BatfishException("Default case of switch should be unreachable");
      }

      SortedSet<NamedStructureOutlierSet<?>> outliers =
          rankNamedStructureRoleConsistency(hypothesis, equivalenceSets);

      // remove outlier sets where the hypothesis is that a particular named structure
      // should *not* exist (this  happens when more nodes lack such a structure than contain
      // such a structure).  such hypotheses do not seem to be useful in general.
      outliers.removeIf(oset -> oset.getNamedStructure() == null);

      if (!_verbose) {
        // remove outlier sets that don't meet our threshold
        outliers.removeIf(oset -> !isWithinThreshold(oset.getConformers(), oset.getRoleConsistency()));
      }
      return outliers;
    }

    private SortedSet<OutlierSet<NavigableSet<String>>> serverRoleConsistency() {
      SortedSet<OutlierSet<NavigableSet<String>>> rankedRoleConsistency = new TreeSet<>();
      addPropertyRoleConsistency("DnsServers", Configuration::getDnsServers, rankedRoleConsistency);
      addPropertyRoleConsistency("LoggingServers", Configuration::getLoggingServers, rankedRoleConsistency);
      addPropertyRoleConsistency("NtpServers", Configuration::getNtpServers, rankedRoleConsistency);
      addPropertyRoleConsistency("SnmpTrapServers", Configuration::getSnmpTrapServers, rankedRoleConsistency);
      addPropertyRoleConsistency("TacacsServers", Configuration::getTacacsServers, rankedRoleConsistency);

      return rankedRoleConsistency;
    }

    /**
     * Use the results of CompareSameName to partition nodes into those containing a structure of a
     * given name and those lacking such a structure. This information will later be used to test
     * the sameName hypothesis.
     */
    private <T> void toNameOnlyEquivalenceSets(
        NamedStructureEquivalenceSets<T> eSets, Set<String> nodes) {
      ImmutableSortedMap.Builder<String, SortedSet<NamedStructureEquivalenceSet<T>>> newESetsMap =
          new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());
      for (Map.Entry<String, SortedSet<NamedStructureEquivalenceSet<T>>> entry :
          eSets.getSameNamedStructures().entrySet()) {
        SortedSet<NamedStructureEquivalenceSet<T>> eSetSets = entry.getValue();
        SortedSet<String> presentNodes =
            eSetSets
                .stream()
                .flatMap(eSet -> eSet.getNodes().stream())
                .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
        T struct = eSetSets.first().getNamedStructure();

        SortedSet<String> absentNodes =
            ImmutableSortedSet.copyOf(Sets.difference(ImmutableSet.copyOf(nodes), presentNodes));
        NamedStructureEquivalenceSet<T> presentSet =
            new NamedStructureEquivalenceSet<T>(presentNodes.first(), struct);
        presentSet.setNodes(presentNodes);
        String name = entry.getKey();
        ImmutableSortedSet.Builder<NamedStructureEquivalenceSet<T>> eqSetsBuilder =
            new ImmutableSortedSet.Builder<>(Comparator.naturalOrder());
        eqSetsBuilder.add(presentSet);
        if (!absentNodes.isEmpty()) {
          NamedStructureEquivalenceSet<T> absentSet =
              new NamedStructureEquivalenceSet<T>(absentNodes.first(), null);
          absentSet.setNodes(absentNodes);
          eqSetsBuilder.add(absentSet);
        }
        newESetsMap.put(name, eqSetsBuilder.build());
      }
      eSets.setSameNamedStructures(newESetsMap.build());
    }

    /* a simple first approach to detect and rank outliers:
     * compute the z-score (see Engler's 2001 paper on detecting outliers) for each
     * <structure type, name> pair, based on a hypothesis that the equivalence class
     * with the largest number of elements is correct and the property equivalence classes
     * represent bugs
     */
    private SortedSet<NamedStructureOutlierSet<?>> rankNamedStructureRoleConsistency(
        RoleConsistencyHypothesis hypothesis,
        SortedMap<String, NamedStructureEquivalenceSets<?>> equivSets) {
      SortedSet<NamedStructureOutlierSet<?>> rankedRoleConsistency = new TreeSet<>();
      for (NamedStructureEquivalenceSets<?> entry : equivSets.values()) {
        addNamedStructureRoleConsistency(hypothesis, entry, rankedRoleConsistency);
      }
      return rankedRoleConsistency;
    }
  }

  // <question_page_comment>
  /**
   * Checks a role-based consistency policy requiring that all nodes of the same role have the
   * same value for some particular configuration property (e.g., DnsServers).
   *
   * @type RoleConsistency multifile
   * @param nodeRoleSpecifier A NodeRoleSpecifier that specifies the role(s) of each node.
   *
   * @param name A string representing the name of the configuration property to check.  Allowed
   *             values are DnsServers, LoggingServers, NtpServers, SnmpServers, TacacsServers.
   */
  public static final class RoleConsistencyQuestion extends Question implements INodeRegexQuestion {

    private static final String PROP_ROLE_SPECIFIER = "roleSpecifier";

    private static final String PROP_NAME = "name";

    private NodeRoleSpecifier _roleSpecifier;

    private String _name;

    public RoleConsistencyQuestion() {
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @JsonProperty(PROP_HYPOTHESIS)
    public RoleConsistencyHypothesis getHypothesis() {
      return _hypothesis;
    }

    @Override
    public String getName() {
      return "outliers";
    }

    @JsonProperty(PROP_NAMED_STRUCT_TYPES)
    public SortedSet<String> getNamedStructTypes() {
      return _namedStructTypes;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_VERBOSE)
    public boolean getVerbose() {
      return _verbose;
    }

    @JsonProperty(PROP_HYPOTHESIS)
    public void setHypothesis(RoleConsistencyHypothesis hypothesis) {
      _hypothesis = hypothesis;
    }

    @JsonProperty(PROP_NAMED_STRUCT_TYPES)
    public void setNamedStructTypes(SortedSet<String> namedStructTypes) {
      _namedStructTypes = namedStructTypes;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier regex) {
      _nodeRegex = regex;
    }

    @JsonProperty(PROP_VERBOSE)
    public void setVerbose(boolean verbose) {
      _verbose = verbose;
    }
  }

  @Override
  protected RoleConsistencyAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new RoleConsistencyAnswerer(question, batfish);
  }

  @Override
  protected RoleConsistencyQuestion createQuestion() {
    return new RoleConsistencyQuestion();
  }
}

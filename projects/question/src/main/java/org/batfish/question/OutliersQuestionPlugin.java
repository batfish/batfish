package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSet;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.collections.NamedStructureOutlierSet;
import org.batfish.datamodel.questions.INodeRegexQuestion;
import org.batfish.datamodel.questions.Question;

public class OutliersQuestionPlugin extends QuestionPlugin {

  public enum Hypothesis {
    SAME_DEFINITION("sameDefinition"), SAME_NAME("sameName");

    private static final Map<String, OutliersQuestionPlugin.Hypothesis> _map = buildMap();

    private static synchronized Map<String, OutliersQuestionPlugin.Hypothesis> buildMap() {
      Map<String, OutliersQuestionPlugin.Hypothesis> map = new HashMap<>();
      for (OutliersQuestionPlugin.Hypothesis value : OutliersQuestionPlugin.Hypothesis.values()) {
        String name = value._name;
        map.put(name, value);
      }
      return Collections.unmodifiableMap(map);
    }

    @JsonCreator public static OutliersQuestionPlugin.Hypothesis fromName(String name) {
      OutliersQuestionPlugin.Hypothesis instance = _map.get(name);
      if (instance == null) {
        throw new BatfishException(
            "No " + OutliersQuestionPlugin.Hypothesis.class.getSimpleName() + " with name: '" + name
                + "'");
      }
      return instance;
    }

    private final String _name;

    private Hypothesis(String name) {
      _name = name;
    }

    @JsonValue public String hypothesisName() {
      return _name;
    }
  }

  public static class OutliersAnswerElement implements AnswerElement {

    private static final String PROP_RANKED_OUTLIERS = "rankedOutliers";

    private SortedSet<NamedStructureOutlierSet<?>> _rankedOutliers;

    public OutliersAnswerElement() {}

    @JsonProperty(PROP_RANKED_OUTLIERS)
    public SortedSet<NamedStructureOutlierSet<?>> getRankedOutliers() {
      return _rankedOutliers;
    }

    @Override
    public String prettyPrint() {
      StringBuilder sb = new StringBuilder("Results for outliers\n");
      for (NamedStructureOutlierSet<?> outlier : _rankedOutliers) {
        sb.append(outlier.getStructType() + " named " + outlier.getName() + ":\n");
        sb.append(outlier.getOutliers() + "\n");
      }
      return sb.toString();
    }

    @JsonProperty(PROP_RANKED_OUTLIERS)
    public void setRankedOutliers(SortedSet<NamedStructureOutlierSet<?>> rankedOutliers) {
      _rankedOutliers = rankedOutliers;
    }
  }

  public static class OutliersAnswerer extends Answerer {

    private OutliersAnswerElement _answerElement;

    public OutliersAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }


    private <T> void addOutliers(
        NamedStructureEquivalenceSets<T> equivSet,
        SortedSet<NamedStructureOutlierSet<?>> rankedOutliers) {
      String structType = equivSet.getStructureClassName();
      for (Map.Entry<String, SortedSet<NamedStructureEquivalenceSet<T>>> entry :
          equivSet.getSameNamedStructures().entrySet()) {
        String name = entry.getKey();
        SortedSet<NamedStructureEquivalenceSet<T>> eClasses = entry.getValue();
        NamedStructureEquivalenceSet<T> max =
            eClasses
                .stream()
                .max(Comparator.comparingInt(es -> es.getNodes().size()))
                .orElseThrow(() -> new BatfishException(
                    "Named structure " + name + " has no equivalence classes"));
        SortedSet<String> conformers = max.getNodes();
        eClasses.remove(max);
        SortedSet<String> outliers = new TreeSet<>();
        for (NamedStructureEquivalenceSet<T> eClass : eClasses) {
          outliers.addAll(eClass.getNodes());
        }
        rankedOutliers.add(
            new NamedStructureOutlierSet<>(
                structType, name, max.getNamedStructure(), conformers, outliers));
      }
    }

    @Override
    public OutliersAnswerElement answer() {

      OutliersQuestion question = (OutliersQuestion) _question;
      _answerElement = new OutliersAnswerElement();

      // first get the results of compareSameName
      CompareSameNameQuestionPlugin.CompareSameNameQuestion inner =
          new CompareSameNameQuestionPlugin.CompareSameNameQuestion();
      inner.setNodeRegex(question.getNodeRegex());
      inner.setNamedStructTypes(question.getNamedStructTypes());
      inner.setSingletons(true);
      CompareSameNameQuestionPlugin.CompareSameNameAnswerer innerAnswerer =
          new CompareSameNameQuestionPlugin().createAnswerer(inner, _batfish);
      CompareSameNameQuestionPlugin.CompareSameNameAnswerElement innerAnswer =
          innerAnswerer.answer();

      SortedMap<String, NamedStructureEquivalenceSets<?>> equivalenceSets =
          innerAnswer.getEquivalenceSets();

      switch (question.getHypothesis()) {
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

      for (NamedStructureEquivalenceSets<?> eSets : equivalenceSets.values()) {
        eSets.clean();
      }

      _answerElement.setRankedOutliers(rankOutliers(equivalenceSets));

      return _answerElement;
    }

    private <T> void toNameOnlyEquivalenceSets(NamedStructureEquivalenceSets<T> eSets,
        List<String> nodes) {
      SortedMap<String, SortedSet<NamedStructureEquivalenceSet<T>>> newESetsMap =
          new TreeMap<>();
      for (Map.Entry<String, SortedSet<NamedStructureEquivalenceSet<T>>> entry :
          eSets.getSameNamedStructures().entrySet()) {
        SortedSet<String> presentNodes = new TreeSet<>();
        for (NamedStructureEquivalenceSet<T> eSet : entry.getValue()) {
          presentNodes.addAll(eSet.getNodes());
        }
        SortedSet<String> absentNodes = new TreeSet<>(nodes);
        absentNodes.removeAll(presentNodes);
        SortedSet<NamedStructureEquivalenceSet<T>> newESets = new TreeSet<>();
        NamedStructureEquivalenceSet<T> presentSet =
            new NamedStructureEquivalenceSet<T>(presentNodes.first());
        presentSet.setNodes(presentNodes);
        newESets.add(presentSet);
        if (absentNodes.size() > 0) {
          NamedStructureEquivalenceSet<T> absentSet =
              new NamedStructureEquivalenceSet<T>(absentNodes.first());
          absentSet.setNodes(absentNodes);
          newESets.add(absentSet);
        }
        newESetsMap.put(entry.getKey(), newESets);
      }
      eSets.setSameNamedStructures(newESetsMap);
    }

    /* a simple first approach to detect and rank outliers:
     * compute the z-score (see Engler's 2001 paper on detecting outliers) for each
     * <structure type, name> pair, based on a hypothesis that the equivalence class
     * with the largest number of elements is correct and the other equivalence classes
     * represent bugs
     */
    private SortedSet<NamedStructureOutlierSet<?>> rankOutliers(
        SortedMap<String, NamedStructureEquivalenceSets<?>> equivSets) {
      SortedSet<NamedStructureOutlierSet<?>> rankedOutliers = new TreeSet<>();
      for (NamedStructureEquivalenceSets<?> entry : equivSets.values()) {
        addOutliers(entry, rankedOutliers);
      }
      return rankedOutliers;
    }
  }

  // <question_page_comment>
  /**
   * Detects and ranks outliers based on differences in named structures.
   *
   * <p>If many nodes have a structure of a given name and a few do not, this may indicate an error.
   * If many nodes have a structure named N whose definition is identical, and a few nodes have a
   * structure named N that is defined differently, this may indicate an error. This question
   * leverages this intuition to find outliers, based on the results of the CompareSameName
   * question.
   *
   * @type InferRoles multifile
   * @param namedStructTypes Set of structure types to analyze drawn from ( AsPathAccessList,
   *     CommunityList, IkeGateway, IkePolicies, IkeProposal, IpAccessList, IpsecPolicy,
   *     IpsecProposal, IpsecVpn, RouteFilterList, RoutingPolicy) Default value is '[]' (which
   *     denotes all structure types).
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @param hypothesis A string that indicates the hypothesis being used to identify outliers.
   *     "sameDefinition" indicates a hypothesis that same-named structures should have identical
   *     definitions.  "sameName" indicates a hypothesis that all nodes should have structures of
   *     the same names.  Default is "sameDefinition".
   */
  public static final class OutliersQuestion extends Question implements INodeRegexQuestion {

    private static final String PROP_HYPOTHESIS = "hypothesis";

    private static final String PROP_NAMED_STRUCT_TYPES = "namedStructTypes";

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private Hypothesis _hypothesis;

    private SortedSet<String> _namedStructTypes;

    private String _nodeRegex;

    public OutliersQuestion() {
      _namedStructTypes = new TreeSet<>();
      _nodeRegex = ".*";
      _hypothesis = Hypothesis.SAME_DEFINITION;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @JsonProperty(PROP_HYPOTHESIS)
    public Hypothesis getHypothesis() {
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

    @Override
    @JsonProperty(PROP_NODE_REGEX)
    public String getNodeRegex() {
      return _nodeRegex;
    }

    @Override
    public boolean getTraffic() {
      return false;
    }

    @JsonProperty(PROP_HYPOTHESIS)
    public void setHypothesis(Hypothesis hypothesis) {
      _hypothesis = hypothesis;
    }

    @JsonProperty(PROP_NAMED_STRUCT_TYPES)
    public void setNamedStructTypes(SortedSet<String> namedStructTypes) {
      _namedStructTypes = namedStructTypes;
    }

    @Override
    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(String regex) {
      _nodeRegex = regex;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new OutliersAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new OutliersQuestion();
  }
}

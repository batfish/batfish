package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSet;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.collections.NamedStructureOutlierSet;
import org.batfish.datamodel.questions.INodeRegexQuestion;
import org.batfish.datamodel.questions.Question;

public class OutliersQuestionPlugin extends QuestionPlugin {

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
                .max((es1, es2) -> Integer.compare(es1.getNodes().size(), es2.getNodes().size()))
                .get();
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
      inner.setMissing(question.getMissing());
      CompareSameNameQuestionPlugin.CompareSameNameAnswerer innerAnswerer =
          new CompareSameNameQuestionPlugin().createAnswerer(inner, _batfish);
      CompareSameNameQuestionPlugin.CompareSameNameAnswerElement innerAnswer =
          innerAnswerer.answer();

      SortedMap<String, NamedStructureEquivalenceSets<?>> equivalenceSets =
          innerAnswer.getEquivalenceSets();

      _answerElement.setRankedOutliers(rankOutliers(equivalenceSets));

      return _answerElement;
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
   * @param missing Whether to consider nodes that lack a particular named structure when doing
   *     outlier detection for that named structure. Default is false.
   */
  public static final class OutliersQuestion extends Question implements INodeRegexQuestion {

    private static final String PROP_MISSING = "missing";

    private static final String PROP_NAMED_STRUCT_TYPES = "namedStructTypes";

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private boolean _missing;

    private SortedSet<String> _namedStructTypes;

    private String _nodeRegex;

    public OutliersQuestion() {
      _namedStructTypes = new TreeSet<>();
      _nodeRegex = ".*";
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @JsonProperty(PROP_MISSING)
    public boolean getMissing() {
      return _missing;
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

    @JsonProperty(PROP_MISSING)
    public void setMissing(boolean missing) {
      _missing = missing;
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

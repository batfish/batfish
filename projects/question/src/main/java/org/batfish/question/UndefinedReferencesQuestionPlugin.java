package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class UndefinedReferencesQuestionPlugin extends QuestionPlugin {

  public static class UndefinedReferencesAnswerElement extends AnswerElement {

    private static final String PROP_UNDEFINED_REFERENCES = "undefinedReferences";

    private SortedMap<
            String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
        _undefinedReferences;

    public UndefinedReferencesAnswerElement() {
      _undefinedReferences = new TreeMap<>();
      setSummary(new AnswerSummary());
    }

    @JsonProperty(PROP_UNDEFINED_REFERENCES)
    public SortedMap<
            String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
        getUndefinedReferences() {
      return _undefinedReferences;
    }

    @Override
    public String prettyPrint() {
      final StringBuilder sb = new StringBuilder();
      _undefinedReferences.forEach(
          (filename, byType) -> {
            sb.append(filename + ":\n");
            byType.forEach(
                (type, byName) -> {
                  sb.append("  " + type + ":\n");
                  byName.forEach(
                      (name, byUsage) -> {
                        sb.append("    " + name + ":\n");
                        byUsage.forEach(
                            (usage, lines) ->
                                sb.append("      " + usage + ": lines " + lines + "\n"));
                      });
                });
          });
      return sb.toString();
    }

    @JsonProperty(PROP_UNDEFINED_REFERENCES)
    public void setUndefinedReferences(
        SortedMap<
                String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
            undefinedReferences) {
      _undefinedReferences = undefinedReferences;
    }

    public void updateSummary() {
      _summary.reset();
      int numResults = 0;
      for (String hostname : _undefinedReferences.keySet()) {
        for (String type : _undefinedReferences.get(hostname).keySet()) {
          numResults += _undefinedReferences.get(hostname).get(type).size();
        }
      }
      _summary.setNumResults(numResults);
    }
  }

  public static class UndefinedReferencesAnswerer extends Answerer {

    public UndefinedReferencesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public UndefinedReferencesAnswerElement answer() {
      UndefinedReferencesQuestion question = (UndefinedReferencesQuestion) _question;

      // Find all the filenames that produced the queried nodes. This might have false positives if
      // a file produced multiple nodes, but that was already mis-handled before. Need to rewrite
      // this question as a TableAnswerElement.
      Set<String> includeNodes = question.getNodeRegex().getMatchingNodes(_batfish);
      SortedMap<String, String> hostnameFilenameMap =
          _batfish.loadParseVendorConfigurationAnswerElement().getFileMap();
      Set<String> includeFiles =
          hostnameFilenameMap
              .entrySet()
              .stream()
              .filter(e -> includeNodes.contains(e.getKey()))
              .map(Entry::getValue)
              .collect(Collectors.toSet());

      UndefinedReferencesAnswerElement answerElement = new UndefinedReferencesAnswerElement();
      SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
          undefinedReferences =
              _batfish.loadConvertConfigurationAnswerElementOrReparse().getUndefinedReferences();
      undefinedReferences
          .entrySet()
          .stream()
          .filter(e -> includeFiles.contains(e.getKey()))
          .forEach(e -> answerElement.getUndefinedReferences().put(e.getKey(), e.getValue()));
      answerElement.updateSummary();
      return answerElement;
    }
  }

  // <question_page_comment>

  /**
   * Outputs cases where undefined structures (e.g., ACL, routemaps) are referenced.
   *
   * <p>Such occurrences indicate configuration errors and can have serious consequences with some
   * vendors.
   *
   * @type UndefinedReferences onefile
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @example bf_answer("Nodes", nodeRegex="as1.*") Analyze all nodes whose names begin with "as1".
   */
  public static class UndefinedReferencesQuestion extends Question {

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private NodesSpecifier _nodeRegex;

    public UndefinedReferencesQuestion() {
      _nodeRegex = NodesSpecifier.ALL;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "undefinedreferences";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier nodeRegex) {
      _nodeRegex = nodeRegex;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new UndefinedReferencesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new UndefinedReferencesQuestion();
  }
}

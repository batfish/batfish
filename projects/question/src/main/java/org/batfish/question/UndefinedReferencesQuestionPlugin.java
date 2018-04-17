package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.Problem;
import org.batfish.datamodel.answers.ProblemsAnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class UndefinedReferencesQuestionPlugin extends QuestionPlugin {

  public static class UndefinedReferencesAnswerElement extends ProblemsAnswerElement {

    private SortedMap<
            String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
        _undefinedReferences;

    public UndefinedReferencesAnswerElement() {
      _undefinedReferences = new TreeMap<>();
      setSummary(new AnswerSummary());
    }

    public SortedMap<
            String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>>
        getUndefinedReferences() {
      return _undefinedReferences;
    }

    @Override
    public String prettyPrint() {
      final StringBuilder sb = new StringBuilder();
      _undefinedReferences.forEach(
          (hostname, byType) -> {
            sb.append(hostname + ":\n");
            byType.forEach(
                (type, byName) -> {
                  sb.append("  " + type + ":\n");
                  byName.forEach(
                      (name, byUsage) -> {
                        sb.append("    " + name + ":\n");
                        byUsage.forEach(
                            (usage, lines) -> {
                              sb.append("      " + usage + ": lines " + lines + "\n");
                            });
                      });
                });
          });
      return sb.toString();
    }

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
      UndefinedReferencesAnswerElement answerElement = new UndefinedReferencesAnswerElement();
      ConvertConfigurationAnswerElement ccae =
          _batfish.loadConvertConfigurationAnswerElementOrReparse();
      Set<String> includeNodes =
          question.getNodeRegex().getMatchingNodes(_batfish.loadConfigurations());

      ccae.getUndefinedReferences()
          .forEach(
              (hostname, byType) -> {
                if (includeNodes.contains(hostname)) {
                  answerElement.getUndefinedReferences().put(hostname, byType);
                }
              });
      ParseVendorConfigurationAnswerElement pvcae =
          _batfish.loadParseVendorConfigurationAnswerElement();
      SortedMap<String, String> hostnameFilenameMap = pvcae.getFileMap();
      answerElement
          .getUndefinedReferences()
          .forEach(
              (hostname, byType) -> {
                String filename = hostnameFilenameMap.get(hostname);
                if (filename != null) {
                  byType.forEach(
                      (type, byName) -> {
                        byName.forEach(
                            (name, byUsage) -> {
                              byUsage.forEach(
                                  (usage, lines) -> {
                                    String problemShort =
                                        "undefined:" + type + ":usage:" + usage + ":" + name;
                                    Problem problem = answerElement.getProblems().get(problemShort);
                                    if (problem == null) {
                                      problem = new Problem();
                                      String problemLong =
                                          "Undefined reference to structure of type: '"
                                              + type
                                              + "' with usage: '"
                                              + usage
                                              + "' named '"
                                              + name
                                              + "'";
                                      problem.setDescription(problemLong);
                                      answerElement.getProblems().put(problemShort, problem);
                                    }
                                    problem.getFiles().put(filename, lines);
                                  });
                            });
                      });
                }
              });
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

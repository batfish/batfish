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
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class UnusedStructuresQuestionPlugin extends QuestionPlugin {

  public static class UnusedStructuresAnswerElement extends AnswerElement {

    private static final String PROP_UNUSED_STRUCTURES = "unusedStructures";

    private SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>
        _unusedStructures;

    public UnusedStructuresAnswerElement() {
      _unusedStructures = new TreeMap<>();
      setSummary(new AnswerSummary());
    }

    @JsonProperty(PROP_UNUSED_STRUCTURES)
    public SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>
        getUnusedStructures() {
      return _unusedStructures;
    }

    @Override
    public String prettyPrint() {
      final StringBuilder sb = new StringBuilder();
      _unusedStructures.forEach(
          (node, types) -> {
            sb.append(node + ":\n");
            types.forEach(
                (type, members) -> {
                  sb.append("  " + type + ":\n");
                  members.forEach(
                      (member, lines) -> sb.append("    " + member + " lines " + lines + "\n"));
                });
          });
      return sb.toString();
    }

    @JsonProperty(PROP_UNUSED_STRUCTURES)
    public void setUnusedStructures(
        SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>
            undefinedReferences) {
      _unusedStructures = undefinedReferences;
    }

    public void updateSummary() {
      _summary.reset();
      int numResults = 0;
      for (String hostname : _unusedStructures.keySet()) {
        for (String type : _unusedStructures.get(hostname).keySet()) {
          numResults += _unusedStructures.get(hostname).get(type).size();
        }
      }
      _summary.setNumResults(numResults);
    }
  }

  public static class UnusedStructuresAnswerer extends Answerer {

    public UnusedStructuresAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public UnusedStructuresAnswerElement answer() {
      UnusedStructuresQuestion question = (UnusedStructuresQuestion) _question;

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

      UnusedStructuresAnswerElement answerElement = new UnusedStructuresAnswerElement();
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
          definedStructures =
              _batfish.loadConvertConfigurationAnswerElementOrReparse().getDefinedStructures();
      definedStructures
          .entrySet()
          .stream()
          .filter(e -> includeFiles.contains(e.getKey()))
          .forEach(
              e -> {
                String filename = e.getKey();
                SortedMap<String, SortedMap<String, DefinedStructureInfo>> byType = e.getValue();
                byType.forEach(
                    (structType, byName) ->
                        byName.forEach(
                            (structName, info) -> {
                              if (info.getNumReferrers() == 0) {
                                SortedMap<String, SortedMap<String, SortedSet<Integer>>> newByType =
                                    answerElement
                                        .getUnusedStructures()
                                        .computeIfAbsent(filename, t -> new TreeMap<>());
                                SortedMap<String, SortedSet<Integer>> newByName =
                                    newByType.computeIfAbsent(structType, t -> new TreeMap<>());
                                newByName.put(structName, info.getDefinitionLines());
                              }
                            }));
              });

      answerElement.updateSummary();
      return answerElement;
    }
  }

  // <question_page_comment>

  /**
   * Outputs cases where structures (e.g., ACL, routemaps) are defined but not used.
   *
   * <p>Such occurrences could be configuration errors or leftover cruft.
   *
   * @type UnusedStructures onefile
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @example bf_answer("Nodes", nodeRegex="as1.*") Analyze all nodes whose names begin with "as1".
   */
  public static class UnusedStructuresQuestion extends Question {

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private NodesSpecifier _nodeRegex;

    public UnusedStructuresQuestion() {
      _nodeRegex = NodesSpecifier.ALL;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "unusedstructures";
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
    return new UnusedStructuresAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new UnusedStructuresQuestion();
  }
}

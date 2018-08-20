package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.FileLines;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

@AutoService(Plugin.class)
public class UnusedStructuresQuestionPlugin extends QuestionPlugin {

  public static class UnusedStructuresAnswerer extends Answerer {

    static final String COL_FILENAME = "filename";
    static final String COL_LINES = "lines";
    static final String COL_STRUCT_NAME = "structName";
    static final String COL_STRUCT_TYPE = "structType";

    public UnusedStructuresAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public TableAnswerElement answer() {
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

      Multiset<Row> rows = LinkedHashMultiset.create();
      SortedMap<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>>
          definedStructures =
              _batfish.loadConvertConfigurationAnswerElementOrReparse().getDefinedStructures();
      definedStructures
          .entrySet()
          .stream()
          .filter(e -> includeFiles.contains(e.getKey()))
          .forEach(e -> rows.addAll(processEntryToRows(e)));

      TableAnswerElement table = new TableAnswerElement(createMetadata());
      table.postProcessAnswer(_question, rows);
      return table;
    }

    @VisibleForTesting
    // Entry is: filename -> struct type -> struct name -> defined structure info
    public static List<Row> processEntryToRows(
        Entry<String, SortedMap<String, SortedMap<String, DefinedStructureInfo>>> e) {
      List<Row> rows = new ArrayList<>();
      String filename = e.getKey();
      for (Entry<String, SortedMap<String, DefinedStructureInfo>> e1 : e.getValue().entrySet()) {
        String structType = e1.getKey();
        for (Entry<String, DefinedStructureInfo> e2 : e1.getValue().entrySet()) {
          String name = e2.getKey();
          DefinedStructureInfo info = e2.getValue();
          if (info.getNumReferrers() == 0) {
            rows.add(
                Row.of(
                    COL_FILENAME,
                    filename,
                    COL_STRUCT_TYPE,
                    structType,
                    COL_STRUCT_NAME,
                    name,
                    COL_LINES,
                    new FileLines(filename, info.getDefinitionLines())));
          }
        }
      }
      return rows;
    }

    private static TableMetadata createMetadata() {
      List<ColumnMetadata> columnMetadata =
          ImmutableList.of(
              new ColumnMetadata(
                  COL_FILENAME, Schema.STRING, "File containing structure", true, false),
              new ColumnMetadata(COL_STRUCT_TYPE, Schema.STRING, "Type of structure", false, false),
              new ColumnMetadata(COL_STRUCT_NAME, Schema.STRING, "Name of structure", true, false),
              new ColumnMetadata(
                  COL_LINES, Schema.FILE_LINES, "Lines where structure appears", false, false));
      String textDesc =
          String.format(
              "'${%s}' has unused '${%s}' called '${%s}' at line(s) ${%s}",
              COL_FILENAME, COL_STRUCT_TYPE, COL_STRUCT_NAME, COL_LINES);
      return new TableMetadata(columnMetadata, textDesc);
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

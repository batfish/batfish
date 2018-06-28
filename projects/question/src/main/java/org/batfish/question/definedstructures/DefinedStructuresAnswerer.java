package org.batfish.question.definedstructures;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class DefinedStructuresAnswerer extends Answerer {
  public static final String COL_DEFINITION_LINES = "definitionLines";
  public static final String COL_NODE_NAME = "nodeName";
  public static final String COL_NUM_REFERENCES = "numReferences";
  public static final String COL_STRUCT_NAME = "structName";
  public static final String COL_STRUCT_TYPE = "structType";

  public DefinedStructuresAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    DefinedStructuresQuestion question = (DefinedStructuresQuestion) _question;
    Multiset<Row> structures = rawAnswer(question);
    TableAnswerElement answer = new TableAnswerElement(createMetadata(question));
    answer.postProcessAnswer(question, structures);
    return answer;
  }

  private Multiset<Row> rawAnswer(DefinedStructuresQuestion question) {
    Multiset<Row> structures = HashMultiset.create();
    Set<String> includeNodes = question.getNodeRegex().getMatchingNodes(_batfish);

    ConvertConfigurationAnswerElement ccae =
        _batfish.loadConvertConfigurationAnswerElementOrReparse();

    ccae.getDefinedStructures()
        .forEach(
            (nodeName, byStructType) -> {
              if (!includeNodes.contains(nodeName)) {
                return;
              }
              byStructType.forEach(
                  (structType, byStructName) ->
                      byStructName.forEach(
                          (structName, info) -> {
                            DefinedStructureRow row =
                                new DefinedStructureRow(
                                    nodeName,
                                    structType,
                                    structName,
                                    info.getNumReferrers(),
                                    info.getDefinitionLines());
                            structures.add(toRow(row));
                          }));
            });

    return structures;
  }

  public static TableMetadata createMetadata(Question question) {
    List<ColumnMetadata> columnMetadataMap =
        ImmutableList.of(
            new ColumnMetadata(
                COL_NODE_NAME, Schema.STRING, "Node where the structure is defined", true, false),
            new ColumnMetadata(
                COL_STRUCT_TYPE, Schema.STRING, "Type of the structure", true, false),
            new ColumnMetadata(
                COL_STRUCT_NAME, Schema.STRING, "Name of the structure", true, false),
            new ColumnMetadata(
                COL_DEFINITION_LINES,
                Schema.list(Schema.INTEGER),
                "Lines where the structure is defined",
                false,
                true),
            new ColumnMetadata(
                COL_NUM_REFERENCES,
                Schema.INTEGER,
                "Number of references to this structure",
                false,
                true));

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(
          String.format(
              "On ${%s} struct ${%s}:${%s} on lines ${%s} has ${%s} references.",
              COL_NODE_NAME,
              COL_STRUCT_TYPE,
              COL_STRUCT_NAME,
              COL_DEFINITION_LINES,
              COL_NUM_REFERENCES));
    }
    return new TableMetadata(columnMetadataMap, dhints);
  }

  private static Row toRow(DefinedStructureRow info) {
    RowBuilder row = Row.builder();
    row.put(COL_DEFINITION_LINES, info.getDefinitionLines())
        .put(COL_NODE_NAME, info.getNodeName())
        .put(COL_NUM_REFERENCES, info.getNumReferences())
        .put(COL_STRUCT_NAME, new Node(info.getStructName()))
        .put(COL_STRUCT_TYPE, info.getStructType());
    return row.build();
  }
}

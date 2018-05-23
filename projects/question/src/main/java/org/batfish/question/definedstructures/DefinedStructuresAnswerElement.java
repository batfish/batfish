package org.batfish.question.definedstructures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class DefinedStructuresAnswerElement extends TableAnswerElement {

  public static final String COL_DEFINITION_LINES = "definitionLines";
  public static final String COL_NODE_NAME = "nodeName";
  public static final String COL_NUM_REFERENCES = "numReferences";
  public static final String COL_STRUCT_NAME = "structName";
  public static final String COL_STRUCT_TYPE = "structType";

  @JsonCreator
  public DefinedStructuresAnswerElement(
      @Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
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

  static Row toRow(DefinedStructureRow info) {
    RowBuilder row = Row.builder();
    row.put(COL_DEFINITION_LINES, info.getDefinitionLines())
        .put(COL_NODE_NAME, info.getNodeName())
        .put(COL_NUM_REFERENCES, info.getNumReferences())
        .put(COL_STRUCT_NAME, new Node(info.getStructName()))
        .put(COL_STRUCT_TYPE, info.getStructType());
    return row.build();
  }
}

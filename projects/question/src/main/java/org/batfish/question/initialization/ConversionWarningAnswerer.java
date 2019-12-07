package org.batfish.question.initialization;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** Implements answer to {@link ConversionWarningQuestion}. */
class ConversionWarningAnswerer extends Answerer {
  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    ConvertConfigurationAnswerElement ccae =
        _batfish.loadConvertConfigurationAnswerElementOrReparse(snapshot);

    Map<String, Warnings> warnings = ccae.getWarnings();

    Rows rows = new Rows();
    warnings.forEach(
        (nodeName, nodeWarnings) -> {
          for (Warning w : nodeWarnings.getRedFlagWarnings()) {
            rows.add(getRow(nodeName, w));
          }
          for (Warning w : nodeWarnings.getUnimplementedWarnings()) {
            rows.add(getRow(nodeName, w));
          }
        });

    TableAnswerElement answerElement = new TableAnswerElement(TABLE_METADATA);
    answerElement.postProcessAnswer(_question, rows.getData());
    return answerElement;
  }

  ConversionWarningAnswerer(ConversionWarningQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  @Nonnull
  @VisibleForTesting
  static Row getRow(String nodeName, Warning warning) {
    return Row.builder(TABLE_METADATA.toColumnMap())
        .put(COL_NODE, new Node(nodeName))
        .put(COL_TYPE, ImmutableList.of(warning.getTag()))
        .put(COL_COMMENT, warning.getText())
        .build();
  }

  static final String COL_NODE = "Node";
  static final String COL_COMMENT = "Comment";
  static final String COL_TYPE = "Type";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(
              COL_NODE, Schema.NODE, "The node that caused the warning", true, false),
          new ColumnMetadata(
              COL_TYPE, Schema.list(Schema.STRING), "The type of the warning", true, false),
          new ColumnMetadata(
              COL_COMMENT, Schema.STRING, "The description of the warning", true, false));

  private static final String TEXT_DESC =
      String.format("Warning for node ${%s}: ${%s} :: ${%s}", COL_NODE, COL_TYPE, COL_COMMENT);

  private static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA, TEXT_DESC);
}

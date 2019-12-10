package org.batfish.question.initialization;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ConvertStatus;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** Implements {@link ConversionStatusQuestion}. */
final class ConversionStatusAnswerer extends Answerer {
  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    ConvertConfigurationAnswerElement ccae =
        _batfish.loadConvertConfigurationAnswerElementOrReparse(snapshot);

    Rows rows = new Rows();
    ccae.getConvertStatus()
        .forEach(
            (nodeName, nodeStatus) -> {
              rows.add(getRow(nodeName, nodeStatus));
            });

    TableAnswerElement answerElement = new TableAnswerElement(TABLE_METADATA);
    answerElement.postProcessAnswer(_question, rows.getData());
    return answerElement;
  }

  ConversionStatusAnswerer(ConversionStatusQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  @Nonnull
  @VisibleForTesting
  static Row getRow(String node, ConvertStatus status) {
    Row.TypedRowBuilder builder = Row.builder(TABLE_METADATA.toColumnMap());
    builder.put(COL_NODE, node);
    builder.put(COL_CONVERT_STATUS, status.toString());
    return builder.build();
  }

  static final String COL_NODE = "Node";
  static final String COL_CONVERT_STATUS = "Status";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(COL_NODE, Schema.STRING, "The node that was converted", true, false),
          new ColumnMetadata(
              COL_CONVERT_STATUS,
              Schema.STRING,
              "The status of the conversion operation",
              false,
              true));

  private static final String TEXT_DESC =
      String.format("Node ${%s} converted with status ${%s}", COL_NODE, COL_CONVERT_STATUS);

  private static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA, TEXT_DESC);
}

package org.batfish.question.initialization;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** Implements {@link FileParseStatusQuestion}. */
class FileParseStatusAnswerer extends Answerer {
  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    ParseVendorConfigurationAnswerElement pvcae =
        _batfish.loadParseVendorConfigurationAnswerElement(snapshot);
    ConvertConfigurationAnswerElement ccae =
        _batfish.loadConvertConfigurationAnswerElementOrReparse(snapshot);

    Map<String, ParseStatus> statusMap = pvcae.getParseStatus();
    Rows rows = new Rows();
    Multimap<String, String> fileToConvertedHosts = ccae.getFileMap();
    Multimap<String, String> fileToHost = TreeMultimap.create();
    pvcae
        .getFileMap()
        .forEach(
            (hostname, filename) -> {
              fileToHost.put(filename, hostname);
              fileToHost.putAll(filename, fileToConvertedHosts.get(filename));
            });

    statusMap.forEach(
        (filename, status) ->
            rows.add(
                getRow(
                    filename,
                    status,
                    pvcae.getFileFormats().getOrDefault(filename, ConfigurationFormat.UNKNOWN),
                    fileToHost.get(filename))));

    TableAnswerElement answerElement = new TableAnswerElement(TABLE_METADATA);
    answerElement.postProcessAnswer(_question, rows.getData());
    return answerElement;
  }

  FileParseStatusAnswerer(FileParseStatusQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  @Nonnull
  @VisibleForTesting
  static Row getRow(
      String filename,
      ParseStatus status,
      ConfigurationFormat format,
      @Nullable Collection<String> hostsProduced) {
    Row.TypedRowBuilder builder = Row.builder(TABLE_METADATA.toColumnMap());
    builder.put(COL_FILENAME, filename);
    builder.put(COL_PARSE_STATUS, status.toString());
    builder.put(COL_FILE_FORMAT, format.toString());

    ImmutableList.Builder<Node> nodesProduced = ImmutableList.builder();
    for (String hostname : firstNonNull(hostsProduced, Collections.<String>emptyList())) {
      nodesProduced.add(new Node(hostname));
    }
    builder.put(COL_NODES, nodesProduced.build());
    return builder.build();
  }

  static final String COL_FILENAME = "File_Name";
  static final String COL_FILE_FORMAT = "File_Format";
  static final String COL_PARSE_STATUS = "Status";
  static final String COL_NODES = "Nodes";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(COL_FILENAME, Schema.STRING, "The file that was parsed", true, false),
          new ColumnMetadata(
              COL_PARSE_STATUS, Schema.STRING, "The status of the parsing operation", false, true),
          new ColumnMetadata(
              COL_FILE_FORMAT, Schema.STRING, "The detected format of the file", false, true),
          new ColumnMetadata(
              COL_NODES,
              Schema.list(Schema.NODE),
              "Names of nodes produced from this file",
              false,
              true));

  private static final String TEXT_DESC =
      String.format(
          "File ${%s} detected as format ${%s} and parsed with status ${%s} and produced nodes"
              + " ${%s}",
          COL_FILENAME, COL_FILE_FORMAT, COL_PARSE_STATUS, COL_NODES);

  @VisibleForTesting
  static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA, TEXT_DESC);
}

package org.batfish.question.aaaauthenticationlogin;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Line;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

@ParametersAreNonnullByDefault
public class AaaAuthenticationLoginAnswerer extends Answerer {

  public static final String COLUMN_NODE = "Node";
  public static final String COLUMN_LINE_NAMES = "Line_Names";

  private static final String TEXT_DESC =
      String.format(
          "Node ${%s} does not require authentication on line(s) ${%s}",
          COLUMN_NODE, COLUMN_LINE_NAMES);

  public AaaAuthenticationLoginAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates a {@link TableAnswerElement} object with the right metadata
   *
   * @param question The question object for which the answer is being created, to borrow its {@link
   *     DisplayHints}
   * @return The creates the answer element object.
   */
  @VisibleForTesting
  static TableAnswerElement create(AaaAuthenticationLoginQuestion question) {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COLUMN_NODE, Schema.NODE, "Node", true, false),
            new ColumnMetadata(
                COLUMN_LINE_NAMES,
                Schema.list(Schema.STRING),
                "Names of virtual terminal lines",
                false,
                true));
    String textDesc = TEXT_DESC;
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null) {
      textDesc = firstNonNull(dhints.getTextDesc(), textDesc);
    }

    TableMetadata metadata = new TableMetadata(columnMetadata, textDesc);
    return new TableAnswerElement(metadata);
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    AaaAuthenticationLoginQuestion question = (AaaAuthenticationLoginQuestion) _question;

    TableAnswerElement answerElement = create(question);

    Set<String> specifiedNodes =
        question.getNodeSpecifier().resolve(_batfish.specifierContext(snapshot));

    SortedMap<String, Configuration> configs = _batfish.loadConfigurations(snapshot);
    configs.forEach(
        (configName, config) -> {
          if (specifiedNodes.contains(configName)) {
            Row row = null;
            if (config.getVendorFamily().getCisco() != null) {
              row = getRow(configName, config.getVendorFamily().getCisco().getLines().values());
            } else if (config.getVendorFamily().getJuniper() != null) {
              row = getRow(configName, config.getVendorFamily().getJuniper().getLines().values());
            }

            if (row != null) {
              answerElement.addRow(row);
            }
          }
        });
    answerElement.setSummary(answerElement.computeSummary(question.getAssertion()));
    return answerElement;
  }

  @VisibleForTesting
  static Row getRow(String nodeName, Collection<Line> lines) {
    List<String> exposedLines = new ArrayList<>();
    for (Line line : lines) {
      if (!line.requiresAuthentication()) {
        exposedLines.add(line.getName());
      }
    }
    if (exposedLines.isEmpty()) {
      return null;
    }
    RowBuilder row = Row.builder();
    row.put(COLUMN_NODE, new Node(nodeName)).put(COLUMN_LINE_NAMES, exposedLines);
    return row.build();
  }
}

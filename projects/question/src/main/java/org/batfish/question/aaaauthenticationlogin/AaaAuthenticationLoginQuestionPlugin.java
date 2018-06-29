package org.batfish.question.aaaauthenticationlogin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.vendor_family.cisco.Line;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public class AaaAuthenticationLoginQuestionPlugin extends QuestionPlugin {

  public static class AaaAuthenticationAnswerer extends Answerer {

    public static final String COLUMN_NODE = "node";
    public static final String COLUMN_LINE_NAMES = "lineNames";

    public AaaAuthenticationAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    /**
     * Creates a {@link TableAnswerElement} object with the right metadata
     *
     * @param question The question object for which the answer is being created, to borrow its
     *     {@link DisplayHints}
     * @return The creates the answer element object.
     */
    @VisibleForTesting
    static TableAnswerElement create(AaaAuthenticationQuestion question) {
      List<ColumnMetadata> columnMetadata =
          ImmutableList.of(
              new ColumnMetadata(COLUMN_NODE, Schema.NODE, "Node", true, false),
              new ColumnMetadata(
                  COLUMN_LINE_NAMES, Schema.list(Schema.STRING), "Line names", false, true));
      DisplayHints dhints = question.getDisplayHints();
      if (dhints == null) {
        dhints = new DisplayHints();
        dhints.setTextDesc(
            String.format(
                "Node ${%s} does not require authentication on line(s) ${%s}",
                COLUMN_NODE, COLUMN_LINE_NAMES));
      }
      TableMetadata metadata = new TableMetadata(columnMetadata, dhints);
      return new TableAnswerElement(metadata);
    }

    @Override
    public TableAnswerElement answer() {
      AaaAuthenticationQuestion question = (AaaAuthenticationQuestion) _question;

      TableAnswerElement answerElement = create(question);

      Set<String> specifiedNodes = question.getNodeRegex().getMatchingNodes(_batfish);

      SortedMap<String, Configuration> configs = _batfish.loadConfigurations();
      configs.forEach(
          (configName, config) -> {
            if (specifiedNodes.contains(configName)
                && config.getVendorFamily().getCisco() != null) {
              Row row = getRow(configName, config.getVendorFamily().getCisco().getLines().values());
              if (row != null) {
                answerElement.addRow(row);
              }
            }
          });
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

  public static class AaaAuthenticationQuestion extends Question {

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private NodesSpecifier _nodeRegex;

    public AaaAuthenticationQuestion() {
      _nodeRegex = NodesSpecifier.ALL;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "AaaAuthenticationLogin";
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
    return new AaaAuthenticationAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new AaaAuthenticationQuestion();
  }
}

package org.batfish.question.aclreachability2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

@ParametersAreNonnullByDefault
public class AclReachability2AnswerElement extends TableAnswerElement {
  public static final String COL_NODES = "nodes";
  public static final String COL_ACL = "acl";
  public static final String COL_LINES = "lines";
  public static final String COL_BLOCKED_LINE_NUM = "blockedlinenum";
  public static final String COL_BLOCKING_LINE_NUMS = "blockinglinenums";
  public static final String COL_DIFF_ACTION = "differentaction";
  public static final String COL_MESSAGE = "message";

  @JsonCreator
  public AclReachability2AnswerElement(@JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
  }

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  static TableMetadata createMetadata(AclReachability2Question question) {
    List<ColumnMetadata> columnMetadata =
        new ImmutableList.Builder<ColumnMetadata>()
            .add(new ColumnMetadata(COL_NODES, Schema.list(Schema.NODE), "Nodes", true, false))
            .add(new ColumnMetadata(COL_ACL, Schema.STRING, "ACL name", true, false))
            .add(
                new ColumnMetadata(
                    COL_LINES, Schema.list(Schema.STRING), "ACL lines", false, false))
            .add(
                new ColumnMetadata(
                    COL_BLOCKED_LINE_NUM, Schema.INTEGER, "Blocked line number", true, false))
            .add(
                new ColumnMetadata(
                    COL_BLOCKING_LINE_NUMS,
                    Schema.list(Schema.INTEGER),
                    "Blocking line numbers",
                    false,
                    true))
            .add(
                new ColumnMetadata(
                    COL_DIFF_ACTION, Schema.BOOLEAN, "Different action", false, true))
            .add(new ColumnMetadata(COL_MESSAGE, Schema.STRING, "Message", false, false))
            .build();

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(String.format("${%s}", COL_MESSAGE));
    }
    return new TableMetadata(columnMetadata, dhints);
  }
}

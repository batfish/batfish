package org.batfish.question.aclreachability2;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AclLines2Rows;
import org.batfish.datamodel.answers.AclLinesAnswerElementInterface.AclSpecs;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

@ParametersAreNonnullByDefault
public class AclReachability2Answerer extends Answerer {

  public AclReachability2Answerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TableAnswerElement answer() {
    AclReachability2Question question = (AclReachability2Question) _question;
    AclLines2Rows answerRows = new AclLines2Rows();

    Set<String> specifiedNodes = question.getNodeRegex().getMatchingNodes(_batfish);
    Pattern aclRegex;
    try {
      aclRegex = Pattern.compile(question.getAclNameRegex());
    } catch (PatternSyntaxException e) {
      throw new BatfishException(
          "Supplied regex for nodes is not a valid Java regex: \""
              + question.getAclNameRegex()
              + "\"",
          e);
    }
    SortedMap<String, Configuration> configurations = _batfish.loadConfigurations();
    List<AclSpecs> aclSpecs =
        AclReachabilityAnswererUtils.getAclSpecs(
            configurations, specifiedNodes, aclRegex, answerRows);
    _batfish.answerAclReachability(aclSpecs, answerRows);
    TableAnswerElement answer = new TableAnswerElement(createMetadata(question));
    answer.postProcessAnswer(question, answerRows.getRows());
    return answer;
  }

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  public static TableMetadata createMetadata(Question question) {
    List<ColumnMetadata> columnMetadata =
        new ImmutableList.Builder<ColumnMetadata>()
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_SOURCES,
                    Schema.list(Schema.STRING),
                    "ACL sources",
                    true,
                    false))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_LINES, Schema.list(Schema.STRING), "ACL lines", false, false))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_BLOCKED_LINE_NUM,
                    Schema.INTEGER,
                    "Blocked line number",
                    true,
                    false))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_BLOCKING_LINE_NUMS,
                    Schema.list(Schema.INTEGER),
                    "Blocking line numbers",
                    false,
                    true))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_DIFF_ACTION, Schema.BOOLEAN, "Different action", false, true))
            .add(
                new ColumnMetadata(
                    AclLines2Rows.COL_MESSAGE, Schema.STRING, "Message", false, false))
            .build();

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(String.format("${%s}", AclLines2Rows.COL_MESSAGE));
    }
    return new TableMetadata(columnMetadata, dhints);
  }
}

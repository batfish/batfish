package org.batfish.question.aclreachability2;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.answers.AclLinesNewAnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameAnswerElement;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameAnswerer;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameQuestion;

public class AclReachability2Answerer extends Answerer {
  public static final String COL_NODES = "nodes";
  public static final String COL_ACL = "acl";
  public static final String COL_LINES = "lines";
  public static final String COL_BLOCKED_LINE_NUM = "blockedlinenum";
  public static final String COL_BLOCKING_LINE_NUMS = "blockinglinenums";
  public static final String COL_DIFF_ACTION = "differentaction";
  public static final String COL_MESSAGE = "message";

  public AclReachability2Answerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AclLinesNewAnswerElement answer() {
    AclReachability2Question question = (AclReachability2Question) _question;
    // get comparesamename results for acls
    CompareSameNameQuestion csnQuestion = new CompareSameNameQuestion();
    csnQuestion.setCompareGenerated(true);
    csnQuestion.setNodeRegex(question.getNodeRegex());
    csnQuestion.setNamedStructTypes(
        new TreeSet<>(Collections.singleton(IpAccessList.class.getSimpleName())));
    csnQuestion.setSingletons(true);
    CompareSameNameAnswerer csnAnswerer = new CompareSameNameAnswerer(csnQuestion, _batfish);
    CompareSameNameAnswerElement csnAnswer = csnAnswerer.answer();
    NamedStructureEquivalenceSets<?> aclEqSets =
        csnAnswer.getEquivalenceSets().get(IpAccessList.class.getSimpleName());

    AclLinesNewAnswerElement answer = new AclLinesNewAnswerElement(createMetadata(question));
    _batfish.answerAclReachability(question.getAclNameRegex(), aclEqSets, answer);
    answer.postProcessAnswer(question, answer.getInitialRows().getData());
    return answer;
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

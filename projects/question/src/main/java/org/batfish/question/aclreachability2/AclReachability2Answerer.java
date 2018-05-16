package org.batfish.question.aclreachability2;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.answers.AclLinesAnswerElement;
import org.batfish.datamodel.answers.AclLinesAnswerElement.AclReachabilityEntry;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameAnswerElement;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameAnswerer;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameQuestion;

public class AclReachability2Answerer extends Answerer {
  public static final String COL_ID = "id";
  public static final String COL_NODES = "nodes";
  public static final String COL_ACL = "acl";
  public static final String COL_LINES = "lines";
  public static final String COL_LINE_NUMS = "linenumbers";
  public static final String COL_DIFF_ACTION = "differentaction";
  public static final String COL_MESSAGE = "message";

  public AclReachability2Answerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AclReachability2AnswerElement answer() {
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

    AclLinesAnswerElement oldAnswer =
        (AclLinesAnswerElement)
            _batfish.answerAclReachability(question.getAclNameRegex(), aclEqSets);

    AclReachability2AnswerElement answer =
        new AclReachability2AnswerElement(AclReachability2AnswerElement.createMetadata(question));
    int id = 0;
    for (Entry<String, SortedMap<String, SortedSet<AclReachabilityEntry>>> e1 :
        oldAnswer.getUnreachableLines().entrySet()) {
      String representativeNode = e1.getKey();
      for (Entry<String, SortedSet<AclReachabilityEntry>> e2 : e1.getValue().entrySet()) {
        String aclName = e2.getKey();
        SortedSet<String> nodes =
            oldAnswer.getEquivalenceClasses().get(aclName).get(representativeNode);
        List<String> lines =
            oldAnswer
                .getAcls()
                .get(representativeNode)
                .get(aclName)
                .getLines()
                .stream()
                .map(l -> l.getName())
                .collect(Collectors.toList());
        for (AclReachabilityEntry reachabilityEntry : e2.getValue()) {
          SortedMap<Integer, String> blockingLines = reachabilityEntry.getBlockingLines();
          ImmutableSortedSet.Builder<Integer> lineNumbers = ImmutableSortedSet.naturalOrder();
          lineNumbers.addAll(blockingLines.keySet());
          lineNumbers.add(reachabilityEntry.getIndex());
          StringBuilder sb =
              new StringBuilder(
                  String.format(
                      "In node(s) '%s', ACL '%s' has an unreachable line '%d: %s'. ",
                      String.join("', '", nodes),
                      aclName,
                      reachabilityEntry.getIndex(),
                      reachabilityEntry.getName()));
          if (reachabilityEntry.unmatchable()) {
            sb.append("This line will never match any packet, independent of preceding lines.");
          } else if (reachabilityEntry.multipleBlockingLines()) {
            sb.append("Multiple earlier lines partially block this line, making it unreachable.");
          } else {
            sb.append("Blocking line(s):\n");
            for (Entry<Integer, String> e : blockingLines.entrySet()) {
              sb.append(String.format("  [index %d] %s\n", e.getKey(), e.getValue()));
            }
          }
          answer.addRow(
              new Row()
                  .put(COL_ID, id)
                  .put(COL_NODES, nodes)
                  .put(COL_ACL, aclName)
                  .put(COL_LINES, lines)
                  .put(COL_LINE_NUMS, lineNumbers.build())
                  .put(COL_DIFF_ACTION, reachabilityEntry.getDifferentAction())
                  .put(COL_MESSAGE, sb.toString()));
          id++;
        }
      }
    }
    return answer;
  }
}

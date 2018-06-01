package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.answers.AclLinesAnswerElement;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameAnswerElement;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameAnswerer;
import org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameQuestion;

@AutoService(Plugin.class)
public class AclReachabilityQuestionPlugin extends QuestionPlugin {

  public static class AclReachabilityAnswerer extends Answerer {

    public AclReachabilityAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      AclReachabilityQuestion question = (AclReachabilityQuestion) _question;
      // get comparesamename results for acls
      CompareSameNameQuestion csnQuestion =
          new CompareSameNameQuestion(
              true,
              null,
              null,
              ImmutableSortedSet.of(IpAccessList.class.getSimpleName()),
              question.getNodeRegex(),
              true);
      CompareSameNameAnswerer csnAnswerer = new CompareSameNameAnswerer(csnQuestion, _batfish);
      CompareSameNameAnswerElement csnAnswer = csnAnswerer.answer();
      NamedStructureEquivalenceSets<?> aclEqSets =
          csnAnswer.getEquivalenceSets().get(IpAccessList.class.getSimpleName());

      AclLinesAnswerElement answer = new AclLinesAnswerElement();
      _batfish.answerAclReachability(question.getAclNameRegex(), aclEqSets, answer);
      return answer;
    }
  }

  // <question_page_comment>

  /**
   * Identifies unreachable lines in ACLs.
   *
   * <p>Report ACLs with unreachable lines, as well as reachability of each line within the ACL.
   * Unreachable lines can indicate erroneous configuration.
   *
   * @type AclReachability onefile
   * @param aclNameRegex Regular expression for names of the ACLs to analyze. Default value is '.*'
   *     (i.e., all ACLs).
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @example bf_answer("AclReachability", aclNameRegex='OUTSIDE_TO_INSIDE.*') Analyzes only ACLs
   *     whose names start with 'OUTSIDE_TO_INSIDE'.
   */
  public static class AclReachabilityQuestion extends Question {

    private static final String PROP_ACL_NAME_REGEX = "aclNameRegex";

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private String _aclNameRegex;

    private NodesSpecifier _nodeRegex;

    public AclReachabilityQuestion() {
      _nodeRegex = NodesSpecifier.ALL;
      _aclNameRegex = ".*";
    }

    @JsonProperty(PROP_ACL_NAME_REGEX)
    public String getAclNameRegex() {
      return _aclNameRegex;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "aclreachability";
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @Override
    public String prettyPrint() {
      String retString =
          String.format(
              "%s %s%s=\"%s\" %s=\"%s\"",
              getName(),
              prettyPrintBase(),
              PROP_ACL_NAME_REGEX,
              _aclNameRegex,
              PROP_NODE_REGEX,
              _nodeRegex);
      return retString;
    }

    @JsonProperty(PROP_ACL_NAME_REGEX)
    public void setAclNameRegex(String regex) {
      _aclNameRegex = regex;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public void setNodeRegex(NodesSpecifier regex) {
      _nodeRegex = regex;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new AclReachabilityAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new AclReachabilityQuestion();
  }
}

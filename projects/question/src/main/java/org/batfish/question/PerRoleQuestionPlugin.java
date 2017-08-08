package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.INodeRegexQuestion;
import org.batfish.datamodel.questions.Question;

public class PerRoleQuestionPlugin extends QuestionPlugin {

  public static class PerRoleAnswerElement implements AnswerElement {

    private static final String ANSWERS_VAR = "answers";

    private SortedMap<String, AnswerElement> _answers;

    public PerRoleAnswerElement() {
      _answers = new TreeMap<>();
    }

    @JsonProperty(ANSWERS_VAR)
    public SortedMap<String, AnswerElement> getAnswers() {
      return _answers;
    }

    @Override
    public String prettyPrint() {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, AnswerElement> entry : _answers.entrySet()) {
        sb.append("Role " + entry.getKey() + ":\n");
        sb.append(entry.getValue().prettyPrint());
      }
      return sb.toString();
    }

    @JsonProperty(ANSWERS_VAR)
    public void setAnswers(SortedMap<String, AnswerElement> answers) {
      _answers = answers;
    }
  }

  public static class PerRoleAnswerer extends Answerer {

    public PerRoleAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public PerRoleAnswerElement answer() {
      PerRoleQuestion question = (PerRoleQuestion) _question;
      PerRoleAnswerElement answerElement = new PerRoleAnswerElement();

      Question innerQuestion = question.getQuestion();
      INodeRegexQuestion innerNRQuestion;
      if (innerQuestion instanceof INodeRegexQuestion) {
        innerNRQuestion = (INodeRegexQuestion) innerQuestion;
      } else {
        throw new BatfishException(
            "The question " + innerQuestion.getName() + " does not implement INodeRegexQuestion");
      }

      SortedMap<String, Configuration> configurations = _batfish.loadConfigurations();
      // collect the desired nodes in a list
      List<String> nodes =
          CommonUtil.getMatchingStrings(question.getNodeRegex(), configurations.keySet());

      // produce a map from each role to the nodes that have it
      SortedMap<String, SortedSet<String>> roleNodeMap = new TreeMap<>();
      for (String node : nodes) {
        SortedSet<String> roles = configurations.get(node).getRoles();
        for (String role : roles) {
          SortedSet<String> roleMembers = roleNodeMap.computeIfAbsent(role, k -> new TreeSet<>());
          roleMembers.add(node);
        }
      }

      List<String> desiredRoles = question.getRoles();
      if (desiredRoles != null) {
        // only keep the roles that the user specified
        SortedMap<String, SortedSet<String>> newMap = new TreeMap<>();
        for (String desiredRole : desiredRoles) {
          SortedSet<String> members = roleNodeMap.get(desiredRole);
          if (members != null) {
            newMap.put(desiredRole, members);
          }
        }
        roleNodeMap = newMap;
      }

      SortedMap<String, AnswerElement> results = new TreeMap<>();

      // now ask the inner question once per role
      String origRegex = innerNRQuestion.getNodeRegex();
      for (Map.Entry<String, SortedSet<String>> entry : roleNodeMap.entrySet()) {
        String role = entry.getKey();
        String regex = namesToRegex(entry.getValue());
        innerNRQuestion.setNodeRegex("(" + origRegex + ")" + "&&" + regex);
        String innerQuestionName = innerQuestion.getName();
        Answerer innerAnswerer =
            _batfish.getAnswererCreators().get(innerQuestionName).apply(innerQuestion, _batfish);
        AnswerElement innerAnswer = innerAnswerer.answer();
        results.put(role, innerAnswer);
      }

      answerElement.setAnswers(results);

      return answerElement;
    }

    String namesToRegex(Set<String> names) {
      return names.stream().collect(Collectors.joining("|"));
    }
  }

  // <question_page_comment>
  /**
   * Answers a given question separately on each "role" within the network.
   *
   * <p>It is common for the nodes in a network to be partitioned into roles that each have a
   * specific function in the network. For example, border routers are responsible for mediating the
   * interactions between the network and its peer networks, and distribution routers are
   * responsible for delivering packets to end hosts within the network. Roles can be useful for
   * improving the precision and utility of several other questions. For example, it may only make
   * sense to run the CompareSameName question on nodes that have the same role.
   *
   * @type PerRole multifile
   * @param question The question to ask on each role. This parameter is mandatory.
   * @param roles List of the role names to include in the answer. Default is to use all role names.
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes). *
   */
  public static final class PerRoleQuestion extends Question {

    private static final String NODE_REGEX_VAR = "nodeRegex";

    private static final String QUESTION_VAR = "question";

    private static final String ROLES_VAR = "roles";

    private String _nodeRegex;

    private Question _question;

    private List<String> _roles;

    public PerRoleQuestion() {
      _nodeRegex = ".*";
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "perrole";
    }

    @JsonProperty(NODE_REGEX_VAR)
    public String getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(QUESTION_VAR)
    public Question getQuestion() {
      return _question;
    }

    @JsonProperty(ROLES_VAR)
    public List<String> getRoles() {
      return _roles;
    }

    @Override
    public boolean getTraffic() {
      return false;
    }

    @JsonProperty(NODE_REGEX_VAR)
    public void setNodeRegex(String regex) {
      _nodeRegex = regex;
    }

    @JsonProperty(QUESTION_VAR)
    public void setQuestion(Question question) {
      _question = question;
    }

    @JsonProperty(ROLES_VAR)
    public void setRoleRegexes(List<String> roles) {
      _roles = roles;
    }
  }

  @Override
  protected PerRoleAnswerer createAnswerer(Question question, IBatfish batfish) {
    return new PerRoleAnswerer(question, batfish);
  }

  @Override
  protected PerRoleQuestion createQuestion() {
    return new PerRoleQuestion();
  }
}

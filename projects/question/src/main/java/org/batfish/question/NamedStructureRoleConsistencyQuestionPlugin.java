package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureOutlierSet;
import org.batfish.datamodel.questions.AbstractRoleConsistencyQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.OutliersQuestionPlugin.OutliersAnswerElement;
import org.batfish.question.OutliersQuestionPlugin.OutliersQuestion;
import org.batfish.question.PerRoleQuestionPlugin.PerRoleAnswerElement;
import org.batfish.question.PerRoleQuestionPlugin.PerRoleQuestion;
import org.batfish.role.OutliersHypothesis;

@AutoService(Plugin.class)
public class NamedStructureRoleConsistencyQuestionPlugin extends QuestionPlugin {

  public static class NamedStructureRoleConsistencyAnswerElement extends AnswerElement {

    private static final String PROP_ANSWERS = "answers";

    private List<NamedStructureOutlierSet<?>> _answers;

    public NamedStructureRoleConsistencyAnswerElement() {
      _answers = new LinkedList<>();
    }

    @JsonProperty(PROP_ANSWERS)
    public List<NamedStructureOutlierSet<?>> getAnswers() {
      return _answers;
    }

    @Override
    public String prettyPrint() {

      StringBuffer sb = new StringBuffer();

      return sb.toString();
    }

    @JsonProperty(PROP_ANSWERS)
    public void setAnswers(List<NamedStructureOutlierSet<?>> answers) {
      _answers = answers;
    }
  }

  public static class NamedStructureRoleConsistencyAnswerer extends Answerer {

    private NamedStructureRoleConsistencyAnswerElement _answerElement;

    public NamedStructureRoleConsistencyAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public NamedStructureRoleConsistencyAnswerElement answer() {

      NamedStructureRoleConsistencyQuestion question =
          (NamedStructureRoleConsistencyQuestion) _question;
      _answerElement = new NamedStructureRoleConsistencyAnswerElement();

      OutliersQuestion innerQ = new OutliersQuestionPlugin().createQuestion();
      innerQ.setHypothesis(question.getHypothesis());
      SortedSet<String> structTypes = new TreeSet<>();
      structTypes.add(question.getStructType());
      innerQ.setNamedStructTypes(structTypes);
      innerQ.setVerbose(true);

      PerRoleQuestionPlugin outerPlugin = new PerRoleQuestionPlugin();
      PerRoleQuestion outerQ = outerPlugin.createQuestion();
      outerQ.setRoleSpecifier(
          question.getRoleSpecifier().orElse(_batfish.getNodeRoleSpecifier(false)));
      outerQ.setQuestion(innerQ);

      PerRoleAnswerElement roleAE = outerPlugin.createAnswerer(outerQ, _batfish).answer();
      List<NamedStructureOutlierSet<?>> answers = new LinkedList<>();
      for (Map.Entry<String, AnswerElement> entry : roleAE.getAnswers().entrySet()) {
        String role = entry.getKey();
        OutliersAnswerElement ae = (OutliersAnswerElement) entry.getValue();
        for (NamedStructureOutlierSet<?> answer : ae.getNamedStructureOutliers()) {
          answer.setRole(role);
          answers.add(answer);
        }
      }

      _answerElement.setAnswers(answers);

      return _answerElement;
    }
  }

  // <question_page_comment>
  /**
   * Checks a role-based consistency policy requiring that all nodes of the same role have the same
   * value for some particular configuration property (e.g., DnsServers).
   *
   * @type NamedStructureRoleConsistency multifile
   * @param roleSpecifier A NodeRoleSpecifier that specifies the role(s) of each node. If not
   *     specified then by default the currently installed NodeRoleSpecifier is used.
   * @param structType A string representing the type of named structure to check.
   * @param hypothesis The hypothesis to check. Allowed values are "sameName" and "sameDefinition".
   */
  public static final class NamedStructureRoleConsistencyQuestion
      extends AbstractRoleConsistencyQuestion {

    private static final String PROP_ROLE_SPECIFIER = "roleSpecifier";

    private static final String PROP_STRUCT_TYPE = "structType";

    private static final String PROP_HYPOTHESIS = "hypothesis";

    private NodeRoleSpecifier _roleSpecifier;

    private String _structType;

    private OutliersHypothesis _hypothesis;

    @JsonCreator
    public NamedStructureRoleConsistencyQuestion() {}

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    @JsonProperty(PROP_HYPOTHESIS)
    public OutliersHypothesis getHypothesis() {
      return _hypothesis;
    }

    @Override
    public String getName() {
      return "namedStructureRoleConsistency";
    }

    @Override
    @JsonProperty(PROP_ROLE_SPECIFIER)
    public Optional<NodeRoleSpecifier> getRoleSpecifier() {
      return Optional.ofNullable(_roleSpecifier);
    }

    @JsonProperty(PROP_STRUCT_TYPE)
    public String getStructType() {
      return _structType;
    }

    @JsonProperty(PROP_HYPOTHESIS)
    public void setHypothesis(OutliersHypothesis hypothesis) {
      _hypothesis = hypothesis;
    }

    @JsonProperty(PROP_ROLE_SPECIFIER)
    public void setRoleSpecifier(NodeRoleSpecifier roleSpecifier) {
      _roleSpecifier = roleSpecifier;
    }

    @JsonProperty(PROP_STRUCT_TYPE)
    public void setStructType(String structType) {
      _structType = structType;
    }
  }

  @Override
  protected NamedStructureRoleConsistencyAnswerer createAnswerer(
      Question question, IBatfish batfish) {
    return new NamedStructureRoleConsistencyAnswerer(question, batfish);
  }

  @Override
  protected NamedStructureRoleConsistencyQuestion createQuestion() {
    return new NamedStructureRoleConsistencyQuestion();
  }
}

package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NamedStructureOutlierSet;
import org.batfish.datamodel.collections.OutlierSet;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.OutliersQuestionPlugin.OutliersAnswerElement;
import org.batfish.question.OutliersQuestionPlugin.OutliersQuestion;
import org.batfish.question.PerRoleQuestionPlugin.PerRoleAnswerElement;
import org.batfish.question.PerRoleQuestionPlugin.PerRoleQuestion;
import org.batfish.role.OutliersHypothesis;

@AutoService(Plugin.class)
public class PerRoleOutliersQuestionPlugin extends QuestionPlugin {

  public static class PerRoleOutliersAnswerElement extends AnswerElement {
    private static final String PROP_NAMED_STRUCTURE_OUTLIERS = "namedStructureOutliers";
    private static final String PROP_SERVER_OUTLIERS = "serverOutliers";

    private SortedSet<NamedStructureOutlierSet<?>> _namedStructureOutliers;

    private SortedSet<OutlierSet<NavigableSet<String>>> _serverOutliers;

    public PerRoleOutliersAnswerElement() {
      _namedStructureOutliers = new TreeSet<>();
      _serverOutliers = new TreeSet<>();
    }

    @JsonProperty(PROP_NAMED_STRUCTURE_OUTLIERS)
    public SortedSet<NamedStructureOutlierSet<?>> getNamedStructureOutliers() {
      return _namedStructureOutliers;
    }

    @JsonProperty(PROP_SERVER_OUTLIERS)
    public SortedSet<OutlierSet<NavigableSet<String>>> getServerOutliers() {
      return _serverOutliers;
    }

    @JsonProperty(PROP_NAMED_STRUCTURE_OUTLIERS)
    public void setNamedStructureOutliers(
        SortedSet<NamedStructureOutlierSet<?>> namedStructureOutliers) {
      _namedStructureOutliers = namedStructureOutliers;
    }

    @JsonProperty(PROP_SERVER_OUTLIERS)
    public void setServerOutliers(SortedSet<OutlierSet<NavigableSet<String>>> serverOutliers) {
      _serverOutliers = serverOutliers;
    }
  }

  public static class PerRoleOutliersAnswerer extends Answerer {

    private PerRoleOutliersAnswerElement _answerElement;

    public PerRoleOutliersAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public PerRoleOutliersAnswerElement answer(NetworkSnapshot snapshot) {

      PerRoleOutliersQuestion question = (PerRoleOutliersQuestion) _question;
      _answerElement = new PerRoleOutliersAnswerElement();

      OutliersQuestion innerQ = new OutliersQuestionPlugin().createQuestion();
      innerQ.setNamedStructTypes(question.getNamedStructTypes());
      innerQ.setHypothesis(question.getHypothesis());

      PerRoleQuestion outerQ =
          new PerRoleQuestion(null, innerQ, question.getRoleDimension(), question.getRoles());

      PerRoleQuestionPlugin outerPlugin = new PerRoleQuestionPlugin();
      PerRoleAnswerElement roleAE = outerPlugin.createAnswerer(outerQ, _batfish).answer(snapshot);

      SortedMap<String, AnswerElement> roleAnswers = roleAE.getAnswers();

      SortedSet<NamedStructureOutlierSet<?>> nsOutliers = new TreeSet<>();
      SortedSet<OutlierSet<NavigableSet<String>>> serverOutliers = new TreeSet<>();
      for (Map.Entry<String, AnswerElement> entry : roleAnswers.entrySet()) {
        String role = entry.getKey();
        OutliersAnswerElement oae = (OutliersAnswerElement) entry.getValue();
        for (NamedStructureOutlierSet<?> nsos : oae.getNamedStructureOutliers()) {
          nsos.setRole(role);
          nsOutliers.add(nsos);
        }
        for (OutlierSet<NavigableSet<String>> os : oae.getServerOutliers()) {
          os.setRole(role);
          serverOutliers.add(os);
        }
      }

      _answerElement.setNamedStructureOutliers(nsOutliers);
      _answerElement.setServerOutliers(serverOutliers);

      return _answerElement;
    }
  }

  /** Runs outlier detection on a per-role basis and then does a global ranking of the results. */
  public static final class PerRoleOutliersQuestion extends Question {
    private static final String PROP_HYPOTHESIS = "hypothesis";
    private static final String PROP_NAMED_STRUCT_TYPES = "namedStructTypes";
    private static final String PROP_ROLE_DIMENSION = "roleDimension";
    private static final String PROP_ROLES = "roles";

    @Nonnull private OutliersHypothesis _hypothesis;

    @Nonnull private SortedSet<String> _namedStructTypes;

    @Nullable private String _roleDimension;

    @Nullable private List<String> _roles;

    @JsonCreator
    public PerRoleOutliersQuestion(
        @JsonProperty(PROP_NAMED_STRUCT_TYPES) SortedSet<String> namedStructTypes,
        @JsonProperty(PROP_HYPOTHESIS) OutliersHypothesis hypothesis,
        @JsonProperty(PROP_ROLE_DIMENSION) String roleDimension,
        @JsonProperty(PROP_ROLES) List<String> roles) {
      _namedStructTypes = namedStructTypes == null ? new TreeSet<>() : namedStructTypes;
      _hypothesis = hypothesis == null ? OutliersHypothesis.SAME_DEFINITION : hypothesis;
      _roleDimension = roleDimension;
      _roles = roles;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @JsonProperty(PROP_HYPOTHESIS)
    public OutliersHypothesis getHypothesis() {
      return _hypothesis;
    }

    @Override
    public String getName() {
      return "perroleoutliers";
    }

    @JsonProperty(PROP_NAMED_STRUCT_TYPES)
    public SortedSet<String> getNamedStructTypes() {
      return _namedStructTypes;
    }

    @JsonProperty(PROP_ROLE_DIMENSION)
    public String getRoleDimension() {
      return _roleDimension;
    }

    @JsonProperty(PROP_ROLES)
    public List<String> getRoles() {
      return _roles;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new PerRoleOutliersAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new PerRoleOutliersQuestion(null, null, null, null);
  }
}

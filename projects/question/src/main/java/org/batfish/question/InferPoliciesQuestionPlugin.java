package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.RoleConsistencyPolicy;
import org.batfish.datamodel.collections.NamedStructureOutlierSet;
import org.batfish.datamodel.collections.NodePropertyOutlierSet;
import org.batfish.datamodel.collections.OutlierSet;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.OutliersQuestionPlugin.OutliersAnswerElement;
import org.batfish.question.OutliersQuestionPlugin.OutliersQuestion;
import org.batfish.question.PerRoleQuestionPlugin.PerRoleAnswerElement;
import org.batfish.question.PerRoleQuestionPlugin.PerRoleQuestion;
import org.batfish.role.OutliersHypothesis;

@AutoService(Plugin.class)
public class InferPoliciesQuestionPlugin extends QuestionPlugin {

  public static class InferPoliciesAnswerElement implements AnswerElement {

    private static final String PROP_ROLE_CONSISTENCY_POLICIES = "roleConsistencyPolicies";

    private Set<RoleConsistencyPolicy> _roleConsistencyPolicies;

    public InferPoliciesAnswerElement() {
      _roleConsistencyPolicies = new TreeSet<>();
    }

    @Override
    public String prettyPrint() {
      StringBuilder sb = new StringBuilder("Results for infer policies\n");

      for (RoleConsistencyPolicy policy : _roleConsistencyPolicies) {
        NodeRoleSpecifier specifier = policy.getNodeRoleSpecifier();
        List<String> roleRegexes = specifier.getRoleRegexes();
        SortedMap<String, SortedSet<String>> roleMap = specifier.getRoleMap();

        sb.append("Policy: nodes in the same role should have ");
        switch(policy.getHypothesis()) {
        case SAME_DEFINITION:
        case SAME_NAME:
          sb.append("same-named structures of type " + policy.getName());
          break;
        case SAME_SERVERS:
          sb.append("the same " + policy.getName());
        }
        sb.append("\n");
        sb.append("Role specifier:\n");
        if (!roleRegexes.isEmpty()) {
          sb.append("  Role regexes: \n");
          for (String regex : roleRegexes) {
            sb.append("    " + regex + "\n");
          }
        }
        if (!roleMap.isEmpty()) {
          sb.append("  Role map: \n");
          for (Map.Entry<String, SortedSet<String>> entry : specifier.getRoleMap().entrySet()) {
            sb.append("    " + entry + "\n");
          }
        }
        sb.append("\n\n");
      }

      return sb.toString();
    }

    @JsonProperty(PROP_ROLE_CONSISTENCY_POLICIES)
    public Set<RoleConsistencyPolicy> getRoleConsistencyPolicies() {
      return _roleConsistencyPolicies;
    }

    public void addRoleConsistencyPolicies(Set<RoleConsistencyPolicy> roleConsistencyPolicies) {
      _roleConsistencyPolicies.addAll(roleConsistencyPolicies);
    }
  }

  public static class InferPoliciesAnswerer extends Answerer {

    private InferPoliciesAnswerElement _answerElement;

    // the minimum percentage of nodes that must conform to a policy, in order for
    // the policy to be returned
    private static final double CONFORMERS_THRESHOLD = 0.9;

    public InferPoliciesAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public InferPoliciesAnswerElement answer() {

      _answerElement = new InferPoliciesAnswerElement();

      _answerElement.addRoleConsistencyPolicies(serverConsistencyPolicies());
      _answerElement.addRoleConsistencyPolicies(nameConsistencyPolicies());

      return _answerElement;
    }

    private SortedSet<RoleConsistencyPolicy> serverConsistencyPolicies() {

      SortedMap<String, AnswerElement> roleAnswers =
          perRoleOutlierInfo(OutliersHypothesis.SAME_SERVERS);

      Multimap<String, OutlierSet<NavigableSet<String>>> outliersPerPropertyName =
          outliersByProperty(roleAnswers.values(), OutliersAnswerElement::getServerOutliers);

      // remove outlier sets where no nodes declare any servers
      for (String name : outliersPerPropertyName.keySet()) {
        Collection<OutlierSet<NavigableSet<String>>> outlierSets =
            outliersPerPropertyName.get(name);
        if (outlierSets
            .stream()
            .allMatch(oset -> oset.getDefinition().isEmpty() && oset.getOutliers().isEmpty())) {
          outliersPerPropertyName.removeAll(name);
        }
      }

      return policiesAboveThreshold(outliersPerPropertyName);
    }

    private SortedSet<RoleConsistencyPolicy> nameConsistencyPolicies() {

      SortedMap<String, AnswerElement> roleAnswers =
          perRoleOutlierInfo(OutliersHypothesis.SAME_NAME);

      Multimap<String, NamedStructureOutlierSet<?>> outliersPerStructureType =
          outliersByProperty(
              roleAnswers.values(), OutliersAnswerElement::getNamedStructureOutliers);

      return policiesAboveThreshold(outliersPerStructureType);
    }


    // obtain all outlier sets for the given hypothesis
    private SortedMap<String, AnswerElement> perRoleOutlierInfo(OutliersHypothesis hypothesis) {
      OutliersQuestion innerQ = new OutliersQuestionPlugin().createQuestion();
      innerQ.setHypothesis(hypothesis);
      innerQ.setVerbose(true);

      PerRoleQuestionPlugin outerPlugin = new PerRoleQuestionPlugin();
      PerRoleQuestion outerQ = outerPlugin.createQuestion();
      outerQ.setQuestion(innerQ);

      PerRoleAnswerElement roleAE = outerPlugin.createAnswerer(outerQ, _batfish).answer();

      SortedMap<String, AnswerElement> answers = roleAE.getAnswers();
      for (Map.Entry<String, AnswerElement> entry : answers.entrySet()) {
        String role = entry.getKey();
        OutliersAnswerElement oae = (OutliersAnswerElement) entry.getValue();
        // update each outlier set to know its associated role
        setRoleAll(oae.getServerOutliers(), role);
        setRoleAll(oae.getNamedStructureOutliers(), role);
      }
      return answers;
    }

    private <T extends NodePropertyOutlierSet> Multimap<String, T> outliersByProperty(
        Collection<AnswerElement> answers,
        Function<OutliersAnswerElement, Collection<T>> accessor) {

      Multimap<String, T> outliersPerProperty = LinkedListMultimap.create();

      // partition the resulting outliers by structure type (e.g., Ip Access List, Route Map)
      for (AnswerElement ae : answers) {
        OutliersAnswerElement oae = (OutliersAnswerElement) ae;
        for (T os : accessor.apply(oae)) {
          outliersPerProperty.put(os.getName(), os);
        }
      }
      return outliersPerProperty;
    }

    private <T extends NodePropertyOutlierSet>
    SortedSet<RoleConsistencyPolicy> policiesAboveThreshold(
        Multimap<String, T> outliersPerPropertyName) {
      NodeRoleSpecifier nodeRoleSpecifier = _batfish.getNodeRoleSpecifier(false);
      SortedSet<RoleConsistencyPolicy> policies =
          new TreeSet<>(Comparator.comparing(RoleConsistencyPolicy::getName));
      for (String name : outliersPerPropertyName.keySet()) {
        Collection<T> outlierSets = outliersPerPropertyName.get(name);
        int conformers = outlierSets.stream().mapToInt(oset -> oset.getConformers().size()).sum();
        int outliers = outlierSets.stream().mapToInt(oset -> oset.getOutliers().size()).sum();
        double all = (double) conformers + outliers;
        if (conformers / all >= CONFORMERS_THRESHOLD) {
          policies.add(
              new RoleConsistencyPolicy(nodeRoleSpecifier, name, OutliersHypothesis.SAME_SERVERS));
        }
      }
      return policies;
    }

    private void setRoleAll(Collection<? extends NodePropertyOutlierSet> outlierSets, String role) {
      for (NodePropertyOutlierSet os : outlierSets) {
        os.setRole(role);
      }
    }
  }

  // <question_page_comment>
  /**
   * Infer likely role-based consistency policies for the network. Each such policy in general has
   * the form: all nodes that have the same role, according to a given role specifier S, must be
   * consistent with one another in some particular way.
   *
   * @type InferPolicies multifile
   */
  public static final class InferPoliciesQuestion extends Question {

    public InferPoliciesQuestion() {}

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "InferPolicies";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new InferPoliciesAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new InferPoliciesQuestion();
  }
}

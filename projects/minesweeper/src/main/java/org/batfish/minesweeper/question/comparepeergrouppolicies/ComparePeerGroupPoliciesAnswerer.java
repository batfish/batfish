package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;
import org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesAnswerer;
import org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer;

public class ComparePeerGroupPoliciesAnswerer extends Answerer {

  public ComparePeerGroupPoliciesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /** This question is only working in Differential mode. */
  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {

    throw new UnsupportedOperationException(
        "SemDiff is only meant to be used in differential mode.");
  }

  @Override
  public AnswerElement answerDiff(NetworkSnapshot snapshot, NetworkSnapshot reference) {
    List<Row> answers =
        ComparePeerGroupPoliciesUtils.getDifferencesStream(_batfish, snapshot, reference)
            .map(t -> TestRoutePoliciesAnswerer.toCompareRow(t.getFirst(), t.getSecond()))
            .collect(ImmutableList.toImmutableList());

    return CompareRoutePoliciesAnswerer.toTableAnswer(_question, answers);
  }
}

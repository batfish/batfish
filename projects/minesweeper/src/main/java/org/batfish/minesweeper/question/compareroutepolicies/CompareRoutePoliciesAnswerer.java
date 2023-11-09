package org.batfish.minesweeper.question.compareroutepolicies;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer;

/** An answerer for {@link CompareRoutePoliciesQuestion}. */
@ParametersAreNonnullByDefault
public final class CompareRoutePoliciesAnswerer extends Answerer {

  private final @Nonnull CompareRoutePoliciesUtils _utils;

  public CompareRoutePoliciesAnswerer(
      org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesQuestion question,
      IBatfish batfish) {
    super(question, batfish);
    _utils =
        new CompareRoutePoliciesUtils(
            question.getDirection(),
            question.getPolicy(),
            question.getReferencePolicy(),
            question.getNodes(),
            _batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    throw new BatfishException(
        String.format("%s can only be run in differential mode.", _question.getName()));
  }

  public static TableAnswerElement toTableAnswer(Question question, List<Row> rows) {
    TableAnswerElement answerElement =
        new TableAnswerElement(TestRoutePoliciesAnswerer.compareMetadata());
    answerElement.postProcessAnswer(question, rows);
    return answerElement;
  }

  @Override
  public AnswerElement answerDiff(NetworkSnapshot snapshot, NetworkSnapshot reference) {
    List<Row> rows =
        _utils
            .getDifferencesStream(snapshot, reference)
            .map(t -> TestRoutePoliciesAnswerer.toCompareRow(t.getFirst(), t.getSecond()))
            .collect(ImmutableList.toImmutableList());
    return toTableAnswer(_question, rows);
  }

  public @Nonnull CompareRoutePoliciesUtils getUtils() {
    return _utils;
  }
}

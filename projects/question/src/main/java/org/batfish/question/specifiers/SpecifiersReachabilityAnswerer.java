package org.batfish.question.specifiers;

import org.apache.commons.lang3.StringUtils;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.flow.TraceWrapperAsAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.traceroute.TracerouteAnswerer;

/** Produces a {@link TableAnswerElement} for a {@link SpecifiersReachabilityQuestion} */
public final class SpecifiersReachabilityAnswerer extends Answerer {

  public SpecifiersReachabilityAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    SpecifiersReachabilityQuestion question = (SpecifiersReachabilityQuestion) _question;
    AnswerElement answer = _batfish.standard(snapshot, question.getReachabilityParameters());
    if (answer instanceof TraceWrapperAsAnswerElement) {
      TableAnswerElement tableAnswer = new TableAnswerElement(TracerouteAnswerer.metadata(false));
      TracerouteAnswerer.flowTracesToRows(
              ((TraceWrapperAsAnswerElement) answer).getFlowTraces(), question.getMaxTraces())
          .forEach(tableAnswer::addRow);
      return tableAnswer;
    } else {
      return answer;
    }
  }

  private static final String[] DIFFERENTIAL_ALTERNATIVES =
      new String[] {
        "Differential Reachability"
            + " https://pybatfish.readthedocs.io/en/latest/questions.html#pybatfish.question.bfq.differentialReachability",
        "Routes (in differential mode)"
            + " https://pybatfish.readthedocs.io/en/latest/questions.html#pybatfish.question.bfq.routes",
      };

  @Override
  public AnswerElement answerDiff(NetworkSnapshot snapshot, NetworkSnapshot reference) {
    throw new IllegalArgumentException(
        "This question should not be run in differential mode. Instead, consider: "
            + StringUtils.join(DIFFERENTIAL_ALTERNATIVES, ", "));
  }
}

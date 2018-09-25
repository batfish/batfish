package org.batfish.question.specifiers;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.traceroute.TracerouteAnswerer;

/** Produces a {@link TableAnswerElement} for a {@link SpecifiersReachabilityQuestion} */
public final class SpecifiersReachabilityAnswerer extends Answerer {

  public SpecifiersReachabilityAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    SpecifiersReachabilityQuestion question = (SpecifiersReachabilityQuestion) _question;
    TableAnswerElement answer = new TableAnswerElement(TracerouteAnswerer.createMetadata(false));
    TracerouteAnswerer.flowHistoryToRows(
            (FlowHistory) _batfish.standard(question.getReachabilityParameters()), false)
        .forEach(answer::addRow);
    return answer;
  }
}

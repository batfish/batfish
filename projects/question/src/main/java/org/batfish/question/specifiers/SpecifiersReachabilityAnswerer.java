package org.batfish.question.specifiers;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.FlowHistory;
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
  public AnswerElement answer() {
    SpecifiersReachabilityQuestion question = (SpecifiersReachabilityQuestion) _question;
    AnswerElement answer = _batfish.standard(question.getReachabilityParameters());
    if (answer instanceof TraceWrapperAsAnswerElement) {
      TableAnswerElement tableAnswer = new TableAnswerElement(TracerouteAnswerer.metadata(false));
      TracerouteAnswerer.flowTracesToRows(
              ((TraceWrapperAsAnswerElement) answer).getFlowTraces(), question.getMaxTraces())
          .forEach(tableAnswer::addRow);
      return tableAnswer;
    } else if (answer instanceof FlowHistory) {
      TableAnswerElement tableAnswer =
          new TableAnswerElement(TracerouteAnswerer.createMetadata(false));
      TracerouteAnswerer.flowHistoryToRows((FlowHistory) answer, false)
          .forEach(tableAnswer::addRow);
      return tableAnswer;
    } else {
      return answer;
    }
  }
}

package org.batfish.datamodel.flow;

import java.util.List;
import java.util.SortedMap;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.answers.AnswerElement;

/** A wrapper around new {@link Trace} answers to allow returning an {@link AnswerElement} */
public class TraceWrapperAsAnswerElement extends AnswerElement {
  private final SortedMap<Flow, List<Trace>> _flowTraces;

  public TraceWrapperAsAnswerElement(SortedMap<Flow, List<Trace>> flowTraces) {
    _flowTraces = flowTraces;
  }

  public SortedMap<Flow, List<Trace>> getFlowTraces() {
    return _flowTraces;
  }
}

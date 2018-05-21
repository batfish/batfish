package org.batfish.symbolic.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.symbolic.ainterpreter.AbstractFlowTrace;

public class AiReachabilityAnswerElement extends AnswerElement {

  private static final String PROP_ABSTRACT_FLOW_TRACES = "abstractFlowTraces";

  private List<AbstractFlowTrace> _abstractFlowTraces;

  @JsonProperty(PROP_ABSTRACT_FLOW_TRACES)
  public List<AbstractFlowTrace> getAbstractFlowTraces() {
    return _abstractFlowTraces;
  }

  @JsonProperty(PROP_ABSTRACT_FLOW_TRACES)
  public void setAbstractFlowTraces(List<AbstractFlowTrace> abstractFlowTraces) {
    this._abstractFlowTraces = abstractFlowTraces;
  }
}

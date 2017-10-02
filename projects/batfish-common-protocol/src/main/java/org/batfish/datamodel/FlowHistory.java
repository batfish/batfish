package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;

public class FlowHistory implements AnswerElement {

  public static class FlowPathsPair {

    private static final String PROP_FLOW = "flow";

    private static final String PROP_PATHS = "paths";

    private Flow _flow;

    private SortedMap<String, SortedSet<FlowTrace>> _paths;

    @JsonCreator
    public FlowPathsPair(
        @JsonProperty(PROP_FLOW) Flow flow,
        @JsonProperty(PROP_PATHS) SortedMap<String, SortedSet<FlowTrace>> paths) {
      _flow = flow;
      _paths = paths;
    }

    public Flow getFlow() {
      return _flow;
    }

    public SortedMap<String, SortedSet<FlowTrace>> getPaths() {
      return _paths;
    }
  }

  private SortedMap<String, FlowPathsPair> _traces;

  public FlowHistory() {
    _traces = new TreeMap<>();
  }

  public void addFlowTrace(Flow flow, String environment, FlowTrace trace) {
    String flowText = flow.toString();
    if (!_traces.containsKey(flowText)) {
      _traces.put(flowText, new FlowPathsPair(flow, new TreeMap<>()));
    }
    if (!_traces.get(flowText).getPaths().containsKey(environment)) {
      _traces.get(flowText).getPaths().put(environment, new TreeSet<>());
    }
    _traces.get(flowText).getPaths().get(environment).add(trace);
  }

  public SortedMap<String, FlowPathsPair> getTraces() {
    return _traces;
  }

  @Override
  public String prettyPrint() {
    StringBuilder retString = new StringBuilder("\n");
    for (String flowStr : _traces.keySet()) {
      Flow flow = _traces.get(flowStr).getFlow();
      retString.append(flow.prettyPrint("") + "\n");
      for (String environment : _traces.get(flowStr).getPaths().keySet()) {
        retString.append("  environment:" + environment + "\n");
        for (FlowTrace trace : _traces.get(flowStr).getPaths().get(environment)) {
          retString.append(trace.toString("    ") + "\n");
        }
      }
    }
    return retString.toString();
  }

  private void setTraces(
      SortedMap<String, FlowPathsPair> traces) {
    _traces = traces;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String flowString : _traces.keySet()) {
      Flow flow = _traces.get(flowString).getFlow();
      sb.append("Flow: " + flowString + "\n");
      SortedMap<String, SortedSet<FlowTrace>> envTraceSetMap = _traces.get(flowString).getPaths();
      for (String environmentName : envTraceSetMap.keySet()) {
        sb.append(" Environment: " + environmentName + "\n");
        Set<FlowTrace> traces = envTraceSetMap.get(environmentName);
        int i = 0;
        for (FlowTrace trace : traces) {
          i++;
          sb.append("  Trace: " + i + "\n");
          String rawTraceString = trace.toString();
          String traceString = CommonUtil.getIndentedString(rawTraceString, 3);
          sb.append(traceString);
        }
      }
    }
    return sb.toString();
  }
}

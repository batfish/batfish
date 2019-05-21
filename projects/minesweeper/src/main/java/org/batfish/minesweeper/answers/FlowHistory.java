package org.batfish.minesweeper.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.pojo.Environment;

public class FlowHistory extends AnswerElement {
  private static final String PROP_TRACES = "traces";

  public static class FlowHistoryInfo {
    private static final String PROP_ENVIRONMENTS = "environments";
    private static final String PROP_FLOW = "flow";
    private static final String PROP_PATHS = "paths";

    private Map<String, Environment> _environments;

    private Flow _flow;

    private Map<String, Set<FlowTrace>> _paths;

    @JsonCreator
    public FlowHistoryInfo(
        @JsonProperty(PROP_FLOW) Flow flow,
        @JsonProperty(PROP_ENVIRONMENTS) Map<String, Environment> environments,
        @JsonProperty(PROP_PATHS) Map<String, Set<FlowTrace>> paths) {
      _flow = flow;
      _environments = environments;
      _paths = paths;
    }

    @JsonProperty(PROP_ENVIRONMENTS)
    public Map<String, Environment> getEnvironments() {
      return _environments;
    }

    @JsonProperty(PROP_FLOW)
    public Flow getFlow() {
      return _flow;
    }

    @JsonProperty(PROP_PATHS)
    public Map<String, Set<FlowTrace>> getPaths() {
      return _paths;
    }
  }

  private Map<String, FlowHistoryInfo> _traces;

  public FlowHistory() {
    _traces = new TreeMap<>();
  }

  public void addFlowTrace(Flow flow, String envTag, Environment environment, FlowTrace trace) {
    String flowText = flow.toString();
    if (!_traces.containsKey(flowText)) {
      _traces.put(flowText, new FlowHistoryInfo(flow, new TreeMap<>(), new TreeMap<>()));
    }
    if (_traces.get(flowText).getEnvironments().containsKey(envTag)) {
      if (!_traces.get(flowText).getEnvironments().get(envTag).equals(environment)) {
        throw new BatfishException(
            "Different environment with the same tag '" + envTag + "' is being added: ");
      }
    } else {
      _traces.get(flowText).getEnvironments().put(envTag, environment);
    }
    if (!_traces.get(flowText).getPaths().containsKey(envTag)) {
      _traces.get(flowText).getPaths().put(envTag, new TreeSet<>());
    }
    _traces.get(flowText).getPaths().get(envTag).add(trace);
  }

  @JsonProperty(PROP_TRACES)
  public Map<String, FlowHistoryInfo> getTraces() {
    return _traces;
  }

  @JsonProperty(PROP_TRACES)
  private void setTraces(SortedMap<String, FlowHistoryInfo> traces) {
    _traces = traces;
  }
}

package org.batfish.datamodel.flow2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class TraceHop implements Comparable<TraceHop>, Serializable {

  private static final String PROP_NODE_NAME = "nodeName";
  private static final String PROP_STEPS = "steps";

  /** */
  private static final long serialVersionUID = 1L;

  private @Nullable String _nodeName;

  private List<Step> _steps;

  @JsonCreator
  public TraceHop(
      @JsonProperty(PROP_NODE_NAME) @Nullable String nodeName,
      @JsonProperty(PROP_STEPS) @Nullable List<Step> steps) {
    _nodeName = nodeName;
    _steps = steps;
  }

  @JsonProperty(PROP_NODE_NAME)
  @Nullable
  public String getNodeName() {
    return _nodeName;
  }

  @JsonProperty(PROP_STEPS)
  public List<Step> getSteps() {
    return _steps;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TraceHop)) {
      return false;
    }
    TraceHop other = (TraceHop) o;
    return Objects.equals(_nodeName, other._nodeName) && Objects.equals(_steps, other._steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeName, _steps);
  }

  @Override
  public int compareTo(TraceHop o) {
    return Comparator.comparing(TraceHop::getNodeName).compare(this, o);
  }
}

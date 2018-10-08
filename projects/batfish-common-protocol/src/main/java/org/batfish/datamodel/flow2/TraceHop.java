package org.batfish.datamodel.flow2;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class TraceHop {

  private String _nodeName;

  private List<Step> _steps;

  public TraceHop(String nodeName, List<Step> steps) {
    _nodeName = nodeName;
    _steps = steps;
  }

  public String getNodeName() {
    return _nodeName;
  }

  public List<Step> getSteps() {
    return _steps;
  }

  @Nullable
  public Step getLastStep() {
    if (_steps.isEmpty()) {
      return null;
    }
    return _steps.get(_steps.size() - 1);
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
}

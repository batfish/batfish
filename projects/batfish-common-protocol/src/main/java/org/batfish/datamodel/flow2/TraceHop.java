package org.batfish.datamodel.flow2;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TraceHop implements Comparable<TraceHop>, Serializable {

  /** */
  private static final long serialVersionUID = 1L;

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

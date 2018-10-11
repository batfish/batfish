package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;

/** Represents a hop in a particular {@link Trace} of a {@link Flow} */
public class Hop implements Comparable<Hop>, Serializable {

  private static final String PROP_NODE_NAME = "nodeName";
  private static final String PROP_STEPS = "steps";

  /** */
  private static final long serialVersionUID = 1L;

  /** Name of the node for this {@link Hop} */
  private @Nullable String _nodeName;

  /** {@link List} of {@link Step} present for the given {@link Hop} */
  private List<Step> _steps;

  @JsonCreator
  public Hop(
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
    if (!(o instanceof Hop)) {
      return false;
    }
    Hop other = (Hop) o;
    return Objects.equals(_nodeName, other._nodeName) && Objects.equals(_steps, other._steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeName, _steps);
  }

  @Override
  public int compareTo(Hop o) {
    return Comparator.comparing(Hop::getNodeName).compare(this, o);
  }
}

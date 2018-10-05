package org.batfish.datamodel.flow2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow2.HopStepGeneral.HopStepGeneralDetail;

/** Represents the result of performing a traceroute for a {@link Flow} */
public class Trace {

  private static final String PROP_DISPOSITION = "disposition";
  private static final String PROP_HOPS = "hops";
  private static final String PROP_NOTES = "notes";

  private final FlowDisposition _disposition;

  private final List<TraceHop> _hops;

  // TODO: get rid of notes
  private final String _notes;

  @JsonCreator
  public Trace(
      @JsonProperty(PROP_DISPOSITION) FlowDisposition disposition,
      @JsonProperty(PROP_HOPS) List<TraceHop> hops,
      @JsonProperty(PROP_NOTES) String notes) {
    _disposition = disposition;
    _hops = hops != null ? hops : ImmutableList.of();
    _notes = notes;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Trace)) {
      return false;
    }
    Trace rhs = (Trace) o;
    return _disposition == rhs._disposition
        && _hops.equals(rhs._hops)
        && Objects.equals(_notes, rhs._notes);
  }

  @JsonProperty(PROP_DISPOSITION)
  public FlowDisposition getDisposition() {
    return _disposition;
  }

  @JsonProperty(PROP_HOPS)
  public List<TraceHop> getHops() {
    return _hops;
  }

  @Nullable
  private TraceHop getLastHop() {
    if (_hops.isEmpty()) {
      return null;
    }
    return _hops.get(_hops.size() - 1);
  }

  /**
   * Get the hostname/interface of the last hop or {@code null} if the flow was no accepted.
   *
   * @return the hostname of the accepting node or {@code null} if the flow disposition is not
   *     "accepted"
   */
  @Nullable
  @JsonIgnore
  public NodeInterfacePair getAcceptingNode() {
    if (getDisposition() != FlowDisposition.ACCEPTED) {
      return null;
    }
    TraceHop lastHop = getLastHop();
    if (lastHop == null) {
      return null;
    } else {
      List<TraceHopStep> steps = lastHop.getSteps();
      if (steps.isEmpty()) {
        return null;
      } else {
        return ((HopStepGeneralDetail) steps.get(steps.size() - 1).getDetail())
            .getOutputInterface();
      }
    }
  }

  @JsonProperty(PROP_NOTES)
  public String getNotes() {
    return _notes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hops, _disposition.ordinal(), _notes);
  }

}

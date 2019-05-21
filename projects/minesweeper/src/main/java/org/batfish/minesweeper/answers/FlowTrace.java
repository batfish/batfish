package org.batfish.minesweeper.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Represents the result of performing a traceroute for a {@link Flow} */
public class FlowTrace implements Comparable<FlowTrace> {
  private static final String PROP_DISPOSITION = "disposition";
  private static final String PROP_HOPS = "hops";
  private static final String PROP_NOTES = "notes";

  private final FlowDisposition _disposition;

  private final List<FlowTraceHop> _hops;

  private final String _notes;

  @JsonCreator
  public FlowTrace(
      @JsonProperty(PROP_DISPOSITION) FlowDisposition disposition,
      @JsonProperty(PROP_HOPS) List<FlowTraceHop> hops,
      @JsonProperty(PROP_NOTES) String notes) {
    _disposition = disposition;
    _hops = hops != null ? hops : ImmutableList.of();
    _notes = notes;
  }

  @Override
  public int compareTo(FlowTrace rhs) {
    return Comparator.comparing(FlowTrace::getHops, Comparators.lexicographical(Ordering.natural()))
        .thenComparing(FlowTrace::getDisposition)
        .thenComparing(FlowTrace::getNotes, Comparator.nullsFirst(Ordering.natural()))
        .compare(this, rhs);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof FlowTrace)) {
      return false;
    }
    FlowTrace rhs = (FlowTrace) o;
    return _disposition == rhs._disposition
        && _hops.equals(rhs._hops)
        && Objects.equals(_notes, rhs._notes);
  }

  @JsonProperty(PROP_DISPOSITION)
  public FlowDisposition getDisposition() {
    return _disposition;
  }

  @JsonProperty(PROP_HOPS)
  public List<FlowTraceHop> getHops() {
    return _hops;
  }

  @Nullable
  private FlowTraceHop getLastHop() {
    int numHops = getHops().size();
    if (numHops == 0) {
      return null;
    }
    return getHops().get(numHops - 1);
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
    FlowTraceHop lastHop = getLastHop();
    return lastHop == null ? null : lastHop.getEdge().getHead();
  }

  @JsonProperty(PROP_NOTES)
  public String getNotes() {
    return _notes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hops, _disposition.ordinal(), _notes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < _hops.size(); i++) {
      FlowTraceHop hop = _hops.get(i);
      Set<String> routes = hop.getRoutes();
      String transformedFlowString = "";
      Flow transformedFlow = hop.getTransformedFlow();
      if (transformedFlow != null) {
        transformedFlowString = " ***TRANSFORMED:" + transformedFlow + "***";
      }
      String routesStr = routes.isEmpty() ? (" --- " + routes) : "";
      String filterOutStr =
          hop.getFilterOut() != null ? (" -- [out: " + hop.getFilterOut() + "]") : "";
      String filterInStr = hop.getFilterIn() != null ? (" -- [in: " + hop.getFilterIn() + "]") : "";
      Edge edge = hop.getEdge();
      int num = i + 1;
      sb.append("Hop ")
          .append(num)
          .append(": ")
          .append(edge.getNode1())
          .append(":")
          .append(edge.getInt1())
          .append(" -> ")
          .append(edge.getNode2())
          .append(":")
          .append(edge.getInt2())
          .append(transformedFlowString)
          .append(routesStr)
          .append(filterOutStr)
          .append(filterInStr)
          .append("\n");
    }
    sb.append(_notes).append("\n");
    return sb.toString();
  }
}

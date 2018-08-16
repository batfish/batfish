package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
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
    for (int i = 0; i < _hops.size(); i++) {
      if (rhs._hops.size() < i + 1) {
        return 1;
      }
      Edge leftHop = _hops.get(i).getEdge();
      Edge rightHop = rhs._hops.get(i).getEdge();
      int result = leftHop.compareTo(rightHop);
      if (result != 0) {
        return result;
      }
    }
    if (rhs._hops.size() == _hops.size()) {
      return _disposition.compareTo(rhs._disposition);
    } else {
      return -1;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof FlowTrace)) {
      return false;
    }
    FlowTrace rhs = (FlowTrace) o;
    return _disposition == rhs._disposition && _hops.equals(rhs._hops);
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
    return lastHop == null ? null : lastHop.getEdge().getInterface2();
  }

  @JsonProperty(PROP_NOTES)
  public String getNotes() {
    return _notes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _disposition.ordinal();
    result = prime * result + _hops.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String prefixString) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < _hops.size(); i++) {
      FlowTraceHop hop = _hops.get(i);
      Set<String> routes = hop.getRoutes();
      String transformedFlowString = "";
      Flow transformedFlow = hop.getTransformedFlow();
      if (transformedFlow != null) {
        transformedFlowString = " ***TRANSFORMED:" + transformedFlow.prettyPrint("") + "***";
      }
      String routesStr = routes != null ? (" --- " + routes) : "";
      String filterOutStr =
          hop.getFilterOut() != null ? (" -- [out: " + hop.getFilterOut() + "]") : "";
      String filterInStr = hop.getFilterIn() != null ? (" -- [in: " + hop.getFilterIn() + "]") : "";
      Edge edge = hop.getEdge();
      int num = i + 1;
      sb.append(prefixString)
          .append("Hop ")
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
    sb.append(prefixString).append(_notes).append("\n");
    return sb.toString();
  }
}

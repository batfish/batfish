package org.batfish.datamodel.flow2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;

/** Represents the result of performing a traceroute for a {@link Flow} */
public class Trace implements Comparable<Trace> {

  private static final String PROP_DISPOSITION = "disposition";
  private static final String PROP_HOPS = "hops";

  private final FlowDisposition _disposition;

  private final List<TraceHop> _hops;

  @JsonCreator
  public Trace(
      @JsonProperty(PROP_DISPOSITION) FlowDisposition disposition,
      @JsonProperty(PROP_HOPS) List<TraceHop> hops) {
    _disposition = disposition;
    _hops = hops != null ? hops : ImmutableList.of();
  }

  @Override
  public int compareTo(Trace rhs) {
    return Comparator.comparing(Trace::getHops, Comparators.lexicographical(Ordering.natural()))
        .thenComparing(Trace::getDisposition)
        .compare(this, rhs);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Trace)) {
      return false;
    }
    Trace rhs = (Trace) o;
    return _disposition == rhs._disposition && _hops.equals(rhs._hops);
  }

  @JsonProperty(PROP_DISPOSITION)
  public FlowDisposition getDisposition() {
    return _disposition;
  }

  @JsonProperty(PROP_HOPS)
  public List<TraceHop> getHops() {
    return _hops;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hops, _disposition.ordinal());
  }
}

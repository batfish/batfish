package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.trace.TraceTree;

public final class AclTrace implements Serializable {
  private static final String PROP_EVENTS = "events";

  private final List<TraceEvent> _events;

  @JsonCreator
  public AclTrace(@JsonProperty(PROP_EVENTS) @Nullable Iterable<TraceEvent> events) {
    _events = events != null ? ImmutableList.copyOf(events) : ImmutableList.of();
  }

  public AclTrace(List<TraceTree> trace) {
    this(
        trace.stream()
            .flatMap(
                traceTree ->
                    Streams.stream(
                        Traverser.forTree(TraceTree::getChildren).depthFirstPreOrder(traceTree)))
            .map(TraceTree::getTraceElement)
            .filter(Objects::nonNull)
            .map(TraceEvent::of)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AclTrace)) {
      return false;
    }
    return _events.equals(((AclTrace) obj)._events);
  }

  @JsonProperty(PROP_EVENTS)
  public @Nonnull List<TraceEvent> getEvents() {
    return _events;
  }

  @Override
  public int hashCode() {
    return _events.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_EVENTS, _events).toString();
  }
}

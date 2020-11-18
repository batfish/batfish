package org.batfish.common.traceroute;

import java.util.stream.Stream;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/**
 * A DAG representation of a collection of {@link TraceAndReverseFlow traces}. Every path through
 * the DAG from a root to a leaf is a valid {@link TraceAndReverseFlow trace}, i.e. one that {@link
 * org.batfish.common.plugin.TracerouteEngine} would create.
 */
public interface TraceDag {

  /** Returns the number of edges in the DAG. */
  int countEdges();

  /** Returns the number of nodes in the DAG. */
  int countNodes();

  /** Returns the number of traces, aka the number of paths through the DAG. */
  int size();

  /** Returns a stream of the {@link TraceAndReverseFlow} corresponding to the traces in this DAG */
  Stream<TraceAndReverseFlow> getTraces();
}

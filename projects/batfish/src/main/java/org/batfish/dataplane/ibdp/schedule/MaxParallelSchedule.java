package org.batfish.dataplane.ibdp.schedule;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import org.batfish.dataplane.ibdp.Node;

/** Allows all nodes to exchange routes at the same time */
public final class MaxParallelSchedule extends IbdpSchedule {
  protected boolean _hasNext = true;

  public MaxParallelSchedule(Map<String, Node> nodes) {
    super(nodes);
  }

  @Override
  public void forEachRemaining(Consumer<? super Map<String, Node>> action) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean hasNext() {
    return _hasNext;
  }

  @Override
  public Map<String, Node> next() {
    if (_hasNext) {
      _hasNext = false;
      return _nodes;
    }
    throw new NoSuchElementException();
  }
}

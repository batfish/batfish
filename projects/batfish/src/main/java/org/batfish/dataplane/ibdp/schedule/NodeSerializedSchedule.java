package org.batfish.dataplane.ibdp.schedule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Map;
import java.util.Map.Entry;
import org.batfish.dataplane.ibdp.Node;

/** A dataplane computation schedule that allows processing of messages at one node at a time */
public final class NodeSerializedSchedule extends IbdpSchedule {

  private UnmodifiableIterator<Entry<String, Node>> _nodeIterator;

  NodeSerializedSchedule(Map<String, Node> nodes) {
    super(nodes);
    _nodeIterator = ImmutableMap.copyOf(nodes).entrySet().iterator();
  }

  @Override
  public boolean hasNext() {
    return _nodeIterator.hasNext();
  }

  @Override
  public Map<String, Node> next() {
    Entry<String, Node> e = _nodeIterator.next();
    return ImmutableMap.of(e.getKey(), e.getValue());
  }
}

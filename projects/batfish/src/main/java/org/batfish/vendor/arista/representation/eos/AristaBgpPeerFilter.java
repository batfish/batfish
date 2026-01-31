package org.batfish.vendor.arista.representation.eos;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.LongSpace;
import org.batfish.vendor.arista.representation.eos.AristaBgpPeerFilterLine.Action;

/**
 * BGP peer filter See http://www.arista.com/en/um-eos/eos-section-33-2-configuring-bgp#ww1319501
 */
public final class AristaBgpPeerFilter implements Serializable {

  public AristaBgpPeerFilter(String name) {
    _name = name;
    _lines = new TreeMap<>();
  }

  public String getName() {
    return _name;
  }

  /** Add a new line to this peer filter. Sequence number will be computed automatically */
  public void addLine(LongSpace range, Action action) {
    addLine(_lines.isEmpty() ? 0 : _lines.lastKey() + 10, range, action);
  }

  /**
   * Add a new line to this peer filter. Any existing lines with same sequence number will be
   * replaced.
   */
  public void addLine(int seq, LongSpace range, Action action) {
    _lines.put(seq, new AristaBgpPeerFilterLine(seq, range, action));
  }

  /** Convert this peer filter to a {@link LongSpace} of AS numbers that are allowed */
  public LongSpace toLongSpace() {
    // Special case no matching lines
    if (_lines.isEmpty()) {
      return BgpPeerConfig.ALL_AS_NUMBERS;
    }

    // Build the space in the reverse order of the lines, using union/difference operations.
    LongSpace currentSpace = LongSpace.EMPTY;
    for (AristaBgpPeerFilterLine l : ImmutableList.copyOf(_lines.values()).reverse()) {
      if (l.getAction() == Action.ACCEPT) {
        currentSpace = currentSpace.union(l.getRange());
      } else if (l.getAction() == Action.REJECT) {
        currentSpace = currentSpace.difference(l.getRange());
      } else {
        throw new IllegalStateException(
            String.format("Unsupported bgp peer-filter action: %s", l.getAction()));
      }
    }
    return currentSpace;
  }

  private final String _name;
  private final SortedMap<Integer, AristaBgpPeerFilterLine> _lines;
}

package org.batfish.representation.arista.eos;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.LongSpace.Builder;
import org.batfish.representation.arista.eos.AristaBgpPeerFilterLine.Action;

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
    Builder builder = LongSpace.builder();
    _lines
        .values()
        .forEach(
            l -> {
              if (l.getAction() == AristaBgpPeerFilterLine.Action.ACCEPT) {
                builder.including(l.getRange());
              } else if (l.getAction() == AristaBgpPeerFilterLine.Action.REJECT) {
                builder.excluding(l.getRange());
              } else {
                throw new IllegalStateException(
                    String.format("Unsupported bgp peer-filter action: %s", l.getAction()));
              }
            });
    /*
    TODO: fix inclusion semantics
      LongSpace builder processes all inclusions first, followed by all exclusions.
      This is equivalent to processing all reject lines first, which will crate problems if
      a peer filter contains an unreachable reject line.
    */
    return builder.build();
  }

  private final String _name;
  private final SortedMap<Integer, AristaBgpPeerFilterLine> _lines;
}

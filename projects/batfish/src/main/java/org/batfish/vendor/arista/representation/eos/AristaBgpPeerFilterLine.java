package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import org.batfish.datamodel.LongSpace;

/** A line in an {@link AristaBgpPeerFilter} */
public final class AristaBgpPeerFilterLine implements Serializable {

  public AristaBgpPeerFilterLine(int sequence, LongSpace range, Action action) {
    _sequence = sequence;
    _range = range;
    _action = action;
  }

  public int getSequence() {
    return _sequence;
  }

  public LongSpace getRange() {
    return _range;
  }

  public Action getAction() {
    return _action;
  }

  public enum Action {
    ACCEPT,
    REJECT,
  }

  private final int _sequence;
  private final LongSpace _range;
  private final Action _action;
}

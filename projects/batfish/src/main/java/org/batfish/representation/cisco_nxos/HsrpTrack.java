package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Track action for an {@link HsrpGroup}. */
public final class HsrpTrack implements Serializable {

  public HsrpTrack(int trackObjectNumber, @Nullable Integer decrement) {
    _trackObjectNumber = trackObjectNumber;
    _decrement = decrement;
  }

  public int getTrackObjectNumber() {
    return _trackObjectNumber;
  }

  /**
   * When tracked object is down, represents value by which priority of {@link HsrpGroup} should be
   * decremented. If {@code null}, {@link HsrpGroup} should be disabled entirely instead.
   */
  public @Nullable Integer getDecrement() {
    return _decrement;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final int _trackObjectNumber;
  private final @Nullable Integer _decrement;
}

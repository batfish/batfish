package org.batfish.representation.cisco_nxos;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Track action for an {@link HsrpGroup}. */
public final class HsrpTrack implements Serializable {

  private static final int DEFAULT_DECREMENT = 10;

  public HsrpTrack(int trackObjectNumber, @Nullable Integer decrement) {
    _trackObjectNumber = trackObjectNumber;
    _decrement = firstNonNull(decrement, DEFAULT_DECREMENT);
  }

  /**
   * Value by which priority of {@link HsrpGroup} should be decremented when tracked object is down.
   */
  public int getDecrement() {
    return _decrement;
  }

  public int getTrackObjectNumber() {
    return _trackObjectNumber;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final int _decrement;
  private final int _trackObjectNumber;
}

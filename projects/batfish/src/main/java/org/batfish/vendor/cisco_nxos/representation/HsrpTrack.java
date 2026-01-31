package org.batfish.vendor.cisco_nxos.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Track action for an {@link HsrpGroup}. */
public final class HsrpTrack implements Serializable {

  private static final int DEFAULT_DECREMENT = 10;

  public HsrpTrack(int trackObjectNumber, @Nullable Integer decrement) {
    _trackObjectNumber = trackObjectNumber;
    _decrement = decrement;
  }

  /**
   * Configured value to subtract from priority of {@link HsrpGroup} when tracked object is down.
   */
  public @Nullable Integer getDecrement() {
    return _decrement;
  }

  /** Effective value to subtract from priority of {@link HsrpGroup} when tracked object is down. */
  public int getDecrementEffective() {
    return firstNonNull(_decrement, DEFAULT_DECREMENT);
  }

  public int getTrackObjectNumber() {
    return _trackObjectNumber;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final @Nullable Integer _decrement;
  private final int _trackObjectNumber;
}

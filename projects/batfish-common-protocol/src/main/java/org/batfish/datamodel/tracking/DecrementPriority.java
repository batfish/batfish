package org.batfish.datamodel.tracking;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;

/** Decrements HSRP/VRRP priority by a given subtrahend when the tracked object is not available */
public class DecrementPriority implements TrackAction {
  private static final String PROP_SUBTRAHEND = "subtrahend";

  private final int _subtrahend;

  @JsonCreator
  public DecrementPriority(@JsonProperty(PROP_SUBTRAHEND) int subtrahend) {
    _subtrahend = subtrahend;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DecrementPriority)) {
      return false;
    }
    DecrementPriority that = (DecrementPriority) obj;
    return _subtrahend == that._subtrahend;
  }

  @JsonProperty(PROP_SUBTRAHEND)
  public int getSubtrahend() {
    return _subtrahend;
  }

  @Override
  public int hashCode() {
    return _subtrahend;
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(PROP_SUBTRAHEND, _subtrahend).toString();
  }

  @Override
  public void accept(GenericTrackActionVisitor visitor) {
    visitor.visitDecrementPriority(this);
  }
}

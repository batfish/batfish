package org.batfish.datamodel.tracking;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;

/** Decrements HSRP/VRRP priority by a given subtrahend when the tracked object is not available */
public class DecrementPriority implements TrackAction {
  private static final String PROP_SUBTRAHEND = "subtrahend";

  private final int _subtrahend;
  private final boolean _negateTrack;

  @JsonCreator
  public DecrementPriority(
      @JsonProperty(PROP_SUBTRAHEND) int subtrahend,
      @JsonProperty(PROP_NEGATE_TRACK) boolean negateTrack) {
    _subtrahend = subtrahend;
    _negateTrack = negateTrack;
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
    return _subtrahend == that._subtrahend && _negateTrack == that._negateTrack;
  }

  @JsonProperty(PROP_SUBTRAHEND)
  public int getSubtrahend() {
    return _subtrahend;
  }

  @JsonProperty(PROP_NEGATE_TRACK)
  @Override
  public boolean getNegateTrack() {
    return _negateTrack;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_subtrahend, _negateTrack);
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

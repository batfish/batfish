package org.batfish.datamodel.tracking;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Negates the value of an inner {@link TrackMethod}. */
public final class NegatedTrackMethod implements TrackMethod {

  @Override
  public <R> R accept(GenericTrackMethodVisitor<R> visitor) {
    return visitor.visitNegatedTrackMethod(this);
  }

  @JsonProperty(PROP_TRACK_METHOD)
  public @Nonnull TrackMethod getTrackMethod() {
    return _trackMethod;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NegatedTrackMethod)) {
      return false;
    }
    NegatedTrackMethod that = (NegatedTrackMethod) o;
    return _trackMethod.equals(that._trackMethod);
  }

  @Override
  public int hashCode() {
    return _trackMethod.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add(PROP_TRACK_METHOD, _trackMethod).toString();
  }

  static @Nonnull NegatedTrackMethod of(TrackMethod trackMethod) {
    return new NegatedTrackMethod(trackMethod);
  }

  @JsonCreator
  private static @Nonnull NegatedTrackMethod create(
      @JsonProperty(PROP_TRACK_METHOD) @Nullable TrackMethod trackMethod) {
    checkArgument(trackMethod != null, "Missing %s", PROP_TRACK_METHOD);
    return of(trackMethod);
  }

  private NegatedTrackMethod(TrackMethod trackMethod) {
    _trackMethod = trackMethod;
  }

  private static final String PROP_TRACK_METHOD = "trackMethod";

  private final @Nonnull TrackMethod _trackMethod;
}

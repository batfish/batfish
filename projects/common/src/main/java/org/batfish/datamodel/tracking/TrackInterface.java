package org.batfish.datamodel.tracking;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Tracks whether a specified interface is active or not */
public class TrackInterface implements TrackMethod {
  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TrackInterface)) {
      return false;
    }
    return _trackedInterface.equals(((TrackInterface) obj)._trackedInterface);
  }

  @JsonProperty(PROP_TRACKED_INTERFACE)
  public @Nonnull String getTrackedInterface() {
    return _trackedInterface;
  }

  @Override
  public int hashCode() {
    return _trackedInterface.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(PROP_TRACKED_INTERFACE, _trackedInterface).toString();
  }

  @Override
  public <R> R accept(GenericTrackMethodVisitor<R> visitor) {
    return visitor.visitTrackInterface(this);
  }

  static @Nonnull TrackInterface of(String trackedInterface) {
    return new TrackInterface(trackedInterface);
  }

  @JsonCreator
  private static @Nonnull TrackInterface create(
      @JsonProperty(PROP_TRACKED_INTERFACE) @Nullable String trackedInterface) {
    checkArgument(trackedInterface != null, "Missing %s", PROP_TRACKED_INTERFACE);
    return of(trackedInterface);
  }

  private TrackInterface(String trackedInterface) {
    _trackedInterface = trackedInterface;
  }

  private static final String PROP_TRACKED_INTERFACE = "trackedInterface";

  private final String _trackedInterface;
}

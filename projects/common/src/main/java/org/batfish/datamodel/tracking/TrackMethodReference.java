package org.batfish.datamodel.tracking;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A reference to a {@link TrackMethod}. */
public final class TrackMethodReference implements TrackMethod {

  @Override
  public <R> R accept(GenericTrackMethodVisitor<R> visitor) {
    return visitor.visitTrackMethodReference(this);
  }

  @JsonProperty(PROP_ID)
  public @Nonnull String getId() {
    return _id;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof TrackMethodReference)) {
      return false;
    }
    TrackMethodReference that = (TrackMethodReference) o;
    return _id.equals(that._id);
  }

  @Override
  public int hashCode() {
    return _id.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add(PROP_ID, _id).toString();
  }

  static @Nonnull TrackMethodReference of(String id) {
    return new TrackMethodReference(id);
  }

  @JsonCreator
  private static @Nonnull TrackMethodReference create(@JsonProperty(PROP_ID) @Nullable String id) {
    checkArgument(id != null, "Missing %s", PROP_ID);
    return of(id);
  }

  private TrackMethodReference(String id) {
    _id = id;
  }

  private static final String PROP_ID = "id";

  private final @Nonnull String _id;
}

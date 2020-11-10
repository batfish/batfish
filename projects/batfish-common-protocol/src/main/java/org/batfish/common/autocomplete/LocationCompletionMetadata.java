package org.batfish.common.autocomplete;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.Location;

/** Metadata about a {@link Location} needed for autocomplete. */
@ParametersAreNonnullByDefault
public final class LocationCompletionMetadata implements Serializable {
  private static final String PROP_LOCATION = "location";
  private static final String PROP_SOURCE = "source";
  private static final String PROP_TRACEROUTE_SOURCE = "tracerouteSource";

  @Nonnull private final Location _location;
  private final boolean _isSource;
  private final boolean _isTracerouteSource;

  public LocationCompletionMetadata(Location location, boolean isSource) {
    this(location, isSource, true);
  }

  public LocationCompletionMetadata(
      Location location, boolean isSource, boolean isTracerouteSource) {
    _location = location;
    _isSource = isSource;
    _isTracerouteSource = isTracerouteSource;
  }

  @JsonCreator
  private static LocationCompletionMetadata jsonCreator(
      @Nullable @JsonProperty(PROP_LOCATION) Location location,
      @Nullable @JsonProperty(PROP_SOURCE) Boolean isSource) {
    checkArgument(location != null, "Location cannot be null for LocationCompletionMetadata");
    return new LocationCompletionMetadata(location, firstNonNull(isSource, false));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocationCompletionMetadata)) {
      return false;
    }
    LocationCompletionMetadata that = (LocationCompletionMetadata) o;
    return _location.equals(that._location)
        && _isSource == that._isSource
        && _isTracerouteSource == that._isTracerouteSource;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_location, _isSource, _isTracerouteSource);
  }

  /** @return The location for which this completion data is about. */
  @JsonProperty(PROP_LOCATION)
  public Location getLocation() {
    return _location;
  }

  /** @return Whether this location is a source based on its LocationInfo. */
  @JsonProperty(PROP_SOURCE)
  public boolean isSource() {
    return _isSource;
  }

  /** @return Whether this location is a valid traceroute source. */
  @JsonProperty(PROP_TRACEROUTE_SOURCE)
  public boolean isTracerouteSource() {
    return _isTracerouteSource;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("location", _location)
        .add("isSource", _isSource)
        .add("isTracerouteSource", _isTracerouteSource)
        .toString();
  }
}

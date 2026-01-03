package org.batfish.common.autocomplete;

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
  private static final String PROP_SOURCE_WITH_IPS = "sourceWithIps";
  private static final String PROP_TRACEROUTE_SOURCE = "tracerouteSource";

  private final @Nonnull Location _location;
  private final boolean _isSourceWithIps;
  private final boolean _isTracerouteSource;

  public LocationCompletionMetadata(Location location, boolean isSourceWithIps) {
    this(location, isSourceWithIps, isSourceWithIps);
  }

  public LocationCompletionMetadata(
      Location location, boolean isSource, boolean isTracerouteSource) {
    _location = location;
    _isSourceWithIps = isSource;
    _isTracerouteSource = isTracerouteSource;
  }

  @JsonCreator
  private static LocationCompletionMetadata jsonCreator(
      @JsonProperty(PROP_LOCATION) @Nullable Location location,
      @JsonProperty(PROP_SOURCE_WITH_IPS) @Nullable Boolean isSource,
      @JsonProperty(PROP_TRACEROUTE_SOURCE) @Nullable Boolean isTracerouteSoure) {
    checkArgument(
        location != null, "%s cannot be null for LocationCompletionMetadata", PROP_LOCATION);
    checkArgument(
        isSource != null, "%s cannot be null for LocationCompletionMetadata", PROP_SOURCE_WITH_IPS);
    checkArgument(
        isTracerouteSoure != null,
        "%s cannot be null for LocationCompletionMetadata",
        PROP_TRACEROUTE_SOURCE);
    return new LocationCompletionMetadata(location, isSource, isTracerouteSoure);
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
        && _isSourceWithIps == that._isSourceWithIps
        && _isTracerouteSource == that._isTracerouteSource;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_location, _isSourceWithIps, _isTracerouteSource);
  }

  /**
   * @return The location for which this completion data is about.
   */
  @JsonProperty(PROP_LOCATION)
  public Location getLocation() {
    return _location;
  }

  /**
   * @return Whether this location is a "natural" source based on its LocationInfo and it has
   *     non-empty set of source IPs.
   */
  @JsonProperty(PROP_SOURCE_WITH_IPS)
  public boolean isSourceWithIps() {
    return _isSourceWithIps;
  }

  /**
   * @return Whether this location is a valid traceroute source.
   */
  @JsonProperty(PROP_TRACEROUTE_SOURCE)
  public boolean isTracerouteSource() {
    return _isTracerouteSource;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("location", _location)
        .add("isSource", _isSourceWithIps)
        .add("isTracerouteSource", _isTracerouteSource)
        .toString();
  }
}

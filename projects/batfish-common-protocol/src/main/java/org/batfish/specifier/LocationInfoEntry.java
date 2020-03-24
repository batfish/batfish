package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Used for json serialization of a Map<Location,LocationInfo>. */
@ParametersAreNonnullByDefault
public final class LocationInfoEntry {
  private static final String PROP_LOCATION = "location";
  private static final String PROP_LOCATION_INFO = "locationInfo";

  private final Location _location;
  private final LocationInfo _locationInfo;

  public LocationInfoEntry(Location location, LocationInfo locationInfo) {
    _location = location;
    _locationInfo = locationInfo;
  }

  @JsonCreator
  private static LocationInfoEntry jsonCreator(
      @Nullable @JsonProperty(PROP_LOCATION) Location location,
      @Nullable @JsonProperty(PROP_LOCATION_INFO) LocationInfo locationInfo) {
    checkNotNull(location, "%s cannot be null", PROP_LOCATION);
    checkNotNull(locationInfo, "%s cannot be null", PROP_LOCATION_INFO);
    return new LocationInfoEntry(location, locationInfo);
  }

  @JsonProperty(PROP_LOCATION)
  public Location getLocation() {
    return _location;
  }

  @JsonProperty(PROP_LOCATION_INFO)
  public LocationInfo getLocationInfo() {
    return _locationInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocationInfoEntry)) {
      return false;
    }
    LocationInfoEntry that = (LocationInfoEntry) o;
    return _location.equals(that._location) && _locationInfo.equals(that._locationInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_location, _locationInfo);
  }
}

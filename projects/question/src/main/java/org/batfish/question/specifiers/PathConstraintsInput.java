package org.batfish.question.specifiers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.PathConstraints;

/** A data class that allows specification of {@link PathConstraints} using string grammars. */
public class PathConstraintsInput {
  private static final String PROP_START_LOCATION = "startLocation";
  private static final String PROP_TRANSIT_LOCATIONS = "transitLocations";
  private static final String PROP_FORBIDDEN_LOCATIONS = "forbiddenLocations";
  private static final String PROP_END_LOCATION = "endLocation";

  @Nullable private String _startLocation;
  @Nullable private String _endLocation;
  @Nullable private String _transitLocations;
  @Nullable private String _forbiddenLocations;

  @JsonCreator
  private PathConstraintsInput(
      @JsonProperty(PROP_START_LOCATION) @Nullable String startLocation,
      @JsonProperty(PROP_END_LOCATION) @Nullable String endLocation,
      @JsonProperty(PROP_TRANSIT_LOCATIONS) @Nullable String transitLocations,
      @JsonProperty(PROP_FORBIDDEN_LOCATIONS) @Nullable String forbiddenLocations) {
    _startLocation = startLocation;
    _endLocation = endLocation;
    _transitLocations = transitLocations;
    _forbiddenLocations = forbiddenLocations;
  }

  public static PathConstraintsInput unconstrained() {
    return new PathConstraintsInput(null, null, null, null);
  }

  @Nullable
  @JsonProperty(PROP_START_LOCATION)
  public String getStartLocation() {
    return _startLocation;
  }

  @Nullable
  @JsonProperty(PROP_END_LOCATION)
  public String getEndLocation() {
    return _endLocation;
  }

  @Nullable
  @JsonProperty(PROP_TRANSIT_LOCATIONS)
  public String getTransitLocations() {
    return _transitLocations;
  }

  @Nullable
  @JsonProperty(PROP_FORBIDDEN_LOCATIONS)
  public String getForbiddenLocations() {
    return _forbiddenLocations;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String _startLocation;
    private String _endLocation;
    private String _transitLocations;
    private String _forbiddenLocations;

    private Builder() {}

    public Builder setStartLocation(String startLocation) {
      _startLocation = startLocation;
      return this;
    }

    public Builder setEndLocation(String endLocation) {
      _endLocation = endLocation;
      return this;
    }

    public Builder setTransitLocations(String transitLocations) {
      _transitLocations = transitLocations;
      return this;
    }

    public Builder setForbiddenLocations(String forbiddenLocations) {
      _forbiddenLocations = forbiddenLocations;
      return this;
    }

    public PathConstraintsInput build() {
      PathConstraintsInput pathConstraintsInput = new PathConstraintsInput(null, null, null, null);
      pathConstraintsInput._startLocation = _startLocation;
      pathConstraintsInput._forbiddenLocations = _forbiddenLocations;
      pathConstraintsInput._transitLocations = _transitLocations;
      pathConstraintsInput._endLocation = _endLocation;
      return pathConstraintsInput;
    }
  }
}

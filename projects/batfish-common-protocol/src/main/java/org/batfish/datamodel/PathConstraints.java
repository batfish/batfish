package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NodeSpecifier;

/**
 * Constraint on which paths a flow can take through the network. In particular, where the flow can
 * start, end, or which {@link Location}s it is allowed (or forbidden) to transit.
 */
@ParametersAreNonnullByDefault
public class PathConstraints {

  private LocationSpecifier _startLocation;
  private NodeSpecifier _endLocation;
  private NodeSpecifier _transitLocations;
  private NodeSpecifier _forbiddenLocations;

  private PathConstraints(
      LocationSpecifier startLocation,
      NodeSpecifier endLocation,
      NodeSpecifier transitLocations,
      NodeSpecifier notTransitNodes) {
    _startLocation = startLocation;
    _endLocation = endLocation;
    _transitLocations = transitLocations;
    _forbiddenLocations = notTransitNodes;
  }

  public LocationSpecifier getStartLocation() {
    return _startLocation;
  }

  public NodeSpecifier getEndLocation() {
    return _endLocation;
  }

  public NodeSpecifier getTransitLocations() {
    return _transitLocations;
  }

  public NodeSpecifier getForbiddenLocations() {
    return _forbiddenLocations;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private LocationSpecifier _startLocation;
    private NodeSpecifier _endLocation;
    private NodeSpecifier _transitLocations;
    private NodeSpecifier _forbiddenLocations;

    private Builder() {}

    public Builder withStartLocation(LocationSpecifier startLocation) {
      _startLocation = startLocation;
      return this;
    }

    public Builder withEndLocation(NodeSpecifier endLocation) {
      _endLocation = endLocation;
      return this;
    }

    public Builder through(NodeSpecifier transitLocations) {
      _transitLocations = transitLocations;
      return this;
    }

    public Builder avoid(NodeSpecifier forbiddenLocations) {
      _forbiddenLocations = forbiddenLocations;
      return this;
    }

    public PathConstraints build() {
      return new PathConstraints(
          _startLocation, _endLocation, _transitLocations, _forbiddenLocations);
    }
  }
}

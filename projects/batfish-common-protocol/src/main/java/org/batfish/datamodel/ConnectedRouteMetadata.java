package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Metadata associated with a particular {@link ConcreteInterfaceAddress} to modify creation of
 * {@link ConnectedRoute connected routes}
 */
public final class ConnectedRouteMetadata implements Serializable {
  private static final String PROP_ADMIN = "admin";
  private static final String PROP_GENERATE_CONNECTED_ROUTE = "generateConnectedRoute";
  private static final String PROP_GENERATE_LOCAL_NULL_ROUTE_IF_DOWN =
      "generateLocalNullRouteIfDown";
  // plural for backwards compatibility, wrong.
  private static final String PROP_GENERATE_LOCAL_ROUTES = "generateLocalRoutes";
  private static final String PROP_TAG = "tag";

  private final @Nullable Integer _admin;

  // If set, controls whether a connected route is generated for this address. If unset, the
  // default behavior is to generate a connected route.
  private final @Nullable Boolean _generateConnectedRoute;

  // If set, controls whether a local route is generated for this connected route. If unset, the
  // default behavior is to generate local routes for /31 networks or larger
  private final @Nullable Boolean _generateLocalRoute;

  // If set, controls whether a local null route is generated for this connected route when the
  // interface is down. If unset, these routes are not generated.
  private final @Nullable Boolean _generateLocalNullRouteIfDown;

  private final @Nullable Long _tag;

  @JsonProperty(PROP_ADMIN)
  public @Nullable Integer getAdmin() {
    return _admin;
  }

  @JsonProperty(PROP_GENERATE_CONNECTED_ROUTE)
  public @Nullable Boolean getGenerateConnectedRoute() {
    return _generateConnectedRoute;
  }

  @JsonProperty(PROP_GENERATE_LOCAL_ROUTES)
  public @Nullable Boolean getGenerateLocalRoute() {
    return _generateLocalRoute;
  }

  @JsonProperty(PROP_GENERATE_LOCAL_NULL_ROUTE_IF_DOWN)
  public @Nullable Boolean getGenerateLocalNullRouteIfDown() {
    return _generateLocalNullRouteIfDown;
  }

  @JsonProperty(PROP_TAG)
  public @Nullable Long getTag() {
    return _tag;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConnectedRouteMetadata)) {
      return false;
    }
    ConnectedRouteMetadata that = (ConnectedRouteMetadata) o;
    return Objects.equals(_admin, that._admin)
        && Objects.equals(_generateConnectedRoute, that._generateConnectedRoute)
        && Objects.equals(_generateLocalRoute, that._generateLocalRoute)
        && Objects.equals(_generateLocalNullRouteIfDown, that._generateLocalNullRouteIfDown)
        && Objects.equals(_tag, that._tag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(ConnectedRouteMetadata.class)
        .omitNullValues()
        .add(PROP_ADMIN, _admin)
        .add(PROP_GENERATE_CONNECTED_ROUTE, _generateConnectedRoute)
        .add(PROP_GENERATE_LOCAL_ROUTES, _generateLocalRoute)
        .add(PROP_GENERATE_LOCAL_NULL_ROUTE_IF_DOWN, _generateLocalNullRouteIfDown)
        .add(PROP_TAG, _tag)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _admin, _generateConnectedRoute, _generateLocalRoute, _generateLocalNullRouteIfDown, _tag);
  }

  public static Builder builder() {
    return new Builder();
  }

  /////////////////////////
  // Private implementation
  /////////////////////////

  private ConnectedRouteMetadata(
      @Nullable Integer admin,
      @Nullable Boolean generateConnectedRoute,
      @Nullable Boolean generateLocalRoute,
      @Nullable Boolean generateLocalNullRouteIfDown,
      @Nullable Long tag) {
    _admin = admin;
    _generateConnectedRoute = generateConnectedRoute;
    _generateLocalRoute = generateLocalRoute;
    _generateLocalNullRouteIfDown = generateLocalNullRouteIfDown;
    _tag = tag;
  }

  public static final class Builder {
    private @Nullable Integer _admin;
    private @Nullable Boolean _generateConnectedRoute;
    private @Nullable Boolean _generateLocalRoute;
    private @Nullable Boolean _generateLocalNullRouteIfDown;
    private @Nullable Long _tag;

    private Builder() {}

    public @Nonnull Builder setAdmin(@Nullable Integer admin) {
      _admin = admin;
      return this;
    }

    public @Nonnull Builder setAdmin(int admin) {
      _admin = admin;
      return this;
    }

    public @Nonnull Builder setGenerateConnectedRoute(@Nullable Boolean generateConnectedRoute) {
      _generateConnectedRoute = generateConnectedRoute;
      return this;
    }

    public @Nonnull Builder setGenerateConnectedRoute(boolean generateConnectedRoute) {
      _generateConnectedRoute = generateConnectedRoute;
      return this;
    }

    public @Nonnull Builder setGenerateLocalRoute(@Nullable Boolean generateLocalRoute) {
      _generateLocalRoute = generateLocalRoute;
      return this;
    }

    public @Nonnull Builder setGenerateLocalNullRouteIfDown(
        @Nullable Boolean generateLocalNullRouteIfDown) {
      _generateLocalNullRouteIfDown = generateLocalNullRouteIfDown;
      return this;
    }

    public @Nonnull Builder setGenerateLocalRoute(boolean generateLocalRoute) {
      _generateLocalRoute = generateLocalRoute;
      return this;
    }

    public @Nonnull Builder setTag(@Nullable Long tag) {
      _tag = tag;
      return this;
    }

    public @Nonnull Builder setTag(long tag) {
      _tag = tag;
      return this;
    }

    public @Nonnull ConnectedRouteMetadata build() {
      return new ConnectedRouteMetadata(
          _admin,
          _generateConnectedRoute,
          _generateLocalRoute,
          _generateLocalNullRouteIfDown,
          _tag);
    }
  }

  @JsonCreator
  private static ConnectedRouteMetadata jsonCreator(
      @JsonProperty(PROP_ADMIN) @Nullable Integer admin,
      @JsonProperty(PROP_GENERATE_CONNECTED_ROUTE) @Nullable Boolean generateConnectedRoute,
      @JsonProperty(PROP_GENERATE_LOCAL_ROUTES) @Nullable Boolean generateLocalRoute,
      @JsonProperty(PROP_GENERATE_LOCAL_NULL_ROUTE_IF_DOWN) @Nullable
          Boolean generateLocalNullRouteIfDown,
      @JsonProperty(PROP_TAG) @Nullable Long tag) {
    return new ConnectedRouteMetadata(
        admin, generateConnectedRoute, generateLocalRoute, generateLocalNullRouteIfDown, tag);
  }
}

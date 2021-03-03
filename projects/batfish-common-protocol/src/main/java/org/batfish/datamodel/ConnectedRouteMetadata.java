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
  // plural for backwards compatibility, wrong.
  private static final String PROP_GENERATE_LOCAL_ROUTES = "generateLocalRoutes";
  private static final String PROP_TAG = "tag";

  @Nullable private final Integer _admin;

  // If set, controls whether a connected route is generated for this address. If unset, the
  // default behavior is to generate a connected route.
  @Nullable private final Boolean _generateConnectedRoute;

  // If set, controls whether a local route is generated for this connected route. If unset, the
  // default behavior is to generate local routes for /31 networks or larger
  @Nullable private final Boolean _generateLocalRoute;

  @Nullable private final Long _tag;

  @Nullable
  @JsonProperty(PROP_ADMIN)
  public Integer getAdmin() {
    return _admin;
  }

  @Nullable
  @JsonProperty(PROP_GENERATE_CONNECTED_ROUTE)
  public Boolean getGenerateConnectedRoute() {
    return _generateConnectedRoute;
  }

  @Nullable
  @JsonProperty(PROP_GENERATE_LOCAL_ROUTES)
  public Boolean getGenerateLocalRoute() {
    return _generateLocalRoute;
  }

  @Nullable
  @JsonProperty(PROP_TAG)
  public Long getTag() {
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
        && Objects.equals(_tag, that._tag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(ConnectedRouteMetadata.class)
        .omitNullValues()
        .add(PROP_ADMIN, _admin)
        .add(PROP_GENERATE_CONNECTED_ROUTE, _generateConnectedRoute)
        .add(PROP_GENERATE_LOCAL_ROUTES, _generateLocalRoute)
        .add(PROP_TAG, _tag)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(_admin, _generateConnectedRoute, _generateLocalRoute, _tag);
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
      @Nullable Long tag) {
    _admin = admin;
    _generateConnectedRoute = generateConnectedRoute;
    _generateLocalRoute = generateLocalRoute;
    _tag = tag;
  }

  public static final class Builder {
    @Nullable private Integer _admin;
    @Nullable private Boolean _generateConnectedRoute;
    @Nullable private Boolean _generateLocalRoute;
    @Nullable private Long _tag;

    private Builder() {}

    @Nonnull
    public Builder setAdmin(@Nullable Integer admin) {
      _admin = admin;
      return this;
    }

    @Nonnull
    public Builder setAdmin(int admin) {
      _admin = admin;
      return this;
    }

    @Nonnull
    public Builder setGenerateConnectedRoute(@Nullable Boolean generateConnectedRoute) {
      _generateConnectedRoute = generateConnectedRoute;
      return this;
    }

    @Nonnull
    public Builder setGenerateConnectedRoute(boolean generateConnectedRoute) {
      _generateConnectedRoute = generateConnectedRoute;
      return this;
    }

    @Nonnull
    public Builder setGenerateLocalRoute(@Nullable Boolean generateLocalRoute) {
      _generateLocalRoute = generateLocalRoute;
      return this;
    }

    @Nonnull
    public Builder setGenerateLocalRoute(boolean generateLocalRoute) {
      _generateLocalRoute = generateLocalRoute;
      return this;
    }

    @Nonnull
    public Builder setTag(@Nullable Long tag) {
      _tag = tag;
      return this;
    }

    @Nonnull
    public Builder setTag(long tag) {
      _tag = tag;
      return this;
    }

    @Nonnull
    public ConnectedRouteMetadata build() {
      return new ConnectedRouteMetadata(_admin, _generateConnectedRoute, _generateLocalRoute, _tag);
    }
  }

  @JsonCreator
  private static ConnectedRouteMetadata jsonCreator(
      @Nullable @JsonProperty(PROP_ADMIN) Integer admin,
      @Nullable @JsonProperty(PROP_GENERATE_CONNECTED_ROUTE) Boolean generateConnectedRoute,
      @Nullable @JsonProperty(PROP_GENERATE_LOCAL_ROUTES) Boolean generateLocalRoute,
      @Nullable @JsonProperty(PROP_TAG) Long tag) {
    return new ConnectedRouteMetadata(admin, generateConnectedRoute, generateLocalRoute, tag);
  }
}

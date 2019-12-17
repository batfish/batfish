package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.aws.TransitGatewayRoute.Type;

/**
 * Represents AWS Transit Gateway Static Routes
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/search-transit-gateway-routes.html
 *
 * <p>The JSON input is a custom format that wraps around multiple calls to the API above, once per
 * route table. See the test file for the format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class TransitGatewayStaticRoutes implements AwsVpcEntity, Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class Attachment implements Serializable {

    @Nonnull private final String _attachmentId;

    @JsonCreator
    private static Attachment create(
        @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ATTACHMENT_ID) String attachmentId) {
      checkArgument(attachmentId != null, "Attachment id cannot be null for transit gateway route");

      return new Attachment(attachmentId);
    }

    Attachment(String attachmentId) {
      _attachmentId = attachmentId;
    }

    @Nonnull
    public String getAttachmentId() {
      return _attachmentId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Attachment)) {
        return false;
      }
      Attachment that = (Attachment) o;
      return Objects.equals(_attachmentId, that._attachmentId);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_attachmentId);
    }
  }

  @Nonnull private final String _routeTableId;

  @Nonnull private final List<TransitGatewayRoute> _routes;

  @JsonCreator
  private static TransitGatewayStaticRoutes create(
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ROUTE_TABLE_ID) String routeTableId,
      @Nullable @JsonProperty(JSON_KEY_ROUTES) List<TransitGatewayRoute> routes) {
    checkArgument(routeTableId != null, "Route table id cannot be null for transit gateway");
    checkArgument(routes != null, "Static routes cannot be null for transit gateway");

    return new TransitGatewayStaticRoutes(
        routeTableId,
        // defensive move -- in case we get non-static routes
        routes.stream()
            .filter(r -> r._type == Type.STATIC)
            .collect(ImmutableList.toImmutableList()));
  }

  TransitGatewayStaticRoutes(String routeTableId, List<TransitGatewayRoute> routes) {
    _routeTableId = routeTableId;
    _routes = routes;
  }

  @Nonnull
  public List<TransitGatewayRoute> getRoutes() {
    return _routes;
  }

  @Nonnull
  @Override
  public String getId() {
    return _routeTableId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransitGatewayStaticRoutes)) {
      return false;
    }
    TransitGatewayStaticRoutes that = (TransitGatewayStaticRoutes) o;
    return Objects.equals(_routeTableId, that._routeTableId)
        && Objects.equals(_routes, that._routes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_routeTableId, _routes);
  }
}

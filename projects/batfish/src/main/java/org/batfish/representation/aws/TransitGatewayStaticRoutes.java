package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.TransitGatewayStaticRoutes.TransitGatewayRoute.Type;

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
      return Objects.equal(_attachmentId, that._attachmentId);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_attachmentId);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class TransitGatewayRoute implements Serializable {

    enum Type {
      STATIC,
      PROPAGATED
    }

    @Nonnull private final Prefix _destinationCidrBlock;

    @Nonnull private final State _state;

    @Nonnull private final Type _type;

    @Nonnull private final List<String> _attachmentIds;

    @JsonCreator
    private static TransitGatewayRoute create(
        @Nullable @JsonProperty(JSON_KEY_DESTINATION_CIDR_BLOCK) Prefix destinationCidrBlock,
        @Nullable @JsonProperty(JSON_KEY_STATE) String state,
        @Nullable @JsonProperty(JSON_KEY_TYPE) String type,
        @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ATTACHMENTS)
            List<Attachment> attachments) {
      checkArgument(
          destinationCidrBlock != null,
          "Destination CIDR block cannot be nul for transit gateway static route");
      checkArgument(state != null, "State cannot be nul for transit gateway attachment");
      checkArgument(type != null, "Type cannot be nul for transit gateway attachment");
      checkArgument(
          attachments != null, "Attachments cannot be nul for transit gateway attachment");

      return new TransitGatewayRoute(
          destinationCidrBlock,
          State.valueOf(state.toUpperCase()),
          Type.valueOf(type.toUpperCase()),
          attachments.stream()
              .map(Attachment::getAttachmentId)
              .collect(ImmutableList.toImmutableList()));
    }

    TransitGatewayRoute(
        Prefix destinationCidrBlock, State state, Type type, List<String> attachmentIds) {
      _destinationCidrBlock = destinationCidrBlock;
      _state = state;
      _type = type;
      _attachmentIds = attachmentIds;
    }

    @Nonnull
    public List<String> getAttachmentIds() {
      return _attachmentIds;
    }

    @Nonnull
    public Prefix getDestinationCidrBlock() {
      return _destinationCidrBlock;
    }

    @Nonnull
    public State getState() {
      return _state;
    }

    @Nonnull
    public Type getType() {
      return _type;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TransitGatewayRoute)) {
        return false;
      }
      TransitGatewayRoute that = (TransitGatewayRoute) o;
      return Objects.equal(_destinationCidrBlock, that._destinationCidrBlock)
          && _state == that._state
          && _type == that._type
          && Objects.equal(_attachmentIds, that._attachmentIds);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_destinationCidrBlock, _state, _type, _attachmentIds);
    }
  }

  @Nonnull private final String _routeTableId;

  @Nonnull private final List<TransitGatewayRoute> _routes;

  @JsonCreator
  private static TransitGatewayStaticRoutes create(
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ROUTE_TABLE_ID) String routeTableId,
      @Nullable @JsonProperty(JSON_KEY_ROUTES) List<TransitGatewayRoute> routes) {
    checkArgument(routeTableId != null, "Route table id cannot be null for transit gateway");
    checkArgument(routes != null, "Static routes cannot be nul for transit gateway");

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
    return Objects.equal(_routeTableId, that._routeTableId) && Objects.equal(_routes, that._routes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_routeTableId, _routes);
  }
}

package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DESTINATION_CIDR_BLOCK;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DESTINATION_IPV6_CIDR_BLOCK;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_STATE;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_TRANSIT_GATEWAY_ATTACHMENTS;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_TYPE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.TransitGatewayStaticRoutes.Attachment;

@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
abstract class TransitGatewayRoute implements Serializable {

  enum Type {
    STATIC,
    PROPAGATED
  }

  @Nonnull protected final State _state;

  @Nonnull protected final Type _type;

  @Nonnull protected final List<String> _attachmentIds;

  @JsonCreator
  private static TransitGatewayRoute create(
      @Nullable @JsonProperty(JSON_KEY_DESTINATION_CIDR_BLOCK) Prefix destinationCidrBlock,
      @Nullable @JsonProperty(JSON_KEY_DESTINATION_IPV6_CIDR_BLOCK)
          Prefix6 destinationIpv6CidrBlock,
      @Nullable @JsonProperty(JSON_KEY_STATE) String state,
      @Nullable @JsonProperty(JSON_KEY_TYPE) String type,
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ATTACHMENTS) List<Attachment> attachments) {
    checkArgument(
        destinationCidrBlock != null || destinationIpv6CidrBlock != null,
        "At least one of v4 or v6 destination CIDR must be present for a transit gateway static"
            + " route");
    checkArgument(
        destinationCidrBlock == null || destinationIpv6CidrBlock == null,
        "Only one of v4 or v6 destination CIDR block must be present for a transit gateway static"
            + " route");
    checkArgument(state != null, "State cannot be null for transit gateway attachment");
    checkArgument(type != null, "Type cannot be null for transit gateway attachment");

    if (destinationCidrBlock != null) {
      return new TransitGatewayRouteV4(
          destinationCidrBlock,
          State.valueOf(state.toUpperCase()),
          Type.valueOf(type.toUpperCase()),
          firstNonNull(attachments, new LinkedList<Attachment>()).stream()
              .map(Attachment::getAttachmentId)
              .collect(ImmutableList.toImmutableList()));
    } else {
      return new TransitGatewayRouteV6(
          destinationIpv6CidrBlock,
          State.valueOf(state.toUpperCase()),
          Type.valueOf(type.toUpperCase()),
          firstNonNull(attachments, new LinkedList<Attachment>()).stream()
              .map(Attachment::getAttachmentId)
              .collect(ImmutableList.toImmutableList()));
    }
  }

  TransitGatewayRoute(State state, Type type, List<String> attachmentIds) {
    _state = state;
    _type = type;
    _attachmentIds = attachmentIds;
  }

  @Nonnull
  public List<String> getAttachmentIds() {
    return _attachmentIds;
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
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_state", _state)
        .add("_type", _type)
        .add("_attachmentIds", _attachmentIds)
        .toString();
  }
}

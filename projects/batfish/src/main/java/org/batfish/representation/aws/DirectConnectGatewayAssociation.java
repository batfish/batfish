package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
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
import org.batfish.datamodel.Prefix;

/**
 * Represents an AWS Direct Connect Gateway Association (linking a DXGW to a TGW or VGW).
 * https://docs.aws.amazon.com/directconnect/latest/APIReference/API_DescribeDirectConnectGatewayAssociations.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class DirectConnectGatewayAssociation implements AwsVpcEntity, Serializable {

  static final String JSON_KEY_DIRECT_CONNECT_GATEWAY_ASSOCIATIONS =
      "DirectConnectGatewayAssociations";
  static final String JSON_KEY_ASSOCIATION_ID = "AssociationId";
  static final String JSON_KEY_ASSOCIATED_GATEWAY = "AssociatedGateway";
  static final String JSON_KEY_ALLOWED_PREFIXES_TO_DIRECT_CONNECT_GATEWAY =
      "AllowedPrefixesToDirectConnectGateway";

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class AssociatedGateway implements Serializable {

    static final String JSON_KEY_GATEWAY_ID = "Id";
    static final String JSON_KEY_GATEWAY_TYPE = "Type";
    static final String JSON_KEY_GATEWAY_OWNER_ACCOUNT = "OwnerAccount";
    static final String JSON_KEY_GATEWAY_REGION = "Region";

    enum GatewayType {
      TRANSIT_GATEWAY,
      VIRTUAL_PRIVATE_GATEWAY
    }

    private final @Nonnull String _id;
    private final @Nonnull GatewayType _type;
    private final @Nonnull String _ownerAccount;
    private final @Nonnull String _region;

    @JsonCreator
    private static AssociatedGateway create(
        @JsonProperty(JSON_KEY_GATEWAY_ID) @Nullable String id,
        @JsonProperty(JSON_KEY_GATEWAY_TYPE) @Nullable String type,
        @JsonProperty(JSON_KEY_GATEWAY_OWNER_ACCOUNT) @Nullable String ownerAccount,
        @JsonProperty(JSON_KEY_GATEWAY_REGION) @Nullable String region) {
      checkArgument(id != null, "Associated gateway id cannot be null");
      checkArgument(type != null, "Associated gateway type cannot be null");
      checkArgument(ownerAccount != null, "Associated gateway owner account cannot be null");
      checkArgument(region != null, "Associated gateway region cannot be null");

      // AWS returns camelCase like "transitGateway" — convert to SCREAMING_SNAKE_CASE
      String enumName = type.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
      return new AssociatedGateway(
          id, Enum.valueOf(GatewayType.class, enumName), ownerAccount, region);
    }

    AssociatedGateway(String id, GatewayType type, String ownerAccount, String region) {
      _id = id;
      _type = type;
      _ownerAccount = ownerAccount;
      _region = region;
    }

    public @Nonnull String getId() {
      return _id;
    }

    public @Nonnull GatewayType getType() {
      return _type;
    }

    public @Nonnull String getOwnerAccount() {
      return _ownerAccount;
    }

    public @Nonnull String getRegion() {
      return _region;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof AssociatedGateway)) {
        return false;
      }
      AssociatedGateway that = (AssociatedGateway) o;
      return Objects.equals(_id, that._id)
          && _type == that._type
          && Objects.equals(_ownerAccount, that._ownerAccount)
          && Objects.equals(_region, that._region);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_id, _type, _ownerAccount, _region);
    }
  }

  private final @Nonnull String _associationId;

  private final @Nonnull String _directConnectGatewayId;

  private final @Nonnull AssociatedGateway _associatedGateway;

  private final @Nonnull List<Prefix> _allowedPrefixes;

  @JsonCreator
  private static DirectConnectGatewayAssociation create(
      @JsonProperty(JSON_KEY_ASSOCIATION_ID) @Nullable String associationId,
      @JsonProperty(DirectConnectGateway.JSON_KEY_DIRECT_CONNECT_GATEWAY_ID) @Nullable
          String directConnectGatewayId,
      @JsonProperty(JSON_KEY_ASSOCIATED_GATEWAY) @Nullable AssociatedGateway associatedGateway,
      @JsonProperty(JSON_KEY_ALLOWED_PREFIXES_TO_DIRECT_CONNECT_GATEWAY) @Nullable
          List<AllowedPrefix> allowedPrefixes) {
    checkArgument(associationId != null, "Association id cannot be null for DXGW association");
    checkArgument(
        directConnectGatewayId != null,
        "Direct Connect Gateway id cannot be null for DXGW association");
    checkArgument(
        associatedGateway != null, "Associated gateway cannot be null for DXGW association");

    return new DirectConnectGatewayAssociation(
        associationId,
        directConnectGatewayId,
        associatedGateway,
        firstNonNull(allowedPrefixes, ImmutableList.<AllowedPrefix>of()).stream()
            .map(AllowedPrefix::getCidr)
            .collect(ImmutableList.toImmutableList()));
  }

  DirectConnectGatewayAssociation(
      String associationId,
      String directConnectGatewayId,
      AssociatedGateway associatedGateway,
      List<Prefix> allowedPrefixes) {
    _associationId = associationId;
    _directConnectGatewayId = directConnectGatewayId;
    _associatedGateway = associatedGateway;
    _allowedPrefixes = allowedPrefixes;
  }

  @Override
  public @Nonnull String getId() {
    return _associationId;
  }

  public @Nonnull String getDirectConnectGatewayId() {
    return _directConnectGatewayId;
  }

  public @Nonnull AssociatedGateway getAssociatedGateway() {
    return _associatedGateway;
  }

  public @Nonnull List<Prefix> getAllowedPrefixes() {
    return _allowedPrefixes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DirectConnectGatewayAssociation)) {
      return false;
    }
    DirectConnectGatewayAssociation that = (DirectConnectGatewayAssociation) o;
    return Objects.equals(_associationId, that._associationId)
        && Objects.equals(_directConnectGatewayId, that._directConnectGatewayId)
        && Objects.equals(_associatedGateway, that._associatedGateway)
        && Objects.equals(_allowedPrefixes, that._allowedPrefixes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _associationId, _directConnectGatewayId, _associatedGateway, _allowedPrefixes);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class AllowedPrefix implements Serializable {

    static final String JSON_KEY_CIDR = "Cidr";

    private final @Nonnull Prefix _cidr;

    @JsonCreator
    private static AllowedPrefix create(@JsonProperty(JSON_KEY_CIDR) @Nullable String cidr) {
      checkArgument(cidr != null, "CIDR cannot be null for allowed prefix");
      return new AllowedPrefix(Prefix.parse(cidr));
    }

    AllowedPrefix(Prefix cidr) {
      _cidr = cidr;
    }

    public @Nonnull Prefix getCidr() {
      return _cidr;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof AllowedPrefix)) {
        return false;
      }
      AllowedPrefix that = (AllowedPrefix) o;
      return Objects.equals(_cidr, that._cidr);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_cidr);
    }
  }
}

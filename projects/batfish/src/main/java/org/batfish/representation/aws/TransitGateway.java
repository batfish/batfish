package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;

/**
 * Represents an AWS Transit Gateway
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-transit-gateways.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class TransitGateway implements AwsVpcEntity, Serializable {

  @Nonnull
  public TransitGatewayOptions getOptions() {
    return _options;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class TransitGatewayOptions {

    private final long _amazonSideAsn;

    private final boolean _defaultRouteTableAssociation;

    @Nonnull private final String _associationDefaultRouteTableId;

    private final boolean _defaultRouteTablePropagation;

    @Nonnull private final String _propagationDefaultRouteTableId;

    private final boolean _vpnEcmpSupport;

    //    "Options": {
    //          "AmazonSideAsn": 64512,
    //          "AutoAcceptSharedAttachments": "disable",
    //          "DefaultRouteTableAssociation": "enable",
    //          "AssociationDefaultRouteTableId": "tgw-rtb-0fa40c8df355dce6e",
    //          "DefaultRouteTablePropagation": "enable",
    //          "PropagationDefaultRouteTableId": "tgw-rtb-0fa40c8df355dce6e",
    //          "VpnEcmpSupport": "enable",
    //          "DnsSupport": "enable"
    //    },
    @JsonCreator
    private static TransitGatewayOptions create(
        @Nullable @JsonProperty(JSON_KEY_AMAZON_SIDE_ASN) Long amazonSideAsn,
        @Nullable @JsonProperty(JSON_KEY_DEFAULT_ROUTE_TABLE_ASSOCIATION)
            String defaultRouteTableAssociation,
        @Nullable @JsonProperty(JSON_KEY_ASSOCIATION_DEFAULT_ROUTE_TABLE_ID)
            String associationDefaultRouteTableId,
        @Nullable @JsonProperty(JSON_KEY_DEFAULT_ROUTE_TABLE_PROPAGATION)
            String defaultRouteTablePropagation,
        @Nullable @JsonProperty(JSON_KEY_PROPAGATION_DEFAULT_ROUTE_TABLE_ID)
            String propagationDefaultRouteTableId,
        @Nullable @JsonProperty(JSON_KEY_VPN_ECMP_SUPPORT) String vpcEcmpSupport) {
      checkArgument(amazonSideAsn != null, "Amazon side ASN cannot be null for a transit gateway");
      checkArgument(
          defaultRouteTableAssociation != null,
          "Default route table association cannot be null for a transit gateway");
      checkArgument(
          associationDefaultRouteTableId != null,
          "Association default route table id cannot be null for a transit gateway");
      checkArgument(
          defaultRouteTablePropagation != null,
          "Default route table propagation cannot be null for a transit gateway");
      checkArgument(
          propagationDefaultRouteTableId != null,
          "Propagation default route table id cannot be null for a transit gateway");
      checkArgument(
          vpcEcmpSupport != null, "VPC ECMP support cannot be null for a transit gateway");

      return new TransitGatewayOptions(
          amazonSideAsn,
          getBool(defaultRouteTableAssociation),
          associationDefaultRouteTableId,
          getBool(defaultRouteTablePropagation),
          propagationDefaultRouteTableId,
          getBool(vpcEcmpSupport));
    }

    TransitGatewayOptions(
        long amazonSideAsn,
        boolean defaultRouteTableAssociation,
        String associationDefaultRouteTableId,
        boolean defaultRouteTablePropagation,
        String propagationDefaultRouteTableId,
        boolean vpnEcmpSupport) {
      _amazonSideAsn = amazonSideAsn;
      _defaultRouteTableAssociation = defaultRouteTableAssociation;
      _associationDefaultRouteTableId = associationDefaultRouteTableId;
      _defaultRouteTablePropagation = defaultRouteTablePropagation;
      _propagationDefaultRouteTableId = propagationDefaultRouteTableId;
      _vpnEcmpSupport = vpnEcmpSupport;
    }

    private static boolean getBool(String stringValue) {
      if (stringValue.equalsIgnoreCase("enable")) {
        return true;
      }
      if (stringValue.equalsIgnoreCase("disable")) {
        return false;
      }
      throw new IllegalArgumentException(
          String.format("'%s' is not a valid boolean value", stringValue));
    }

    public long getAmazonSideAsn() {
      return _amazonSideAsn;
    }

    public boolean isDefaultRouteTableAssociation() {
      return _defaultRouteTableAssociation;
    }

    @Nonnull
    public String getAssociationDefaultRouteTableId() {
      return _associationDefaultRouteTableId;
    }

    public boolean isDefaultRouteTablePropagation() {
      return _defaultRouteTablePropagation;
    }

    @Nonnull
    public String getPropagationDefaultRouteTableId() {
      return _propagationDefaultRouteTableId;
    }

    public boolean isVpnEcmpSupport() {
      return _vpnEcmpSupport;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof TransitGatewayOptions)) {
        return false;
      }
      TransitGatewayOptions that = (TransitGatewayOptions) o;
      return _amazonSideAsn == that._amazonSideAsn
          && _defaultRouteTableAssociation == that._defaultRouteTableAssociation
          && _defaultRouteTablePropagation == that._defaultRouteTablePropagation
          && _vpnEcmpSupport == that._vpnEcmpSupport
          && com.google.common.base.Objects.equal(
              _associationDefaultRouteTableId, that._associationDefaultRouteTableId)
          && com.google.common.base.Objects.equal(
              _propagationDefaultRouteTableId, that._propagationDefaultRouteTableId);
    }

    @Override
    public int hashCode() {
      return com.google.common.base.Objects.hashCode(
          _amazonSideAsn,
          _defaultRouteTableAssociation,
          _associationDefaultRouteTableId,
          _defaultRouteTablePropagation,
          _propagationDefaultRouteTableId,
          _vpnEcmpSupport);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("_amazonSideAsn", _amazonSideAsn)
          .add("_defaultRouteTableAssociation", _defaultRouteTableAssociation)
          .add("_associationDefaultRouteTableId", _associationDefaultRouteTableId)
          .add("_defaultRouteTablePropagation", _defaultRouteTablePropagation)
          .add("_propagationDefaultRouteTableId", _propagationDefaultRouteTableId)
          .add("_vpnEcmpSupport", _vpnEcmpSupport)
          .toString();
    }
  }

  @Nonnull private final String _gatewayId;

  @Nonnull private final TransitGatewayOptions _options;

  @JsonCreator
  private static TransitGateway create(
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) String gatewayId,
      @Nullable @JsonProperty(JSON_KEY_OPTIONS) TransitGatewayOptions options) {
    checkArgument(gatewayId != null, "Transit Gateway Id cannot be null");
    checkArgument(options != null, "Transit Gateway Options cannot be null");

    return new TransitGateway(gatewayId, options);
  }

  public TransitGateway(String gatewayId, TransitGatewayOptions options) {
    _gatewayId = gatewayId;
    _options = options;
  }

  /**
   * Creates a node for the transit gateway. Other essential elements of this node are created
   * elsewhere.
   */
  Configuration toConfigurationNode(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_gatewayId, "aws");
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    return cfgNode;
  }

  @Override
  public String getId() {
    return _gatewayId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransitGateway)) {
      return false;
    }
    TransitGateway that = (TransitGateway) o;
    return Objects.equals(_gatewayId, that._gatewayId) && Objects.equals(_options, that._options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_gatewayId, _options);
  }
}

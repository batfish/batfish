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
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;

/** Represents an AWS Internet Gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class InternetGateway implements AwsVpcEntity, Serializable {

  @Nonnull private final List<String> _attachmentVpcIds;

  @Nonnull private String _internetGatewayId;

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class Attachment {

    @Nonnull private final String _vpcId;

    @JsonCreator
    private static Attachment create(@Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId) {
      checkArgument(vpcId != null, "Vpc id cannot be null for Internet gateway attachment");
      return new Attachment(vpcId);
    }

    private Attachment(String vpcId) {
      _vpcId = vpcId;
    }

    @Nonnull
    public String getVpcId() {
      return _vpcId;
    }
  }

  @JsonCreator
  private static InternetGateway create(
      @Nullable @JsonProperty(JSON_KEY_INTERNET_GATEWAY_ID) String internetGatewayId,
      @Nullable @JsonProperty(JSON_KEY_ATTACHMENTS) List<Attachment> attachments) {
    checkArgument(internetGatewayId != null, "Id cannot be null for Internet gateway");
    checkArgument(attachments != null, "Attachments cannot be nul for Internet gateway");

    return new InternetGateway(
        internetGatewayId,
        attachments.stream().map(Attachment::getVpcId).collect(ImmutableList.toImmutableList()));
  }

  public InternetGateway(String internetGatewayId, List<String> attachmentVpcIds) {
    _internetGatewayId = internetGatewayId;
    _attachmentVpcIds = attachmentVpcIds;
  }

  @Override
  public String getId() {
    return _internetGatewayId;
  }

  Configuration toConfigurationNode(AwsConfiguration awsConfiguration, Region region) {
    Configuration cfgNode = Utils.newAwsConfiguration(_internetGatewayId, "aws");
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    for (String vpcId : _attachmentVpcIds) {

      String igwIfaceName = vpcId;
      Prefix igwAddresses = awsConfiguration.getNextGeneratedLinkSubnet();
      ConcreteInterfaceAddress igwIfaceAddress =
          ConcreteInterfaceAddress.create(
              igwAddresses.getStartIp(), igwAddresses.getPrefixLength());
      Utils.newInterface(igwIfaceName, cfgNode, igwIfaceAddress);

      // add the interface to the vpc router
      Configuration vpcConfigNode = awsConfiguration.getConfigurationNodes().get(vpcId);
      String vpcIfaceName = _internetGatewayId;
      ConcreteInterfaceAddress vpcIfaceAddress =
          ConcreteInterfaceAddress.create(igwAddresses.getEndIp(), igwAddresses.getPrefixLength());
      Utils.newInterface(vpcIfaceName, vpcConfigNode, vpcIfaceAddress);

      // associate this gateway with the vpc
      region.getVpcs().get(vpcId).setInternetGatewayId(_internetGatewayId);

      // add a route on the gateway to the vpc
      Vpc vpc = region.getVpcs().get(vpcId);
      vpc.getCidrBlockAssociations()
          .forEach(
              prefix -> {
                StaticRoute igwVpcRoute =
                    StaticRoute.builder()
                        .setNetwork(prefix)
                        .setNextHopIp(vpcIfaceAddress.getIp())
                        .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
                        .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
                        .build();
                cfgNode.getDefaultVrf().getStaticRoutes().add(igwVpcRoute);
              });
    }

    return cfgNode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InternetGateway)) {
      return false;
    }
    InternetGateway that = (InternetGateway) o;
    return Objects.equals(_attachmentVpcIds, that._attachmentVpcIds)
        && Objects.equals(_internetGatewayId, that._internetGatewayId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_attachmentVpcIds, _internetGatewayId);
  }
}

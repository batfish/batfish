package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;

/** Represents an AWS VPC https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-vpcs.html */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class Vpc implements AwsVpcEntity, Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class CidrBlockAssociation implements Serializable {

    @Nonnull private final Prefix _block;

    @JsonCreator
    private static CidrBlockAssociation create(
        @Nullable @JsonProperty(JSON_KEY_CIDR_BLOCK) Prefix block) {
      checkArgument(block != null, "CIDR block cannot be null in CIDR block association");
      return new CidrBlockAssociation(block);
    }

    CidrBlockAssociation(Prefix block) {
      _block = block;
    }

    @Nonnull
    Prefix getBlock() {
      return _block;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof CidrBlockAssociation)) {
        return false;
      }
      CidrBlockAssociation that = (CidrBlockAssociation) o;
      return Objects.equals(_block, that._block);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_block);
    }
  }

  @Nonnull private final Set<Prefix> _cidrBlockAssociations;

  @Nonnull private final Map<String, String> _tags;

  @Nonnull private final String _vpcId;

  @JsonCreator
  private static Vpc create(
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_TAGS) List<Tag> tags,
      @Nullable @JsonProperty(JSON_KEY_CIDR_BLOCK_ASSOCIATION_SET)
          Set<CidrBlockAssociation> cidrBlockAssociations) {
    /*
     We do not parse CidrBlock. The information there also shows up CidrBlockAssociationSet
    */
    checkNonNull(vpcId, JSON_KEY_VPC_ID, "VPC");
    checkNonNull(cidrBlockAssociations, JSON_KEY_CIDR_BLOCK_ASSOCIATION_SET, "VPC");
    return new Vpc(
        vpcId,
        cidrBlockAssociations.stream()
            .map(CidrBlockAssociation::getBlock)
            .collect(ImmutableSet.toImmutableSet()),
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)));
  }

  Vpc(String vpcId, Set<Prefix> cidrBlockAssociations, Map<String, String> tags) {
    _vpcId = vpcId;
    _cidrBlockAssociations = cidrBlockAssociations;
    _tags = tags;
  }

  @Nonnull
  Set<Prefix> getCidrBlockAssociations() {
    return _cidrBlockAssociations;
  }

  @Override
  public String getId() {
    return _vpcId;
  }

  /**
   * Returns {@link Configuration} corresponding to this VPC node.
   *
   * <p>We only create the node here. Interfaces are added when we traverse its neighbors such as
   * subnets and internet gateways
   */
  Configuration toConfigurationNode(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(nodeName(_vpcId), "aws", _tags, DeviceModel.AWS_VPC);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());
    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);

    initializeVrf(cfgNode.getDefaultVrf());
    return cfgNode;
  }

  /**
   * The VPC installs a null route for all prefixes associated with the VPC.
   *
   * <ul>
   *   <li>The route must exist, so the VPC can advertise the prefix.
   *   <li>The null route must be forwarding, so the VPC can drop traffic to that prefix that does
   *       not have a destination (e.g., associated subnet) even if there's an associated gateway
   *       providing a default route.
   *   <li>The route must have a large admin distance. That way a subnet that owns the entire VPC
   *       will get the traffic, rather than ECMP.
   * </ul>
   */
  void initializeVrf(Vrf vrf) {
    _cidrBlockAssociations.forEach(
        cb ->
            vrf.getStaticRoutes()
                .add(
                    StaticRoute.builder()
                        .setAdministrativeCost(255)
                        .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
                        .setNetwork(cb)
                        .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
                        .build()));
  }

  /** Return the hostname used for a VPC Id */
  static String nodeName(String vpcId) {
    return vpcId;
  }

  /**
   * Return the VRF name used on the VPC node for links to remote entities (e.g., a VPC peering
   * connection or a transit gateway attachment).
   */
  static String vrfNameForLink(String linkId) {
    return "vrf-" + linkId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Vpc)) {
      return false;
    }
    Vpc vpc = (Vpc) o;
    return Objects.equals(_cidrBlockAssociations, vpc._cidrBlockAssociations)
        && Objects.equals(_tags, vpc._tags)
        && Objects.equals(_vpcId, vpc._vpcId);
  }

  @Override
  public int hashCode() {
    return com.google.common.base.Objects.hashCode(_cidrBlockAssociations, _tags, _vpcId);
  }
}

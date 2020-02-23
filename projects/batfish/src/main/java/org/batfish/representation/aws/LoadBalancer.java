package org.batfish.representation.aws;

import static org.batfish.representation.aws.Utils.addNodeToSubnet;
import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;

/**
 * Represents an elastic load balancer v2 https://docs.aws.amazon.com/elasticloadbalancing/.
 *
 * <p>We currently only support network load balancer (not application)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class LoadBalancer implements AwsVpcEntity, Serializable {

  enum Scheme {
    INTERNAL,
    INTERNET_FACING
  }

  enum Type {
    APPLICATION,
    NETWORK
  }

  enum Protocol {
    HTTP,
    HTTPS,
    TCP,
    TLS,
    UDP,
    TCP_UDP
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class AvailabilityZone {

    @Nonnull private final String _subnetId;

    @Nonnull private final String _zoneName;

    @JsonCreator
    private static AvailabilityZone create(
        @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId,
        @Nullable @JsonProperty(JSON_KEY_ZONE_NAME) String zoneName) {
      // not parsing "LoadBalancerAddresses" -- assuming that network interfaces cover the info
      checkNonNull(subnetId, JSON_KEY_SUBNET_ID, "Load balancer availability zone");
      checkNonNull(zoneName, JSON_KEY_ZONE_NAME, "Load balancer availability zone");
      return new AvailabilityZone(subnetId, zoneName);
    }

    AvailabilityZone(String subnetId, String zoneName) {
      _subnetId = subnetId;
      _zoneName = zoneName;
    }

    @Nonnull
    public String getSubnetId() {
      return _subnetId;
    }

    @Nonnull
    public String getZoneName() {
      return _zoneName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof AvailabilityZone)) {
        return false;
      }
      AvailabilityZone that = (AvailabilityZone) o;
      return _subnetId.equals(that._subnetId) && _zoneName.equals(that._zoneName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_subnetId, _zoneName);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("_subnetId", _subnetId)
          .add("_zoneName", _zoneName)
          .toString();
    }
  }

  @Nonnull private String _arn;

  @Nonnull private final List<AvailabilityZone> _availabilityZones;

  @Nonnull private final String _name;

  @Nonnull private final Scheme _scheme;

  @Nonnull private final Type _type;

  @Nonnull private final String _vpcId;

  @JsonCreator
  private static LoadBalancer create(
      @Nullable @JsonProperty(JSON_KEY_LOAD_BALANCER_ARN) String arn,
      @Nullable @JsonProperty(JSON_KEY_AVAILABILITY_ZONES) List<AvailabilityZone> availabilityZones,
      @Nullable @JsonProperty(JSON_KEY_LOAD_BALANCER_NAME) String name,
      @Nullable @JsonProperty(JSON_KEY_SCHEME) String scheme,
      @Nullable @JsonProperty(JSON_KEY_TYPE) String type,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId) {
    checkNonNull(arn, JSON_KEY_LOAD_BALANCER_ARN, "LoadBalancer");
    checkNonNull(availabilityZones, JSON_KEY_AVAILABILITY_ZONES, "LoadBalancer");
    checkNonNull(name, JSON_KEY_LOAD_BALANCER_NAME, "LoadBalancer");
    checkNonNull(scheme, JSON_KEY_SCHEME, "LoadBalancer");
    checkNonNull(type, JSON_KEY_TYPE, "LoadBalancer");
    checkNonNull(vpcId, JSON_KEY_VPC_ID, "LoadBalancer");

    return new LoadBalancer(
        arn,
        availabilityZones,
        name,
        Scheme.valueOf(scheme.toUpperCase().replace('-', '_')),
        Type.valueOf(type.toUpperCase()),
        vpcId);
  }

  public LoadBalancer(
      String arn,
      List<AvailabilityZone> availabilityZones,
      String name,
      Scheme scheme,
      Type type,
      String vpcId) {
    _arn = arn;
    _availabilityZones = availabilityZones;
    _name = name;
    _scheme = scheme;
    _type = type;
    _vpcId = vpcId;
  }

  List<Configuration> toConfigurationNodes(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    if (_type == Type.APPLICATION) {
      warnings.redFlag("Application load balancer is not currently supported");
      return ImmutableList.of();
    }
    List<Configuration> configurations = new LinkedList<>();
    _availabilityZones.forEach(
        zone -> configurations.add(toConfigurationNode(zone, awsConfiguration, region, warnings)));
    return configurations;
  }

  private Configuration toConfigurationNode(
      AvailabilityZone zone,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(
            getNodeId(_arn, zone.getZoneName()), "aws", DeviceModel.AWS_ELB_NETWORK);
    cfgNode.setHumanName(_name);
    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setSubnetId(zone.getSubnetId());
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    Optional<NetworkInterface> networkInterface = getMyInterface(zone.getSubnetId(), _arn, region);
    if (!networkInterface.isPresent()) {
      warnings.redFlag(
          String.format(
              "Network interface not found for load balancer %s in subnet %s.",
              _name, zone.getSubnetId()));
      return cfgNode;
    }
    Subnet subnet = region.getSubnets().get(zone.getSubnetId());
    // Interface viIface =
    addNodeToSubnet(cfgNode, networkInterface.get(), subnet, awsConfiguration, warnings);

    //    Set<TargetGroup> targetGroups =
    //        region.getTargetGroups().values().stream()
    //            .filter(group -> group.getLoadBalancerArns().contains(_arn))
    //            .collect(ImmutableSet.toImmutableSet());

    // viIface.setIncomingTransformation();
    return cfgNode;
  }

  /**
   * Regex for load balancer ARN. Used to find the right interface in {@link #getMyInterface}.
   *
   * <p>Example arn is
   * 'arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1'.
   * The regex extracts 'net/lb-lb/6f57a43b75d8f2c1' and uses that to join with interface
   * descriptions which are like 'ELB net/lb-lb/6f57a43b75d8f2c1'.
   * https://docs.aws.amazon.com/elasticloadbalancing/latest/network/network-load-balancers.html#availability-zones
   */
  static final Pattern LOAD_BALANCER_ARN_PATTERN =
      Pattern.compile("^arn:aws:elasticloadbalancing:[^:]+:[0-9]+:loadbalancer\\/(.+)$");

  static Optional<NetworkInterface> getMyInterface(String subnetId, String arn, Region region) {
    Matcher matcher = LOAD_BALANCER_ARN_PATTERN.matcher(arn);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    String matchString = matcher.group(1);
    return region.getNetworkInterfaces().values().stream()
        .filter(iface -> iface.getDescription().equals("ELB " + matchString))
        .findAny();
  }

  static String getNodeId(String loadBalancerArn, String availabilityZoneName) {
    return String.format("%s::%s", loadBalancerArn, availabilityZoneName);
  }

  @Override
  public String getId() {
    return _arn;
  }

  @Nonnull
  public List<AvailabilityZone> getAvailabilityZones() {
    return _availabilityZones;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public Scheme getScheme() {
    return _scheme;
  }

  @Nonnull
  public Type getType() {
    return _type;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LoadBalancer)) {
      return false;
    }
    LoadBalancer that = (LoadBalancer) o;
    return _arn.equals(that._arn)
        && _availabilityZones.equals(that._availabilityZones)
        && Objects.equals(_name, that._name)
        && _scheme == that._scheme
        && _type == that._type
        && _vpcId.equals(that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_arn, _availabilityZones, _name, _scheme, _type, _vpcId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_arn", _arn)
        .add("_availabilityZones", _availabilityZones)
        .add("_name", _name)
        .add("_scheme", _scheme)
        .add("_type", _type)
        .add("_vpcId", _vpcId)
        .toString();
  }
}

package org.batfish.representation.aws;

import static org.batfish.datamodel.NamedPort.EPHEMERAL_HIGHEST;
import static org.batfish.datamodel.NamedPort.EPHEMERAL_LOWEST;
import static org.batfish.representation.aws.Utils.addNodeToSubnet;
import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.ApplyAny;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.aws.LoadBalancerListener.ActionType;
import org.batfish.representation.aws.LoadBalancerListener.DefaultAction;
import org.batfish.representation.aws.LoadBalancerTargetHealth.HealthState;
import org.batfish.representation.aws.LoadBalancerTargetHealth.TargetHealthDescription;

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

  /** Regex for load balancer ARN. Used to find the interface in {@link #findMyInterface}. */
  static final Pattern LOAD_BALANCER_ARN_PATTERN =
      Pattern.compile("^arn:aws:elasticloadbalancing:[^:]+:[0-9]+:loadbalancer\\/(.+)$");

  /** The prefix is that used for interfaces that belong to load balancer */
  static final String LOAD_BALANCER_INTERFACE_DESCRIPTION_PREFIX = "ELB ";

  /** Name for the filter that drops all packets that are not transformed */
  static final String DEFAULT_FILTER_NAME = "~DENY~UNMATCHED~PACKETS~";

  /** A no-op transformation that helps with tracing */
  static final Transformation TRACING_TRANSFORMATION =
      new Transformation(TrueExpr.INSTANCE, ImmutableList.of(Noop.NOOP_DEST_NAT), null, null);

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static class AvailabilityZone implements Serializable {

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

  @Nonnull private final String _dnsName;

  @Nonnull private final String _name;

  @Nonnull private final Scheme _scheme;

  @Nonnull private final Type _type;

  @Nonnull private final String _vpcId;

  @JsonCreator
  private static LoadBalancer create(
      @Nullable @JsonProperty(JSON_KEY_LOAD_BALANCER_ARN) String arn,
      @Nullable @JsonProperty(JSON_KEY_AVAILABILITY_ZONES) List<AvailabilityZone> availabilityZones,
      @Nullable @JsonProperty(JSON_KEY_DNS_NAME) String dnsName,
      @Nullable @JsonProperty(JSON_KEY_LOAD_BALANCER_NAME) String name,
      @Nullable @JsonProperty(JSON_KEY_SCHEME) String scheme,
      @Nullable @JsonProperty(JSON_KEY_TYPE) String type,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId) {
    checkNonNull(arn, JSON_KEY_LOAD_BALANCER_ARN, "LoadBalancer");
    checkNonNull(availabilityZones, JSON_KEY_AVAILABILITY_ZONES, "LoadBalancer");
    checkNonNull(dnsName, JSON_KEY_DNS_NAME, "LoadBalancer");
    checkNonNull(name, JSON_KEY_LOAD_BALANCER_NAME, "LoadBalancer");
    checkNonNull(scheme, JSON_KEY_SCHEME, "LoadBalancer");
    checkNonNull(type, JSON_KEY_TYPE, "LoadBalancer");
    checkNonNull(vpcId, JSON_KEY_VPC_ID, "LoadBalancer");

    return new LoadBalancer(
        arn,
        availabilityZones,
        dnsName,
        name,
        Scheme.valueOf(scheme.toUpperCase().replace('-', '_')),
        Type.valueOf(type.toUpperCase()),
        vpcId);
  }

  public LoadBalancer(
      String arn,
      List<AvailabilityZone> availabilityZones,
      String dnsName,
      String name,
      Scheme scheme,
      Type type,
      String vpcId) {
    _arn = arn;
    _availabilityZones = availabilityZones;
    _dnsName = dnsName;
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

  /** Creates configuration node corresponding to one availability zone */
  @VisibleForTesting
  Configuration toConfigurationNode(
      AvailabilityZone availabilityZone,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(
            getNodeId(_dnsName, availabilityZone.getZoneName()),
            "aws",
            DeviceModel.AWS_ELB_NETWORK);
    cfgNode.setHumanName(_name);
    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setSubnetId(availabilityZone.getSubnetId());
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    Optional<NetworkInterface> networkInterface =
        findMyInterface(availabilityZone.getSubnetId(), _arn, region);
    if (!networkInterface.isPresent()) {
      warnings.redFlag(
          String.format(
              "Network interface not found for load balancer %s (%s) in subnet %s.",
              _name, _arn, availabilityZone.getSubnetId()));
      return cfgNode;
    }
    Subnet subnet = region.getSubnets().get(availabilityZone.getSubnetId());
    Interface viIface =
        addNodeToSubnet(cfgNode, networkInterface.get(), subnet, awsConfiguration, warnings);

    LoadBalancerAttributes loadBalancerAttributes = region.getLoadBalancerAttributes().get(_arn);
    if (loadBalancerAttributes == null) {
      warnings.redFlag(
          String.format(
              "Attributes not found for load balancer %s (%s). Assuming that cross zone load balancing is disabled.",
              _name, _arn));
    }
    boolean crossZoneLoadBalancing =
        loadBalancerAttributes != null && loadBalancerAttributes.getCrossZoneLoadBalancing();

    installTransformations(
        viIface, availabilityZone.getZoneName(), crossZoneLoadBalancing, region, warnings);

    IpAccessList defaultFilter =
        computeDefaultFilter(networkInterface.get().getPrivateIpAddresses());
    viIface.setPostTransformationIncomingFilter(defaultFilter);
    cfgNode.getIpAccessLists().put(defaultFilter.getName(), defaultFilter);

    return cfgNode;
  }

  @VisibleForTesting
  void installTransformations(
      Interface viIface,
      String lbAvailabilityZoneName,
      boolean crossZoneLoadBalancing,
      Region region,
      Warnings warnings) {
    List<LoadBalancerTransformation> listenerTransformations;
    LoadBalancerListener loadBalancerListener = region.getLoadBalancerListeners().get(_arn);
    if (loadBalancerListener == null) {
      warnings.redFlag(
          String.format("Listeners not found for load balancer %s (%s).", _name, _arn));
      listenerTransformations = ImmutableList.of();
    } else {
      listenerTransformations =
          loadBalancerListener.getListeners().stream()
              .map(
                  listener ->
                      computerListenerTransformation(
                          listener,
                          lbAvailabilityZoneName,
                          viIface.getConcreteAddress().getIp(),
                          crossZoneLoadBalancing,
                          region,
                          warnings))
              .filter(Objects::nonNull)
              .collect(ImmutableList.toImmutableList());
    }

    viIface.setIncomingTransformation(chainListenerTransformations(listenerTransformations));
    viIface.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(false, ImmutableList.of(viIface.getName()), null, null));
  }

  /**
   * Gets {@link LoadBalancerTransformation} corresponding to the given {@code listener}. The
   * listener has match conditions and a sequence of actions. The first valid action of type forward
   * is used for the returned transformation.
   *
   * @return null if we cannot build a valid transformation, e.g., the listener protocol for
   *     incoming packets is not supported or none of the targets are valid.
   */
  @Nullable
  @VisibleForTesting
  LoadBalancerTransformation computerListenerTransformation(
      LoadBalancerListener.Listener listener,
      String lbAvailabilityZoneName,
      Ip loadBalancerIp,
      boolean crossZoneLoadBalancing,
      Region region,
      Warnings warnings) {
    try {
      HeaderSpace matchHeaderSpace = listener.getMatchingHeaderSpace();

      Optional<TransformationStep> transformationStep =
          listener.getDefaultActions().stream()
              .filter(defaultAction -> defaultAction.getType() == ActionType.FORWARD)
              .sorted(Comparator.comparing(DefaultAction::getOrder))
              .map(
                  action ->
                      computeTargetGroupTransformationStep(
                          action.getTargetGroupArn(),
                          lbAvailabilityZoneName,
                          loadBalancerIp,
                          crossZoneLoadBalancing,
                          region,
                          warnings))
              .filter(Objects::nonNull)
              .findFirst();

      if (!transformationStep.isPresent()) {
        warnings.redFlag(
            String.format(
                "No valid transformation step could be built for listener %s of load balancer %s (%s)",
                listener, _arn, _name));
        return null;
      }

      return new LoadBalancerTransformation(
          new MatchHeaderSpace(matchHeaderSpace), transformationStep.get());
    } catch (Exception e) {
      warnings.redFlag(e.getMessage());
      return null;
    }
  }

  /**
   * Gets {@link TransformationStep} for a {@link TargetGroup}. The load balancer sprays packets
   * across valid targets in the group. Validity is based on target health and zone criteria.
   *
   * @return null if a valid transformation step could not be constructed
   */
  @Nullable
  @VisibleForTesting
  static TransformationStep computeTargetGroupTransformationStep(
      String targetGroupArn,
      String lbAvailabilityZoneName,
      Ip loadBalancerIp,
      boolean crossZoneLoadBalancing,
      Region region,
      Warnings warnings) {
    TargetGroup targetGroup = region.getTargetGroups().get(targetGroupArn);
    if (targetGroup == null) {
      warnings.redFlag(String.format("Target group ARN %s not found", targetGroupArn));
      return null;
    }

    LoadBalancerTargetHealth targetHealths =
        region.getLoadBalancerTargetHealths().get(targetGroupArn);
    if (targetHealths == null) {
      warnings.redFlag(
          String.format(
              "Target health information not found for target group ARN %s", targetGroupArn));
      return null;
    }

    Set<TransformationStep> transformationSteps =
        targetHealths.getTargetHealthDescriptions().stream()
            .filter(
                desc ->
                    isValidTarget(
                        desc, targetGroup, lbAvailabilityZoneName, crossZoneLoadBalancing, region))
            .map(
                desc ->
                    computeTargetTransformationStep(
                        desc.getTarget(), targetGroup.getTargetType(), loadBalancerIp, region))
            .filter(Objects::nonNull)
            .collect(ImmutableSet.toImmutableSet());
    if (transformationSteps.isEmpty()) {
      return null;
    }

    return new ApplyAny(transformationSteps);
  }

  /**
   * Returns the transformation step corresponding to {@code target}. This method assumes that the
   * target is valid.
   *
   * @return The transformation step or null if the Ip of the target cannot be determined.
   */
  @Nullable
  @VisibleForTesting
  static TransformationStep computeTargetTransformationStep(
      LoadBalancerTarget target,
      TargetGroup.Type targetGroupType,
      Ip loadBalancerIp,
      Region region) {
    Ip targetIp =
        targetGroupType.equals(TargetGroup.Type.IP)
            ? Ip.parse(target.getId())
            // instance must exist since this target is valid (see isValidTarget)
            : region.getInstances().get(target.getId()).getPrimaryPrivateIpAddress();
    if (targetIp == null) {
      return null;
    }
    return new ApplyAll(
        TransformationStep.assignSourceIp(loadBalancerIp, loadBalancerIp),
        TransformationStep.assignSourcePort(EPHEMERAL_LOWEST.number(), EPHEMERAL_HIGHEST.number()),
        TransformationStep.assignDestinationIp(targetIp, targetIp),
        TransformationStep.assignDestinationPort(target.getPort(), target.getPort()));
  }

  /**
   * A target is deemed valid for this load balancer if it is healthy and availability zone
   * parameters match, that is, the load balancer does cross zone load balancing or the target is
   * either in zone "all" or in the same zone as the load balancer.
   */
  @VisibleForTesting
  static boolean isValidTarget(
      TargetHealthDescription targetHealthDescription,
      TargetGroup targetGroup,
      String lbAvailabilityZoneName,
      boolean crossZoneLoadBalancing,
      Region region) {
    if (targetHealthDescription.getTargetHealth().getState() != HealthState.HEALTHY) {
      return false;
    }
    switch (targetGroup.getTargetType()) {
      case IP:
        return crossZoneLoadBalancing
            || "all".equals(targetHealthDescription.getTarget().getAvailabilityZone())
            || lbAvailabilityZoneName.equals(
                targetHealthDescription.getTarget().getAvailabilityZone());
      case INSTANCE:
        Instance instance = region.getInstances().get(targetHealthDescription.getTarget().getId());
        return instance != null
            && instance.getPlacement() != null
            && (crossZoneLoadBalancing
                || lbAvailabilityZoneName.equals(instance.getPlacement().getAvailabilityZone()));
      default:
        throw new IllegalArgumentException(
            "Unknown target group type " + targetGroup.getTargetType());
    }
  }

  /** Chains the provided list of transformations. */
  @Nonnull
  @VisibleForTesting
  static Transformation chainListenerTransformations(
      List<LoadBalancerTransformation> listenerTransformations) {
    Transformation tailTransformation = TRACING_TRANSFORMATION;
    for (int index = listenerTransformations.size() - 1; index >= 0; index--) {
      tailTransformation = listenerTransformations.get(index).toTransformation(tailTransformation);
    }
    return tailTransformation;
  }

  /**
   * The computed filter rejects all packets that match the network interface's addresses. The idea
   * is that all packets addressed to the load balancer (whose interface is provided as an argument)
   * are dropped if they were not transformed. We are relying on the fact that all transformed
   * packets have an IP address different from that of the load balancer.
   */
  @VisibleForTesting
  static IpAccessList computeDefaultFilter(List<PrivateIpAddress> loadBalancerInterfaceAddresses) {
    return IpAccessList.builder()
        .setName(DEFAULT_FILTER_NAME)
        .setLines(
            ExprAclLine.rejecting(
                "Deny untransformed packets",
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(
                            loadBalancerInterfaceAddresses.stream()
                                .map(privateIp -> IpWildcard.create(privateIp.getPrivateIp()))
                                .collect(ImmutableList.toImmutableList()))
                        .build())),
            ExprAclLine.accepting("Permit transformed packets", TrueExpr.INSTANCE))
        .build();
  }

  /**
   * Finds the interface that the load balancer uses inside the given subnet.
   *
   * <p>Example arn is
   * 'arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1'.
   * The regex extracts 'net/lb-lb/6f57a43b75d8f2c1' and uses that to join with interface
   * descriptions which are like 'ELB net/lb-lb/6f57a43b75d8f2c1'.
   * https://docs.aws.amazon.com/elasticloadbalancing/latest/network/network-load-balancers.html#availability-zones
   */
  @VisibleForTesting
  static Optional<NetworkInterface> findMyInterface(String subnetId, String arn, Region region) {
    Matcher matcher = LOAD_BALANCER_ARN_PATTERN.matcher(arn);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    String matchString = matcher.group(1);
    return region.getNetworkInterfaces().values().stream()
        .filter(
            iface ->
                iface
                        .getDescription()
                        .equals(LOAD_BALANCER_INTERFACE_DESCRIPTION_PREFIX + matchString)
                    && iface.getSubnetId().equals(subnetId))
        .findAny();
  }

  static String getNodeId(String dnsName, String availabilityZoneName) {
    return String.format("%s-%s", availabilityZoneName, dnsName);
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
  public String getDnsName() {
    return _dnsName;
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
        && Objects.equals(_dnsName, that._dnsName)
        && Objects.equals(_name, that._name)
        && _scheme == that._scheme
        && _type == that._type
        && _vpcId.equals(that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_arn, _availabilityZones, _dnsName, _name, _scheme, _type, _vpcId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_arn", _arn)
        .add("_availabilityZones", _availabilityZones)
        .add("_dnsName", _dnsName)
        .add("_name", _name)
        .add("_scheme", _scheme)
        .add("_type", _type)
        .add("_vpcId", _vpcId)
        .toString();
  }
}

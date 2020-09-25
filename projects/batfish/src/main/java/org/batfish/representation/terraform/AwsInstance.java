package org.batfish.representation.terraform;

import static org.batfish.representation.terraform.Constants.JSON_KEY_ARN;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_INSTANCE_STATE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_NETWORK_INTERFACE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_NETWORK_INTERFACE_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PRIMARY_NETWORK_INTERFACE_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PRIVATE_IP;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PUBLIC_IP;
import static org.batfish.representation.terraform.Constants.JSON_KEY_SECONDARY_PRIVATE_IPS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_SECURITY_GROUPS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_SUBNET_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_VPC_SECURITY_GROUP_IDS;
import static org.batfish.representation.terraform.Utils.checkMandatoryAttributes;
import static org.batfish.representation.terraform.Utils.getArnAcccount;
import static org.batfish.representation.terraform.Utils.getArnRegion;
import static org.batfish.representation.terraform.Utils.getAwsResourceTags;
import static org.batfish.representation.terraform.Utils.getStrings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.representation.aws.Instance;
import org.batfish.representation.aws.Instance.Status;
import org.batfish.representation.aws.NetworkInterface;
import org.batfish.representation.aws.PrivateIpAddress;

/** Represents an AWS VPC */
@ParametersAreNonnullByDefault
class AwsInstance extends AwsResource {

  @ParametersAreNonnullByDefault
  private static final class AwsInstanceNetworkInterface implements Serializable {
    private static final List<String> MANDATORY_ATTRIBUTES =
        ImmutableList.of(JSON_KEY_NETWORK_INTERFACE_ID);

    @Nonnull private final String _networkInterfaceId;

    static AwsInstanceNetworkInterface create(
        Map<String, Object> attributes, String resourceDescription) {
      checkMandatoryAttributes(attributes, MANDATORY_ATTRIBUTES, resourceDescription);
      return new AwsInstanceNetworkInterface(
          attributes.get(JSON_KEY_NETWORK_INTERFACE_ID).toString());
    }

    AwsInstanceNetworkInterface(String networkInterfaceId) {
      _networkInterfaceId = networkInterfaceId;
    }

    @Nonnull
    public String getNetworkInterfaceId() {
      return _networkInterfaceId;
    }
  }

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(
          JSON_KEY_ARN,
          JSON_KEY_ID,
          JSON_KEY_SUBNET_ID,
          JSON_KEY_INSTANCE_STATE,
          JSON_KEY_VPC_SECURITY_GROUP_IDS,
          JSON_KEY_SECURITY_GROUPS,
          JSON_KEY_PRIMARY_NETWORK_INTERFACE_ID,
          JSON_KEY_NETWORK_INTERFACE,
          JSON_KEY_SECONDARY_PRIVATE_IPS,
          JSON_KEY_PRIVATE_IP,
          JSON_KEY_PUBLIC_IP);

  @Nonnull private final String _arn;
  @Nonnull private final String _id;
  @Nonnull private final String _subnetId;
  @Nonnull private final Map<String, String> _tags;
  @Nonnull private final Status _status;

  @Nonnull private final String _primaryNetworkInterfaceId;
  @Nonnull Ip _privateIp;
  @Nullable Ip _publicIp;
  @Nonnull private final List<AwsInstanceNetworkInterface> _networkInterfaces;
  @Nonnull private final List<Ip> _secondaryPrivateIps;

  @Nonnull private final List<String> _securityGroups;
  @Nonnull private final List<String> _vpcSecurityGroupIds;

  // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/instance
  static AwsInstance create(CommonResourceProperties common, Map<String, Object> attributes) {
    checkMandatoryAttributes(attributes, MANDATORY_ATTRIBUTES, common.getName());
    String publicIP = attributes.get(JSON_KEY_PUBLIC_IP).toString();
    return new AwsInstance(
        common,
        attributes.get(JSON_KEY_ARN).toString(),
        attributes.get(JSON_KEY_ID).toString(),
        attributes.get(JSON_KEY_SUBNET_ID).toString(),
        Ip.parse(attributes.get(JSON_KEY_PRIVATE_IP).toString()),
        publicIP.isEmpty() ? null : Ip.parse(publicIP),
        attributes.get(JSON_KEY_PRIMARY_NETWORK_INTERFACE_ID).toString(),
        getNetworkInterfaces(attributes, common.getName()),
        getStrings(attributes, JSON_KEY_SECONDARY_PRIVATE_IPS).stream()
            .map(Ip::parse)
            .collect(ImmutableList.toImmutableList()),
        Status.fromString(attributes.get(JSON_KEY_INSTANCE_STATE).toString()),
        getStrings(attributes, JSON_KEY_SECURITY_GROUPS),
        getStrings(attributes, JSON_KEY_VPC_SECURITY_GROUP_IDS),
        getAwsResourceTags(attributes));
  }

  @SuppressWarnings("unchecked")
  private static List<AwsInstanceNetworkInterface> getNetworkInterfaces(
      Map<String, Object> attributes, String resourceName) {
    return ((List<?>) attributes.get(JSON_KEY_NETWORK_INTERFACE))
        .stream()
            .map(
                m ->
                    AwsInstanceNetworkInterface.create(
                        (Map<String, Object>) m, "NetworkInterface of " + resourceName))
            .collect(ImmutableList.toImmutableList());
  }

  public AwsInstance(
      CommonResourceProperties common,
      String arn,
      String id,
      String subnetId,
      Ip privateIp,
      @Nullable Ip publicIp,
      String primaryNetworkInterfaceId,
      List<AwsInstanceNetworkInterface> networkInterfaces,
      List<Ip> secondaryPrivateIps,
      Status state,
      List<String> securityGroups,
      List<String> vpcSecurityGroupIds,
      Map<String, String> tags) {
    super(common);
    _arn = arn;
    _id = id;
    _subnetId = subnetId;
    _privateIp = privateIp;
    _publicIp = publicIp;
    _primaryNetworkInterfaceId = primaryNetworkInterfaceId;
    _networkInterfaces = networkInterfaces;
    _secondaryPrivateIps = secondaryPrivateIps;
    _status = state;
    _securityGroups = securityGroups;
    _vpcSecurityGroupIds = vpcSecurityGroupIds;
    _tags = tags;
  }

  public Instance convert(@Nullable String vpcId) {
    return new Instance(
        _id,
        vpcId,
        _subnetId,
        getAllSecurityGroups(),
        ImmutableList.of(_primaryNetworkInterfaceId),
        _privateIp,
        _tags,
        _status);
  }

  private List<String> getAllSecurityGroups() {
    return ImmutableList.copyOf(
        ImmutableSet.<String>builder()
            .addAll(_securityGroups)
            .addAll(_vpcSecurityGroupIds)
            .build());
  }

  /**
   * Returns the implicit interface (if one exists) for this Instance.
   *
   * <p>Interfaces that are not explicitly configured for an instance do not have a resource
   * definition in Terraform. We create one to match the data that is read from AWS.
   */
  public Optional<NetworkInterface> getImplicitNetworkInterface(String vpcId) {
    if (_networkInterfaces.stream()
        .anyMatch(ni -> ni.getNetworkInterfaceId().equals(_primaryNetworkInterfaceId))) {
      return Optional.empty();
    }

    List<PrivateIpAddress> allAddresses =
        Stream.concat(
                Stream.of(new PrivateIpAddress(true, _privateIp, _publicIp)),
                _secondaryPrivateIps.stream().map(ip -> new PrivateIpAddress(false, ip, null)))
            .collect(ImmutableList.toImmutableList());

    return Optional.of(
        new NetworkInterface(
            _primaryNetworkInterfaceId,
            _subnetId,
            vpcId,
            getAllSecurityGroups(),
            allAddresses,
            "Primary Network Interface of " + _id,
            _id,
            ImmutableMap.of()));
  }

  public boolean isMySubnet(AwsSubnet awsSubnet) {
    return awsSubnet.getId().equals(_subnetId)
        && getArnAcccount(awsSubnet.getArn()).equals(getArnAcccount(_arn))
        && getArnRegion(awsSubnet.getArn()).equals(getArnRegion(_arn));
  }

  @Nonnull
  public String getArn() {
    return _arn;
  }

  @Nonnull
  public String getSubnetId() {
    return _arn;
  }
}

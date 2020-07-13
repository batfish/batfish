package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/**
 * Represents an EC2 address
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-addresses.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class Address implements AwsVpcEntity, Serializable {

  @Nullable private final String _instanceId;

  @Nullable private final Ip _privateIp;

  @Nonnull private final Ip _publicIp;

  @Nonnull private final String _allocationId;

  @JsonCreator
  private static Address create(
      @Nullable @JsonProperty(JSON_KEY_PUBLIC_IP) String publicIp,
      @Nullable @JsonProperty(JSON_KEY_INSTANCE_ID) String instanceId,
      @Nullable @JsonProperty(JSON_KEY_PRIVATE_IP_ADDRESS) String privateIpAddress,
      @Nullable @JsonProperty(JSON_KEY_ALLOCATION_ID) String allocationId) {
    checkArgument(publicIp != null, "Public IP of an EC2 address cannot be null");
    checkArgument(allocationId != null, "Allocation ID of an Elastic IP address cannot be null");
    return new Address(
        Ip.parse(publicIp),
        instanceId,
        privateIpAddress == null ? null : Ip.parse(privateIpAddress),
        allocationId);
  }

  public Address(
      Ip publicIp, @Nullable String instanceId, @Nullable Ip privateIp, String allocationId) {
    _publicIp = publicIp;
    _instanceId = instanceId;
    _privateIp = privateIp;
    _allocationId = allocationId;
  }

  @Override
  public String getId() {
    return _publicIp.toString();
  }

  @Nullable
  public String getInstanceId() {
    return _instanceId;
  }

  @Nullable
  public Ip getPrivateIp() {
    return _privateIp;
  }

  @Nonnull
  public Ip getPublicIp() {
    return _publicIp;
  }

  @Nonnull
  public String getAllocationId() {
    return _allocationId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Address)) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(_instanceId, address._instanceId)
        && Objects.equals(_privateIp, address._privateIp)
        && Objects.equals(_publicIp, address._publicIp)
        && Objects.equals(_allocationId, address._allocationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_instanceId, _privateIp, _publicIp, _allocationId);
  }
}

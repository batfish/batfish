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

/** Represents an EC2 address */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class Address implements AwsVpcEntity, Serializable {

  @Nullable private final String _instanceId;

  @Nullable private final Ip _privateIp;

  @Nonnull private final Ip _publicIp;

  @JsonCreator
  private static Address create(
      @Nullable @JsonProperty(JSON_KEY_PUBLIC_IP) String publicIp,
      @Nullable @JsonProperty(JSON_KEY_INSTANCE_ID) String instanceId,
      @Nullable @JsonProperty(JSON_KEY_PRIVATE_IP_ADDRESS) String privateIpAddress) {
    checkArgument(publicIp != null, "Public IP of an EC2 address cannot be null");
    return new Address(
        Ip.parse(publicIp),
        instanceId,
        privateIpAddress == null ? null : Ip.parse(privateIpAddress));
  }

  Address(Ip publicIp, @Nullable String instanceId, @Nullable Ip privateIp) {
    _publicIp = publicIp;
    _instanceId = instanceId;
    _privateIp = privateIp;
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
        && Objects.equals(_publicIp, address._publicIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_instanceId, _privateIp, _publicIp);
  }
}

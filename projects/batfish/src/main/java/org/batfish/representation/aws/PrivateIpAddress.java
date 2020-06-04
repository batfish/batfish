package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_ASSOCIATION;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PRIMARY;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PRIVATE_IP_ADDRESS;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PRIVATE_IP_ADDRESSES;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PUBLIC_IP;
import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Represents a private address for {@link org.batfish.representation.aws.NetworkInterface} */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class PrivateIpAddress implements Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class Association {

    @Nonnull private final Ip _publicIp;

    @JsonCreator
    private static Association create(@Nullable @JsonProperty(JSON_KEY_PUBLIC_IP) Ip publicIp) {
      checkNonNull(publicIp, JSON_KEY_PUBLIC_IP, "Association (Network Interface)");
      return new Association(publicIp);
    }

    private Association(Ip publicIp) {
      _publicIp = publicIp;
    }

    @Nonnull
    Ip getPublicIp() {
      return _publicIp;
    }
  }

  private final boolean _primary;

  @Nonnull private final Ip _privateIp;

  @Nullable private final Ip _publicIp;

  @JsonCreator
  private static PrivateIpAddress create(
      @Nullable @JsonProperty(JSON_KEY_PRIMARY) Boolean primary,
      @Nullable @JsonProperty(JSON_KEY_PRIVATE_IP_ADDRESS) Ip privateIp,
      @Nullable @JsonProperty(JSON_KEY_ASSOCIATION) Association association) {
    checkNonNull(primary, JSON_KEY_PRIMARY, "PrivateIpAddress");
    checkNonNull(privateIp, JSON_KEY_PRIVATE_IP_ADDRESSES, "PrivateIpAddress");

    return new PrivateIpAddress(
        primary, privateIp, association == null ? null : association.getPublicIp());
  }

  public PrivateIpAddress(boolean primary, Ip privateIp, @Nullable Ip publicIp) {
    _primary = primary;
    _privateIp = privateIp;
    _publicIp = publicIp;
  }

  @Nonnull
  public Ip getPrivateIp() {
    return _privateIp;
  }

  @Nullable
  public Ip getPublicIp() {
    return _publicIp;
  }

  public boolean isPrimary() {
    return _primary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PrivateIpAddress)) {
      return false;
    }
    PrivateIpAddress that = (PrivateIpAddress) o;
    return _primary == that._primary
        && Objects.equals(_privateIp, that._privateIp)
        && Objects.equals(_publicIp, that._publicIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_primary, _privateIp, _publicIp);
  }
}

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

    private final @Nonnull Ip _publicIp;

    @JsonCreator
    private static Association create(@JsonProperty(JSON_KEY_PUBLIC_IP) @Nullable Ip publicIp) {
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

  private final @Nonnull Ip _privateIp;

  private final @Nullable Ip _publicIp;

  @JsonCreator
  private static PrivateIpAddress create(
      @JsonProperty(JSON_KEY_PRIMARY) @Nullable Boolean primary,
      @JsonProperty(JSON_KEY_PRIVATE_IP_ADDRESS) @Nullable Ip privateIp,
      @JsonProperty(JSON_KEY_ASSOCIATION) @Nullable Association association) {
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

  public @Nonnull Ip getPrivateIp() {
    return _privateIp;
  }

  public @Nullable Ip getPublicIp() {
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

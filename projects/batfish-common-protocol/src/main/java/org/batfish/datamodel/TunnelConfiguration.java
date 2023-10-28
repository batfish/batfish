package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Stores settings applicable to tunnel interfaces. Does not handle IPsec configuration, for that
 * see {@link IpsecPeerConfig}
 */
@ParametersAreNonnullByDefault
public final class TunnelConfiguration implements Serializable {
  private static final String PROP_SOURCE_ADDRESS = "sourceAddress";
  private static final String PROP_DESTINATION_ADDRESS = "destinationAddress";

  private @Nonnull Ip _sourceAddress;
  private @Nonnull Ip _destinationAddress;

  private TunnelConfiguration(Ip sourceAddress, Ip destinationAddress) {
    _sourceAddress = sourceAddress;
    _destinationAddress = destinationAddress;
  }

  @JsonCreator
  private static TunnelConfiguration jsonCreator(
      @JsonProperty(PROP_SOURCE_ADDRESS) @Nullable Ip sourceAddress,
      @JsonProperty(PROP_DESTINATION_ADDRESS) @Nullable Ip destinationAddress) {
    checkArgument(sourceAddress != null, "Missing %s", PROP_SOURCE_ADDRESS);
    checkArgument(destinationAddress != null, "Missing %s", PROP_DESTINATION_ADDRESS);
    return new TunnelConfiguration(sourceAddress, destinationAddress);
  }

  @JsonProperty(PROP_SOURCE_ADDRESS)
  public @Nonnull Ip getSourceAddress() {
    return _sourceAddress;
  }

  @JsonProperty(PROP_DESTINATION_ADDRESS)
  public @Nonnull Ip getDestinationAddress() {
    return _destinationAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TunnelConfiguration)) {
      return false;
    }
    TunnelConfiguration that = (TunnelConfiguration) o;
    return _sourceAddress.equals(that._sourceAddress)
        && _destinationAddress.equals(that._destinationAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_sourceAddress, _destinationAddress);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @Nullable Ip _sourceAddress;
    private @Nullable Ip _destinationAddress;

    private Builder() {}

    public Builder setSourceAddress(Ip sourceAddress) {
      _sourceAddress = sourceAddress;
      return this;
    }

    public Builder setDestinationAddress(Ip destinationAddress) {
      _destinationAddress = destinationAddress;
      return this;
    }

    public TunnelConfiguration build() {
      checkArgument(_sourceAddress != null, "Missing %s", PROP_SOURCE_ADDRESS);
      checkArgument(_destinationAddress != null, "Missing %s", PROP_DESTINATION_ADDRESS);
      return new TunnelConfiguration(_sourceAddress, _destinationAddress);
    }
  }
}

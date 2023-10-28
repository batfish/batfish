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
 * Abstracted configuration for Multi-chassis link aggregation, as configured on a single device.
 *
 * <p><b>Terminology Note</b>: despite being called Mlag, this class is designed to represent not
 * just Arista's MLAG but also other vendors (e.g., Cisco's VPC and Juniper's MC-LAG)
 */
@ParametersAreNonnullByDefault
public final class Mlag implements Serializable {

  private static final String PROP_ID = "id";
  private static final String PROP_LOCAL_INTERFACE = "localInterface";
  private static final String PROP_PEER_ADDRESS = "peerAddress";
  private static final String PROP_PEER_INTERFACE = "peerInterface";

  private final String _id;
  private final @Nullable Ip _peerAddress;
  private final @Nullable String _peerInterface;
  private final @Nullable String _localInterface;

  private Mlag(
      String id,
      @Nullable Ip peerAddress,
      @Nullable String peerInterface,
      @Nullable String localInterface) {
    _id = id;
    _peerAddress = peerAddress;
    _peerInterface = peerInterface;
    _localInterface = localInterface;
  }

  @JsonCreator
  private static Mlag create(
      @JsonProperty(PROP_ID) @Nullable String id,
      @JsonProperty(PROP_PEER_ADDRESS) @Nullable Ip peerAddress,
      @JsonProperty(PROP_PEER_INTERFACE) @Nullable String peerInterface,
      @JsonProperty(PROP_LOCAL_INTERFACE) @Nullable String localInterface) {
    checkArgument(id != null, "Missing Mlag %s", PROP_ID);
    return new Mlag(id, peerAddress, peerInterface, localInterface);
  }

  @JsonProperty(PROP_ID)
  public String getId() {
    return _id;
  }

  @JsonProperty(PROP_PEER_ADDRESS)
  public @Nullable Ip getPeerAddress() {
    return _peerAddress;
  }

  @JsonProperty(PROP_PEER_INTERFACE)
  public @Nullable String getPeerInterface() {
    return _peerInterface;
  }

  @JsonProperty(PROP_LOCAL_INTERFACE)
  public @Nullable String getLocalInterface() {
    return _localInterface;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Mlag)) {
      return false;
    }
    Mlag mlag = (Mlag) o;
    return _id.equals(mlag._id)
        && Objects.equals(_peerAddress, mlag._peerAddress)
        && Objects.equals(_peerInterface, mlag._peerInterface)
        && Objects.equals(_localInterface, mlag._localInterface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, _peerAddress, _peerInterface, _localInterface);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @Nullable String _id;
    private @Nullable Ip _peerAddress;
    private @Nullable String _peerInterface;
    private @Nullable String _localInterface;

    private Builder() {}

    public Builder setId(@Nonnull String id) {
      _id = id;
      return this;
    }

    public Builder setPeerAddress(@Nullable Ip peerAddress) {
      _peerAddress = peerAddress;
      return this;
    }

    public Builder setPeerInterface(@Nullable String peerInterface) {
      _peerInterface = peerInterface;
      return this;
    }

    public Builder setLocalInterface(@Nullable String localInterface) {
      _localInterface = localInterface;
      return this;
    }

    public Mlag build() throws IllegalArgumentException {
      checkArgument(_id != null, "Missing Mlag %s", PROP_ID);
      return new Mlag(_id, _peerAddress, _peerInterface, _localInterface);
    }
  }
}

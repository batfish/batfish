package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;

public final class InterfaceForwardingBehavior implements Serializable {
  private final @Nonnull IpSpace _acceptedIps;
  private final @Nonnull IpSpace _deliveredToSubnet;
  private final @Nonnull IpSpace _exitsNetwork;
  private final @Nonnull IpSpace _neighborUnreachable;
  private final @Nonnull IpSpace _insufficientInfo;

  public InterfaceForwardingBehavior(
      IpSpace acceptedIps,
      IpSpace deliveredToSubnet,
      IpSpace exitsNetwork,
      IpSpace neighborUnreachable,
      IpSpace insufficientInfo) {
    _acceptedIps = firstNonNull(acceptedIps, EmptyIpSpace.INSTANCE);
    _deliveredToSubnet = firstNonNull(deliveredToSubnet, EmptyIpSpace.INSTANCE);
    _exitsNetwork = firstNonNull(exitsNetwork, EmptyIpSpace.INSTANCE);
    _neighborUnreachable = firstNonNull(neighborUnreachable, EmptyIpSpace.INSTANCE);
    _insufficientInfo = firstNonNull(insufficientInfo, EmptyIpSpace.INSTANCE);
  }

  /** Destination IPs {@link FlowDisposition#ACCEPTED} by this interface. */
  public IpSpace getAcceptedIps() {
    return _acceptedIps;
  }

  /**
   * Destination IPs assigned disposition {@link FlowDisposition#DELIVERED_TO_SUBNET} at this
   * interface.
   */
  public IpSpace getDeliveredToSubnet() {
    return _deliveredToSubnet;
  }

  /**
   * Destination IPs assigned disposition {@link FlowDisposition#EXITS_NETWORK} at this interface.
   */
  public IpSpace getExitsNetwork() {
    return _exitsNetwork;
  }

  /** Mapping: hostname -&gt; outInterface -&gt; dstIpsForWhichNoSufficientInfoToDetermine */
  public IpSpace getInsufficientInfo() {
    return _insufficientInfo;
  }

  /**
   * Destination IPs assigned disposition {@link FlowDisposition#NEIGHBOR_UNREACHABLE} at this
   * interface.
   */
  public IpSpace getNeighborUnreachable() {
    return _neighborUnreachable;
  }

  public static InterfaceForwardingBehavior withAcceptedIps(IpSpace ips) {
    return builder().setAccepted(ips).build();
  }

  public static InterfaceForwardingBehavior withDeliveredToSubnet(IpSpace deliveredToSubnet) {
    return builder().setDeliveredToSubnet(deliveredToSubnet).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @Nonnull IpSpace _accepted = EmptyIpSpace.INSTANCE;
    private @Nonnull IpSpace _deliveredToSubnet = EmptyIpSpace.INSTANCE;
    private @Nonnull IpSpace _exitsNetwork = EmptyIpSpace.INSTANCE;
    private @Nonnull IpSpace _insufficientInfo = EmptyIpSpace.INSTANCE;
    private @Nonnull IpSpace _neighborUnreachable = EmptyIpSpace.INSTANCE;

    public Builder setAccepted(@Nonnull IpSpace accepted) {
      this._accepted = accepted;
      return this;
    }

    public Builder setDeliveredToSubnet(@Nonnull IpSpace deliveredToSubnet) {
      this._deliveredToSubnet = deliveredToSubnet;
      return this;
    }

    public Builder setExitsNetwork(@Nonnull IpSpace exitsNetwork) {
      this._exitsNetwork = exitsNetwork;
      return this;
    }

    public Builder setInsufficientInfo(@Nonnull IpSpace insufficientInfo) {
      this._insufficientInfo = insufficientInfo;
      return this;
    }

    public Builder setNeighborUnreachable(@Nonnull IpSpace neighborUnreachable) {
      this._neighborUnreachable = neighborUnreachable;
      return this;
    }

    public InterfaceForwardingBehavior build() {
      return new InterfaceForwardingBehavior(
          _accepted, _deliveredToSubnet, _exitsNetwork, _neighborUnreachable, _insufficientInfo);
    }
  }
}

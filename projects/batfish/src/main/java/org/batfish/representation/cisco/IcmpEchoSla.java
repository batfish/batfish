package org.batfish.representation.cisco;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Configuration for an {@code ip sla} object of type {@code icmp-echo}. */
@ParametersAreNonnullByDefault
public final class IcmpEchoSla extends IpSla implements HasWritableVrf {

  public IcmpEchoSla(
      @Nullable Ip destinationIp, @Nullable String sourceInterface, @Nullable Ip sourceIp) {
    checkArgument(
        sourceInterface == null || sourceIp == null,
        "Can only specify one of sourceIp and sourceInterface");
    _destinationIp = destinationIp;
    _sourceInterface = sourceInterface;
    _sourceIp = sourceIp;
  }

  @Override
  public <T> T accept(IpSlaVisitor<T> visitor) {
    return visitor.visitIcmpEchoSla(this);
  }

  @Override
  public @Nullable String getVrf() {
    return _vrf;
  }

  @Override
  public void setVrf(@Nullable String vrf) {
    _vrf = vrf;
  }

  public @Nullable Ip getDestinationIp() {
    return _destinationIp;
  }

  public @Nullable String getSourceInterface() {
    return _sourceInterface;
  }

  public @Nullable Ip getSourceIp() {
    return _sourceIp;
  }

  private final @Nullable Ip _destinationIp;
  private final @Nullable String _sourceInterface;
  private final @Nullable Ip _sourceIp;
  private @Nullable String _vrf;
}

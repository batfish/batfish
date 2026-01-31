package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** UDP options for an {@link ActionIpAccessListLine}. */
public final class UdpOptions implements Layer4Options {

  public static final class Builder {

    private @Nullable PortSpec _dstPortSpec;
    private @Nullable PortSpec _srcPortSpec;

    private Builder() {}

    public @Nonnull UdpOptions build() {
      return new UdpOptions(_dstPortSpec, _srcPortSpec);
    }

    public @Nonnull Builder setDstPortSpec(PortSpec dstPortSpec) {
      _dstPortSpec = dstPortSpec;
      return this;
    }

    public @Nonnull Builder setSrcPortSpec(PortSpec srcPortSpec) {
      _srcPortSpec = srcPortSpec;
      return this;
    }
  }

  public static final @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nullable PortSpec _dstPortSpec;
  private final @Nullable PortSpec _srcPortSpec;

  private UdpOptions(@Nullable PortSpec dstPortSpec, @Nullable PortSpec srcPortSpec) {
    _dstPortSpec = dstPortSpec;
    _srcPortSpec = srcPortSpec;
  }

  @Override
  public <T> T accept(Layer4OptionsVisitor<T> visitor) {
    return visitor.visitUdpOptions(this);
  }

  public @Nullable PortSpec getDstPortSpec() {
    return _dstPortSpec;
  }

  public @Nullable PortSpec getSrcPortSpec() {
    return _srcPortSpec;
  }
}

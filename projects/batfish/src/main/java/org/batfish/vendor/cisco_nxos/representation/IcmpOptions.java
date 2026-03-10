package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nullable;

/** ICMP options for an {@link ActionIpAccessListLine}. */
public final class IcmpOptions implements Layer4Options {

  private final @Nullable Integer _code;
  private final int _type;

  public IcmpOptions(int type, @Nullable Integer code) {
    _type = type;
    _code = code;
  }

  @Override
  public <T> T accept(Layer4OptionsVisitor<T> visitor) {
    return visitor.visitIcmpOptions(this);
  }

  public @Nullable Integer getCode() {
    return _code;
  }

  public int getType() {
    return _type;
  }
}

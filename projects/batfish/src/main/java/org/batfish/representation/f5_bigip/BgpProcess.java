package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class BgpProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull BgpIpv6AddressFamily _ipv6AddressFamily;
  private final @Nonnull BgpIpv4AddressFamily _ipv4AddressFamily;
  private final @Nonnull String _name;

  public BgpProcess(String name) {
    _name = name;
    _ipv4AddressFamily = new BgpIpv4AddressFamily();
    _ipv6AddressFamily = new BgpIpv6AddressFamily();
  }

  public @Nonnull BgpIpv4AddressFamily getIpv4AddressFamily() {
    return _ipv4AddressFamily;
  }

  public @Nonnull BgpIpv6AddressFamily getIpv6AddressFamily() {
    return _ipv6AddressFamily;
  }
}

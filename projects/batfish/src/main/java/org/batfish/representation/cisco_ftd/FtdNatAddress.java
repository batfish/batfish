package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

public interface FtdNatAddress extends Serializable {
  <T> T accept(Visitor<T> visitor);

  interface Visitor<T> {
    T visitFtdNatAddressIp(FtdNatAddressIp ftdNatAddressIp);

    T visitFtdNatAddressName(FtdNatAddressName ftdNatAddressName);
  }

  class FtdNatAddressIp implements FtdNatAddress {
    private final @Nonnull Ip _ip;

    public FtdNatAddressIp(@Nonnull Ip ip) {
      _ip = ip;
    }

    public @Nonnull Ip getIp() {
      return _ip;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
      return visitor.visitFtdNatAddressIp(this);
    }
  }

  class FtdNatAddressName implements FtdNatAddress {
    private final @Nonnull String _name;

    public FtdNatAddressName(@Nonnull String name) {
      _name = name;
    }

    public @Nonnull String getName() {
      return _name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
      return visitor.visitFtdNatAddressName(this);
    }
  }
}

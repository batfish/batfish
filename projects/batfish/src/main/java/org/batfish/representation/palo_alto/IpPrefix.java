package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * Palo Alto ipv4 prefix structure preserving initial (not canonical) {@code Ip} in addition to a
 * canonical {@code Prefix}.
 */
public class IpPrefix implements Serializable {

  /** A "0.0.0.0/0" prefix */
  public static final IpPrefix ZERO = new IpPrefix(Ip.ZERO, 0);

  private Prefix _prefix;
  private Ip _ip;

  IpPrefix(Ip ip, int prefixLength) {
    _prefix = Prefix.create(ip, prefixLength);
    _ip = ip;
  }

  /** Parse a {@link Prefix} from a string. */
  @Nonnull
  public static IpPrefix parse(@Nonnull String text) {
    ConcreteInterfaceAddress concreteInterfaceAddress = ConcreteInterfaceAddress.parse(text);
    return new IpPrefix(
        concreteInterfaceAddress.getIp(), concreteInterfaceAddress.getNetworkBits());
  }

  public Ip getIp() {
    return _ip;
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}

package org.batfish.representation.palo_alto;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * Palo Alto ipv4 prefix structure preserving initial (not canonical) {@code Ip} in addition to a
 * canonical {@code Prefix}.
 */
public class IpPrefix implements Serializable {

  /** A "0.0.0.0/0" prefix */
  public static final IpPrefix ZERO = new IpPrefix(Ip.ZERO, 0);

  private final @Nonnull Prefix _prefix;
  private final @Nonnull Ip _ip;

  IpPrefix(Ip ip, int prefixLength) {
    _prefix = Prefix.create(ip, prefixLength);
    _ip = ip;
  }

  /** Parse a {@link Prefix} from a string. */
  @Nonnull
  public static IpPrefix parse(@Nonnull String text) {
    String[] parts = text.split("/");
    checkArgument(
        parts.length == 2, "Invalid %s string: \"%s\"", IpPrefix.class.getSimpleName(), text);
    Ip ip = Ip.parse(parts[0]);
    int prefixLength = Integer.parseUnsignedInt(parts[1]);
    return new IpPrefix(ip, prefixLength);
  }

  public Ip getIp() {
    return _ip;
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}

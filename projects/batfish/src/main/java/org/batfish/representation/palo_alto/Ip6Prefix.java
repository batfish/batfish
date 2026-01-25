package org.batfish.representation.palo_alto;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;

/**
 * Palo Alto ipv6 prefix structure preserving initial (not canonical) {@code Ip6} in addition to a
 * canonical {@code Prefix6}.
 */
public class Ip6Prefix implements Serializable {

  private final @Nonnull Prefix6 _prefix;
  private final @Nonnull Ip6 _ip;

  public Ip6Prefix(Ip6 ip, int prefixLength) {
    _prefix = Prefix6.create(ip, prefixLength);
    _ip = ip;
  }

  /** Parse a {@link Prefix6} from a string. */
  public static @Nonnull Ip6Prefix parse(@Nonnull String text) {
    String[] parts = text.split("/");
    checkArgument(
        parts.length == 2, "Invalid %s string: \"%s\"", Ip6Prefix.class.getSimpleName(), text);
    Ip6 ip = Ip6.parse(parts[0]);
    int prefixLength = Integer.parseUnsignedInt(parts[1]);
    return new Ip6Prefix(ip, prefixLength);
  }

  public @Nonnull Ip6 getIp() {
    return _ip;
  }

  public @Nonnull Prefix6 getPrefix() {
    return _prefix;
  }
}

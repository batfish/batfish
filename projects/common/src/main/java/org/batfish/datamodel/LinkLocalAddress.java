package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Link-local IPv4 address. A non-routable interface address in the 169.254.0.0/16 space.
 *
 * <p>See <a href="https://tools.ietf.org/html/rfc3927">RFC 3927</a> for details
 */
@ParametersAreNonnullByDefault
public final class LinkLocalAddress extends InterfaceAddress {

  private static final int NETWORK_BITS = 16;
  private static final Prefix PREFIX = Prefix.create(Ip.parse("169.254.0.0"), NETWORK_BITS);
  private static final String STR_PREFIX = "link-local";

  private final @Nonnull Ip _ip;

  private LinkLocalAddress(Ip ip) {
    _ip = ip;
  }

  public static LinkLocalAddress of(Ip ip) {
    checkArgument(
        PREFIX.containsIp(ip), "Provided ip %s is not in the link-local space (%s)", ip, PREFIX);
    return new LinkLocalAddress(ip);
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  public Prefix getPrefix() {
    return PREFIX;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LinkLocalAddress)) {
      return false;
    }
    LinkLocalAddress that = (LinkLocalAddress) o;
    return _ip.equals(that._ip);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ip);
  }

  @Override
  @JsonValue
  public String toString() {
    return STR_PREFIX + ":" + _ip;
  }

  @JsonCreator
  private static LinkLocalAddress jsonCreator(@Nullable String text) {
    checkArgument(text != null);
    return parse(text);
  }

  public static LinkLocalAddress parse(String text) {
    String[] parts = text.split(":");
    checkArgument(
        parts.length == 2 && parts[0].equalsIgnoreCase(STR_PREFIX),
        "Invalid %s string: \"%s\"",
        LinkLocalAddress.class.getSimpleName(),
        text);
    Ip ip = Ip.parse(parts[1]);
    return of(ip);
  }

  @Override
  public int compareTo(InterfaceAddress o) {
    if (o instanceof LinkLocalAddress) {
      return _ip.compareTo(((LinkLocalAddress) o).getIp());
    }
    return -1;
  }
}

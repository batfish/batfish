package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An unnumbered interface address that borrows its IP from another interface (typically a
 * loopback).
 *
 * <p>Unnumbered interfaces do not own a connected route for the borrowed IP and do not participate
 * in subnet-based L3 topology synthesis. They form adjacencies (e.g., OSPF point-to-point) via
 * physical link adjacency rather than shared subnets.
 */
@ParametersAreNonnullByDefault
public final class UnnumberedAddress extends InterfaceAddress {

  static final String STR_PREFIX = "unnumbered";

  private final @Nonnull String _sourceInterfaceName;
  private final @Nonnull Ip _ip;

  private UnnumberedAddress(String sourceInterfaceName, Ip ip) {
    _sourceInterfaceName = sourceInterfaceName;
    _ip = ip;
  }

  public static UnnumberedAddress of(String sourceInterfaceName, Ip ip) {
    checkArgument(!sourceInterfaceName.isEmpty(), "Source interface name must not be empty");
    return new UnnumberedAddress(sourceInterfaceName, ip);
  }

  public @Nonnull String getSourceInterfaceName() {
    return _sourceInterfaceName;
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnnumberedAddress)) {
      return false;
    }
    UnnumberedAddress that = (UnnumberedAddress) o;
    return _sourceInterfaceName.equals(that._sourceInterfaceName) && _ip.equals(that._ip);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_sourceInterfaceName, _ip);
  }

  @Override
  @JsonValue
  public String toString() {
    return STR_PREFIX + ":" + _sourceInterfaceName + ":" + _ip;
  }

  @JsonCreator
  private static UnnumberedAddress jsonCreator(@Nullable String text) {
    checkArgument(text != null);
    return parse(text);
  }

  public static UnnumberedAddress parse(String text) {
    checkArgument(
        text.startsWith(STR_PREFIX + ":"),
        "Invalid %s string: \"%s\"",
        UnnumberedAddress.class.getSimpleName(),
        text);
    String remainder = text.substring(STR_PREFIX.length() + 1);
    int lastColon = remainder.lastIndexOf(':');
    checkArgument(
        lastColon > 0, "Invalid %s string: \"%s\"", UnnumberedAddress.class.getSimpleName(), text);
    String sourceInterface = remainder.substring(0, lastColon);
    Ip ip = Ip.parse(remainder.substring(lastColon + 1));
    return of(sourceInterface, ip);
  }

  @Override
  public int compareTo(InterfaceAddress o) {
    if (o instanceof UnnumberedAddress) {
      UnnumberedAddress that = (UnnumberedAddress) o;
      int c = _sourceInterfaceName.compareTo(that._sourceInterfaceName);
      return c != 0 ? c : _ip.compareTo(that._ip);
    }
    if (o instanceof LinkLocalAddress) {
      return 1;
    }
    // ConcreteInterfaceAddress
    return -1;
  }
}

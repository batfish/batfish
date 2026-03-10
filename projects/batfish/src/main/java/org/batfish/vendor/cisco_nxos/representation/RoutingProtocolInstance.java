package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An instance of a routing protocol on NX-OS - a {@link NxosRoutingProtocol} and an optional
 * process/instance tag or ID.
 */
public final class RoutingProtocolInstance implements Serializable {
  public static RoutingProtocolInstance bgp(long asn) {
    return new RoutingProtocolInstance(NxosRoutingProtocol.BGP, Long.toString(asn));
  }

  public static RoutingProtocolInstance direct() {
    return new RoutingProtocolInstance(NxosRoutingProtocol.DIRECT, null);
  }

  public static RoutingProtocolInstance eigrp(String tag) {
    return new RoutingProtocolInstance(NxosRoutingProtocol.EIGRP, tag);
  }

  public static RoutingProtocolInstance isis(String tag) {
    return new RoutingProtocolInstance(NxosRoutingProtocol.ISIS, tag);
  }

  public static RoutingProtocolInstance lisp() {
    return new RoutingProtocolInstance(NxosRoutingProtocol.LISP, null);
  }

  public static RoutingProtocolInstance ospf(String tag) {
    return new RoutingProtocolInstance(NxosRoutingProtocol.OSPF, tag);
  }

  public static RoutingProtocolInstance ospfv3(String tag) {
    return new RoutingProtocolInstance(NxosRoutingProtocol.OSPFv3, tag);
  }

  public static RoutingProtocolInstance rip(String tag) {
    return new RoutingProtocolInstance(NxosRoutingProtocol.RIP, tag);
  }

  // can't be called static - language keyword
  public static RoutingProtocolInstance staticc() {
    return new RoutingProtocolInstance(NxosRoutingProtocol.STATIC, null);
  }

  public @Nonnull NxosRoutingProtocol getProtocol() {
    return _protocol;
  }

  public @Nullable String getTag() {
    return _tag;
  }

  //////////////////////////////////
  // private implementation details
  //////////////////////////////////

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof RoutingProtocolInstance)) {
      return false;
    }
    RoutingProtocolInstance that = (RoutingProtocolInstance) o;
    return _protocol == that._protocol && Objects.equals(_tag, that._tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_protocol, _tag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("protocol", _protocol)
        .add("tag", _tag)
        .toString();
  }

  private RoutingProtocolInstance(NxosRoutingProtocol protocol, @Nullable String tag) {
    _protocol = protocol;
    _tag = tag;
  }

  private final @Nonnull NxosRoutingProtocol _protocol;
  private final @Nullable String _tag;
}

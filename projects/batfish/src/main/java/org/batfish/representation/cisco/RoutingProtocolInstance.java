package org.batfish.representation.cisco;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.RoutingProtocol;

/**
 * An instance of a routing protocol on IOS - a {@link CiscoRoutingProtocol} and an optional
 * process/instance tag or ID.
 */
public final class RoutingProtocolInstance
    implements Comparable<RoutingProtocolInstance>, Serializable {
  public static RoutingProtocolInstance bgp(long tag) {
    return new RoutingProtocolInstance(RoutingProtocol.BGP, Long.toString(tag));
  }

  public static RoutingProtocolInstance connected() {
    return new RoutingProtocolInstance(RoutingProtocol.CONNECTED, null);
  }

  public static RoutingProtocolInstance eigrp(long tag) {
    return new RoutingProtocolInstance(RoutingProtocol.EIGRP, Long.toString(tag));
  }

  public static RoutingProtocolInstance ospf() {
    return new RoutingProtocolInstance(RoutingProtocol.OSPF, null);
  }

  public static RoutingProtocolInstance rip() {
    return new RoutingProtocolInstance(RoutingProtocol.RIP, null);
  }

  public static RoutingProtocolInstance isis_l1() {
    return new RoutingProtocolInstance(RoutingProtocol.ISIS_L1, null);
  }

  public static RoutingProtocolInstance staticRoutingProtocol() {
    return new RoutingProtocolInstance(RoutingProtocol.STATIC, null);
  }

  private final @Nonnull RoutingProtocol _protocol;
  private final @Nullable String _tag;

  public RoutingProtocolInstance(RoutingProtocol protocol, @Nullable String tag) {
    _protocol = protocol;
    _tag = tag;
  }

  public @Nonnull RoutingProtocol getProtocol() {
    return _protocol;
  }

  public @Nullable String getTag() {
    return _tag;
  }

  @Override
  public int compareTo(RoutingProtocolInstance other) {
    int proto = _protocol.ordinal() - other._protocol.ordinal();
    if (proto != 0) {
      return proto;
    }
    return Comparator.nullsFirst(String::compareTo).compare(_tag, other._tag);
  }

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
    return Objects.hash(_protocol.ordinal(), _tag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("protocol", _protocol)
        .add("tag", _tag)
        .toString();
  }
}

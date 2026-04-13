package org.batfish.datamodel.route.nh;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Route;

/**
 * Represent a generic routing next hop. There are many types of next hops: IPv4 concrete address,
 * interface, next hops to be looked up in a different VRF, null-routing next hops. Consumers of
 * this interface <em>should</em> be prepared to deal with different types of next hops.
 */
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = NextHopDiscard.class, name = "discard"),
  @JsonSubTypes.Type(value = NextHopInterface.class, name = "interface"),
  @JsonSubTypes.Type(value = NextHopIp.class, name = "ip"),
  @JsonSubTypes.Type(value = NextHopVrf.class, name = "vrf"),
  @JsonSubTypes.Type(value = NextHopVtep.class, name = "vtep")
})
public interface NextHop extends Serializable, Comparable<NextHop> {
  <T> T accept(NextHopVisitor<T> visitor);

  /**
   * Returns a type-specific ordering value for this NextHop. Used to establish a strict total
   * ordering across NextHop types: Discard < Ip < Interface < Vrf < Vtep.
   */
  @JsonIgnore
  default int getTypeOrder() {
    return accept(
        new NextHopVisitor<Integer>() {
          @Override
          public Integer visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
            return 0;
          }

          @Override
          public Integer visitNextHopIp(NextHopIp nextHopIp) {
            return 1;
          }

          @Override
          public Integer visitNextHopInterface(NextHopInterface nextHopInterface) {
            return 2;
          }

          @Override
          public Integer visitNextHopVrf(NextHopVrf nextHopVrf) {
            return 3;
          }

          @Override
          public Integer visitNextHopVtep(NextHopVtep nextHopVtep) {
            return 4;
          }
        });
  }

  @Override
  default int compareTo(NextHop other) {
    // First compare by type
    int typeComparison = Integer.compare(getTypeOrder(), other.getTypeOrder());
    if (typeComparison != 0) {
      return typeComparison;
    }

    // Same type - delegate to type-specific comparison
    return accept(
        new NextHopVisitor<Integer>() {
          @Override
          public Integer visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
            return 0; // All NextHopDiscard instances are equal
          }

          @Override
          public Integer visitNextHopIp(NextHopIp nextHopIp) {
            return nextHopIp.getIp().compareTo(((NextHopIp) other).getIp());
          }

          @Override
          public Integer visitNextHopInterface(NextHopInterface nextHopInterface) {
            NextHopInterface otherInterface = (NextHopInterface) other;
            int cmp =
                nextHopInterface.getInterfaceName().compareTo(otherInterface.getInterfaceName());
            if (cmp != 0) {
              return cmp;
            }
            // Compare IPs null-safe
            Ip ip1 = nextHopInterface.getIp();
            Ip ip2 = otherInterface.getIp();
            if (ip1 == null) {
              return ip2 == null ? 0 : -1;
            } else {
              return ip2 == null ? 1 : ip1.compareTo(ip2);
            }
          }

          @Override
          public Integer visitNextHopVrf(NextHopVrf nextHopVrf) {
            return nextHopVrf.getVrfName().compareTo(((NextHopVrf) other).getVrfName());
          }

          @Override
          public Integer visitNextHopVtep(NextHopVtep nextHopVtep) {
            NextHopVtep otherVtep = (NextHopVtep) other;
            int cmp = Integer.compare(nextHopVtep.getVni(), otherVtep.getVni());
            if (cmp != 0) {
              return cmp;
            }
            return nextHopVtep.getVtepIp().compareTo(otherVtep.getVtepIp());
          }
        });
  }

  /**
   * Returns a {@link NextHop} based on next hop interface and next hop ip, one of which must be
   * nonnull
   */
  static NextHop legacyConverter(@Nullable String nextHopInterface, @Nullable Ip nextHopIp) {
    if (nextHopInterface != null && !Route.UNSET_NEXT_HOP_INTERFACE.equals(nextHopInterface)) {
      if (nextHopInterface.equals(Interface.NULL_INTERFACE_NAME)) {
        return NextHopDiscard.instance();
      }
      if (nextHopIp != null) {
        return NextHopInterface.of(nextHopInterface, nextHopIp);
      } else {
        return NextHopInterface.of(nextHopInterface);
      }
    } else {
      if (nextHopIp != null) {
        return NextHopIp.of(nextHopIp);
      } else {
        throw new IllegalArgumentException(
            "Cannot construct a next hop when missing both a next hop IP and interface");
      }
    }
  }
}

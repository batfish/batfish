package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import java.util.function.Supplier;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public enum HostProtocol {
  ALL,
  BFD,
  BGP,
  DVMRP,
  IGMP,
  LDP,
  MSDP,
  NHRP,
  OSPF,
  OSPF3,
  PGM,
  PIM,
  RIP,
  RIPNG,
  ROUTER_DISCOVERY,
  RSVP,
  SAP,
  VRRP;

  private final Supplier<Optional<AclLineMatchExpr>> _matchExpr;

  HostProtocol() {
    _matchExpr = Suppliers.memoize(this::init);
  }

  public Optional<AclLineMatchExpr> getMatchExpr() {
    return _matchExpr.get();
  }

  private Optional<AclLineMatchExpr> init() {
    TraceElement traceElement =
        TraceElement.of(String.format("Matched host-inbound-traffic protocol %s", this));
    switch (this) {
      case ALL:
        {
          ImmutableList.Builder<AclLineMatchExpr> exprs = ImmutableList.builder();
          for (HostProtocol other : values()) {
            if (other != ALL) {
              other.getMatchExpr().ifPresent(exprs::add);
            }
          }
          return Optional.of(or(exprs.build(), traceElement));
        }

      case BFD:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP))
                  .setDstPorts(
                      ImmutableSet.of(
                          new SubRange(
                              NamedPort.BFD_CONTROL.number(), NamedPort.BFD_ECHO.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case BGP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.BGP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case DVMRP:
        {
          // TODO: DVMRP uses IGMP (an IP Protocol) type 3. need to add support
          // for IGMP types in packet headers
          HeaderSpace hs =
              HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.IGMP)).build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case IGMP:
        {
          HeaderSpace hs =
              HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.IGMP)).build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case LDP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.LDP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case MSDP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.MSDP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case NHRP:
        {
          HeaderSpace hs =
              HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.NARP)).build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case OSPF:
        {
          HeaderSpace hs =
              HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.OSPF)).build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case OSPF3:
        {
          // TODO: OSPFv3 is an IPV6-encapsulated protocol
          return Optional.empty();
        }

      case PGM:
        {
          HeaderSpace hs =
              HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.PGM)).build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case PIM:
        {
          HeaderSpace hs =
              HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.PIM)).build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case RIP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(
                      ImmutableSet.of(SubRange.singleton(NamedPort.EFStcp_OR_RIPudp.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case RIPNG:
        {
          // TODO: RIPng is an IPV6-encapsulated protocol
          return Optional.empty();
        }

      case ROUTER_DISCOVERY:
        {
          // TODO: ROUTER_DISCOVERY uses ICMP (an IP Protocol) type 9. need to
          // add support
          // for ICMP types in packet headers
          HeaderSpace hs =
              HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.ICMP)).build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case RSVP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(
                      ImmutableSortedSet.of(IpProtocol.RSVP, IpProtocol.RSVP_E2E_IGNORE))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case SAP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.SAP.number())))
                  .setDstIps(ImmutableSet.of(IpWildcard.parse("224.2.127.254/32")))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }

      case VRRP:
        {
          HeaderSpace hs =
              HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.VRRP)).build();
          return Optional.of(new MatchHeaderSpace(hs, traceElement));
        }
    }
    throw new IllegalStateException("Should be unreachable");
  }
}

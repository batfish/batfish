package org.batfish.representation.juniper;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import java.util.function.Supplier;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.OrMatchExpr;

public enum HostSystemService {
  ALL,
  ANY_SERVICE,
  DHCP,
  DNS,
  FINGER,
  FTP,
  HTTP,
  HTTPS,
  IDENT_RESET,
  IKE,
  LSPING,
  NETCONF,
  NTP,
  PING,
  R2CP,
  REVERSE_SSH,
  REVERSE_TELNET,
  RLOGIN,
  RPM,
  RSH,
  SIP,
  SNMP,
  SNMP_TRAP,
  SSH,
  TELNET,
  TFTP,
  TRACEROUTE,
  XNM_CLEAR_TEXT,
  XNM_SSL;

  private final Supplier<Optional<AclLineMatchExpr>> _matchExpr;

  public Optional<AclLineMatchExpr> getMatchExpr() {
    return _matchExpr.get();
  }

  HostSystemService() {
    _matchExpr = Suppliers.memoize(this::init);
  }

  TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched host-inbound-traffic system-service %s", this));
  }

  private Optional<AclLineMatchExpr> init() {
    switch (this) {
      case ALL:
        {
          ImmutableList.Builder<AclLineMatchExpr> exprs = ImmutableList.builder();
          for (HostSystemService other : values()) {
            if (other != ALL && other != ANY_SERVICE) {
              other.getMatchExpr().ifPresent(exprs::add);
            }
          }
          return Optional.of(new OrMatchExpr(exprs.build(), getTraceElement()));
        }

      case ANY_SERVICE:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case DHCP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(
                      ImmutableSet.of(
                          new SubRange(
                              NamedPort.BOOTPS_OR_DHCP.number(), NamedPort.BOOTPC.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case DNS:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.DOMAIN.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case FINGER:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.FINGER.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case FTP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.FTP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case HTTP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.HTTP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case HTTPS:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.HTTPS.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case IDENT_RESET:
        {
          // TODO: ??? (Juniper documentation is opaque)
          return Optional.empty();
        }

      case IKE:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(
                      ImmutableSortedSet.of(
                          SubRange.singleton(NamedPort.ISAKMP.number()),
                          SubRange.singleton(NamedPort.NON500_ISAKMP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case LSPING:
        {
          // TODO: ??? (Juniper documentation is missing or hiding for this
          // service)
          return Optional.empty();
        }

      case NETCONF:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.NETCONF_SSH.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case NTP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.NTP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case PING:
        {
          HeaderSpace hs =
              HeaderSpace.builder().setIpProtocols(ImmutableSet.of(IpProtocol.ICMP)).build();
          // TODO: PING (ECHO REQUEST) uses ICMP (an IP Protocol) type 8. need to
          // add support for ICMP types in packet headers
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case R2CP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.R2CP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case REVERSE_SSH:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.REVERSE_SSH.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case REVERSE_TELNET:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(
                      ImmutableSet.of(SubRange.singleton(NamedPort.REVERSE_TELNET.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case RLOGIN:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(
                      ImmutableSet.of(SubRange.singleton(NamedPort.LOGINtcp_OR_WHOudp.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case RPM:
        {
          // TODO: It appears there is no default port, and the port must be
          // deduced from other settings. It can be any combination of TCP/UDP
          return Optional.of(
              new MatchHeaderSpace(HeaderSpace.builder().build(), getTraceElement()));
        }

      case RSH:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(
                      ImmutableSet.of(SubRange.singleton(NamedPort.CMDtcp_OR_SYSLOGudp.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case SIP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP))
                  .setDstPorts(
                      ImmutableSet.of(
                          new SubRange(NamedPort.SIP_5060.number(), NamedPort.SIP_5061.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case SNMP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.SNMP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case SNMP_TRAP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.SNMPTRAP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case SSH:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.SSH.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case TELNET:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.TELNET.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case TFTP:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.TFTP.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case TRACEROUTE:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.UDP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.TRACEROUTE.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case XNM_CLEAR_TEXT:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(
                      ImmutableSet.of(SubRange.singleton(NamedPort.XNM_CLEAR_TEXT.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      case XNM_SSL:
        {
          HeaderSpace hs =
              HeaderSpace.builder()
                  .setIpProtocols(ImmutableSet.of(IpProtocol.TCP))
                  .setDstPorts(ImmutableSet.of(SubRange.singleton(NamedPort.XNM_SSL.number())))
                  .build();
          return Optional.of(new MatchHeaderSpace(hs, getTraceElement()));
        }

      default:
        {
          throw new BatfishException(
              "missing definition for host-inbound-traffic system-service: \"" + name() + "\"");
        }
    }
  }
}

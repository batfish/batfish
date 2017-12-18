package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;

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

  private boolean _initialized;

  private List<IpAccessListLine> _lines;

  public List<IpAccessListLine> getLines() {
    init();
    return _lines;
  }

  private synchronized void init() {
    if (_initialized) {
      return;
    }
    _initialized = true;
    _lines = new ArrayList<>();
    switch (this) {
      case ALL:
        {
          for (HostSystemService other : values()) {
            if (other != ALL && other != ANY_SERVICE) {
              _lines.addAll(other.getLines());
            }
          }
          break;
        }

      case ANY_SERVICE:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP));
          break;
        }

      case DHCP:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.UDP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.BOOTPS_OR_DHCP.number(), NamedPort.BOOTPC.number())));
          break;
        }

      case DNS:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.DOMAIN.number(), NamedPort.DOMAIN.number())));
          break;
        }

      case FINGER:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.FINGER.number(), NamedPort.FINGER.number())));
          break;
        }

      case FTP:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(new SubRange(NamedPort.FTP.number(), NamedPort.FTP.number())));
          break;
        }

      case HTTP:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.HTTP.number(), NamedPort.HTTP.number())));
          break;
        }

      case HTTPS:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.HTTPS.number(), NamedPort.HTTPS.number())));
          break;
        }

      case IDENT_RESET:
        {
          // TODO: ??? (Juniper documentation is opaque)
          break;
        }

      case IKE:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.UDP));
          line.setDstPorts(
              ImmutableSortedSet.of(
                  new SubRange(NamedPort.ISAKMP.number(), NamedPort.ISAKMP.number()),
                  new SubRange(
                      NamedPort.NON500_ISAKMP.number(), NamedPort.NON500_ISAKMP.number())));
          break;
        }

      case LSPING:
        {
          // TODO: ??? (Juniper documentation is missing or hiding for this
          // service)
          break;
        }

      case NETCONF:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.NETCONF_SSH.number(), NamedPort.NETCONF_SSH.number())));
          break;
        }

      case NTP:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.UDP));
          line.setDstPorts(
              Collections.singleton(new SubRange(NamedPort.NTP.number(), NamedPort.NTP.number())));
          break;
        }

      case PING:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.ICMP));
          // TODO: PING (ECHO REQUEST) uses ICMP (an IP Protocol) type 8. need to
          // add support for ICMP types in packet headers
          break;
        }

      case R2CP:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.UDP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.R2CP.number(), NamedPort.R2CP.number())));
          break;
        }

      case REVERSE_SSH:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.REVERSE_SSH.number(), NamedPort.REVERSE_SSH.number())));
          break;
        }

      case REVERSE_TELNET:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(
                      NamedPort.REVERSE_TELNET.number(), NamedPort.REVERSE_TELNET.number())));
          break;
        }

      case RLOGIN:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(
                      NamedPort.LOGINtcp_OR_WHOudp.number(),
                      NamedPort.LOGINtcp_OR_WHOudp.number())));
          break;
        }

      case RPM:
        {
          // TODO: It appears there is no default port, and the port must be
          // deduced from other settings. It can be any combination of TCP/UDP
          break;
        }

      case RSH:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(
                      NamedPort.CMDtcp_OR_SYSLOGudp.number(),
                      NamedPort.CMDtcp_OR_SYSLOGudp.number())));
          break;
        }

      case SIP:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(ImmutableSortedSet.of(IpProtocol.TCP, IpProtocol.UDP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.SIP_5060.number(), NamedPort.SIP_5061.number())));
          break;
        }

      case SNMP:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.UDP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.SNMP.number(), NamedPort.SNMP.number())));
          break;
        }

      case SNMP_TRAP:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.UDP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.SNMPTRAP.number(), NamedPort.SNMPTRAP.number())));
          break;
        }

      case SSH:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(new SubRange(NamedPort.SSH.number(), NamedPort.SSH.number())));
          break;
        }

      case TELNET:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.TELNET.number(), NamedPort.TELNET.number())));
          break;
        }

      case TFTP:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.UDP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.TFTP.number(), NamedPort.TFTP.number())));
          break;
        }

      case TRACEROUTE:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.UDP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.TRACEROUTE.number(), NamedPort.TRACEROUTE.number())));
          break;
        }

      case XNM_CLEAR_TEXT:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(
                      NamedPort.XNM_CLEAR_TEXT.number(), NamedPort.XNM_CLEAR_TEXT.number())));
          break;
        }

      case XNM_SSL:
        {
          IpAccessListLine line = new IpAccessListLine();
          _lines.add(line);
          line.setIpProtocols(Collections.singleton(IpProtocol.TCP));
          line.setDstPorts(
              Collections.singleton(
                  new SubRange(NamedPort.XNM_SSL.number(), NamedPort.XNM_SSL.number())));
          break;
        }

      default:
        {
          throw new BatfishException(
              "missing definition for host-inbound-traffic system-service: \"" + name() + "\"");
        }
    }

    for (IpAccessListLine line : _lines) {
      line.setAction(LineAction.ACCEPT);
    }
  }
}

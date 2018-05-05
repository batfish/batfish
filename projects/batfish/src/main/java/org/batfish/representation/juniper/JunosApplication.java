package org.batfish.representation.juniper;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.common.util.DefinedStructure;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;
import org.batfish.representation.juniper.BaseApplication.Term;

public enum JunosApplication implements Application {
  /**
   * TODO: separate applications from application-sets
   * the list below inlcudes both applications and application-sets and does not distinguish
   * between them
   */
  JUNOS_AOL,
  JUNOS_BGP,
  JUNOS_BIFF,
  JUNOS_BOOTPC,
  JUNOS_BOOTPS,
  JUNOS_CHARGEN,
  JUNOS_CIFS,
  JUNOS_CVSPSERVER,
  JUNOS_DHCP_CLIENT,
  JUNOS_DHCP_RELAY,
  JUNOS_DHCP_SERVER,
  JUNOS_DISCARD,
  JUNOS_DNS_TCP,
  JUNOS_DNS_UDP,
  JUNOS_ECHO,
  JUNOS_FINGER,
  JUNOS_FTP,
  JUNOS_FTP_DATA,
  JUNOS_GNUTELLA,
  JUNOS_GOPHER,
  JUNOS_GPRS_GTP_C,
  JUNOS_GPRS_GTP_U,
  JUNOS_GPRS_GTP_V0,
  JUNOS_GPRS_SCTP,
  JUNOS_GRE,
  JUNOS_GTP,
  JUNOS_H323,
  JUNOS_HTTP,
  JUNOS_HTTP_EXT,
  JUNOS_HTTPS,
  JUNOS_ICMP_ALL,
  JUNOS_ICMP_PING,
  JUNOS_ICMP6_ALL,
  JUNOS_ICMP6_DST_UNREACH_ADDR,
  JUNOS_ICMP6_DST_UNREACH_ADMIN,
  JUNOS_ICMP6_DST_UNREACH_BEYOND,
  JUNOS_ICMP6_DST_UNREACH_PORT,
  JUNOS_ICMP6_DST_UNREACH_ROUTE,
  JUNOS_ICMP6_ECHO_REPLY,
  JUNOS_ICMP6_ECHO_REQUEST,
  JUNOS_ICMP6_PACKET_TOO_BIG,
  JUNOS_ICMP6_PARAM_PROB_HEADER,
  JUNOS_ICMP6_PARAM_PROB_NEXTHDR,
  JUNOS_ICMP6_PARAM_PROB_OPTION,
  JUNOS_ICMP6_TIME_EXCEED_REASSEMBLY,
  JUNOS_ICMP6_TIME_EXCEED_TRANSIT,
  JUNOS_IDENT,
  JUNOS_IKE,
  JUNOS_IKE_NAT,
  JUNOS_IMAP,
  JUNOS_IMAPS,
  JUNOS_INTERNET_LOCATOR_SERVICE,
  JUNOS_IRC,
  JUNOS_L2TP,
  JUNOS_LDAP,
  JUNOS_LDP_TCP,
  JUNOS_LDP_UDP,
  JUNOS_LPR,
  JUNOS_MAIL,
  JUNOS_MGCP,
  JUNOS_MGCP_CA,
  JUNOS_MGCP_UA,
  JUNOS_MS_RPC,
  JUNOS_MS_RPC_ANY,
  JUNOS_MS_RPC_EPM,
  JUNOS_MS_RPC_IIS_COM,
  JUNOS_MS_RPC_IIS_COM_1,
  JUNOS_MS_RPC_IIS_COM_ADMINBASE,
  JUNOS_MS_RPC_MSEXCHANGE,
  JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP,
  JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR,
  JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE,
  JUNOS_MS_RPC_TCP,
  JUNOS_MS_RPC_UDP,
  JUNOS_MS_RPC_UUID_ANY_TCP,
  JUNOS_MS_RPC_UUID_ANY_UDP,
  JUNOS_MS_RPC_WMIC,
  JUNOS_MS_RPC_WMIC_ADMIN,
  JUNOS_MS_RPC_WMIC_ADMIN2,
  JUNOS_MS_RPC_WMIC_MGMT,
  JUNOS_MS_RPC_WMIC_WEBM_CALLRESULT,
  JUNOS_MS_RPC_WMIC_WEBM_CLASSOBJECT,
  JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN,
  JUNOS_MS_RPC_WMIC_WEBM_LOGIN_CLIENTID,
  JUNOS_MS_RPC_WMIC_WEBM_LOGIN_HELPER,
  JUNOS_MS_RPC_WMIC_WEBM_OBJECTSINK,
  JUNOS_MS_RPC_WMIC_WEBM_REFRESHING_SERVICES,
  JUNOS_MS_RPC_WMIC_WEBM_REMOTE_REFRESHER,
  JUNOS_MS_RPC_WMIC_WEBM_SERVICES,
  JUNOS_MS_RPC_WMIC_WEBM_SHUTDOWN,
  JUNOS_MS_SQL,
  JUNOS_MSN,
  JUNOS_NBDS,
  JUNOS_NBNAME,
  JUNOS_NETBIOS_SESSION,
  JUNOS_NFS,
  JUNOS_NFSD_TCP,
  JUNOS_NFSD_UDP,
  JUNOS_NNTP,
  JUNOS_NS_GLOBAL,
  JUNOS_NS_GLOBAL_PRO,
  JUNOS_NSM,
  JUNOS_NTALK,
  JUNOS_NTP,
  JUNOS_OSPF,
  JUNOS_PC_ANYWHERE,
  JUNOS_PERSISTENT_NAT,
  JUNOS_PING,
  JUNOS_PINGV6,
  JUNOS_POP3,
  JUNOS_PPTP,
  JUNOS_PRINTER,
  JUNOS_R2CP,
  JUNOS_RADACCT,
  JUNOS_RADIUS,
  JUNOS_REALAUDIO,
  JUNOS_RIP,
  JUNOS_ROUTING_INBOUND,
  JUNOS_RSH,
  JUNOS_RTSP,
  JUNOS_SCCP,
  JUNOS_SCTP_ANY,
  JUNOS_SIP,
  JUNOS_SMB,
  JUNOS_SMB_SESSION,
  JUNOS_SMTP,
  JUNOS_SMTPS,
  JUNOS_SNMP_AGENTX,
  JUNOS_SNPP,
  JUNOS_SQL_MONITOR,
  JUNOS_SQLNET_V1,
  JUNOS_SQLNET_V2,
  JUNOS_SSH,
  JUNOS_STUN,
  JUNOS_SUN_RPC,
  JUNOS_SUN_RPC_ANY,
  JUNOS_SUN_RPC_ANY_TCP,
  JUNOS_SUN_RPC_ANY_UDP,
  JUNOS_SUN_RPC_MOUNTD,
  JUNOS_SUN_RPC_MOUNTD_TCP,
  JUNOS_SUN_RPC_MOUNTD_UDP,
  JUNOS_SUN_RPC_NFS,
  JUNOS_SUN_RPC_NFS_ACCESS,
  JUNOS_SUN_RPC_NFS_TCP,
  JUNOS_SUN_RPC_NFS_UDP,
  JUNOS_SUN_RPC_NLOCKMGR,
  JUNOS_SUN_RPC_NLOCKMGR_TCP,
  JUNOS_SUN_RPC_NLOCKMGR_UDP,
  JUNOS_SUN_RPC_PORTMAP,
  JUNOS_SUN_RPC_PORTMAP_TCP,
  JUNOS_SUN_RPC_PORTMAP_UDP,
  JUNOS_SUN_RPC_RQUOTAD,
  JUNOS_SUN_RPC_RQUOTAD_TCP,
  JUNOS_SUN_RPC_RQUOTAD_UDP,
  JUNOS_SUN_RPC_RUSERD,
  JUNOS_SUN_RPC_RUSERD_TCP,
  JUNOS_SUN_RPC_RUSERD_UDP,
  JUNOS_SUN_RPC_SADMIND,
  JUNOS_SUN_RPC_SADMIND_TCP,
  JUNOS_SUN_RPC_SADMIND_UDP,
  JUNOS_SUN_RPC_SPRAYD,
  JUNOS_SUN_RPC_SPRAYD_TCP,
  JUNOS_SUN_RPC_SPRAYD_UDP,
  JUNOS_SUN_RPC_STATUS,
  JUNOS_SUN_RPC_STATUS_TCP,
  JUNOS_SUN_RPC_STATUS_UDP,
  JUNOS_SUN_RPC_TCP,
  JUNOS_SUN_RPC_UDP,
  JUNOS_SUN_RPC_WALLD,
  JUNOS_SUN_RPC_WALLD_TCP,
  JUNOS_SUN_RPC_WALLD_UDP,
  JUNOS_SUN_RPC_YPBIND,
  JUNOS_SUN_RPC_YPBIND_TCP,
  JUNOS_SUN_RPC_YPBIND_UDP,
  JUNOS_SUN_RPC_YPSERV,
  JUNOS_SUN_RPC_YPSERV_TCP,
  JUNOS_SUN_RPC_YPSERV_UDP,
  JUNOS_SYSLOG,
  JUNOS_TACACS,
  JUNOS_TACACS_DS,
  JUNOS_TALK,
  JUNOS_TCP_ANY,
  JUNOS_TELNET,
  JUNOS_TFTP,
  JUNOS_UDP_ANY,
  JUNOS_UUCP,
  JUNOS_VDO_LIVE,
  JUNOS_VNC,
  JUNOS_WAIS,
  JUNOS_WHO,
  JUNOS_WHOIS,
  JUNOS_WINFRAME,
  JUNOS_WXCONTROL,
  JUNOS_X_WINDOWS,
  JUNOS_XNM_CLEAR_TEXT,
  JUNOS_XNM_SSL,
  JUNOS_YMSG;

  private final Supplier<BaseApplication> _baseApplication;

  private JunosApplication() {
    _baseApplication = Suppliers.memoize(this::init);
  }

  @Override
  public void applyTo(
      JuniperConfiguration jc,
      HeaderSpace.Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<IpAccessListLine> lines,
      Warnings w) {
    _baseApplication.get().applyTo(jc, srcHeaderSpaceBuilder, action, lines, w);
  }

  @Override
  public boolean getIpv6() {
    return _baseApplication.get().getIpv6();
  }

  private void setHeaderSpaceInfo(HeaderSpace.Builder hb,
      @Nullable IpProtocol ip,
      @Nullable Integer portRangeStart,
      @Nullable Integer portRangeEnd) {
    if (ip != null) hb.setIpProtocols(ImmutableSet.of(ip));
    if (portRangeStart != null) hb.setDstPorts(ImmutableSet.of(new SubRange(portRangeStart, portRangeEnd == null ? portRangeStart : portRangeEnd)));
  }

  private BaseApplication init() {
    BaseApplication baseApplication =
        new BaseApplication(name(), DefinedStructure.IGNORED_DEFINITION_LINE);
    Map<String, Term> terms = baseApplication.getTerms();

    Integer portRangeStart = null;
    Integer portRangeEnd = null;
    IpProtocol ip = null;
    Integer icmpType = null;

    switch (this) {
      case JUNOS_AOL:
      {
        portRangeStart = NamedPort.AOL.number();
        portRangeEnd = portRangeStart + 3;
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_BGP:
      {
        portRangeStart = NamedPort.BGP.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_BIFF:
      {
        portRangeStart = NamedPort.BIFFudp_OR_EXECtcp.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_BOOTPC:
      {
        portRangeStart = NamedPort.BOOTPC.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_BOOTPS:
      {
        portRangeStart = NamedPort.BOOTPS_OR_DHCP.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_CHARGEN:
      {
        portRangeStart = NamedPort.CHARGEN.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_CVSPSERVER:
      {
        portRangeStart = NamedPort.CVSPSERVER.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_DHCP_CLIENT:
      {
        portRangeStart = NamedPort.BOOTPC.number();   // TODO: rename BOOTPC to BOOTPC_OR_DHCP_CLIENT
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_DHCP_RELAY:
      {
        portRangeStart = NamedPort.BOOTPS_OR_DHCP.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_DHCP_SERVER:
      {
        portRangeStart = NamedPort.BOOTPS_OR_DHCP.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_DISCARD:
      {
        portRangeStart = NamedPort.DISCARD.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_DNS_TCP:
      {
        portRangeStart = NamedPort.DOMAIN.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_DNS_UDP:
      {
        portRangeStart = NamedPort.DOMAIN.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_ECHO:
      {
        portRangeStart = NamedPort.ECHO.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_FINGER:
      {
        portRangeStart = NamedPort.FINGER.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_FTP:
      {
        portRangeStart = NamedPort.FTP.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_FTP_DATA:
      {
        portRangeStart = NamedPort.FTP_DATA.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_GNUTELLA:
      {
        portRangeStart = NamedPort.GNUTELLA.number();
        portRangeEnd = portRangeStart + 1;
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_GOPHER:
      {
        portRangeStart = NamedPort.GOPHER.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_GPRS_GTP_C:
      {
        portRangeStart = NamedPort.GPRS_GTP_C.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_GPRS_GTP_U:
      {
        portRangeStart = NamedPort.GPRS_GTP_U.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_GPRS_GTP_V0:
      {
        portRangeStart = NamedPort.GPRS_GTP_V0.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_GPRS_SCTP:
      {
        portRangeStart = NamedPort.GPRS_SCTP.number();
        ip = IpProtocol.SCTP;
        break;
      }

      case JUNOS_GRE:
      {
        ip = IpProtocol.GRE;
        break;
      }

      case JUNOS_GTP:
      {
        portRangeStart = NamedPort.GPRS_GTP_C.number();
        ip = IpProtocol.UDP;
        break;
      }

      case JUNOS_H323:
      {
        portRangeStart = NamedPort.H323.number();
        ip = IpProtocol.TCP;

        String t2Name = "t2";
        Term t2 = new Term(t2Name);
        HeaderSpace.Builder l2 = t2.getHeaderSpace().rebuild();
        setHeaderSpaceInfo(l2, IpProtocol.UDP, NamedPort.H323_T2.number(), null);
        t2.setHeaderSpace(l2.build());

        String t3Name = "t3";
        Term t3 = new Term(t3Name);
        HeaderSpace.Builder l3 = t3.getHeaderSpace().rebuild();
        setHeaderSpaceInfo(l3, IpProtocol.TCP, NamedPort.H323_T3.number(), null);
        t3.setHeaderSpace(l3.build());

        String t4Name = "t4";
        Term t4 = new Term(t4Name);
        HeaderSpace.Builder l4 = t4.getHeaderSpace().rebuild();
        setHeaderSpaceInfo(l4, IpProtocol.TCP, NamedPort.LDAP.number(), null);  //TODO: rename LDAP to LDAP_OR_H323_T4
        t4.setHeaderSpace(l4.build());

        String t5Name = "t5";
        Term t5 = new Term(t5Name);
        HeaderSpace.Builder l5 = t5.getHeaderSpace().rebuild();
        setHeaderSpaceInfo(l5, IpProtocol.TCP, NamedPort.H323_T5.number(), null);
        t5.setHeaderSpace(l5.build());

        String t6Name = "t6";
        Term t6 = new Term(t6Name);
        HeaderSpace.Builder l6 = t6.getHeaderSpace().rebuild();
        setHeaderSpaceInfo(l6, IpProtocol.TCP, NamedPort.H323_T6.number(), null);
        t6.setHeaderSpace(l6.build());

        terms.put(t2Name, t2);
        terms.put(t3Name, t3);
        terms.put(t4Name, t4);
        terms.put(t5Name, t5);
        terms.put(t6Name, t6);
        break;
      }

      case JUNOS_HTTP:
      {
        portRangeStart = NamedPort.HTTP.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_HTTP_EXT:
      {
        portRangeStart = NamedPort.HTTP_EXT.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_HTTPS:
      {
        portRangeStart = NamedPort.HTTPS.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_ICMP_ALL:
      {
        ip = IpProtocol.ICMP;
        break;
      }

      case JUNOS_ICMP_PING:
      {
        ip = IpProtocol.ICMP;
        icmpType = IcmpType.ECHO_REQUEST;
        break;
      }

      case JUNOS_ICMP6_ALL:
      {
        baseApplication.setIpv6(true);
        ip = IpProtocol.IPV6_ICMP;
        break;
      }

//      case JUNOS_ICMP6_DST_UNREACH_ADDR:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_DST_UNREACH_ADMIN:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_DST_UNREACH_BEYOND:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_DST_UNREACH_PORT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_DST_UNREACH_ROUTE:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_ECHO_REPLY:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_ECHO_REQUEST:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_PACKET_TOO_BIG:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_PARAM_PROB_HEADER:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_PARAM_PROB_NEXTHDR:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_PARAM_PROB_OPTION:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_TIME_EXCEED_REASSEMBLY:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_ICMP6_TIME_EXCEED_TRANSIT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_IDENT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_IKE:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_IKE_NAT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_IMAP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_IMAPS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_INTERNET_LOCATOR_SERVICE:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_IRC:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_L2TP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_LDAP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_LDP_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_LDP_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_LPR:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MAIL:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MGCP_CA:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MGCP_UA:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_EPM:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_IIS_COM_1:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_IIS_COM_ADMINBASE:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE:
//      {
//        // definition here
//        break;
//      }

      case JUNOS_MS_RPC_TCP:
      {
        portRangeStart = NamedPort.MSRPC.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_MS_RPC_UDP:
      {
        portRangeStart = NamedPort.MSRPC.number();
        ip = IpProtocol.UDP;
        break;
      }

//      case JUNOS_MS_RPC_UUID_ANY_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_UUID_ANY_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_ADMIN:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_ADMIN2:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_MGMT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_CALLRESULT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_CLASSOBJECT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_LOGIN_CLIENTID:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_LOGIN_HELPER:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_OBJECTSINK:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_REFRESHING_SERVICES:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_REMOTE_REFRESHER:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_SERVICES:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_RPC_WMIC_WEBM_SHUTDOWN:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MS_SQL:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_MSN:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_NBDS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_NBNAME:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_NETBIOS_SESSION:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_NFS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_NFSD_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_NFSD_UDP:
//      {
//        // definition here
//        break;
//      }

      case JUNOS_NNTP:
      {
        portRangeStart = NamedPort.NNTP.number();
        ip = IpProtocol.TCP;
        break;
      }

//      case JUNOS_NS_GLOBAL:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_NS_GLOBAL_PRO:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_NSM:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_NTALK:
//      {
//        // definition here
//        break;
//      }

      case JUNOS_NTP:
      {
        portRangeStart = NamedPort.NTP.number();
        ip = IpProtocol.UDP;
        break;
      }

//      case JUNOS_OSPF:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_PC_ANYWHERE:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_PERSISTENT_NAT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_PING:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_PINGV6:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_POP3:
//      {
//        // definition here
//        break;
//      }

      case JUNOS_PPTP:
      {
        portRangeStart = NamedPort.PPTP.number();
        ip = IpProtocol.TCP;
        break;
      }

      case JUNOS_PRINTER:
      {
        portRangeStart = NamedPort.LPD.number();  // TODO: rename LPD to LPD_OR_PRINTER
        ip = IpProtocol.TCP;
        break;
      }

//      case JUNOS_R2CP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_RADACCT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_RADIUS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_REALAUDIO:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_RIP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_RSH:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_RTSP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SCCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SCTP_ANY:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SIP:
//      {
//        // definition here
//        break;
//      }

      case JUNOS_SMB:
      {
        portRangeStart = NamedPort.NETBIOS_SSN.number();  // TODO: rename NETBIOS_SSN to NETBIOS_SSN_OR_SMB
        ip = IpProtocol.TCP;

        String t2Name = "t2";
        Term t2 = new Term(t2Name);
        HeaderSpace.Builder l2 = t2.getHeaderSpace().rebuild();
        setHeaderSpaceInfo(l2, IpProtocol.TCP, NamedPort.MICROSOFT_DS.number(), null);  // TODO: rename MICROSOFT_DS to MICROSOFT_DS_OR_SMB_T2
        t2.setHeaderSpace(l2.build());

        terms.put(t2Name, t2);
        break;
      }

//      case JUNOS_SMB_SESSION:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SMTP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SMTPS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SNMP_AGENTX:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SNPP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SQL_MONITOR:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SQLNET_V1:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SQLNET_V2:
//      {
//        // definition here
//        break;
//      }

      case JUNOS_SSH:
      {
        portRangeStart = NamedPort.SSH.number();
        ip = IpProtocol.TCP;
        break;
      }

//      case JUNOS_STUN:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_ANY_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_ANY_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_MOUNTD_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_MOUNTD_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_NFS_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_NFS_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_NLOCKMGR_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_NLOCKMGR_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_PORTMAP_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_PORTMAP_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_RQUOTAD_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_RQUOTAD_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_RUSERD_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_RUSERD_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_SADMIND_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_SADMIND_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_SPRAYD_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_SPRAYD_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_STATUS_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_STATUS_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_WALLD_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_WALLD_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_YPBIND_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_YPBIND_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_YPSERV_TCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SUN_RPC_YPSERV_UDP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_SYSLOG:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_TACACS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_TACACS_DS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_TALK:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_TCP_ANY:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_TELNET:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_TFTP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_UDP_ANY:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_UUCP:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_VDO_LIVE:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_VNC:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_WAIS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_WHO:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_WHOIS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_WINFRAME:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_WXCONTROL:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_X_WINDOWS:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_XNM_CLEAR_TEXT:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_XNM_SSL:
//      {
//        // definition here
//        break;
//      }
//
//      case JUNOS_YMSG:
//      {
//        // definition here
//        break;
//      }

        // $CASES-OMITTED$
      default:
        return null;
    }

    String t1Name = "t1";
    Term t1 = new Term(t1Name);
    HeaderSpace.Builder l1 = t1.getHeaderSpace().rebuild();

    setHeaderSpaceInfo(l1, ip, portRangeStart, portRangeEnd);
    if (icmpType != null) l1.setIcmpTypes(ImmutableSet.of(new SubRange(icmpType)));
    t1.setHeaderSpace(l1.build());
    terms.put(t1Name, t1);

    return baseApplication;
  }

  public boolean hasDefinition() {
    return _baseApplication.get() != null;
  }
}

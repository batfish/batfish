package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum NamedPort {
  /** Application Configuration Access Protocol. */
  ACAP(674),
  /** ACR-NEMA Digital Imaging and Communications in Medicine. */
  ACR_NEMA(104),
  /** Apple Filing Protocol Over TCP. */
  AFPOVERTCP(548),
  AFS(1483),
  AOL(5190),
  /** A Remote Network Server System. */
  ARNS(384),
  /** AppleShare IP Web Administration. */
  ASIP_WEBADMIN(311),
  /** AppleTalk Routing Maintenance. */
  AT_RTMP(201),
  /** AppleTalk Update-Based Routing Protocol. */
  AURP(387),
  /** Background File Transfer Program. */
  BFTP(152),
  BFD_CONTROL(3784),
  BFD_ECHO(3785),
  /** Border Gateway Multicast Protocol. */
  BGMP(264),
  /** Border Gateway Protocol. */
  BGP(179),
  BIFFudp_OR_EXECtcp(512),
  BOOTPC(68),
  BOOTPS_OR_DHCP(67),
  /** Character Generator. */
  CHARGEN(19),
  CIFS(3020),
  /** Cisco Tag Distribution Protocol. */
  CISCO_TDP(711),
  /** Citadel. */
  CITADEL(504),
  CITRIX_ICA(1494),
  /** Clearcase albd. */
  CLEARCASE(371),
  CMDtcp_OR_SYSLOGudp(514),
  /** Commerce Applications. */
  COMMERCE(542),
  /** CCSO Name Server Protocol. */
  CSNET_NS(105),
  CTIQBE(2748),
  CVSPSERVER(2401),
  /** CVX. */
  CVX(50003),
  /** CVX Cluster. */
  CVX_CLUSTER(50004),
  /** CVX Licensing Port. */
  CVX_LICENSE(3333),
  /** Daytime. */
  DAYTIME(13),
  /** DHCP Failover Protocol. */
  DHCP_FAILOVER2(847),
  /** DHCPv6 Client. */
  DHCPV6_CLIENT(546),
  /** DHCPv6 Server. */
  DHCPV6_SERVER(547),
  DISCARD(9),
  DNSIX(195),
  /** Domain Name Service. */
  DOMAIN(53),
  /** Display Support Protocol. */
  DSP(33),
  /** Echo. */
  ECHO(7),
  /** Extended File Name Server (TCP) or Routing Information Protocol (UDP). */
  EFStcp_OR_RIPudp(520),
  EKLOGIN(2105),
  EKSHELL(2106),
  EPHEMERAL_HIGHEST(65535),
  EPHEMERAL_LOWEST(49152),
  /** Extensible Provision Protocol. */
  EPP(700),
  /** Efficient Short Remote Operations. */
  ESRO_GEN(259),
  FINGER(79),
  FTP(21),
  FTP_DATA(20),
  /** FTPS Protocol (control). */
  FTPS(990),
  /** FTPS Protocol (data). */
  FTPS_DATA(989),
  GNUTELLA(6346),
  /** Group Domain of Interpretation Protocol. */
  GODI(848),
  GOPHER(70),
  GPRS_GTP_C(2123),
  GPRS_GTP_U(2152),
  GPRS_GTP_V0(3386),
  /** Generic Routing Encapsulation. */
  GRE(47),
  H323(1720),
  H323_T2(1719),
  H323_T3(1503),
  H323_T5(522),
  H323_T6(1731),
  /** Linux-HA Heartbeat. */
  HA_CLUSTER(694),
  HOSTNAME(101),
  /** HP Performance Data Alarm Manager. */
  HP_ALARM_MGR(383),
  HTTP(80),
  /** Filemaker, Inc. HTTP Alternate. */
  HTTP_ALT(591),
  /** http-mgmt. */
  HTTP_MGMT(280),
  /** HTTP RPC Ep Map. */
  HTTP_RPC_EPMAP(593),
  HTTP_EXT(7001),
  HTTPS(443),
  IDENT(113),
  /** Internet Message Access Protocol (IMAP). */
  IMAP(143),
  /** IMAP v3. */
  IMAP3(220),
  /** IMAP over SSL. */
  IMAPS(993),
  /** Internet Printing Protocol. */
  IPP(631),
  /** IPSec. */
  IPSEC(1293),
  /** Internetwork Packet Exchange. */
  IPX(213),
  IRC(194),
  /** Internet Registry Information Service over BEEP. */
  IRIS_BEEP(702),
  ISAKMP(500),
  /** Internet Small Computers Systems Interface. */
  ISCSI(860),
  /** ISI Graphics Language. */
  ISI_GL(55),
  /** ISO-TSAP Class 0. */
  ISO_TSAP(102),
  KERBEROS(750),
  /** Kerberos Administration. */
  KERBEROS_ADM(749),
  /** Kerberos Authentication System. */
  KERBEROS_SEC(88),
  KLOGIN(543),
  KPASSWDV5(464),
  KPASSWDV4(761),
  KRB_PROP(754),
  KRBUPDATE(760),
  KSHELL(544),
  /** IMP Logical Address Maintenance. */
  LA_MAINT(51),
  /** Lanz Streaming. */
  LANZ(50001),
  LDAP(389),
  LDAPS(636),
  LDP(646),
  /** Link Management Protocol. */
  LMP(701),
  LOGINtcp_OR_WHOudp(513),
  LOTUSNOTES(1352),
  LPD(515),
  /** MacOS Server Admin. */
  MAC_SRVR_ADMIN(660),
  /** MATIP Type A. */
  MATIP_TYPE_A(350),
  /** MATIP Type B. */
  MATIP_TYPE_B(351),
  MGCP_CA(2727),
  MGCP_UA(2427),
  MICROSOFT_DS(445),
  /** not authoritative */
  MLAG(6784),
  /** Arista MLAG port. */
  MLAG_ARISTA(4432),
  MOBILE_IP_AGENT(434),
  MOBILE_IP_MN(435),
  /** Netix Message Posting Protocol. */
  MPP(218),
  /** Microsoft SQL Server. */
  MS_SQL(1433),
  /** Microsoft SQL Monitor. */
  MS_SQL_M(1434),
  /** Microsoft SQL Server. */
  /** Multicast Source Discovery Protocol. */
  MSDP(639),
  /** Microsoft Exchange Routing. */
  MSEXCH_ROUTING(691),
  /** MSG ICP. */
  MSG_ICP(29),
  MSN(1863),
  /** Message Send Protocol. */
  MSP(18),
  MSRPC(135),
  /** Netnews Administration System. */
  NAS(991),
  /** NAT Sync Protocol. */
  NAT(4532),
  NAMESERVER(42),
  /** NetWare Core Protocol. */
  NCP(524),
  NETBIOS_DGM(138),
  NETBIOS_NS(137),
  NETBIOS_SSN(139),
  NETCONF_SSH(830),
  /** Remote Job Service. */
  NETRJS_1(71),
  /** Remote Job Service. */
  NETRJS_2(72),
  /** Remote Job Service. */
  NETRJS_3(73),
  /** Remote Job Service. */
  NETRJS_4(74),
  /** Readnews. */
  NETWNEWS(532),
  /** new-who. */
  NEW_RWHO(550),
  NFSD(2049),
  NNTP(119),
  /** Network News Transfer Protocol Over TSL/SSH. */
  NNTPS(563),
  NON500_ISAKMP(4500),
  /** NSW User System FE. */
  NSW_FE(27),
  NTALK(518),
  NTP(123),
  /** On Demand Mail Retry. */
  ODMR(366),
  /** OpenVPN. */
  OPENVPN(1194),
  PCANYWHERE_DATA(5631),
  PCANYWHERE_STATUS(5632),
  /** PKIX Timestamp. */
  PKIX_TIMESTAMP(318),
  PIM_AUTO_RP(496),
  /* Post Office Protocol (POP) v2. */
  POP2(109),
  /* Post Office Protocol (POP) v3. */
  POP3(110),
  /* Post Office Protocol (POP) v3 over TLS/SSL. */
  POP3S(995),
  PPTP(1723),
  /** Network PostScript. */
  PRINT_SRV(170),
  /** Precision Time Protocol (PTP) Event. */
  PTP_EVENT(319),
  /** Precision Time Protocol (PTP) General. */
  PTP_GENERAL(320),
  /** Quick Mail Transfer Protocol. */
  QMTP(209),
  /** Quote of the Day. */
  QOTD(17),
  R2CP(28762),
  RADIUS_ACCT_CISCO(1646),
  RADIUS_ACCT_JUNIPER(1813),
  RADIUS_CISCO(1645),
  RADIUS_JUNIPER(1812),
  REVERSE_SSH(2901),
  REVERSE_TELNET(2900),
  RKINIT(2108),
  RTSP(554),
  SAP(9875),
  SECUREID_UDP(5510),
  SIP_5060(5060),
  SIP_5061(5061),
  SMTP(25),
  SNMP(161),
  SNMPTRAP(162),
  SNPP(444),
  SOCKS(1080),
  SQLNET(1521),
  SSH(22),
  SUNRPC(111),
  TACACS(49),
  TACACS_DS(65),
  TALK(517),
  TELNET(23),
  TFTP(69),
  TIME(37),
  TIMED(525),
  TRACEROUTE(33434),
  UUCP(540),
  VXLAN(4789),
  WHOIS(43),
  XDMCP(177),
  XNM_CLEAR_TEXT(3221),
  XNM_SSL(3220);

  private static final Map<Integer, NamedPort> NUMBER_TO_PORT_MAP = buildNumberToPortMap();

  private static Map<Integer, NamedPort> buildNumberToPortMap() {
    ImmutableMap.Builder<Integer, NamedPort> map = ImmutableMap.builder();
    for (NamedPort protocol : values()) {
      map.put(protocol._number, protocol);
    }
    return map.build();
  }

  public static NamedPort fromNumber(int number) {
    NamedPort ret = NUMBER_TO_PORT_MAP.get(number);
    if (ret == null) {
      throw new BatfishException("missing enumeration for protocol number: " + number);
    }
    return ret;
  }

  public static String nameFromNumber(int i) {
    NamedPort namedPort = NUMBER_TO_PORT_MAP.get(i);
    if (namedPort == null) {
      return Integer.toString(i);
    } else {
      return namedPort.name() + "(" + i + ")";
    }
  }

  private int _number;

  NamedPort(int number) {
    _number = number;
  }

  public int number() {
    return _number;
  }
}

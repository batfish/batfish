package org.batfish.representation;

import java.util.HashMap;
import java.util.Map;

import org.batfish.main.BatfishException;

public enum NamedPort {
   AFS(1483),
   AOL(5190),
   BGP(179),
   BIFFudp_OR_EXECtcp(512),
   BOOTPC(68),
   BOOTPS_OR_DHCP(67),
   CHARGEN(19),
   CITRIX_ICA(1494),
   CMDtcp_OR_SYSLOGudp(514),
   CTIQBE(2748),
   CVSPSERVER(2401),
   DAYTIME(13),
   DISCARD(9),
   DNSIX(195),
   DOMAIN(53),
   ECHO(7),
   EKLOGIN(2105),
   EKSHELL(2106),
   FINGER(79),
   FTP(21),
   FTP_DATA(20),
   GOPHER(70),
   H323(1720),
   HOSTNAME(101),
   HTTP(80),
   HTTPS(443),
   IDENT(113),
   IMAP(143),
   IRC(194),
   ISAKMP(500),
   KERBEROS(750),
   KERBEROS_SEC(88),
   KLOGIN(543),
   KPASSWD(761),
   KRB_PROP(754),
   KRBUPDATE(760),
   KSHELL(544),
   LDAP(389),
   LDAPS(636),
   LDP(646),
   LOGINtcp_OR_WHOudp(513),
   LOTUSNOTES(1352),
   LPD(515),
   /**
    * not authoritative
    */
   MLAG(6784),
   MOBILE_IP_AGENT(434),
   MOBILE_IP_MN(435),
   MSDP(639),
   NAMESERVER(42),
   NETBIOS_DGM(138),
   NETBIOS_NS(137),
   NETBIOS_SSN(139),
   NFSD(2049),
   NNTP(119),
   NON500_ISAKMP(4500),
   NTALK(518),
   NTP(123),
   PCANYWHERE_DATA(5631),
   PCANYWHERE_STATUS(5632),
   PIM_AUTO_RP(496),
   POP2(109),
   POP3(110),
   PPTP(1723),
   RADIUS_ACCT_CISCO(1646),
   RADIUS_ACCT_JUNIPER(1813),
   RADIUS_CISCO(1645),
   RADIUS_JUNIPER(1812),
   RIP(520),
   RKINIT(2108),
   SECUREID_UDP(5510),
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
   UUCP(540),
   WHOIS(43),
   XDMCP(177);

   private static final Map<Integer, NamedPort> NUMBER_TO_PORT_MAP = buildNumberToPortMap();

   private static Map<Integer, NamedPort> buildNumberToPortMap() {
      Map<Integer, NamedPort> map = new HashMap<Integer, NamedPort>();
      for (NamedPort protocol : values()) {
         map.put(protocol._number, protocol);
      }
      return map;
   }

   public static NamedPort fromNumber(int number) {
      NamedPort ret = NUMBER_TO_PORT_MAP.get(number);
      if (ret == null) {
         throw new BatfishException("missing enumeration for protocol number: "
               + number);
      }
      return ret;
   }

   public static String nameFromNumber(int i) {
      NamedPort namedPort = NUMBER_TO_PORT_MAP.get(i);
      if (namedPort == null) {
         return Integer.toString(i);
      }
      else {
         return namedPort.name() + "(" + i + ")";
      }
   }

   private int _number;

   private NamedPort(int number) {
      _number = number;
   }

   public int number() {
      return _number;
   }

}

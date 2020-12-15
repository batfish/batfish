package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;

/**
 * BuiltIn applications available on PAN devices. See here for details about specific applications:
 * https://applipedia.paloaltonetworks.com/
 */
@ParametersAreNonnullByDefault
public final class ApplicationBuiltIn {
  public static final Application AMAZON_CLOUD_DRIVE_BASE =
      Application.builder("amazon-cloud-drive-base")
          .setDescription("built-in application amazon-cloud-drive-base")
          .addService(
              Service.builder("amazon-cloud-drive-base")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(80, 443)
                  .build())
          .build();
  public static final Application AMAZON_CLOUD_DRIVE_UPLOADING =
      Application.builder("amazon-cloud-drive-uploading")
          .setDescription("built-in application amazon-cloud-drive-uploading")
          .addService(
              Service.builder("amazon-cloud-drive-uploading")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(80, 443)
                  .build())
          .build();
  public static final Application AOL_MESSAGEBOARD_POSTING =
      Application.builder("aol-messageboard-posting")
          .setDescription("built-in application aol-messageboard-posting")
          .addService(
              Service.builder("aol-messageboard-posting")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPort(80)
                  .build())
          .build();
  public static final Application AOL_PROXY =
      Application.builder("aol-proxy")
          .setDescription("built-in application aol-proxy")
          .addService(
              Service.builder("aol-proxy")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(80, 5192, 5190)
                  .build())
          .build();
  public static final Application BFD =
      Application.builder("bfd")
          .setDescription("built-in application bfd")
          .addService(
              Service.builder("bfd")
                  .setIpProtocol(IpProtocol.UDP)
                  .addPort(3784)
                  .addPort(3785)
                  .addPort(4784)
                  .build())
          .build();
  public static final Application BGP =
      Application.builder("bgp")
          .setDescription("built-in application bgp")
          .addService(Service.builder("bgp-tcp").setIpProtocol(IpProtocol.TCP).addPort(179).build())
          .addService(Service.builder("bgp-udp").setIpProtocol(IpProtocol.UDP).addPort(179).build())
          .build();
  public static final Application BOXNET =
      Application.builder("boxnet")
          .setDescription("built-in application boxnet")
          .addService(
              Service.builder("boxnet").setIpProtocol(IpProtocol.TCP).addPorts(80, 443).build())
          .build();
  public static final Application CISCO_SPARK_BASE =
      Application.builder("cisco-spark-base")
          .setDescription("built-in application cisco-spark-base")
          .addService(
              Service.builder("cisco-spark-base")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(80, 443)
                  .build())
          .build();
  public static final Application DHCP =
      Application.builder("dhcp")
          .setDescription("built-in application dhcp")
          .addService(
              Service.builder("dhcp-tcp").setIpProtocol(IpProtocol.TCP).addPorts(67, 68).build())
          .addService(
              Service.builder("dhcp-udp").setIpProtocol(IpProtocol.UDP).addPorts(67, 68).build())
          .build();
  public static final Application DNS =
      Application.builder("dns")
          .setDescription("built-in application dns")
          .addService(Service.builder("dns-tcp").setIpProtocol(IpProtocol.TCP).addPorts(53).build())
          .addService(
              Service.builder("dns-udp").setIpProtocol(IpProtocol.UDP).addPorts(53, 5353).build())
          .build();
  public static final Application FINGER =
      Application.builder("finger")
          .setDescription("built-in application finger")
          .addService(
              Service.builder("finger-tcp").setIpProtocol(IpProtocol.TCP).addPort(79).build())
          .addService(
              Service.builder("finger-udp").setIpProtocol(IpProtocol.UDP).addPorts(79).build())
          .build();

  public static final Application FTP =
      Application.builder("ftp")
          .setDescription("built-in application ftp")
          .addService(Service.builder("ftp").setIpProtocol(IpProtocol.TCP).addPort(21).build())
          .build();

  public static final Application GNUTELLA =
      // No well-known ports, DPI only
      Application.builder("gnutella").setDescription("built-in application gnutella").build();

  public static final Application GRE =
      Application.builder("gre")
          .setDescription("built-in application gre")
          .addService(Service.builder("gre").setIpProtocol(IpProtocol.GRE).build())
          .build();

  public static final Application GOOGLE_BASE =
      Application.builder("google-base")
          .setDescription("built-in application google-base")
          .addService(
              Service.builder("google-base")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(80, 443, 5228, 5229)
                  .addPorts(new SubRange(5222, 5224))
                  .build())
          .build();

  public static final Application GOPHER =
      Application.builder("gopher")
          .setDescription("built-in application gopher")
          .addService(Service.builder("gopher").setIpProtocol(IpProtocol.TCP).addPort(70).build())
          .build();

  public static final Application ICMP =
      Application.builder("icmp")
          .setDescription("built-in application icmp")
          .addService(Service.builder("icmp").setIpProtocol(IpProtocol.ICMP).build())
          .build();
  public static final Application LDAP =
      Application.builder("ldap")
          .setDescription("built-in application ldap")
          .addService(
              Service.builder("ldap tcp")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(389, 636, 3268, 3269)
                  .build())
          .addService(
              Service.builder("ldap udp").setIpProtocol(IpProtocol.UDP).addPorts(389, 3268).build())
          .build();

  public static final Application MCSQL_MON =
      Application.builder("mssql-mon")
          .setDescription("built-in application mssql-mon")
          .addService(
              Service.builder("mssql-mon").setIpProtocol(IpProtocol.UDP).addPort(1434).build())
          .build();

  public static final Application MS_DS_SMB =
      Application.builder("ms-ds-smb")
          .setDescription("built-in application ms-ds-smb")
          .addService(
              Service.builder("ms-ds-smb tcp")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(139, 445)
                  .build())
          .addService(
              Service.builder("ms-ds-smb udp").setIpProtocol(IpProtocol.UDP).addPorts(445).build())
          .build();
  public static final Application MSN =
      Application.builder("msn")
          .setDescription("built-in application msn")
          .addService(
              Service.builder("msn tcp")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(1863, 80, 7001, 443)
                  .addPorts(new SubRange(1025, 65535))
                  .build())
          .addService(
              Service.builder("msn udp")
                  .setIpProtocol(IpProtocol.UDP)
                  .addPorts(new SubRange(1025, 65535))
                  .addPorts(7001)
                  .build())
          .build();
  public static final Application MS_NETLOGON =
      Application.builder("ms-netlogon")
          .setDescription("built-in application ms-netlogon")
          .addService(
              Service.builder("ms-netlogon tcp")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(135, 139, 445)
                  .addPorts(new SubRange(1025, 5000))
                  .addPorts(new SubRange(49152, 65535))
                  .build())
          .addService(
              Service.builder("ms-netlogon udp")
                  .setIpProtocol(IpProtocol.UDP)
                  .addPorts(137, 138, 445)
                  .build())
          .build();
  public static final Application MS_KMS =
      Application.builder("ms-kms")
          .setDescription("built-in application ms-kms")
          .addService(
              Service.builder("ms-kms tcp").setIpProtocol(IpProtocol.TCP).addPorts(1688).build())
          .build();
  public static final Application MSRPC =
      // No well-known ports, DPI only
      Application.builder("msrpc").setDescription("built-in application msrpc").build();
  public static final Application MSSQL_DB =
      Application.builder("mssql-db")
          .addService(
              Service.builder("mssql-db tcp").setIpProtocol(IpProtocol.TCP).addPorts(1433).build())
          .addService(
              Service.builder("mssql-db udp").setIpProtocol(IpProtocol.UDP).addPorts(1433).build())
          .setDescription("built-in application mssql-db")
          .build();
  public static final Application NETBIOS_SS =
      Application.builder("netbios-ss")
          .setDescription("built-in application netbios-ss")
          .addService(
              Service.builder("netbios-ss").setIpProtocol(IpProtocol.TCP).addPorts(139).build())
          .build();
  public static final Application NTP =
      Application.builder("ntp")
          .setDescription("built-in application ntp")
          .addService(
              Service.builder("ntp tcp").setIpProtocol(IpProtocol.TCP).addPorts(123).build())
          .addService(
              Service.builder("ntp udp").setIpProtocol(IpProtocol.UDP).addPorts(123).build())
          .build();
  public static final Application OFFICE365_ENTERPRISE_ACCESS =
      Application.builder("office365-enterprise-access")
          .setDescription("built-in application office365-enterprise-access")
          .addService(
              Service.builder("office365-enterprise-access")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(80, 443)
                  .build())
          .build();
  public static final Application OSPF =
      Application.builder("ospf")
          .setDescription("built-in application ospf")
          .addService(Service.builder("ospf").setIpProtocol(IpProtocol.OSPF).build())
          .build();
  public static final Application PAN_DB_CLOUD =
      Application.builder("pan-db-cloud")
          .setDescription("built-in application pan-db-cloud")
          .addService(
              Service.builder("pan-db-cloud tcp")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(443)
                  .build())
          .build();
  public static final Application PIM =
      Application.builder("pim")
          .setDescription("built-in application pim")
          .addService(Service.builder("pim").setIpProtocol(IpProtocol.PIM).build())
          .build();
  public static final Application PING =
      Application.builder("ping")
          .setDescription("built-in application ping")
          .addService(Service.builder("ping").setIpProtocol(IpProtocol.ICMP).build())
          .build();
  public static final Application PC_ANYWHERE =
      Application.builder("pcanywhere")
          .setDescription("built-in application pcanywhere")
          .addService(
              Service.builder("pcanywhere-tcp")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(5631, 65301)
                  .build())
          .addService(
              Service.builder("pcanywhere-udp")
                  .setIpProtocol(IpProtocol.UDP)
                  .addPorts(22, 5632)
                  .build())
          .build();
  public static final Application POP3 =
      Application.builder("pop3")
          .setDescription("built-in application pop3")
          .addService(Service.builder("pop3").setIpProtocol(IpProtocol.TCP).addPort(110).build())
          .build();
  public static final Application RPC =
      // No well-known ports, DPI only
      Application.builder("rpc").setDescription("built-in application rpc").build();
  public static final Application RTCP =
      Application.builder("rtcp")
          .setDescription("built-in application rtcp")
          .addService(
              Service.builder("rtcp")
                  .setIpProtocol(IpProtocol.UDP)
                  .addPorts(new SubRange(1, 65535)) // udp/dynamic
                  .build())
          .build();
  public static final Application RTP_BASE =
      Application.builder("rtp-base")
          .setDescription("built-in application rtp-base")
          .addService(
              Service.builder("rtp-base")
                  .setIpProtocol(IpProtocol.UDP)
                  .addPorts(new SubRange(1, 65535)) // udp/dynamic
                  .build())
          .build();
  public static final Application SMTP =
      Application.builder("smtp")
          .setDescription("built-in application smtp")
          .addService(
              Service.builder("smtp").setIpProtocol(IpProtocol.TCP).addPorts(25, 587).build())
          .build();
  public static final Application SNMP =
      Application.builder("snmp")
          .setDescription("built-in application snmp")
          .addService(
              Service.builder("snmp tcp").setIpProtocol(IpProtocol.TCP).addPorts(161).build())
          .addService(
              Service.builder("snmp udp").setIpProtocol(IpProtocol.UDP).addPorts(161).build())
          .build();
  public static final Application SNMP_TRAP =
      Application.builder("snmp-trap")
          .setDescription("built-in application snmp-trap")
          .addService(
              Service.builder("snmp-trap tcp").setIpProtocol(IpProtocol.TCP).addPorts(162).build())
          .addService(
              Service.builder("snmp-trap udp").setIpProtocol(IpProtocol.UDP).addPorts(162).build())
          .build();
  public static final Application SOAP =
      Application.builder("soap")
          .setDescription("built-in application soap")
          .addService(
              Service.builder("soap").setIpProtocol(IpProtocol.TCP).addPorts(80, 443).build())
          .build();
  public static final Application SSH =
      Application.builder("ssh")
          .setDescription("built-in application ssh")
          .addService(Service.builder("ssh").setIpProtocol(IpProtocol.TCP).addPort(22).build())
          .build();
  public static final Application SSL =
      Application.builder("ssl")
          .setDescription("built-in application ssl")
          .addService(Service.builder("ssl").setIpProtocol(IpProtocol.TCP).addPort(443).build())
          .build();
  public static final Application STUN =
      Application.builder("stun")
          .setDescription("built-in application stun")
          .addService(
              Service.builder("stun tcp").setIpProtocol(IpProtocol.TCP).addPort(3478).build())
          .addService(
              Service.builder("stun udp").setIpProtocol(IpProtocol.UDP).addPort(3478).build())
          .build();
  public static final Application SYSLOG =
      Application.builder("syslog")
          .setDescription("built-in application syslog")
          .addService(
              Service.builder("syslog tcp")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPort(1468)
                  .addPort(1514)
                  .addPort(6514)
                  .build())
          .addService(
              Service.builder("syslog udp")
                  .setIpProtocol(IpProtocol.UDP)
                  .addPort(514)
                  .addPort(1514)
                  .build())
          .build();
  public static final Application TACACS =
      Application.builder("tacacs")
          .setDescription("built-in application tacacs")
          .addService(Service.builder("tacacs").setIpProtocol(IpProtocol.TCP).addPort(49).build())
          .build();
  public static final Application TACACS_PLUS =
      Application.builder("tacacs-plus")
          .setDescription("built-in application tacacs-plus")
          .addService(
              Service.builder("tacacs-plus").setIpProtocol(IpProtocol.TCP).addPort(49).build())
          .build();
  public static final Application TRACEROUTE =
      Application.builder("traceroute")
          .setDescription("built-in application traceroute")
          .addService(Service.builder("traceroute icmp").setIpProtocol(IpProtocol.ICMP).build())
          .addService(
              Service.builder("traceroute icmpv6").setIpProtocol(IpProtocol.IPV6_ICMP).build())
          .addService(
              Service.builder("traceroute udp")
                  .setIpProtocol(IpProtocol.UDP)
                  .addPorts(new SubRange(33434, 33534))
                  .build())
          .build();
  public static final Application WEB_BROWSING =
      Application.builder("web-browsing")
          .setDescription("built-in application web-browsing")
          .addService(
              Service.builder("web-browsing").setIpProtocol(IpProtocol.TCP).addPort(80).build())
          .build();
  public static final Application WEBEX =
      Application.builder("webex")
          .setDescription("built-in application webex")
          .addService(
              Service.builder("webex tcp")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(80, 443, 1270)
                  .build())
          .addService(
              Service.builder("webex udp")
                  .setIpProtocol(IpProtocol.UDP)
                  .addPorts(8070, 8090, 9000)
                  .build())
          .build();
  public static final Application WEBSOCKET =
      Application.builder("websocket")
          .setDescription("built-in application websocket")
          .addService(
              Service.builder("websocket")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(80, 443, 8080)
                  .build())
          .build();

  private static final List<Application> BUILTIN_LIST =
      ImmutableList.of(
          AMAZON_CLOUD_DRIVE_BASE,
          AMAZON_CLOUD_DRIVE_UPLOADING,
          AOL_MESSAGEBOARD_POSTING,
          AOL_PROXY,
          BFD,
          BGP,
          BOXNET,
          CISCO_SPARK_BASE,
          DHCP,
          DNS,
          FINGER,
          FTP,
          GNUTELLA,
          GRE,
          GOOGLE_BASE,
          GOPHER,
          ICMP,
          LDAP,
          MCSQL_MON,
          MS_DS_SMB,
          MS_KMS,
          MSN,
          MS_NETLOGON,
          MSSQL_DB,
          MSRPC,
          NETBIOS_SS,
          NTP,
          OFFICE365_ENTERPRISE_ACCESS,
          OSPF,
          PAN_DB_CLOUD,
          PC_ANYWHERE,
          PIM,
          PING,
          POP3,
          RPC,
          RTCP,
          RTP_BASE,
          SMTP,
          SNMP,
          SNMP_TRAP,
          SOAP,
          SSH,
          SSL,
          STUN,
          SYSLOG,
          TACACS,
          TACACS_PLUS,
          TRACEROUTE,
          WEB_BROWSING,
          WEBEX,
          WEBSOCKET);

  private static final Map<String, Application> BUILTINS =
      BUILTIN_LIST.stream()
          .collect(ImmutableMap.toImmutableMap(Application::getName, Function.identity()));

  /**
   * Returns the definition of the builtin corresponding to the named {@link Application}, or an
   * absent {@link Optional} if none exists (or has had its definition implemented here).
   */
  public static @Nonnull Optional<Application> getBuiltInApplication(@Nonnull String name) {
    return Optional.ofNullable(BUILTINS.get(name));
  }

  private ApplicationBuiltIn() {} // prevent instantiation of utility class
}

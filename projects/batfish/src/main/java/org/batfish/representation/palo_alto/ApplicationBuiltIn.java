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
  public static final Application FTP =
      Application.builder("ftp")
          .setDescription("built-in application ftp")
          .addService(Service.builder("ftp").setIpProtocol(IpProtocol.TCP).addPort(21).build())
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
  //  // MSRPC("msrpc"),
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
  public static final Application PAN_DB_CLOUD =
      Application.builder("pan-db-cloud")
          .setDescription("built-in application pan-db-cloud")
          .addService(
              Service.builder("pan-db-cloud tcp")
                  .setIpProtocol(IpProtocol.TCP)
                  .addPorts(443)
                  .build())
          .build();
  public static final Application PING =
      Application.builder("ping")
          .setDescription("built-in application ping")
          .addService(Service.builder("ping").setIpProtocol(IpProtocol.ICMP).build())
          .build();
  public static final Application POP3 =
      Application.builder("pop3")
          .setDescription("built-in application pop3")
          .addService(Service.builder("pop3").setIpProtocol(IpProtocol.TCP).addPort(110).build())
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
  public static final Application WEB_BROWSING =
      Application.builder("web-browsing")
          .setDescription("built-in application web-browsing")
          .addService(
              Service.builder("web-browsing").setIpProtocol(IpProtocol.TCP).addPort(80).build())
          .build();

  private static final List<Application> BUILTIN_LIST =
      ImmutableList.of(
          FTP,
          ICMP,
          LDAP,
          MS_DS_SMB,
          MS_KMS,
          MS_NETLOGON,
          NETBIOS_SS,
          NTP,
          PAN_DB_CLOUD,
          PING,
          POP3,
          SMTP,
          SNMP,
          SOAP,
          SSH,
          SSL,
          WEB_BROWSING);

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

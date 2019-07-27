package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableMap;

import org.batfish.datamodel.IpProtocol;

import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * BuiltIn applications available on PAN devices. See here for details about specific applications:
 * https://applipedia.paloaltonetworks.com/
 */
@ParametersAreNonnullByDefault
public enum ApplicationBuiltIn {
  FTP("ftp", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 21 })),
  ICMP("icmp", ImmutableMap.of(IpProtocol.ICMP, new Integer[] {})),
  LDAP("ldap", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 389, 3268, 3269, 636 }, IpProtocol.UDP, new Integer[] { 389, 3268 })),
  MS_DS_SMB("ms-ds-smb", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 445, 139 }, IpProtocol.UDP, new Integer[] { 445 })),
  MS_NETLOGON("ms-netlogon", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 135, 139, 445 }, IpProtocol.UDP, new Integer[] { 137, 138, 445 })),
  MS_KMS("ms-kms", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 1688 })),
  // MSRPC("msrpc"),
  NETBIOS_SS("netbios-ss", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 139 })),
  NTP("ntp", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 123 }, IpProtocol.UDP, new Integer[] { 123 })),
  PAN_DB_CLOUD("pan-db-cloud", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 443 })),
  PING("ping", ImmutableMap.of(IpProtocol.ICMP, new Integer[] {})),
  POP3("pop3", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 110 })),
  SMTP("smtp", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 25, 587 })),
  SNMP("snmp", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 161 }, IpProtocol.UDP, new Integer[] { 161 })),
  SOAP("soap", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 110 })),
  SSH("ssh", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 22 })),
  SSL("ssl", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 443 })),
  WEB_BROWSING("web-browsing", ImmutableMap.of(IpProtocol.TCP, new Integer[] { 80 }));

  public static final Map<String, Application> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(
              ImmutableMap.toImmutableMap(
                  ApplicationBuiltIn::getName, ApplicationBuiltIn::getApplication));

  private final Application _application;

  private final String _name;

  ApplicationBuiltIn(String name, Map<IpProtocol, Integer[]> ports) {
    _name = name;
    _application = init(name, ports);
  }

  private Application init(String name, Map<IpProtocol, Integer[]> ports) {
    return Application.builder().setName(name).setPorts(ports).build();
  }

  public @Nonnull Application getApplication() {
    return _application;
  }

  public @Nonnull String getName() {
    return _name;
  }
}

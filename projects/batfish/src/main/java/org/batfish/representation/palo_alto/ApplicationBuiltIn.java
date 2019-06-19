package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableMap;
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
  FTP("ftp"),
  ICMP("icmp"),
  LDAP("ldap"),
  MS_DS_SMB("ms-ds-smb"),
  MS_NETLOGON("ms-netlogon"),
  MSRPC("msrpc"),
  NETBIOS_SS("netbios-ss"),
  PAN_DB_CLOUD("pan-db-cloud"),
  PING("ping"),
  SSH("ssh"),
  SSL("ssl");

  public static final Map<String, Application> FOR_NAME_MAP =
      Arrays.stream(values())
          .collect(
              ImmutableMap.toImmutableMap(
                  ApplicationBuiltIn::getName, ApplicationBuiltIn::getApplication));

  private final Application _application;

  private final String _name;

  ApplicationBuiltIn(String name) {
    _name = name;
    _application = init();
  }

  private Application init() {
    return Application.builder().setName(getName()).build();
  }

  public @Nonnull Application getApplication() {
    return _application;
  }

  public @Nonnull String getName() {
    return _name;
  }
}

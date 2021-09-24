package org.batfish.representation.cumulus;

import java.util.regex.Pattern;

public enum CumulusInterfaceType {
  BOND,
  BOND_SUBINTERFACE,
  LOOPBACK,
  PHYSICAL,
  PHYSICAL_SUBINTERFACE,
  VLAN,
  VXLAN;

  static final Pattern NULL_INTERFACE_PATTERN =
      Pattern.compile("Null0|blackhole|reject", Pattern.CASE_INSENSITIVE);
}

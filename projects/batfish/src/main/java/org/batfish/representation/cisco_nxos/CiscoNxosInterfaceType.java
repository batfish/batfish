package org.batfish.representation.cisco_nxos;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Set;

public enum CiscoNxosInterfaceType {
  ETHERNET,
  LOOPBACK,
  MGMT,
  PORT_CHANNEL,
  VLAN;

  public static Set<CiscoNxosInterfaceType> ALL_INTERFACE_TYPES =
      ImmutableSet.copyOf(Arrays.asList(CiscoNxosInterfaceType.values()));
}

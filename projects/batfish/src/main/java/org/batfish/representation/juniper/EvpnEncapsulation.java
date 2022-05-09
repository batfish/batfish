package org.batfish.representation.juniper;

/**
 * Juniper EVPN Encapsulation Options. Based on encapsulation options here:
 * https://www.juniper.net/documentation/us/en/software/junos/evpn-vxlan/topics/ref/statement/enapsulation-vxlan-edit-routing-instances.html
 */
public enum EvpnEncapsulation {
  VXLAN,
  MPLS
}

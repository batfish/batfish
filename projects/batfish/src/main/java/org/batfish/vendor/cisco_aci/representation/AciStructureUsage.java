package org.batfish.vendor.cisco_aci.representation;

import org.batfish.vendor.StructureUsage;

/** Named structure-usage types for Cisco ACI devices */
public enum AciStructureUsage implements StructureUsage {
  APPLICATION_PROFILE_EPG("application profile endpoint group"),
  APPLICATION_PROFILE_SELF_REF("application profile"),
  BRIDGE_DOMAIN_CONSUMER_CONTRACT("bridge domain consumer contract"),
  BRIDGE_DOMAIN_CONTRACT("bridge domain contract"),
  BRIDGE_DOMAIN_L3OUT("bridge domain l3out"),
  BRIDGE_DOMAIN_PROVIDER_CONTRACT("bridge domain provider contract"),
  BRIDGE_DOMAIN_SELF_REF("bridge domain"),
  BRIDGE_DOMAIN_SUBNET("bridge domain subnet"),
  CONTRACT_CONSUMER("contract consumer"),
  CONTRACT_PROVIDER("contract provider"),
  CONTRACT_SUBJECT("contract subject"),
  CONTRACT_SUBJECT_FILTER("contract subject filter"),
  EPG_CONTRACT_CONSUMER("endpoint group consumer contract"),
  EPG_CONTRACT_PROVIDER("endpoint group provider contract"),
  EPG_DOMAIN_BINDING("endpoint group domain binding"),
  EPG_INGRESS_CONTRACT("endpoint group ingress contract"),
  EPG_EGRESS_CONTRACT("endpoint group egress contract"),
  EPG_PHYSICAL_DOMAIN("endpoint group physical domain"),
  EPG_SELF_REF("endpoint group"),
  EPG_STATIC_PATH("endpoint group static path"),
  EPG_VLAN_BINDING("endpoint group vlan binding"),
  FILTER_ENTRY("filter entry"),
  FILTER_SELF_REF("filter"),
  L3OUT_EXTERNAL_EPG("l3out external endpoint group"),
  L3OUT_NODE_PROFILE("l3out node profile"),
  L3OUT_SELF_REF("l3out"),
  L3OUT_VRF("l3out vrf"),
  SUBJECT_FILTER("subject filter"),
  SUBJECT_SELF_REF("subject"),
  TENANT_APPLICATION_PROFILE("tenant application profile"),
  TENNT_BRIDGE_DOMAIN("tenant bridge domain"),
  TENANT_CONTRACT("tenant contract"),
  TENANT_FILTER("tenant filter"),
  TENANT_L3OUT("tenant l3out"),
  TENANT_SELF_REF("tenant"),
  TENANT_VRF("tenant vrf"),
  VRF_BRIDGE_DOMAIN("vrf bridge domain"),
  VRF_CONTRACT("vrf contract"),
  VRF_L3OUT("vrf l3out"),
  VRF_SELF_REF("vrf");

  private final String _description;

  AciStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}

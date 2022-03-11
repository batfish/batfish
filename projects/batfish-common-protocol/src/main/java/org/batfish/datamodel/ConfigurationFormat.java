package org.batfish.datamodel;

/** Vendor configuration file format. */
public enum ConfigurationFormat {
  A10_ACOS("a10_acos"),
  ALCATEL_AOS("alcatel_aos"),
  ALTEON("alteon"),
  ARBOR("arbor"),
  ARRCUS("arrcus"),
  ARISTA("arista"),
  ARUBAOS("aruba"),
  AVOCENT("avocent"),
  AXIS("axis"),
  AWS("aws"),
  BAYNET("baynet"),
  BLADENETWORK("bladenetwork"),
  BROCADE("brocade"),
  CADANT("cadant"),
  CHECK_POINT_GATEWAY("check_point_gateway"),
  CIENA_WS("ciena"),
  CISCO_AGM("cisco"), // cisco anomaly guard module
  CISCO_ASA("cisco"),
  CISCO_CATOS("cisco"),
  CISCO_FX("cisco"), // cisco firepower
  CISCO_IOS("cisco"),
  CISCO_IOS_XR("cisco"),
  CISCO_NX("cisco"),
  CISCO_SB("cisco"), // cisco small business
  CISCO_WLC("cisco"), // cisco wireless lan controller
  CUMULUS_CONCATENATED("cumulus_concatenated"),
  CUMULUS_NCLU("cumulus_nclu"),
  DELL("dell"),
  DELL_SMC("dell"),
  EMPTY("empty"),
  ENTERASYS("enterasys"),
  EXTREME("extreme"),
  EZT3("ezt3"), // not sure what this is
  F5("f5"),
  F5_BIGIP_STRUCTURED("f5_bigip_structured"),
  FLAT_JUNIPER("juniper"),
  FLAT_VYOS("vyos"),
  FORCE10("force10"),
  FORTIOS("fortios"),
  FOUNDRY("foundry"),
  FRR("frr"), // raw FRR, not bundled with Cumulus or SONiC
  FUJITSU_FSS("fss"),
  HITACHI("hitachi"),
  HOST("host"),
  HP("hp"),
  HUAWEI_CSS("huawei"), // cluster switch system
  HUAWEI_VRP("huawei"),
  IBM_BNT("ibmbnt"),
  IGNORED("ignored"),
  IPTABLES("iptables"),
  JUNIPER("juniper"),
  JUNIPER_EVO("juniper"),
  JUNIPER_SWITCH("juniper"),
  METAMAKO("metamako"),
  MICROTIK("mirotik"),
  MRTD("mtr"), // multi-threaded routing
  MRV("mrv"),
  MRV_COMMANDS("mrv_commands"),
  MSS("mss"),
  NETOPIA("mtr"), // multi-threaded routing
  NETSCREEN("netscreen"),
  NOKIA_SROS("nokia"),
  REDBACK("redback"),
  RIVERBED("riverbed"),
  RIVERSTONE("riverstone"),
  SONIC("sonic"),
  PALO_ALTO("paloalto"),
  PALO_ALTO_NESTED("paloalto"),
  RUCKUS_ICX("ruckus_icx"),
  UBIQUITI_EDGEMAX("ubiquiti"),
  UBIQUITI_EDGEROUTER("ubiquiti"),
  UBIQUITI_EDGEROUTER_X("ubiquiti"),
  UNKNOWN("unknown"),
  VXWORKS("vxworks"),
  VYOS("vyos"),
  XIRRUS("xirrus");

  private String _vendorString;

  ConfigurationFormat(String vendorString) {
    _vendorString = vendorString;
  }

  public String getVendorString() {
    return _vendorString;
  }
}

package org.batfish.datamodel;

/** Vendor configuration file format. */
public enum ConfigurationFormat {
  A10_ACOS("a10_acos"),
  ALCATEL_AOS("alcatel_aos"),
  ARISTA("arista"),
  ARUBAOS("aruba"),
  AWS("aws"),
  AZURE("azure"),
  BLADENETWORK("bladenetwork"),
  CADANT("cadant"),
  CHECK_POINT_GATEWAY("check_point_gateway"),
  CISCO_ASA("cisco"),
  CISCO_IOS("cisco"),
  CISCO_IOS_XR("cisco"),
  CISCO_NX("cisco"),
  CUMULUS_CONCATENATED("cumulus_concatenated"),
  CUMULUS_NCLU("cumulus_nclu"),
  EMPTY("empty"),
  F5("f5"),
  F5_BIGIP_STRUCTURED("f5_bigip_structured"),
  FLAT_JUNIPER("juniper"),
  FLAT_VYOS("vyos"),
  FORCE10("force10"),
  FORTIOS("fortios"),
  FOUNDRY("foundry"),
  HOST("host"),
  HUAWEI("huawei"),
  IBM_BNT("ibmbnt"),
  IGNORED("ignored"),
  IPTABLES("iptables"),
  JUNIPER("juniper"),
  JUNIPER_SWITCH("juniper"),
  METAMAKO("metamako"),
  MRV("mrv"),
  MRV_COMMANDS("mrv_commands"),
  MSS("mss"),
  SONIC("sonic"),
  PALO_ALTO("paloalto"),
  PALO_ALTO_NESTED("paloalto"),
  RUCKUS_ICX("ruckus_icx"),
  UNKNOWN("unknown"),
  UNSUPPORTED("unsupported"),
  VXWORKS("vxworks"),
  VYOS("vyos");

  private String _vendorString;

  ConfigurationFormat(String vendorString) {
    _vendorString = vendorString;
  }

  public String getVendorString() {
    return _vendorString;
  }
}

package org.batfish.representation.palo_alto;

import org.batfish.vendor.StructureType;

public enum PaloAltoStructureType implements StructureType {
  GLOBAL_PROTECT_APP_CRYPTO_PROFILE("global-protect-app-crypto-profile"),
  IKE_CRYPTO_PROFILE("ike-crypto-profile"),
  IPSEC_CRYPTO_PROFILE("ipsec-crypto-profile"),
  INTERFACE("interface"),
  RULE("rule"),
  SERVICE("service"),
  SERVICE_GROUP("service-group"),
  SERVICE_OR_SERVICE_GROUP("service or service-group"),
  ZONE("zone");

  private final String _description;

  PaloAltoStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}

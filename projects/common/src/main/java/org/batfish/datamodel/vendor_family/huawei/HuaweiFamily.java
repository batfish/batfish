package org.batfish.datamodel.vendor_family.huawei;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;

/**
 * Vendor-specific configuration data for Huawei devices.
 *
 * <p>This class stores Huawei-specific configuration information that doesn't fit into the
 * vendor-independent model. It can be extended as needed to support additional Huawei features.
 */
public class HuaweiFamily implements Serializable {

  private static final long serialVersionUID = 1L;

  // Add Huawei-specific fields here as needed
  // For now, this is a minimal implementation

  public HuaweiFamily() {
    // Initialize default values if needed
  }

  @JsonCreator
  private static HuaweiFamily jsonCreator() {
    return new HuaweiFamily();
  }
}

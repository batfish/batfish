package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;

/** Class for ospf process */
public class OspfProcess implements Serializable {

  private final @Nonnull OspfVrf _defaultVrf;
  private final @Nonnull Map<String, OspfVrf> _vrfs;
  public static final double DEFAULT_REFERENCE_BANDWIDTH =
      100E9D; // https://docs.cumulusnetworks.com/cumulus-linux/Layer-3/Open-Shortest-Path-First-OSPF/#auto-cost-reference-bandwidth
  public static String DEFAULT_OSPF_PROCESS_NAME = "default";

  public OspfProcess() {
    _defaultVrf = new OspfVrf(Configuration.DEFAULT_VRF_NAME);
    _vrfs = new HashMap<>();
  }

  public @Nonnull OspfVrf getDefaultVrf() {
    return _defaultVrf;
  }

  public @Nonnull Map<String, OspfVrf> getVrfs() {
    return _vrfs;
  }
}

package org.batfish.vendor.cisco_aci.representation;

/** Constants used by Cisco ACI representation/conversion logic. */
public final class AciConstants {

  private AciConstants() {}

  /** Default administrative distance for EBGP routes on ACI. */
  public static final int DEFAULT_EBGP_ADMIN_COST = 20;

  /** Default administrative distance for IBGP routes on ACI. */
  public static final int DEFAULT_IBGP_ADMIN_COST = 200;

  /** Default local BGP weight on ACI. */
  public static final int DEFAULT_LOCAL_BGP_WEIGHT = 0;

  /** Default MTU for ACI interfaces. */
  public static final int DEFAULT_MTU = 9000;

  /** Typical number of fabric-facing ports on a spine for fallback interface creation. */
  public static final int SPINE_FABRIC_PORT_COUNT = 32;

  /** First typical leaf uplink port to spine in fallback interface creation. */
  public static final int LEAF_UPLINK_START = 53;

  /** Last typical leaf uplink port to spine in fallback interface creation. */
  public static final int LEAF_UPLINK_END = 54;

  /** Number of fallback downstream ports created on leaf/service nodes. */
  public static final int FALLBACK_DOWNSTREAM_PORT_COUNT = 8;
}

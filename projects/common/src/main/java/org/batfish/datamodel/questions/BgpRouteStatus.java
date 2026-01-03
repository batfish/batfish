package org.batfish.datamodel.questions;

/** Status of a BGP route advertisement. */
public enum BgpRouteStatus {
  // these should all be upper case for parsing below to work

  /** A backup route that is not selected, installed, or advertised. */
  BACKUP,
  /** An overall best route, or a route equivalent to the best route for the purpose of ECMP. */
  BEST;

  public static BgpRouteStatus parse(String status) {
    return Enum.valueOf(BgpRouteStatus.class, status.toUpperCase());
  }
}

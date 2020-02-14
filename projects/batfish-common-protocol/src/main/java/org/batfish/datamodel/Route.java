package org.batfish.datamodel;

import org.batfish.common.ip.Ip;

/** Generic route constants */
public final class Route {

  public static final String UNSET_NEXT_HOP = "(unknown)";
  public static final String UNSET_NEXT_HOP_INTERFACE = "dynamic";
  public static final int UNSET_ROUTE_ADMIN = -1;
  public static final Ip UNSET_ROUTE_NEXT_HOP_IP = Ip.AUTO;
  public static final long UNSET_ROUTE_TAG = -1L;

  private Route() {}
}

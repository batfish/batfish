package org.batfish.representation.palo_alto;

import java.io.Serializable;

/** Stores the administrative distances for a single {@link VirtualRouter}. */
public class AdminDistances implements Serializable {
  private static final int DEFAULT_STATIC = 10;
  private static final int DEFAULT_STATIC_IPV6 = 10;
  private static final int DEFAULT_OSPF_INT = 30;
  private static final int DEFAULT_OSPF_EXT = 110;
  private static final int DEFAULT_OSPFv3_INT = 30;
  private static final int DEFAULT_OSPFv3_EXT = 110;
  private static final int DEFAULT_IBGP = 200;
  private static final int DEFAULT_EBGP = 21;
  private static final int DEFAULT_RIP = 120;

  public int getStatic() {
    return _staticv4;
  }

  public void setStatic(int admin) {
    _staticv4 = admin;
  }

  public int getStaticv6() {
    return _staticv6;
  }

  public void setStaticv6(int admin) {
    _staticv6 = admin;
  }

  public int getOspfInt() {
    return _ospfInt;
  }

  public void setOspfInt(int admin) {
    _ospfInt = admin;
  }

  public int getOspfExt() {
    return _ospfExt;
  }

  public void setOspfExt(int admin) {
    _ospfExt = admin;
  }

  public int getOspfV3Int() {
    return _ospfV3Int;
  }

  public void setOspfV3Int(int admin) {
    _ospfV3Int = admin;
  }

  public int getOspfV3Ext() {
    return _ospfV3Ext;
  }

  public void setOspfV3Ext(int admin) {
    _ospfV3Ext = admin;
  }

  public int getIbgp() {
    return _ibgp;
  }

  public void setIbgp(int admin) {
    _ibgp = admin;
  }

  public int getEbgp() {
    return _ebgp;
  }

  public void setEbgp(int admin) {
    _ebgp = admin;
  }

  public int getRip() {
    return _rip;
  }

  public void setRip(int admin) {
    _rip = admin;
  }

  // private implementation details

  private int _staticv4 = DEFAULT_STATIC;
  private int _staticv6 = DEFAULT_STATIC_IPV6;
  private int _ospfInt = DEFAULT_OSPF_INT;
  private int _ospfExt = DEFAULT_OSPF_EXT;
  private int _ospfV3Int = DEFAULT_OSPFv3_INT;
  private int _ospfV3Ext = DEFAULT_OSPFv3_EXT;
  private int _ibgp = DEFAULT_IBGP;
  private int _ebgp = DEFAULT_EBGP;
  private int _rip = DEFAULT_RIP;
}

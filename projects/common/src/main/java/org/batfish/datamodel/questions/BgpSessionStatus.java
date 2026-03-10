package org.batfish.datamodel.questions;

/** Enum for possible status values for BGP sessions */
public enum BgpSessionStatus {
  // these should all be upper case for parsing below to work
  ESTABLISHED,
  NOT_ESTABLISHED,
  NOT_COMPATIBLE;

  public static BgpSessionStatus parse(String status) {
    return Enum.valueOf(BgpSessionStatus.class, status.toUpperCase());
  }
}

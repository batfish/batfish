package org.batfish.datamodel;

import javax.annotation.Nonnull;

public enum LineType {
  AUX,
  CON,
  HTTP,
  SERIAL,
  SSH,
  TELNET,
  TTY,
  UNKNOWN,
  VTY;

  public static LineType toLineType(@Nonnull String lineName) {
    if (lineName.startsWith("con")) {
      // both "con" and "console" are acceptable
      return CON;
    }
    String removedDigits = lineName.replaceAll("\\d", "");
    try {
      return valueOf(removedDigits.toUpperCase());
    } catch (IllegalArgumentException e) {
      return UNKNOWN;
    }
  }
}

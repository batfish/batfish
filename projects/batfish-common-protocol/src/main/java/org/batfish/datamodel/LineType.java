package org.batfish.datamodel;

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

  public static LineType toLineType(String lineType) {
    String lineTypeLowerCase = lineType.toLowerCase();
    if (lineTypeLowerCase.startsWith("aux")) {
      return AUX;
    } else if (lineTypeLowerCase.startsWith("con")) {
      return CON;
    } else if (lineTypeLowerCase.startsWith("http")) {
      return HTTP;
    } else if (lineTypeLowerCase.startsWith("serial")) {
      return SERIAL;
    } else if (lineTypeLowerCase.startsWith("ssh")) {
      return SSH;
    } else if (lineTypeLowerCase.startsWith("telnet")) {
      return TELNET;
    } else if (lineTypeLowerCase.startsWith("tty")) {
      return TTY;
    } else if (lineTypeLowerCase.startsWith("vty")) {
      return VTY;
    } else {
      return UNKNOWN;
    }
  }
}

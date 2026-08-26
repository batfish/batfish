package org.batfish.vendor.fastpath.representation;

/** An AAA authentication/authorization/accounting method (i.e. where a request is sent). */
public enum AaaMethod {
  DENY,
  ENABLE,
  LINE,
  LOCAL,
  NONE,
  RADIUS,
  TACACS
}

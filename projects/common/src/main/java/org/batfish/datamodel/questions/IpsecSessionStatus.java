package org.batfish.datamodel.questions;

import javax.annotation.Nonnull;

public enum IpsecSessionStatus {
  // these should all be upper case for parse below to work
  IPSEC_SESSION_ESTABLISHED,
  IKE_PHASE1_FAILED,
  IKE_PHASE1_KEY_MISMATCH,
  IPSEC_PHASE2_FAILED,
  MISSING_END_POINT;

  public static IpsecSessionStatus parse(@Nonnull String status) {
    return Enum.valueOf(IpsecSessionStatus.class, status.toUpperCase());
  }
}

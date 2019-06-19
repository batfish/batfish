package org.batfish.datamodel.questions;

public enum IpsecSessionStatus {
  IPSEC_SESSION_ESTABLISHED,
  IKE_PHASE1_FAILED,
  IKE_PHASE1_KEY_MISMATCH,
  IPSEC_PHASE2_FAILED,
  MISSING_END_POINT
}

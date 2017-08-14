package org.batfish.datamodel.pojo;

import org.batfish.common.BatfishException;

public enum AccessLevel {
  FULL,
  SUMMARY,
  ONELINE;

  public AccessLevel nextLevel() {
    switch (this) {
      case FULL:
        return FULL;
      case SUMMARY:
        return ONELINE;
      case ONELINE:
        return ONELINE;
      default:
        throw new BatfishException("Unknown AccessType " + this);
    }
  }

}

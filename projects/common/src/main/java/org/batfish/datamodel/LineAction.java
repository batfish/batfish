package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.annotations.VisibleForTesting;

/** An access-control action */
public enum LineAction {
  PERMIT,
  DENY;

  @JsonCreator
  @VisibleForTesting
  static LineAction forValue(String value) {
    switch (value.toLowerCase()) {
      case "permit":
      case "accept":
        return PERMIT;
      case "deny":
      case "reject":
        return DENY;
      default:
        throw new IllegalArgumentException("Unrecognized ACL line action: " + value);
    }
  }
}

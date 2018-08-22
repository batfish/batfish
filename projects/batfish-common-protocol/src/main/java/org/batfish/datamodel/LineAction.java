package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.common.BatfishException;

/** An access-control action */
public enum LineAction {
  PERMIT,
  DENY;

  @JsonCreator
  public static LineAction forValue(String value) {
    switch (value.toLowerCase()) {
      case "permit":
      case "accept":
        return PERMIT;
      case "deny":
      case "reject":
        return DENY;
      default:
        throw new BatfishException("Unrecognized ACL line action: " + value);
    }
  }
}

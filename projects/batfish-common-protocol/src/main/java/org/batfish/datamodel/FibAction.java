package org.batfish.datamodel;

import java.io.Serializable;

/** An action for a device to take when a packet matches a given {@link FibEntry}. */
public interface FibAction extends Serializable {
  public enum FibActionType {
    FORWARD,
    NULL_ROUTE
  }

  FibActionType getType();
}

package org.batfish.ibdp.exceptions;

import org.batfish.common.BatfishException;

/** Thrown when best route selection fails */
public class BestPathSelectionException extends BatfishException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a BatfishException with a detail message
   *
   * @param msg The detail message
   */
  public BestPathSelectionException(String msg) {
    super(msg);
  }
}

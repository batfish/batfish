package org.batfish.minesweeper.bdd;

import org.batfish.common.BatfishException;

/** A fatal error in the TransferBDD symbolic route analysis. */
public class TransferBDDException extends BatfishException {
  public TransferBDDException(String msg) {
    super(msg);
  }
}

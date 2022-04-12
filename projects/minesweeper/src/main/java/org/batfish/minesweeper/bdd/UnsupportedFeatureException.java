package org.batfish.minesweeper.bdd;

/**
 * Thrown whenever the symbolic route analysis {@link TransferBDD} encounters a routing policy
 * feature that it does not support.
 */
public class UnsupportedFeatureException extends Exception {
  public UnsupportedFeatureException(String message) {
    super(message);
  }
}

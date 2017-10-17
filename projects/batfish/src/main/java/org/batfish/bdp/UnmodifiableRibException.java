package org.batfish.bdp;

class UnmodifiableRibException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  UnmodifiableRibException(String message) {
    super(message);
  }
}

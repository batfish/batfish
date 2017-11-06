package org.batfish.bdp;

import org.batfish.common.BatfishException;

class UnmodifiableRibException extends BatfishException {
  private static final long serialVersionUID = 1L;

  UnmodifiableRibException(String message) {
    super(message);
  }
}

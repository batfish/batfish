package org.batfish.common;

/** Indicates that there is no stable dataplane solution */
public class BdpOscillationException extends BatfishException {

  public BdpOscillationException(String msg) {
    super(msg);
  }

  public BdpOscillationException(String msg, Throwable cause) {
    super(msg, cause);
  }
}

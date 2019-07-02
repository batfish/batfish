package org.batfish.common;

/**
 * Thrown if Batfish determines during parsing a file that a device would reject (not commmit) a
 * given configuration
 */
public class WillNotCommitException extends BatfishException {

  /**
   * Create a new exception
   *
   * @param offendingText line text that would be rejected by the device
   */
  public WillNotCommitException(String offendingText) {
    super("Would not be able to commit the following line: " + offendingText);
  }
}

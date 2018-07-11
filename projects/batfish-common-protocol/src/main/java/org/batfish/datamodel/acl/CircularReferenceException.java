package org.batfish.datamodel.acl;

import org.batfish.common.BatfishException;

/** TODO */
public class CircularReferenceException extends BatfishException {
  private static final long serialVersionUID = 1L;

  public CircularReferenceException(String msg) {
    super(msg);
  }
}

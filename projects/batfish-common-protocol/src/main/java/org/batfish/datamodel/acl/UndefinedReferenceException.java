package org.batfish.datamodel.acl;

import org.batfish.common.BatfishException;

/** TODO */
public class UndefinedReferenceException extends BatfishException {
  private static final long serialVersionUID = 1L;

  public UndefinedReferenceException(String msg) {
    super(msg);
  }
}

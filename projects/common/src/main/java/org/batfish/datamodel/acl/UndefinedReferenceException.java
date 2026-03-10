package org.batfish.datamodel.acl;

import org.batfish.common.BatfishException;

/**
 * Raised when attempting to use an undefined reference. For example, an ACL can reference a named
 * IP space that is never defined.
 */
public class UndefinedReferenceException extends BatfishException {

  public UndefinedReferenceException(String msg) {
    super(msg);
  }
}

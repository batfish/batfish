package org.batfish.datamodel.acl;

import org.batfish.common.BatfishException;

/**
 * Raised when attempting to use a reference that is part of a circular chain of references. For
 * example, two named IP spaces can reference each other, rendering both invalid.
 */
public class CircularReferenceException extends BatfishException {

  public CircularReferenceException(String msg) {
    super(msg);
  }
}

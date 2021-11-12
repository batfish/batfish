package org.batfish.representation.palo_alto;

import java.io.Serializable;

/**
 * Datamodel interface representing a structure reference in Palo Alto. e.g. an application-group
 * reference from a security rule.
 */
public interface Reference extends Serializable {
  <T, U> T accept(ReferenceVisitor<T, U> visitor, U arg);

  String getName();
}

package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Datamodel interface representing a structure reference in Palo Alto. e.g. an application-group
 * reference from a security rule.
 */
public interface Reference extends Serializable {
  <T, U> T accept(ReferenceVisitor<T, U> visitor, U arg);

  @Nonnull
  String getName();
}

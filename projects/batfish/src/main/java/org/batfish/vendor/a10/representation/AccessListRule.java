package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** Datamodel interface representing an A10 access-list rule. */
public interface AccessListRule extends Serializable {
  enum Action {
    DENY,
    PERMIT,
  }

  @Nonnull
  Action getAction();

  @Nonnull
  AccessListAddress getSource();

  @Nonnull
  AccessListAddress getDestination();

  <T> T accept(AccessListRuleVisitor<T> visitor);
}

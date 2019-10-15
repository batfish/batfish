package org.batfish.representation.palo_alto;

import java.io.Serializable;

/** An update statement of a {@link PolicyRule}. */
public interface PolicyRuleUpdate extends Serializable {

  <T> T accept(PolicyRuleUpdateVisitior<T> visitor);
}

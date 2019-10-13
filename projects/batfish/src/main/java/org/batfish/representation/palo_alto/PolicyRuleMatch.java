package org.batfish.representation.palo_alto;

import java.io.Serializable;

/** A match statement of a {@link PolicyRule}. */
public interface PolicyRuleMatch extends Serializable {

  <T> T accept(PolicyRuleMatchVisitor<T> visitor);
}

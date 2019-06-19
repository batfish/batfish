package org.batfish.representation.juniper;

import java.io.Serializable;

/** Represents the matching condition for Juniper nat rule sets */
public interface NatRuleMatch extends Serializable {
  <T> T accept(NatRuleMatchVisitor<T> visitor);
}

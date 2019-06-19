package org.batfish.representation.juniper;

import java.io.Serializable;

public interface FwThen extends Serializable {
  <T> T accept(FwThenVisitor<T> visitor);
}

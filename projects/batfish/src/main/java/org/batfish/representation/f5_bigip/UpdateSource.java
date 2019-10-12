package org.batfish.representation.f5_bigip;

import java.io.Serializable;

/** BGP neighbor update source setting */
public interface UpdateSource extends Serializable {
  <T> T accept(UpdateSourceVisitor<T> visitor);
}

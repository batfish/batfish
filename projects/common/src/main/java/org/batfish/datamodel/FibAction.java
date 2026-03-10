package org.batfish.datamodel;

import java.io.Serializable;
import org.batfish.datamodel.visitors.FibActionVisitor;

/** An action for a device to take when a packet matches a given {@link FibEntry}. */
public interface FibAction extends Serializable {
  <T> T accept(FibActionVisitor<T> visitor);
}

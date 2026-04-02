package org.batfish.datamodel;

import java.io.ObjectStreamException;
import java.io.Serial;
import org.batfish.datamodel.visitors.FibActionVisitor;

/** A {@link FibAction} that discards a packet. */
public final class FibNullRoute implements FibAction {

  public static final FibNullRoute INSTANCE = new FibNullRoute();

  private FibNullRoute() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof FibNullRoute;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public <T> T accept(FibActionVisitor<T> visitor) {
    return visitor.visitFibNullRoute(this);
  }

  /** Deserialize to singleton instance. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return INSTANCE;
  }
}

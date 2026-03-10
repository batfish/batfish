package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.SessionActionVisitor;

/**
 * A {@link SessionAction} whereby return traffic is be accepted by the node from which it
 * originated.
 */
@JsonTypeName("Accept")
@ParametersAreNonnullByDefault
public final class Accept implements SessionAction {

  public static final Accept INSTANCE = new Accept();

  private Accept() {}

  @Override
  public <T> T accept(SessionActionVisitor<T> visitor) {
    return visitor.visitAcceptVrf(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof Accept;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}

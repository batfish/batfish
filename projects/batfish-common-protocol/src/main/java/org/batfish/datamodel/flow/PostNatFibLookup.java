package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.SessionActionVisitor;

/**
 * A {@link SessionAction} whereby return traffic is forwarded according to a FIB lookup using the
 * post-transformation packet.
 */
@JsonTypeName("PostNatFibLookup")
@ParametersAreNonnullByDefault
public final class PostNatFibLookup implements SessionAction {

  public static final PostNatFibLookup INSTANCE = new PostNatFibLookup();

  private PostNatFibLookup() {}

  @Override
  public <T> T accept(SessionActionVisitor<T> visitor) {
    return visitor.visitPostNatFibLookup(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof PostNatFibLookup;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}

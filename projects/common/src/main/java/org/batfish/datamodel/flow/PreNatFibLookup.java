package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.SessionActionVisitor;

/**
 * A {@link SessionAction} whereby return traffic is forwarded according to a FIB lookup using the
 * pre-transformation packet.
 */
@JsonTypeName("PreNatFibLookup")
@ParametersAreNonnullByDefault
public final class PreNatFibLookup implements SessionAction {

  public static final PreNatFibLookup INSTANCE = new PreNatFibLookup();

  private PreNatFibLookup() {}

  @Override
  public <T> T accept(SessionActionVisitor<T> visitor) {
    return visitor.visitPreNatFibLookup(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return o instanceof PreNatFibLookup;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}

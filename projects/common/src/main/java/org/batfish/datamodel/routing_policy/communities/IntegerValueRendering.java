package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A rendering of a {@link org.batfish.datamodel.bgp.community.Community} as its unsigned decimal
 * integer value.
 */
public final class IntegerValueRendering implements CommunityRendering {

  public static @Nonnull IntegerValueRendering instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(CommunityRenderingVisitor<T, U> visitor, U arg) {
    return visitor.visitIntegerValueRendering(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof IntegerValueRendering;
  }

  @Override
  public int hashCode() {
    return 0x65139068; // randomly generated
  }

  private static final IntegerValueRendering INSTANCE = new IntegerValueRendering();

  @JsonCreator
  private static @Nonnull IntegerValueRendering create() {
    return INSTANCE;
  }

  private IntegerValueRendering() {}
}

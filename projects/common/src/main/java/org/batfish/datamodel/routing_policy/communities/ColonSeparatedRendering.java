package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A rendering of a {@link org.batfish.datamodel.bgp.community.Community} as a colon-separated list
 * of its integer components.
 *
 * <ul>
 *   <li>A standard community would be rendered as A:B where A and B are unsigned 16-bit decimal
 *       numbers with no leading zeros, A is the value of the high 16-bits, and B is the value of
 *       the low 16 bits.
 *   <li>The rendering for 64-bit extended communities is undefined.
 *   <li>The rendering for 96-bit large communities is undefined.
 * </ul>
 */
public final class ColonSeparatedRendering implements CommunityRendering {

  public static @Nonnull ColonSeparatedRendering instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(CommunityRenderingVisitor<T, U> visitor, U arg) {
    return visitor.visitColonSeparatedRendering(this, arg);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof ColonSeparatedRendering;
  }

  @Override
  public int hashCode() {
    return 0xAB60CF95; // randomly generated
  }

  private static final ColonSeparatedRendering INSTANCE = new ColonSeparatedRendering();

  @JsonCreator
  private static @Nonnull ColonSeparatedRendering create() {
    return INSTANCE;
  }

  private ColonSeparatedRendering() {}
}

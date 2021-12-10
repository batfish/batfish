package org.batfish.datamodel.route.nh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that traffic must be discarded when matching a route. In vendor language(s) this is the
 * same as null-routing, discarding, rejecting, etc.
 */
public final class NextHopDiscard implements NextHop {

  @JsonValue
  @JsonCreator
  public static @Nonnull NextHopDiscard instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof NextHopDiscard;
  }

  @Override
  public int hashCode() {
    return 856925083; // randomly generated
  }

  @Override
  public String toString() {
    return NextHopDiscard.class.getName();
  }

  @Override
  public <T> T accept(NextHopVisitor<T> visitor) {
    return visitor.visitNextHopDiscard(this);
  }

  private static final NextHopDiscard INSTANCE = new NextHopDiscard();

  private NextHopDiscard() {}
}

package org.batfish.vendor.cool_nos;

import javax.annotation.Nonnull;

/** Indicates traffic matching a route should be discarded. */
public final class NextHopDiscard implements NextHop {

  /** Returns singleton instance. */
  public static @Nonnull NextHopDiscard instance() {
    return INSTANCE;
  }

  @Override
  public <T> T accept(NextHopVisitor<T> visitor) {
    return visitor.visitNextHopDiscard(this);
  }

  // prevent instantiation
  private NextHopDiscard() {}

  private static final NextHopDiscard INSTANCE = new NextHopDiscard();
}

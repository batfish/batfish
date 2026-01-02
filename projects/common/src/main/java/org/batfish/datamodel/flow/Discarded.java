package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A flow being discarded. */
public final class Discarded implements ForwardingDetail {

  private static final Discarded INSTANCE = new Discarded();

  @JsonCreator
  @JsonValue
  public static @Nonnull Discarded instance() {
    return INSTANCE;
  }

  private Discarded() {}

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof Discarded;
  }

  @Override
  public int hashCode() {
    return 0xC67C43CB; // randomly generated
  }
}

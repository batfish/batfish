package org.batfish.datamodel.route.nh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;

public final class NextHopSelf implements NextHopResult {

  @JsonValue
  @JsonCreator
  public static @Nonnull NextHopSelf instance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return NextHopSelf.class.getName();
  }

  private static final NextHopSelf INSTANCE = new NextHopSelf();

  private NextHopSelf() {}
}

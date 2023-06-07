package org.batfish.datamodel.route.nh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;

public final class NextHopBgpPeerAddress implements NextHopResult {

  @JsonValue
  @JsonCreator
  public static @Nonnull NextHopBgpPeerAddress instance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return NextHopBgpPeerAddress.class.getName();
  }

  private static final NextHopBgpPeerAddress INSTANCE = new NextHopBgpPeerAddress();

  private NextHopBgpPeerAddress() {}
}

package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class Replacemsg implements Serializable {

  public @Nullable String getBuffer() {
    return _buffer;
  }

  public void setBuffer(@Nullable String buffer) {
    _buffer = buffer;
  }

  private @Nullable String _buffer;
}

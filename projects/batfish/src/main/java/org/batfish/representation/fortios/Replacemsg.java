package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration of replacement of a default FortiOS message, e.g. pre- and post-login banners */
public final class Replacemsg implements Serializable {

  public @Nullable String getBuffer() {
    return _buffer;
  }

  public void setBuffer(@Nullable String buffer) {
    _buffer = buffer;
  }

  private @Nullable String _buffer;
}

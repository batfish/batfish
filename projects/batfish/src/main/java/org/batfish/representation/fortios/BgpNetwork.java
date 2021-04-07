package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** FortiOS datamodel component containing BGP network */
public class BgpNetwork implements Serializable {
  public @Nonnull String getId() {
    return _id;
  }

  public @Nullable Prefix getPrefix() {
    return _prefix;
  }

  public void setPrefix(Prefix prefix) {
    _prefix = prefix;
  }

  public BgpNetwork(String id) {
    _id = id;
  }

  private @Nonnull final String _id;
  private @Nullable Prefix _prefix;
}

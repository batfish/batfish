package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** FortiOS datamodel component containing BGP network */
public class BgpNetwork implements Serializable {
  /** ID of this BGP network as defined in its edit block, or 0 if that ID is invalid. */
  public long getId() {
    return _id;
  }

  public @Nullable Prefix getPrefix() {
    return _prefix;
  }

  public void setPrefix(Prefix prefix) {
    _prefix = prefix;
  }

  public BgpNetwork(long id) {
    _id = id;
  }

  private final long _id;
  private @Nullable Prefix _prefix;
}

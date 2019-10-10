package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** BGP confederation settings */
public class BgpConfederation implements Serializable {
  private final long _id;
  private final List<Long> _peers;

  public BgpConfederation(long id) {
    _id = id;
    _peers = new ArrayList<>();
  }

  public long getId() {
    return _id;
  }

  public List<Long> getPeers() {
    return _peers;
  }
}

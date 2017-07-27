package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix;

public class FwThenNextIp implements FwThen {

  /** */
  private static final long serialVersionUID = 1L;

  private final Prefix _nextPrefix;

  public FwThenNextIp(Prefix nextPrefix) {
    _nextPrefix = nextPrefix;
  }

  public Prefix getNextPrefix() {
    return _nextPrefix;
  }
}

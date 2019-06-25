package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix;

public class FwThenNextIp implements FwThen {

  private final Prefix _nextPrefix;

  public FwThenNextIp(Prefix nextPrefix) {
    _nextPrefix = nextPrefix;
  }

  public Prefix getNextPrefix() {
    return _nextPrefix;
  }

  @Override
  public <T> T accept(FwThenVisitor<T> visitor) {
    return visitor.visitFwThenNextIp(this);
  }
}

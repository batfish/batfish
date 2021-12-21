package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;

/** An access-list rule matching traffic of any ip protocol. */
public class AccessListRuleIp implements AccessListRule {
  @Nonnull
  @Override
  public Action getAction() {
    return _action;
  }

  @Nonnull
  @Override
  public AccessListAddress getSource() {
    return _source;
  }

  @Nonnull
  @Override
  public AccessListAddress getDestination() {
    return _destination;
  }

  @Nonnull
  public String getLineText() {
    return _lineText;
  }

  @Override
  public <T> T accept(AccessListRuleVisitor<T> visitor) {
    return visitor.visitIp(this);
  }

  public AccessListRuleIp(
      Action action, AccessListAddress source, AccessListAddress destination, String lineText) {
    _action = action;
    _source = source;
    _destination = destination;
    _lineText = lineText;
  }

  @Nonnull private final Action _action;
  @Nonnull private final AccessListAddress _source;
  @Nonnull private final AccessListAddress _destination;
  @Nonnull private final String _lineText;
}

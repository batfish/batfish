package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;

/** An access-list rule matching icmp traffic. */
public class AccessListRuleIcmp implements AccessListRule {
  @Override
  public @Nonnull Action getAction() {
    return _action;
  }

  @Override
  public @Nonnull AccessListAddress getSource() {
    return _source;
  }

  @Override
  public @Nonnull AccessListAddress getDestination() {
    return _destination;
  }

  @Override
  public @Nonnull String getLineText() {
    return _lineText;
  }

  @Override
  public <T> T accept(AccessListRuleVisitor<T> visitor) {
    return visitor.visitIcmp(this);
  }

  public AccessListRuleIcmp(
      Action action, AccessListAddress source, AccessListAddress destination, String lineText) {
    _action = action;
    _source = source;
    _destination = destination;
    _lineText = lineText;
  }

  private final @Nonnull Action _action;
  private final @Nonnull AccessListAddress _source;
  private final @Nonnull AccessListAddress _destination;
  private final @Nonnull String _lineText;
}

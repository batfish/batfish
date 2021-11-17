package org.batfish.vendor.a10.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.SubRange;

/** An access-list rule matching tcp traffic. */
public class AccessListRuleTcp implements AccessListRule {
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

  /** Destination port range this rule matches on. */
  @Nullable
  public SubRange getDestinationRange() {
    return _destinationRange;
  }

  public void setDestinationRange(@Nullable SubRange destinationRange) {
    _destinationRange = destinationRange;
  }

  public AccessListRuleTcp(Action action, AccessListAddress source, AccessListAddress destination) {
    _action = action;
    _source = source;
    _destination = destination;
  }

  @Nonnull private final Action _action;
  @Nonnull private final AccessListAddress _source;
  @Nonnull private final AccessListAddress _destination;
  @Nullable private SubRange _destinationRange;
}

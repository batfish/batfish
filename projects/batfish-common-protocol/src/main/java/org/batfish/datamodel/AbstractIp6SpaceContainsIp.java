package org.batfish.datamodel;

import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

/**
 * An {@link GenericIp6SpaceVisitor Ip6Space visitor} that tests if the {@link Ip6Space} contains an
 * {@link Ip6}. Implementation of {@link Ip6SpaceReference} is left abstract.
 */
public abstract class AbstractIp6SpaceContainsIp implements GenericIp6SpaceVisitor<Boolean> {
  private final Ip6 _ip6;

  public AbstractIp6SpaceContainsIp(Ip6 ip6) {
    _ip6 = ip6;
  }

  @Override
  public final Boolean visitPrefixIp6Space(PrefixIp6Space prefixIp6Space) {
    return prefixIp6Space.getPrefix().contains(_ip6);
  }
}

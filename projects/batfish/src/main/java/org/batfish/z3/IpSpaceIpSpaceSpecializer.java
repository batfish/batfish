package org.batfish.z3;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.ipspace.IpSpaceSimplifier;
import org.batfish.common.ipspace.IpSpaceSpecializer;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.IpSpaceContainedInWildcard;
import org.batfish.datamodel.visitors.IpSpaceMayIntersectWildcard;

/**
 * Specialize an {@link IpSpace} to input destination IP whitelist and blacklist. The goal is to
 * simplify the {@link IpSpace} as much as possible under the assumption that the whitelist and
 * blacklist are always true (i.e. all packets match the whitelist, no packets match the blacklist).
 * For example, if the {@link IpSpace} is disjoint from the whitelist, it is effectively empty (i.e.
 * it containsIp no IPs in the whitelist).
 */
public class IpSpaceIpSpaceSpecializer extends IpSpaceSpecializer {
  private final IpSpace _ipSpace;

  public IpSpaceIpSpaceSpecializer(IpSpace ipSpace, @Nonnull Map<String, IpSpace> namedIpSpaces) {
    super(namedIpSpaces);
    _ipSpace = new IpSpaceSimplifier(namedIpSpaces).simplify(ipSpace);
  }

  /**
   * Specialize the whitelist using a refined specializer obtained by specializing _ipSpace to the
   * blacklist. This helps situations like this: _ipSpace &lt;= blacklist &lt;= whitelist If
   * _ipSpace is covered by the blacklist, then refinedIpSpace will be empty, and we will later
   * infer that the input ipWildcardSetIpSpace should be specialized to empty as well. Without this,
   * specialization would not be able to infer this, so we'd do less optimization.
   */
  @Override
  protected Optional<IpSpaceSpecializer> restrictSpecializerToBlacklist(Set<IpWildcard> blacklist) {
    IpSpace refinedIpSpace =
        _ipSpace.accept(
            new IpSpaceIpSpaceSpecializer(
                IpWildcardSetIpSpace.builder()
                    .including(IpWildcard.ANY)
                    .excluding(blacklist)
                    .build(),
                _namedIpSpaces));

    /* blacklist covers the entire _ipSpace, so no need to consider the whitelist.
     * TODO is this possible if !blacklistIpSpace.containsIp(UniverseIpSpace.INSTANCE)?
     */
    if (refinedIpSpace == EmptyIpSpace.INSTANCE) {
      return Optional.empty();
    }

    return Optional.of(new IpSpaceIpSpaceSpecializer(refinedIpSpace, _namedIpSpaces));
  }

  @Override
  public IpSpace specialize(Ip ip) {
    if (_ipSpace.containsIp(ip, ImmutableMap.of())) {
      return ip.toIpSpace();
    } else {
      return EmptyIpSpace.INSTANCE;
    }
  }

  @Override
  public IpSpace specialize(IpWildcard ipWildcard) {
    if (!_ipSpace.accept(new IpSpaceMayIntersectWildcard(ipWildcard, _namedIpSpaces))) {
      return EmptyIpSpace.INSTANCE;
    } else if (_ipSpace.accept(new IpSpaceContainedInWildcard(ipWildcard, _namedIpSpaces))) {
      return UniverseIpSpace.INSTANCE;
    } else {
      return ipWildcard.toIpSpace();
    }
  }
}

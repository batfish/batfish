package org.batfish.datamodel.visitors;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;

/**
 * Convert (some) {@link IpSpace IpSpaces} to an equivalent {@link RangeSet}. Supported IpSpaces are
 * {@link AclIpSpace}, {@link EmptyIpSpace}, {@link IpIpSpace}, {@link PrefixIpSpace}, and {@link
 * UniverseIpSpace}. Other IpSpaces are excluded, in particular {@link IpWildcardIpSpace} and {@link
 * IpWildcardSetIpSpace}.
 */
public final class IpSpaceToRangeSet implements GenericIpSpaceVisitor<RangeSet<Ip>> {
  private IpSpaceToRangeSet() {}

  private static final IpSpaceToRangeSet INSTANCE = new IpSpaceToRangeSet();

  public static RangeSet<Ip> toRangeSet(IpSpace ipSpace) {
    return ipSpace.accept(INSTANCE);
  }

  @Override
  public RangeSet<Ip> visitAclIpSpace(AclIpSpace aclIpSpace) {
    RangeSet<Ip> ips = TreeRangeSet.create();
    for (AclIpSpaceLine line : Lists.reverse(aclIpSpace.getLines())) {
      RangeSet<Ip> lineIps = line.getIpSpace().accept(this);
      switch (line.getAction()) {
        case PERMIT:
          ips.addAll(lineIps);
          break;
        case DENY:
          ips.removeAll(lineIps);
          break;
        default:
          throw new IllegalStateException("Unexpected AclIpSpace line action: " + line.getAction());
      }
    }
    return ImmutableRangeSet.copyOf(ips);
  }

  @Override
  public RangeSet<Ip> visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return ImmutableRangeSet.of();
  }

  @Override
  public RangeSet<Ip> visitIpIpSpace(IpIpSpace ipIpSpace) {
    Ip ip = ipIpSpace.getIp();
    return ImmutableRangeSet.of(Range.closed(ip, ip));
  }

  @Override
  public RangeSet<Ip> visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    throw new UnsupportedOperationException("Converting IpWildcard to RangeSet is unsupported");
  }

  @Override
  public RangeSet<Ip> visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    throw new UnsupportedOperationException("Converting IpWildcard to RangeSet is unsupported");
  }

  @Override
  public RangeSet<Ip> visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    throw new UnsupportedOperationException("Converting IpWildcardSet to RangeSet is unsupported");
  }

  @Override
  public RangeSet<Ip> visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    Prefix prefix = prefixIpSpace.getPrefix();
    return ImmutableRangeSet.of(Range.closed(prefix.getStartIp(), prefix.getEndIp()));
  }

  @Override
  public RangeSet<Ip> visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return ImmutableRangeSet.of(Range.closed(Prefix.ZERO.getStartIp(), Prefix.ZERO.getEndIp()));
  }
}

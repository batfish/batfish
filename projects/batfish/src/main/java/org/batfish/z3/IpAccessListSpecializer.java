package org.batfish.z3;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.visitors.HeaderSpaceConverter;

/**
 * Specialize an {@link IpAccessList} to a given {@link HeaderSpace}. Lines that can never match the
 * {@link HeaderSpace} can be removed.
 */
public class IpAccessListSpecializer {
  private final boolean _canSpecialize;
  private final HeaderSpace _headerSpace;
  private final IpSpaceSpecializer _dstIpSpaceSpecializer;
  private final IpSpaceSpecializer _srcIpSpaceSpecializer;

  public IpAccessListSpecializer(HeaderSpace headerSpace) {
    _headerSpace = headerSpace;

    /*
     * Currently, specialization is based on srcIp and dstIp only. We can specialize only
     * if we have a meaningful constraint on srcIp or on dstIp.
     */
    _canSpecialize =
        !(_headerSpace.getDstIps().isEmpty()
            && _headerSpace.getSrcOrDstIps().isEmpty()
            && _headerSpace.getNotDstIps().isEmpty()
            && _headerSpace.getSrcIps().isEmpty()
            && _headerSpace.getNotSrcIps().isEmpty());

    _dstIpSpaceSpecializer =
        new IpSpaceSpecializer(
            Sets.union(_headerSpace.getDstIps(), _headerSpace.getSrcOrDstIps()),
            _headerSpace.getNotDstIps());
    _srcIpSpaceSpecializer =
        new IpSpaceSpecializer(
            Sets.union(_headerSpace.getSrcIps(), _headerSpace.getSrcOrDstIps()),
            _headerSpace.getNotSrcIps());
  }

  public IpAccessList specialize(IpAccessList ipAccessList) {
    if (!_canSpecialize) {
      return ipAccessList;
    } else {
      List<IpAccessListLine> specializedLines =
          ipAccessList
              .getLines()
              .stream()
              .map(this::specialize)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(Collectors.toList());

      return IpAccessList.builder()
          .setName(ipAccessList.getName())
          .setLines(specializedLines)
          .build();
    }
  }

  public Optional<IpAccessListLine> specialize(IpAccessListLine ipAccessListLine) {
    /* TODO: handle other match conditions */
    HeaderSpace oldHeaderSpace = HeaderSpaceConverter.convert(ipAccessListLine.getMatchCondition());
    IpWildcardSetIpSpace.Builder srcIpSpaceBuilder =
        IpWildcardSetIpSpace.builder().excluding(oldHeaderSpace.getNotSrcIps());
    if (oldHeaderSpace.getSrcIps().isEmpty() && oldHeaderSpace.getSrcOrDstIps().isEmpty()) {
      srcIpSpaceBuilder.including(IpWildcard.ANY);
    } else {
      srcIpSpaceBuilder.including(oldHeaderSpace.getSrcIps());
      srcIpSpaceBuilder.including(oldHeaderSpace.getSrcOrDstIps());
    }
    IpSpace specializedSrcIpSpace = _srcIpSpaceSpecializer.specialize(srcIpSpaceBuilder.build());

    IpWildcardSetIpSpace.Builder dstIpSpaceBuilder =
        IpWildcardSetIpSpace.builder().excluding(oldHeaderSpace.getNotDstIps());
    if (oldHeaderSpace.getDstIps().isEmpty() && oldHeaderSpace.getSrcOrDstIps().isEmpty()) {
      dstIpSpaceBuilder.including(IpWildcard.ANY);
    } else {
      dstIpSpaceBuilder.including(oldHeaderSpace.getDstIps());
      dstIpSpaceBuilder.including(oldHeaderSpace.getSrcOrDstIps());
    }
    IpSpace specializedDstIpSpace = _dstIpSpaceSpecializer.specialize(dstIpSpaceBuilder.build());

    if (specializedDstIpSpace instanceof EmptyIpSpace
        || specializedSrcIpSpace instanceof EmptyIpSpace) {
      return Optional.empty();
    }

    Set<IpWildcard> specializedDstIps;
    Set<IpWildcard> specializedNotDstIps;
    if (specializedDstIpSpace instanceof UniverseIpSpace) {
      // for a HeaderSpace, empty dstIps means Universe
      specializedDstIps = ImmutableSet.of();
      specializedNotDstIps = ImmutableSet.of();
    } else if (specializedDstIpSpace instanceof IpWildcardSetIpSpace) {
      IpWildcardSetIpSpace dstIpWildcardSetIpSpace = (IpWildcardSetIpSpace) specializedDstIpSpace;
      specializedDstIps = dstIpWildcardSetIpSpace.getWhitelist();
      specializedNotDstIps = dstIpWildcardSetIpSpace.getBlacklist();
    } else if (specializedDstIpSpace instanceof IpWildcard) {
      specializedDstIps = ImmutableSet.of((IpWildcard) specializedDstIpSpace);
      specializedNotDstIps = ImmutableSet.of();
    } else {
      throw new BatfishException("unexpected specializedDstIpSpace type");
    }

    Set<IpWildcard> specializedSrcIps;
    Set<IpWildcard> specializedNotSrcIps;
    if (specializedSrcIpSpace instanceof UniverseIpSpace) {
      specializedSrcIps = ImmutableSet.of();
      specializedNotSrcIps = ImmutableSet.of();
    } else if (specializedSrcIpSpace instanceof IpWildcardSetIpSpace) {
      IpWildcardSetIpSpace srcIpWildcardSetIpSpace = (IpWildcardSetIpSpace) specializedSrcIpSpace;
      specializedSrcIps = srcIpWildcardSetIpSpace.getWhitelist();
      specializedNotSrcIps = srcIpWildcardSetIpSpace.getBlacklist();
    } else if (specializedSrcIpSpace instanceof IpWildcard) {
      specializedSrcIps = ImmutableSet.of((IpWildcard) specializedSrcIpSpace);
      specializedNotSrcIps = ImmutableSet.of();
    } else {
      throw new BatfishException("unexpected specializedSrcIpSpace type");
    }

    HeaderSpace newHeaderSpace =
        oldHeaderSpace
            .rebuild()
            .setDstIps(specializedDstIps)
            .setNotDstIps(specializedNotDstIps)
            .setSrcIps(specializedSrcIps)
            .setNotSrcIps(specializedNotSrcIps)
            .build();
    AclLineMatchExpr matchCondition =
        newHeaderSpace.unrestricted() ? TrueExpr.INSTANCE : new MatchHeaderSpace(newHeaderSpace);

    return Optional.of(
        IpAccessListLine.builder()
            .setAction(ipAccessListLine.getAction())
            .setMatchCondition(matchCondition)
            .setName(ipAccessListLine.getName())
            .build());
  }
}

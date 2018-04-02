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

/**
 * Specialize an {@link IpAccessList} to a given {@link HeaderSpace}. Lines that can never match the
 * {@link HeaderSpace} can be removed.
 */
public class IpAccessListSpecializer {
  private final boolean _canSpecialize;
  private final boolean _haveDstIpConstraint;
  private final boolean _haveSrcIpConstraint;
  private final HeaderSpace _headerSpace;
  private final IpSpaceSpecializer _dstIpSpaceSpecializer;
  private final IpSpaceSpecializer _srcIpSpaceSpecializer;

  public IpAccessListSpecializer(HeaderSpace headerSpace) {
    _headerSpace = headerSpace;

    _haveDstIpConstraint =
        !_headerSpace.getDstIps().isEmpty()
            || !_headerSpace.getSrcOrDstIps().isEmpty()
            || !_headerSpace.getNotDstIps().isEmpty();

    _haveSrcIpConstraint =
        !_headerSpace.getSrcIps().isEmpty()
            || !_headerSpace.getSrcOrDstIps().isEmpty()
            || !_headerSpace.getNotSrcIps().isEmpty();

    /*
     * Currently, specialization is based on srcIp and dstIp only.
     */
    _canSpecialize = _haveSrcIpConstraint || _haveDstIpConstraint;

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
    IpWildcardSetIpSpace.Builder srcIpSpaceBuilder =
        IpWildcardSetIpSpace.builder().excluding(ipAccessListLine.getNotSrcIps());
    if (ipAccessListLine.getSrcIps().isEmpty() && ipAccessListLine.getSrcOrDstIps().isEmpty()) {
      srcIpSpaceBuilder.including(IpWildcard.ANY);
    } else {
      srcIpSpaceBuilder.including(ipAccessListLine.getSrcIps());
      srcIpSpaceBuilder.including(ipAccessListLine.getSrcOrDstIps());
    }
    IpSpace specializedSrcIpSpace = _srcIpSpaceSpecializer.specialize(srcIpSpaceBuilder.build());

    IpWildcardSetIpSpace.Builder dstIpSpaceBuilder =
        IpWildcardSetIpSpace.builder().excluding(ipAccessListLine.getNotDstIps());
    if (ipAccessListLine.getDstIps().isEmpty() && ipAccessListLine.getSrcOrDstIps().isEmpty()) {
      dstIpSpaceBuilder.including(IpWildcard.ANY);
    } else {
      dstIpSpaceBuilder.including(ipAccessListLine.getDstIps());
      dstIpSpaceBuilder.including(ipAccessListLine.getSrcOrDstIps());
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
    } else {
      throw new BatfishException("unexpected specializedSrcIpSpace type");
    }

    return Optional.of(
        ipAccessListLine
            .rebuild()
            .setDstIps(specializedDstIps)
            .setNotDstIps(specializedNotDstIps)
            .setSrcIps(specializedSrcIps)
            .setNotSrcIps(specializedNotSrcIps)
            .build());
  }
}

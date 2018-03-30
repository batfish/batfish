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
 * Specialize an IpAccessList to a given HeaderSpace. Lines that can never match the HeaderSpace can
 * be removed.
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
    IpSpace specializedDstIpSpace =
        _dstIpSpaceSpecializer.specialize(
            IpWildcardSetIpSpace.builder()
                .including(ipAccessListLine.getDstIps())
                .including(ipAccessListLine.getSrcOrDstIps())
                .excluding(ipAccessListLine.getNotDstIps())
                .build());

    IpSpace specializedSrcIpSpace =
        _srcIpSpaceSpecializer.specialize(
            IpWildcardSetIpSpace.builder()
                .including(ipAccessListLine.getSrcIps())
                .including(ipAccessListLine.getSrcOrDstIps())
                .including(ipAccessListLine.getNotSrcIps())
                .build());

    if (specializedDstIpSpace instanceof EmptyIpSpace
        || specializedSrcIpSpace instanceof EmptyIpSpace) {
      return Optional.empty();
    }

    Set<IpWildcard> specializedDstIps, specializedNotDstIps;
    if (specializedDstIpSpace instanceof UniverseIpSpace) {
      specializedDstIps = ImmutableSet.of(IpWildcard.ANY);
      specializedNotDstIps = ImmutableSet.of();
    } else if (specializedDstIpSpace instanceof IpWildcardSetIpSpace) {
      IpWildcardSetIpSpace dstIpWildcardSetIpSpace = (IpWildcardSetIpSpace) specializedDstIpSpace;
      specializedDstIps = dstIpWildcardSetIpSpace.getWhitelist();
      specializedNotDstIps = dstIpWildcardSetIpSpace.getBlacklist();
    } else {
      throw new BatfishException("unexpected specializedDstIpSpace type");
    }

    Set<IpWildcard> specializedSrcIps, specializedNotSrcIps;
    if (specializedSrcIpSpace instanceof UniverseIpSpace) {
      specializedSrcIps = ImmutableSet.of(IpWildcard.ANY);
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

  private boolean aclLineIsRelevant(IpAccessListLine ipAccessListLine) {
    if (_haveSrcIpConstraint
        && !ipAccessListLine.getSrcIps().isEmpty()
        && ipAccessListLine.getSrcIps().stream().noneMatch(this::aclSrcIpIsRelevant)) {
      return false;
    }

    if (_haveDstIpConstraint
        && !ipAccessListLine.getDstIps().isEmpty()
        && ipAccessListLine.getDstIps().stream().noneMatch(this::aclDstIpIsRelevant)) {
      return false;
    }

    return true;
  }

  private boolean aclDstIpIsRelevant(IpWildcard dstIp) {
    return _headerSpace.getDstIps().stream().anyMatch(dstIp::intersects)
        && _headerSpace.getNotDstIps().stream().noneMatch(dstIp::subsetOf);
  }

  private boolean aclSrcIpIsRelevant(IpWildcard srcIp) {
    return _headerSpace.getSrcIps().stream().anyMatch(srcIp::intersects)
        && _headerSpace.getNotSrcIps().stream().noneMatch(srcIp::subsetOf);
  }
}

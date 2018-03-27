package org.batfish.datamodel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialize an IpAccessList to a given HeaderSpace. Lines that can never match the HeaderSpace can
 * be removed.
 */
public class IpAccessListSpecializer {
  private final boolean _canSpecialize;
  private final boolean _haveDstIpConstraint;
  private final boolean _haveSrcIpConstraint;
  private final HeaderSpace _headerSpace;

  public IpAccessListSpecializer(HeaderSpace headerSpace) {
    _headerSpace = headerSpace;

    _haveDstIpConstraint =
        !_headerSpace.getDstIps().isEmpty() || !_headerSpace.getNotDstIps().isEmpty();

    _haveSrcIpConstraint =
        !_headerSpace.getSrcIps().isEmpty() || !_headerSpace.getNotSrcIps().isEmpty();

    /*
     * Currently, specialization is based on srcIp and dstIp only.
     */
    _canSpecialize = _haveSrcIpConstraint || _haveDstIpConstraint;
  }

  public IpAccessList specialize(IpAccessList ipAccessList) {

    if (!_canSpecialize) {
      return ipAccessList;
    } else {
      List<IpAccessListLine> specializedLines =
          ipAccessList
              .getLines()
              .stream()
              .filter(this::aclLineIsRelevant)
              .collect(Collectors.toList());

      return IpAccessList.builder()
          .setName(ipAccessList.getName())
          .setLines(specializedLines)
          .build();
    }
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

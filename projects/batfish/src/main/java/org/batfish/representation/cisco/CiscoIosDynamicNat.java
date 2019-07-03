package org.batfish.representation.cisco;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/** Representation of a Cisco IOS dynamic NAT. */
public final class CiscoIosDynamicNat extends CiscoIosNat {

  private @Nullable String _aclName;
  private @Nullable String _natPool;

  @VisibleForTesting
  public static String computeDynamicDestinationNatAclName(@Nonnull String natAclName) {
    return String.format("~DYNAMIC_DESTINATION_NAT_INSIDE_ACL~%s~", natAclName);
  }

  @Nullable
  public String getAclName() {
    return _aclName;
  }

  public void setAclName(@Nullable String aclName) {
    _aclName = aclName;
  }

  public void setNatPool(@Nullable String natPool) {
    _natPool = natPool;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CiscoIosDynamicNat)) {
      return false;
    }
    CiscoIosDynamicNat other = (CiscoIosDynamicNat) o;
    return (getAction() == other.getAction())
        && Objects.equals(_aclName, other._aclName)
        && Objects.equals(_natPool, other._natPool);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_aclName, getAction(), _natPool);
  }

  @Override
  public int natCompare(CiscoIosNat other) {
    return 0;
  }

  @Override
  public Optional<Transformation.Builder> toOutgoingTransformation(
      Map<String, IpAccessList> ipAccessLists,
      Map<String, NatPool> natPools,
      @Nullable Set<String> insideInterfaces,
      Configuration c) {

    /*
     * SOURCE_INSIDE matches and dynamically translates source addresses on egress
     * DESTINATION_INSIDE matches and dynamically translates destination addresses on egress
     */

    // reverse translation is not supported for dynamic NAT
    if (getAction() == RuleAction.SOURCE_OUTSIDE) {
      return Optional.empty();
    }

    String natAclName = _aclName;
    if (natAclName == null) {
      // Parser rejects this case
      return Optional.empty();
    }

    /*
     * Cisco IOS only supports standard ACLs for dynamic NAT
     * Arista supports extended ACLs for dynamic NAT
     */
    IpAccessList natAcl = ipAccessLists.get(natAclName);
    if (natAcl == null || natAcl.getSourceType() == null) {
      // Invalid reference
      return Optional.empty();
    }
    boolean isStandardAcl =
        natAcl
            .getSourceType()
            .equals(CiscoStructureType.IPV4_ACCESS_LIST_STANDARD.getDescription());
    if (!isStandardAcl) {
      // Invalid reference
      return Optional.empty();
    }

    if (_natPool == null) {
      // Parser allows this for Arista NAT (nat overload), but it is not supported
      return Optional.empty();
    }
    NatPool natPool = natPools.get(_natPool);
    if (natPool == null) {
      // Configuration has an invalid reference
      return Optional.empty();
    }

    if (getAction() == RuleAction.DESTINATION_INSIDE) {
      // Expect all lines to be header space matches for NAT ACL
      if (!natAcl.getLines().stream()
          .map(IpAccessListLine::getMatchCondition)
          .allMatch(cond -> cond instanceof MatchHeaderSpace)) {
        return Optional.empty();
      }

      // Create reverse acl to match destination address instead of source address
      String reverseAclName = computeDynamicDestinationNatAclName(natAclName);
      List<IpAccessListLine> lines =
          natAcl.getLines().stream()
              .map(
                  line -> {
                    HeaderSpace origHeader =
                        ((MatchHeaderSpace) line.getMatchCondition()).getHeaderspace();
                    HeaderSpace headerSpace =
                        origHeader
                            .toBuilder()
                            .setDstIps(origHeader.getSrcIps())
                            .setSrcIps((IpSpace) null)
                            .build();
                    return IpAccessListLine.builder()
                        .setAction(line.getAction())
                        .setMatchCondition(new MatchHeaderSpace(headerSpace))
                        .build();
                  })
              .collect(Collectors.toList());
      IpAccessList.builder()
          .setLines(lines)
          .setName(reverseAclName)
          .setOwner(c)
          .setSourceName(natAclName)
          .setSourceType(natAcl.getSourceType())
          .build();

      natAclName = reverseAclName;
    }

    AclLineMatchExpr natAclExpr = permittedByAcl(natAclName);
    if (insideInterfaces != null) {
      natAclExpr = and(natAclExpr, new MatchSrcInterface(insideInterfaces));
    }
    TransformationStep step;
    if (getAction() == RuleAction.SOURCE_INSIDE) {
      step = assignSourceIp(natPool.getFirst(), natPool.getLast());
    } else if (getAction() == RuleAction.DESTINATION_INSIDE) {
      step = assignDestinationIp(natPool.getFirst(), natPool.getLast());
    } else {
      throw new BatfishException("Unexpected RuleAction");
    }
    return Optional.of(when(natAclExpr).apply(step));
  }

  @Override
  public Optional<Transformation.Builder> toIncomingTransformation(Map<String, NatPool> natPools) {

    /*
     * SOURCE_OUTSIDE matches and dynamically translates source addresses on ingress
     */

    // reverse translation is not supported for dynamic NAT
    if (getAction() != RuleAction.SOURCE_OUTSIDE) {
      return Optional.empty();
    }

    String natAclName = _aclName;
    if (natAclName == null) {
      // Parser rejects this case
      return Optional.empty();
    }

    if (_natPool == null) {
      // Parser allows this for Arista NAT (nat overload), but it is not supported
      return Optional.empty();
    }
    NatPool natPool = natPools.get(_natPool);
    if (natPool == null) {
      // Configuration has an invalid reference
      return Optional.empty();
    }

    AclLineMatchExpr natAclExpr = permittedByAcl(natAclName);
    TransformationStep step = assignSourceIp(natPool.getFirst(), natPool.getLast());
    return Optional.of(when(natAclExpr).apply(step));
  }
}

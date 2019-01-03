package org.batfish.representation.cisco;

import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

public class CiscoAristaNat extends CiscoIosDynamicNat {
  private static final long serialVersionUID = 1L;

  @Override
  public Optional<Transformation.Builder> toOutgoingTransformation(
      Map<String, IpAccessList> ipAccessLists,
      Map<String, NatPool> natPools,
      @Nullable Set<String> insideInterfaces,
      Configuration c) {

    String natAclName = getAclName();
    if (natAclName == null) {
      // Parser rejects this case
      return Optional.empty();
    }

    String natPoolName = getNatPool();
    if (natPoolName == null) {
      // Allowed but not supported
      return Optional.empty();
    }

    NatPool natPool = natPools.get(natPoolName);
    if (natPool == null) {
      // Configuration has an invalid reference
      return Optional.empty();
    }

    AclLineMatchExpr natAclExpr = permittedByAcl(natAclName);
    TransformationStep step = assignSourceIp(natPool.getFirst(), natPool.getLast());
    return Optional.of(when(natAclExpr).apply(step));
  }
}

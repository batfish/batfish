package org.batfish.representation.cisco;

import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.transformation.Transformation;

/** Representation of a Arista dynamic source NAT. */
@ParametersAreNonnullByDefault
public final class AristaDynamicSourceNat implements Serializable {
  private static final long serialVersionUID = 1L;
  private final @Nonnull String _natAclName;
  private final @Nonnull String _natPoolName;

  public AristaDynamicSourceNat(String natAclName, String natPoolName) {
    _natAclName = natAclName;
    _natPoolName = natPoolName;
  }

  public Optional<Transformation> toOutgoingTransformation(
      Map<String, NatPool> natPools, Transformation orElse) {
    NatPool natPool = natPools.get(_natPoolName);
    if (natPool == null) {
      // Configuration has an invalid reference
      return Optional.empty();
    }

    return Optional.of(
        when(permittedByAcl(_natAclName))
            .apply(assignSourceIp(natPool.getFirst(), natPool.getLast()))
            .setOrElse(orElse)
            .build());
  }
}

package org.batfish.vendor.arista.representation;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.transformation.Transformation;

/** Interface-level configuration of {@code ip nat source dynamic}. */
@ParametersAreNonnullByDefault
public final class AristaDynamicSourceNat implements Serializable {

  private final @Nonnull String _natAclName;
  private final String _natPoolName;
  private final boolean _overload;

  public AristaDynamicSourceNat(String natAclName, @Nullable String natPoolName, boolean overload) {
    checkArgument(
        natPoolName != null ^ overload,
        "Must either have a pool or be an overload rule (but not both).");
    _natAclName = natAclName;
    _natPoolName = natPoolName;
    _overload = overload;
  }

  public @Nonnull String getNatAclName() {
    return _natAclName;
  }

  public Optional<Transformation> toTransformation(
      Ip interfaceIp, Map<String, NatPool> natPools, @Nullable Transformation orElse) {
    NatPool natPool =
        _overload ? new NatPool(interfaceIp, interfaceIp) : natPools.get(_natPoolName);
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

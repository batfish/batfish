package org.batfish.common.bdd;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.IpSpace;

/** An {@link IpSpaceToBDD} that memoizes its {@link IpSpaceToBDD#visit} method. */
public final class MemoizedIpSpaceToBDD extends IpSpaceToBDD {
  private final Map<IpSpace, BDD> _cache = new HashMap<>();

  public MemoizedIpSpaceToBDD(BDDInteger var, Map<String, IpSpace> namedIpSpaces) {
    super(var, namedIpSpaces);
  }

  @Override
  public BDD visit(IpSpace ipSpace) {
    BDD bdd = _cache.get(ipSpace);
    if (bdd == null) {
      bdd = super.visit(ipSpace);
      _cache.put(ipSpace, bdd);
    }
    return bdd;
  }

  @VisibleForTesting
  Optional<BDD> getMemoizedBdd(IpSpace ipSpace) {
    return Optional.ofNullable(_cache.get(ipSpace));
  }
}

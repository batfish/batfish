package org.batfish.common.bdd;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.IpSpace;

/** An {@link IpSpaceToBDD} that memoizes its {@link IpSpaceToBDD#visit} method. */
public final class MemoizedIpSpaceToBDD extends IpSpaceToBDD {
  private final Map<IpSpace, BDD> _cache = new HashMap<>();

  public MemoizedIpSpaceToBDD(
      BDDFactory factory, BDDInteger var, Map<String, IpSpace> namedIpSpaces) {
    super(factory, var, namedIpSpaces);
  }

  @Override
  public BDD visit(IpSpace ipSpace) {
    return _cache.computeIfAbsent(ipSpace, super::visit);
  }

  @VisibleForTesting
  Optional<BDD> getMemoizedBdd(IpSpace ipSpace) {
    return Optional.ofNullable(_cache.get(ipSpace));
  }
}

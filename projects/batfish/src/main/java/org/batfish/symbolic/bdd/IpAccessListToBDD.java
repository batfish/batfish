package org.batfish.symbolic.bdd;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.util.NonRecursiveSupplier;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;

@ParametersAreNonnullByDefault
public final class IpAccessListToBDD {

  @Nonnull private final Map<String, Supplier<BDD>> _aclEnv;

  @Nonnull private final AclLineMatchExprToBDD _aclLineMatchExprToBDD;

  @Nonnull private final BDDPacket _pkt;

  private IpAccessListToBDD(
      @Nonnull Map<String, IpAccessList> aclEnv,
      @Nonnull BDDSourceManager bddSrcManager,
      @Nonnull Map<String, IpSpace> ipSpaceEnv,
      @Nonnull BDDPacket pkt) {
    // use laziness to tie the recursive knot.
    _aclEnv = new HashMap<>();
    aclEnv.forEach(
        (name, acl) ->
            _aclEnv.put(name, Suppliers.memoize(new NonRecursiveSupplier<>(() -> toBdd(acl)))));
    _aclLineMatchExprToBDD =
        new AclLineMatchExprToBDD(pkt.getFactory(), pkt, _aclEnv, ipSpaceEnv, bddSrcManager);
    _pkt = pkt;
  }

  public static IpAccessListToBDD create(
      BDDPacket pkt,
      Map<String, IpAccessList> aclEnv,
      Map<String, IpSpace> ipSpaceEnv,
      BDDSourceManager bddSrcManager) {
    return new IpAccessListToBDD(aclEnv, bddSrcManager, ipSpaceEnv, pkt);
  }

  public AclLineMatchExprToBDD getAclLineMatchExprToBDD() {
    return _aclLineMatchExprToBDD;
  }

  /**
   * Convert an Access Control List (ACL) to a symbolic boolean expression. The default action in an
   * ACL is to deny all traffic.
   */
  @Nonnull
  public BDD toBdd(IpAccessList acl) {
    BDDFactory bddFactory = _pkt.getFactory();
    BDD result = bddFactory.zero();
    for (IpAccessListLine line : Lists.reverse(acl.getLines())) {
      BDD lineBDD = _aclLineMatchExprToBDD.visit(line.getMatchCondition());
      BDD actionBDD = line.getAction() == LineAction.PERMIT ? bddFactory.one() : bddFactory.zero();
      result = lineBDD.ite(actionBDD, result);
    }
    return result;
  }
}

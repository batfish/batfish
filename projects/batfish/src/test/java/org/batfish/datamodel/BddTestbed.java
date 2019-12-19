package org.batfish.datamodel;

import java.util.Map;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/** Testbed for {@link BDD}-based comparisons and conversions. */
public final class BddTestbed {

  public @Nonnull BDD toBDD(AclLine aclLine) {
    return _aclToBdd.toPermitAndDenyBdds(aclLine).getMatchBdd();
  }

  public @Nonnull BDD toBDD(AclLineMatchExpr aclLineMatchExpr) {
    return _aclToBdd.toBdd(aclLineMatchExpr);
  }

  public @Nonnull BDD toBDD(HeaderSpace headerSpace) {
    return _hsToBdd.toBDD(headerSpace);
  }

  public @Nonnull IpAccessListToBdd getAclToBdd() {
    return _aclToBdd;
  }

  public @Nonnull IpSpaceToBDD getDstIpBdd() {
    return _dstIpBdd;
  }

  public @Nonnull HeaderSpaceToBDD getHsToBdd() {
    return _hsToBdd;
  }

  public @Nonnull BDDPacket getPkt() {
    return _pkt;
  }

  public @Nonnull IpSpaceToBDD getSrcIpBdd() {
    return _srcIpBdd;
  }

  public BddTestbed(Map<String, IpAccessList> aclEnv, Map<String, IpSpace> ipSpaceEnv) {
    _pkt = new BDDPacket();
    _dstIpBdd = _pkt.getDstIpSpaceToBDD();
    _srcIpBdd = _pkt.getSrcIpSpaceToBDD();
    _hsToBdd = new HeaderSpaceToBDD(_pkt, ipSpaceEnv);
    _aclToBdd = new IpAccessListToBddImpl(_pkt, BDDSourceManager.empty(_pkt), _hsToBdd, aclEnv);
  }

  private final IpAccessListToBdd _aclToBdd;
  private final IpSpaceToBDD _dstIpBdd;
  private final HeaderSpaceToBDD _hsToBdd;
  private final BDDPacket _pkt;
  private final IpSpaceToBDD _srcIpBdd;
}

package org.batfish.symbolic.bdd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;

public class BDDAcl {

  private IpAccessList _acl;

  private BDD _bdd;

  private BDDFactory _factory;

  private BDDPacket _pkt;

  private BDDAcl(IpAccessList acl) {
    _bdd = null;
    _acl = acl;
    _factory = BDDPacket.factory;
    _pkt = new BDDPacket();
  }

  private BDDAcl(BDDAcl other) {
    _bdd = other._bdd;
    _acl = other._acl;
    _factory = other._factory;
    _pkt = other._pkt;
  }

  public static BDDAcl create(IpAccessList acl) {
    BDDAcl abdd = new BDDAcl(acl);
    abdd.computeACL();
    return abdd;
  }

  /*
   * Convert an Access Control List (ACL) to a symbolic boolean expression.
   * The default action in an ACL is to deny all traffic.
   */
  private void computeACL() {
    // Check if there is an ACL first
    if (_acl == null) {
      _bdd = _factory.one();
    }

    _bdd = _factory.zero();

    AclLineMatchExprToBDD aclLineMatchExprToBDD = new AclLineMatchExprToBDD(_factory, _pkt);

    List<IpAccessListLine> lines = new ArrayList<>(_acl.getLines());
    Collections.reverse(lines);

    for (IpAccessListLine line : lines) {
      BDD lineBDD = aclLineMatchExprToBDD.toBDD(line.getMatchCondition());
      BDD actionBDD = line.getAction() == LineAction.ACCEPT ? _factory.one() : _factory.zero();
      _bdd = lineBDD.ite(actionBDD, _bdd);
    }
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  public BDD getBdd() {
    return _bdd;
  }

  public BDDFactory getFactory() {
    return _factory;
  }

  public BDDPacket getPkt() {
    return _pkt;
  }

  @Override
  public int hashCode() {
    return _bdd != null ? _bdd.hashCode() : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDAcl)) {
      return false;
    }
    BDDAcl other = (BDDAcl) o;
    return Objects.equals(_bdd, other._bdd);
  }

  /*
   * Create a new version of the BDD restricted to a prefix
   */
  public BDDAcl restrict(Prefix pfx) {
    BDDAcl other = new BDDAcl(this);
    other._bdd = this._pkt.restrict(this._bdd, pfx);
    return other;
  }

  /*
   * Create a new version of the BDD restricted to a list of prefixes
   */
  public BDDAcl restrict(List<Prefix> prefixes) {
    BDDAcl other = new BDDAcl(this);
    other._bdd = this._pkt.restrict(this._bdd, prefixes);
    return other;
  }
}

package org.batfish.symbolic.bdd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.Graph;

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

  public static BDDAcl create(
      @Nullable Configuration conf, IpAccessList acl, boolean ignoreNetworks) {
    Set<Prefix> networks = null;
    if (ignoreNetworks) {
      networks = Graph.getOriginatedNetworks(conf);
    }
    BDDAcl abdd = new BDDAcl(acl);
    abdd.computeACL(networks);
    return abdd;
  }

  /*
   * Convert an Access Control List (ACL) to a symbolic boolean expression.
   * The default action in an ACL is to deny all traffic.
   */
  private void computeACL(@Nullable Set<Prefix> networks) {
    // Check if there is an ACL first
    BDD allowed = _factory.zero();
    BDD denied = _factory.zero();
    List<IpAccessListLine> lines = new ArrayList<>(_acl.getLines());
    // Collections.reverse(lines);
    for (IpAccessListLine l : lines) {
      // System.out.println("ACL Line: " + l.getName() + ", " + l.getAction());
      BDD local = _pkt.fromHeaderSpace(l);
      if (l.getAction() == LineAction.ACCEPT) {
        allowed = allowed.or(local.and(denied.not()));
      } else {
        denied = denied.or(local);
      }
    }
    _bdd = allowed;
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

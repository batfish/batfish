package org.batfish.symbolic.bdd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.symbolic.bdd.BDDNetFactory.BDDPacket;

public class BDDAcl {

  private IpAccessList _acl;

  private BDD _bdd;

  private BDDNetFactory _netFactory;

  private BDDPacket _pkt;

  private BDDAcl(BDDNetFactory netFactory, IpAccessList acl) {
    _bdd = null;
    _acl = acl;
    _netFactory = netFactory;
    _pkt = netFactory.createPacket();
  }

  private BDDAcl(BDDAcl other) {
    _bdd = other._bdd;
    _acl = other._acl;
    _netFactory = other._netFactory;
    _pkt = other._pkt;
  }

  public static BDDAcl create(BDDNetFactory netFactory, IpAccessList acl) {
    BDDAcl abdd = new BDDAcl(netFactory, acl);
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
      _bdd = _netFactory.one();
    }

    _bdd = _netFactory.zero();

    AclLineMatchExprToBDD aclLineMatchExprToBDD =
        new AclLineMatchExprToBDD(_netFactory.getFactory(), _pkt);

    List<IpAccessListLine> lines = new ArrayList<>(_acl.getLines());
    Collections.reverse(lines);

    for (IpAccessListLine line : lines) {
      BDD lineBDD = aclLineMatchExprToBDD.toBDD(line.getMatchCondition());
      BDD actionBDD =
          line.getAction() == LineAction.ACCEPT ? _netFactory.one() : _netFactory.zero();
      _bdd = lineBDD.ite(actionBDD, _bdd);
    }
  }

  /*
   * Convert a set of ip protocols to a boolean expression on the symbolic packet
   */
  private BDD computeIpProtocols(Set<IpProtocol> ipProtos) {
    BDD acc = _netFactory.zero();
    for (IpProtocol proto : ipProtos) {
      BDD isValue = _pkt.getIpProtocol().value(proto.number());
      acc = acc.or(isValue);
    }
    return acc;
  }

  /*
   * Convert Tcp flags to a boolean expression on the symbolic packet
   */
  private BDD computeTcpFlags(List<TcpFlags> flags) {
    BDD acc = _netFactory.zero();
    for (TcpFlags fs : flags) {
      acc = acc.or(computeTcpFlags(fs));
    }
    return acc;
  }

  /*
   * Convert a Tcp flag to a boolean expression on the symbolic packet
   */
  private BDD computeTcpFlags(TcpFlags flags) {
    BDD acc = _netFactory.one();
    if (flags.getUseAck()) {
      BDD value = flags.getAck() ? _pkt.getTcpAck() : _pkt.getTcpAck().not();
      acc = acc.and(value);
    }
    if (flags.getUseCwr()) {
      BDD value = flags.getCwr() ? _pkt.getTcpCwr() : _pkt.getTcpCwr().not();
      acc = acc.and(value);
    }
    if (flags.getUseEce()) {
      BDD value = flags.getEce() ? _pkt.getTcpEce() : _pkt.getTcpEce().not();
      acc = acc.and(value);
    }
    if (flags.getUseFin()) {
      BDD value = flags.getFin() ? _pkt.getTcpFin() : _pkt.getTcpFin().not();
      acc = acc.and(value);
    }
    if (flags.getUsePsh()) {
      BDD value = flags.getPsh() ? _pkt.getTcpPsh() : _pkt.getTcpPsh().not();
      acc = acc.and(value);
    }
    if (flags.getUseRst()) {
      BDD value = flags.getRst() ? _pkt.getTcpRst() : _pkt.getTcpRst().not();
      acc = acc.and(value);
    }
    if (flags.getUseSyn()) {
      BDD value = flags.getSyn() ? _pkt.getTcpSyn() : _pkt.getTcpSyn().not();
      acc = acc.and(value);
    }
    if (flags.getUseUrg()) {
      BDD value = flags.getUrg() ? _pkt.getTcpUrg() : _pkt.getTcpUrg().not();
      acc = acc.and(value);
    }
    return acc;
  }

  /*
   * Convert a set of ranges and a packet field to a symbolic boolean expression
   */
  private BDD computeValidRange(Set<SubRange> ranges, BDDInteger field) {
    BDD acc = _netFactory.zero();
    for (SubRange range : ranges) {
      int start = range.getStart();
      int end = range.getEnd();
      // System.out.println("Range: " + start + "--" + end);
      if (start == end) {
        BDD isValue = field.value(start);
        acc = acc.or(isValue);
      } else {
        BDD r = field.geq(start).and(field.leq(end));
        acc = acc.or(r);
      }
    }
    return acc;
  }

  /*
   * Convert a set of wildcards and a packet field to a symbolic boolean expression
   */
  private BDD computeWildcardMatch(Set<IpWildcard> wcs, BDDInteger field) {
    BDD acc = _netFactory.zero();
    for (IpWildcard wc : wcs) {
      if (!wc.isPrefix()) {
        throw new BatfishException("ERROR: computeDstWildcards, non sequential mask detected");
      }
      Prefix p = wc.toPrefix();
      acc = acc.or(isRelevantFor(p, field));
    }
    return acc;
  }

  /*
   * Does the 32 bit integer match the prefix using lpm?
   */
  private BDD isRelevantFor(Prefix p, BDDInteger i) {
    return firstBitsEqual(i.getBitvec(), p, p.getPrefixLength());
  }

  /*
   * Check if the first length bits match the BDDInteger
   * representing the advertisement prefix.
   *
   * Note: We assume the prefix is never modified, so it will
   * be a bitvector containing only the underlying routeVariables:
   * [var(0), ..., var(n)]
   */
  private BDD firstBitsEqual(BDD[] bits, Prefix p, int length) {
    long b = p.getStartIp().asLong();
    BDD acc = _netFactory.one();
    for (int i = 0; i < length; i++) {
      boolean res = Ip.getBitAtPosition(b, i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc = acc.and(bits[i].not());
      }
    }
    return acc;
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  public BDD getBdd() {
    return _bdd;
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

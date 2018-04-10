package org.batfish.symbolic.bdd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.acl.MatchHeaderspace;
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

  public static BDDAcl create(Configuration conf, IpAccessList acl, boolean ignoreNetworks) {
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
    if (_acl == null) {
      _bdd = _factory.one();
    }

    _bdd = _factory.zero();

    List<IpAccessListLine> lines = new ArrayList<>(_acl.getLines());
    Collections.reverse(lines);

    for (IpAccessListLine line : lines) {
      // System.out.println("ACL Line: " + l.getName() + ", " + l.getAction());
      /* TODO: handle other match types */
      HeaderSpace h = ((MatchHeaderspace) line.getMatchCondition()).getHeaderspace();
      BDD local = null;

      if (h.getDstIps() != null) {
        BDD val = computeWildcardMatch(h.getDstIps(), _pkt.getDstIp(), networks);
        val = h.getDstIps().isEmpty() ? _factory.one() : val;
        local = val;
      }

      if (h.getSrcIps() != null) {
        BDD val = computeWildcardMatch(h.getSrcIps(), _pkt.getSrcIp(), null);
        val = h.getDstIps().isEmpty() ? _factory.one() : val;
        local = (local == null ? val : local.and(val));
      }

      if (h.getDscps() != null && !h.getDscps().isEmpty()) {
        throw new BatfishException("detected dscps");
      }

      if (h.getDstPorts() != null) {
        BDD val = computeValidRange(h.getDstPorts(), _pkt.getDstPort());
        val = h.getDstPorts().isEmpty() ? _factory.one() : val;
        local = (local == null ? val : local.and(val));
      }

      if (h.getSrcPorts() != null) {
        BDD val = computeValidRange(h.getSrcPorts(), _pkt.getSrcPort());
        val = h.getSrcPorts().isEmpty() ? _factory.one() : val;
        local = (local == null ? val : local.and(val));
      }

      if (h.getEcns() != null && !h.getEcns().isEmpty()) {
        throw new BatfishException("detected ecns");
      }

      if (h.getTcpFlags() != null) {
        BDD val = computeTcpFlags(h.getTcpFlags());
        val = h.getTcpFlags().isEmpty() ? _factory.one() : val;
        local = (local == null ? val : local.and(val));
      }

      if (h.getFragmentOffsets() != null && !h.getFragmentOffsets().isEmpty()) {
        throw new BatfishException("detected fragment offsets");
      }

      if (h.getIcmpCodes() != null) {
        BDD val = computeValidRange(h.getIcmpCodes(), _pkt.getIcmpCode());
        val = h.getIcmpCodes().isEmpty() ? _factory.one() : val;
        local = (local == null ? val : local.and(val));
      }

      if (h.getIcmpTypes() != null) {
        BDD val = computeValidRange(h.getIcmpTypes(), _pkt.getIcmpType());
        val = h.getIcmpTypes().isEmpty() ? _factory.one() : val;
        local = (local == null ? val : local.and(val));
      }

      if (h.getStates() != null && !h.getStates().isEmpty()) {
        throw new BatfishException("detected states");
      }

      if (h.getIpProtocols() != null) {
        BDD val = computeIpProtocols(h.getIpProtocols());
        val = h.getIpProtocols().isEmpty() ? _factory.one() : val;
        local = (local == null ? val : local.and(val));
      }

      if (h.getNotDscps() != null && !h.getNotDscps().isEmpty()) {
        throw new BatfishException("detected NOT dscps");
      }

      if (h.getNotDstIps() != null && !h.getNotDstIps().isEmpty()) {
        throw new BatfishException("detected NOT dst ip");
      }

      if (h.getNotSrcIps() != null && !h.getNotSrcIps().isEmpty()) {
        throw new BatfishException("detected NOT src ip");
      }

      if (h.getNotDstPorts() != null && !h.getNotDstPorts().isEmpty()) {
        throw new BatfishException("detected NOT dst port");
      }

      if (h.getNotSrcPorts() != null && !h.getNotSrcPorts().isEmpty()) {
        throw new BatfishException("detected NOT src port");
      }

      if (h.getNotEcns() != null && !h.getNotEcns().isEmpty()) {
        throw new BatfishException("detected NOT ecns");
      }

      if (h.getNotIcmpCodes() != null && !h.getNotIcmpCodes().isEmpty()) {
        throw new BatfishException("detected NOT icmp codes");
      }

      if (h.getNotIcmpTypes() != null && !h.getNotIcmpTypes().isEmpty()) {
        throw new BatfishException("detected NOT icmp types");
      }

      if (h.getNotFragmentOffsets() != null && !h.getNotFragmentOffsets().isEmpty()) {
        throw new BatfishException("detected NOT fragment offset");
      }

      if (h.getNotIpProtocols() != null && !h.getNotIpProtocols().isEmpty()) {
        throw new BatfishException("detected NOT ip protocols");
      }

      if (local != null) {
        BDD ret;
        if (line.getAction() == LineAction.ACCEPT) {
          ret = _factory.one();
        } else {
          ret = _factory.zero();
        }

        if (h.getNegate()) {
          local = local.not();
        }

        _bdd = local.ite(ret, _bdd);
      }
    }
  }

  /*
   * Convert a set of ip protocols to a boolean expression on the symbolic packet
   */
  private BDD computeIpProtocols(Set<IpProtocol> ipProtos) {
    BDD acc = _factory.zero();
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
    BDD acc = _factory.zero();
    for (TcpFlags fs : flags) {
      acc = acc.or(computeTcpFlags(fs));
    }
    return acc;
  }

  /*
   * Convert a Tcp flag to a boolean expression on the symbolic packet
   */
  private BDD computeTcpFlags(TcpFlags flags) {
    BDD acc = _factory.one();
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
    BDD acc = _factory.zero();
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
  private BDD computeWildcardMatch(
      Set<IpWildcard> wcs, BDDInteger field, @Nullable Set<Prefix> ignored) {
    BDD acc = _factory.zero();
    for (IpWildcard wc : wcs) {
      if (!wc.isPrefix()) {
        throw new BatfishException("ERROR: computeDstWildcards, non sequential mask detected");
      }
      Prefix p = wc.toPrefix();
      // if (!PrefixUtils.isContainedBy(p, ignored)) {
      acc = acc.or(isRelevantFor(p, field));
      // }
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
   * be a bitvector containing only the underlying variables:
   * [var(0), ..., var(n)]
   */
  private BDD firstBitsEqual(BDD[] bits, Prefix p, int length) {
    long b = p.getStartIp().asLong();
    BDD acc = _factory.one();
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

package org.batfish.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.symbolic.bdd.BDDPacket;

/*
 * High-dimensional hyperrectangle.
 * For a 2D version of the rectangle, the bounds will be
 * (xlow, xhigh, ylow, yhigh)
 */
public class EquivalenceClass implements Comparable<EquivalenceClass> {

  private BDD _bdd;

  private int _alphaIndex;

  public static EquivalenceClass one() {
    return new EquivalenceClass(BDDPacket.factory.one());
  }

  public static EquivalenceClass zero() {
    return new EquivalenceClass(BDDPacket.factory.zero());
  }

  EquivalenceClass(BDD bdd) {
    this._bdd = bdd;
    this._alphaIndex = -1;
  }

  public BDD getBdd() {
    return _bdd;
  }

  int getAlphaIndex() {
    return _alphaIndex;
  }

  void setAlphaIndex(int alphaIndex) {
    this._alphaIndex = alphaIndex;
  }

  EquivalenceClass and(EquivalenceClass other) {
    BDD bdd = _bdd.and(other._bdd);
    return new EquivalenceClass(bdd);
  }

  EquivalenceClass or(EquivalenceClass other) {
    BDD bdd = _bdd.or(other._bdd);
    return new EquivalenceClass(bdd);
  }


  EquivalenceClass not() {
    return new EquivalenceClass(_bdd.not());
  }

  boolean isZero() {
    return _bdd.isZero();
  }

  boolean isOne() {
    return _bdd.isOne();
  }

  @Override public String toString() {
    return "EquivalenceClass{" + "_bdd=" + _bdd + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EquivalenceClass that = (EquivalenceClass) o;
    return _bdd.hashCode() == that._bdd.hashCode();
  }

  @Override
  public int hashCode() {
    return _bdd.hashCode();
  }

  @Override public int compareTo(@Nonnull EquivalenceClass that) {
    int x = this._bdd.hashCode();
    int y = that._bdd.hashCode();
    if (x < y) {
      return -1;
    } else if (x > y) {
      return 1;
    }
    return 0;
  }

  public HeaderSpace example(BDDPacket packet) {
    HeaderSpace space = new HeaderSpace();
    EquivalenceClass acc = EquivalenceClass.one();

    // this.getBdd().satOne().printSet();

    SortedSet<IpWildcard> dstIps = new TreeSet<>();
    SortedSet<IpWildcard> srcIps = new TreeSet<>();
    List<SubRange> dstPorts = new ArrayList<>();
    List<SubRange> srcPorts = new ArrayList<>();
    List<SubRange> icmpTypes = new ArrayList<>();
    List<SubRange> icmpCodes = new ArrayList<>();
    List<IpProtocol> ipProtos = new ArrayList<>();
    List<TcpFlags> tcpFlags = new ArrayList<>();

    TcpFlags flags = new TcpFlags();
    tcpFlags.add(flags);
    space.setDstIps(dstIps);
    space.setSrcIps(srcIps);
    space.setDstPorts(dstPorts);
    space.setSrcPorts(srcPorts);
    space.setIcmpTypes(icmpTypes);
    space.setIcmpCodes(icmpCodes);
    space.setIpProtocols(ipProtos);
    space.setTcpFlags(tcpFlags);
    return space;
  }

  public static EquivalenceClass fromHeaderSpace(HeaderSpace h, BDDPacket packet) {
    return EquivalenceClass.one();
  }
}

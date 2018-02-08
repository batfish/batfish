package org.batfish.geometry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;

public class GeometricSpace {

  public static int NUM_FIELDS = 15;

  private static long DSTIP_LOW = 0;

  private static long DSTIP_HIGH = (long) Math.pow(2, 32);

  private static long SRCIP_LOW = 0;

  private static long SRCIP_HIGH = (long) Math.pow(2, 32);

  private static long DSTPORT_LOW = 0;

  private static long DSTPORT_HIGH = (long) Math.pow(2, 16);

  private static long SRCPORT_LOW = 0;

  private static long SRCPORT_HIGH = (long) Math.pow(2, 16);

  private static long IPPROTO_LOW = 0;

  private static long IPPROTO_HIGH = (long) Math.pow(2, 8);

  private static long ICMPTYPE_LOW = 0;

  private static long ICMPTYPE_HIGH = (long) Math.pow(2, 8);

  private static long ICMPCODE_LOW = 0;

  private static long ICMPCODE_HIGH = (long) Math.pow(2, 4);

  private static long TCPACK_LOW = 0;

  private static long TCPACK_HIGH = (long) Math.pow(2, 1);

  private static long TCPCWR_LOW = 0;

  private static long TCPCWR_HIGH = (long) Math.pow(2, 1);

  private static long TCPECE_LOW = 0;

  private static long TCPECE_HIGH = (long) Math.pow(2, 1);

  private static long TCPFIN_LOW = 0;

  private static long TCPFIN_HIGH = (long) Math.pow(2, 1);

  private static long TCPPST_LOW = 0;

  private static long TCPPST_HIGH = (long) Math.pow(2, 1);

  private static long TCPRST_LOW = 0;

  private static long TCPRST_HIGH = (long) Math.pow(2, 1);

  private static long TCPSYN_LOW = 0;

  private static long TCPSYN_HIGH = (long) Math.pow(2, 1);

  private static long TCPURG_LOW = 0;

  private static long TCPURG_HIGH = (long) Math.pow(2, 1);

  private static GeometricSpace ZERO = null;

  private static GeometricSpace ONE = null;

  private static HyperRectangle FULL_SPACE = null;

  static {
    long[] bounds = {
      DSTIP_LOW,
      DSTIP_HIGH,
      SRCIP_LOW,
      SRCIP_HIGH,
      DSTPORT_LOW,
      DSTPORT_HIGH,
      SRCPORT_LOW,
      SRCPORT_HIGH,
      IPPROTO_LOW,
      IPPROTO_HIGH,
      ICMPTYPE_LOW,
      ICMPTYPE_HIGH,
      ICMPCODE_LOW,
      ICMPCODE_HIGH,
      TCPACK_LOW,
      TCPACK_HIGH,
      TCPCWR_LOW,
      TCPCWR_HIGH,
      TCPECE_LOW,
      TCPECE_HIGH,
      TCPFIN_LOW,
      TCPFIN_HIGH,
      TCPPST_LOW,
      TCPPST_HIGH,
      TCPRST_LOW,
      TCPRST_HIGH,
      TCPSYN_LOW,
      TCPSYN_HIGH,
      TCPURG_LOW,
      TCPURG_HIGH
    };
    FULL_SPACE = new HyperRectangle(bounds);
    Set<HyperRectangle> rects = new HashSet<>();
    rects.add(FULL_SPACE);
    ONE = new GeometricSpace(rects);
    ZERO = new GeometricSpace(new HashSet<>());
  }

  private Set<HyperRectangle> _rectangles;

  private GeometricSpace(Set<HyperRectangle> rectangles) {
    this._rectangles = rectangles;
  }

  public static HyperRectangle fullSpace() {
    return new HyperRectangle(FULL_SPACE);
  }

  public static GeometricSpace one() {
    return ONE;
  }

  public static GeometricSpace zero() {
    return ZERO;
  }

  public static GeometricSpace and(GeometricSpace x, GeometricSpace y) {
    Set<HyperRectangle> rects = new HashSet<>();
    for (HyperRectangle r1 : x._rectangles) {
      for (HyperRectangle r2 : y._rectangles) {
        HyperRectangle overlap = r1.overlap(r2);
        if (overlap != null) {
          rects.add(overlap);
        }
      }
    }
    return new GeometricSpace(rects);
  }

  public static GeometricSpace or(GeometricSpace x, GeometricSpace y) {
    Set<HyperRectangle> rects = new HashSet<>();
    rects.addAll(x._rectangles);
    rects.addAll(y._rectangles);
    return new GeometricSpace(rects);
  }

  public static GeometricSpace not(GeometricSpace x) {
    GeometricSpace acc = ZERO;
    for (HyperRectangle r : x._rectangles) {
      HyperRectangle one = ONE.rectangles().iterator().next();
      assert (one != null);
      Collection<HyperRectangle> divided = one.divide(r);
      assert (divided != null);
      divided.remove(r);
      Set<HyperRectangle> negated = new HashSet<>(divided);
      GeometricSpace space = new GeometricSpace(negated);
      acc = GeometricSpace.or(acc, space);
    }
    return acc;
  }

  // TODO: assume for now that there is only a single entry per line (is this true?)
  static HyperRectangle fromAcl(IpAccessListLine aclLine) {
    long dstIpStart = DSTIP_LOW;
    long dstIpEnd = DSTIP_HIGH;
    long srcIpStart = SRCIP_LOW;
    long srcIpEnd = SRCIP_HIGH;
    long dstPortStart = DSTPORT_LOW;
    long dstPortEnd = DSTPORT_HIGH;
    long srcPortStart = SRCPORT_LOW;
    long srcPortEnd = SRCPORT_HIGH;
    long ipProtoStart = IPPROTO_LOW;
    long ipProtoEnd = IPPROTO_HIGH;
    long icmpTypeStart = ICMPTYPE_LOW;
    long icmpTypeEnd = ICMPTYPE_HIGH;
    long icmpCodeStart = ICMPCODE_LOW;
    long icmpCodeEnd = ICMPCODE_HIGH;

    if (!aclLine.getDstIps().isEmpty()) {
      IpWildcard wc = aclLine.getDstIps().first();
      Prefix p = wc.toPrefix();
      dstIpStart = p.getStartIp().asLong();
      dstIpEnd = p.getEndIp().asLong() + 1;
    }

    if (!aclLine.getSrcIps().isEmpty()) {
      IpWildcard wc = aclLine.getSrcIps().first();
      Prefix p = wc.toPrefix();
      srcIpStart = p.getStartIp().asLong();
      srcIpEnd = p.getEndIp().asLong() + 1;
    }

    if (!aclLine.getDstPorts().isEmpty()) {
      SubRange sr = aclLine.getDstPorts().first();
      dstPortStart = sr.getStart();
      dstPortEnd = sr.getEnd() + 1;
    }

    if (!aclLine.getSrcPorts().isEmpty()) {
      SubRange sr = aclLine.getSrcPorts().first();
      srcPortStart = sr.getStart();
      srcPortEnd = sr.getEnd() + 1;
    }

    if (!aclLine.getIpProtocols().isEmpty()) {
      IpProtocol proto = aclLine.getIpProtocols().first();
      ipProtoStart = proto.number();
      ipProtoEnd = proto.number() + 1;
    }

    if (!aclLine.getIcmpTypes().isEmpty()) {
      SubRange sr = aclLine.getIcmpTypes().first();
      icmpTypeStart = sr.getStart();
      icmpTypeEnd = sr.getEnd() + 1;
    }

    if (!aclLine.getIcmpCodes().isEmpty()) {
      SubRange sr = aclLine.getIcmpCodes().first();
      icmpCodeStart = sr.getStart();
      icmpCodeEnd = sr.getEnd() + 1;
    }

    long[] bounds = {
      dstIpStart,
      dstIpEnd,
      srcIpStart,
      srcIpEnd,
      dstPortStart,
      dstPortEnd,
      srcPortStart,
      srcPortEnd,
      ipProtoStart,
      ipProtoEnd,
      icmpTypeStart,
      icmpTypeEnd,
      icmpCodeStart,
      icmpCodeEnd,
      TCPACK_LOW,
      TCPACK_HIGH,
      TCPCWR_LOW,
      TCPCWR_HIGH,
      TCPECE_LOW,
      TCPECE_HIGH,
      TCPFIN_LOW,
      TCPFIN_HIGH,
      TCPPST_LOW,
      TCPPST_HIGH,
      TCPRST_LOW,
      TCPRST_HIGH,
      TCPSYN_LOW,
      TCPSYN_HIGH,
      TCPURG_LOW,
      TCPURG_HIGH
    };
    return new HyperRectangle(bounds);
  }

  Set<HyperRectangle> rectangles() {
    return this._rectangles;
  }
}

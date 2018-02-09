package org.batfish.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;

public class GeometricSpace {

  public static int NUM_FIELDS = 15;

  private static long DSTIP_LOW = 0;

  private static long DSTIP_HIGH = (long) Math.pow(2, 32);

  private static int DSTIP_IDX = 0;

  private static long SRCIP_LOW = 0;

  private static long SRCIP_HIGH = (long) Math.pow(2, 32);

  private static int SRCIP_IDX = 1;

  private static long DSTPORT_LOW = 0;

  private static long DSTPORT_HIGH = (long) Math.pow(2, 16);

  private static int DSTPORT_IDX = 2;

  private static long SRCPORT_LOW = 0;

  private static long SRCPORT_HIGH = (long) Math.pow(2, 16);

  private static int SRCPORT_IDX = 3;

  private static long IPPROTO_LOW = 0;

  private static long IPPROTO_HIGH = (long) Math.pow(2, 8);

  private static int IPPROTO_IDX = 4;

  private static long ICMPTYPE_LOW = 0;

  private static long ICMPTYPE_HIGH = (long) Math.pow(2, 8);

  private static int ICMPTYPE_IDX = 5;

  private static long ICMPCODE_LOW = 0;

  private static long ICMPCODE_HIGH = (long) Math.pow(2, 4);

  private static int ICMPCODE_IDX = 6;

  private static long TCPACK_LOW = 0;

  private static long TCPACK_HIGH = (long) Math.pow(2, 1);

  private static int TCPACK_IDX = 7;

  private static long TCPCWR_LOW = 0;

  private static long TCPCWR_HIGH = (long) Math.pow(2, 1);

  private static int TCPCWR_IDX = 8;

  private static long TCPECE_LOW = 0;

  private static long TCPECE_HIGH = (long) Math.pow(2, 1);

  private static int TCPECE_IDX = 9;

  private static long TCPFIN_LOW = 0;

  private static long TCPFIN_HIGH = (long) Math.pow(2, 1);

  private static int TCPFIN_IDX = 10;

  private static long TCPPSH_LOW = 0;

  private static long TCPPSH_HIGH = (long) Math.pow(2, 1);

  private static int TCPPSH_IDX = 11;

  private static long TCPRST_LOW = 0;

  private static long TCPRST_HIGH = (long) Math.pow(2, 1);

  private static int TCPRST_IDX = 12;

  private static long TCPSYN_LOW = 0;

  private static long TCPSYN_HIGH = (long) Math.pow(2, 1);

  private static int TCPSYN_IDX = 13;

  private static long TCPURG_LOW = 0;

  private static long TCPURG_HIGH = (long) Math.pow(2, 1);

  private static int TCPURG_IDX = 14;

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
      TCPPSH_LOW,
      TCPPSH_HIGH,
      TCPRST_LOW,
      TCPRST_HIGH,
      TCPSYN_LOW,
      TCPSYN_HIGH,
      TCPURG_LOW,
      TCPURG_HIGH
    };
    FULL_SPACE = new HyperRectangle(bounds);
    List<HyperRectangle> rects = new ArrayList<>();
    rects.add(FULL_SPACE);
    ONE = new GeometricSpace(rects);
    ZERO = new GeometricSpace(new ArrayList<>());
  }

  private List<HyperRectangle> _rectangles;

  private GeometricSpace(List<HyperRectangle> rectangles) {
    this._rectangles = rectangles;
  }

  public static GeometricSpace singleton(HyperRectangle r) {
    List<HyperRectangle> rects = new ArrayList<>();
    rects.add(r);
    return new GeometricSpace(rects);
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
    List<HyperRectangle> rects = new ArrayList<>();
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
    List<HyperRectangle> z = new ArrayList<>();
    Stack<HyperRectangle> workListX = new Stack<>();
    Stack<HyperRectangle> workListY = new Stack<>();
    workListX.addAll(x.rectangles());
    workListY.addAll(y.rectangles());
    while (!workListY.isEmpty()) {
      HyperRectangle r1 = workListY.pop();
      boolean disjoint = true;
      for (HyperRectangle r2 : x.rectangles()) {
        HyperRectangle overlap = r1.overlap(r2);
        if (overlap != null) {
          disjoint = false;
          Collection<HyperRectangle> newRects1 = r1.divide(overlap);
          Collection<HyperRectangle> newRects2 = r2.divide(overlap);
          assert(newRects1 != null);
          assert(newRects2 != null);
          workListX.addAll(newRects2);
          for (HyperRectangle newRect : newRects1) {
            if (!newRect.equals(overlap)) {
              workListY.push(newRect);
            }
          }
        }
      }
      if (disjoint) {
        z.add(r1);
      }
    }
    z.addAll(workListX);
    return new GeometricSpace(z);
  }

  public static GeometricSpace not(GeometricSpace x) {
    GeometricSpace acc = ZERO;
    for (HyperRectangle r : x._rectangles) {
      HyperRectangle one = ONE.rectangles().iterator().next();
      assert (one != null);
      Collection<HyperRectangle> divided = one.divide(r);
      assert (divided != null);
      divided.remove(r);
      List<HyperRectangle> negated = new ArrayList<>(divided);
      GeometricSpace space = new GeometricSpace(negated);
      acc = GeometricSpace.or(acc, space);
    }
    return acc;
  }

  public static HeaderSpace example(HyperRectangle counterExample) {
    HeaderSpace space = new HeaderSpace();
    long[] bounds = counterExample.getBounds();

    SortedSet<IpWildcard> dstIps = new TreeSet<>();
    SortedSet<IpWildcard> srcIps = new TreeSet<>();
    List<SubRange> dstPorts = new ArrayList<>();
    List<SubRange> srcPorts = new ArrayList<>();
    List<SubRange> icmpTypes = new ArrayList<>();
    List<SubRange> icmpCodes = new ArrayList<>();
    List<IpProtocol> ipProtos = new ArrayList<>();
    List<TcpFlags> tcpFlags = new ArrayList<>();

    TcpFlags flags = new TcpFlags();
    flags.setAck(bounds[2 * TCPACK_IDX] == 1);
    flags.setCwr(bounds[2 * TCPCWR_IDX] == 1);
    flags.setEce(bounds[2 * TCPECE_IDX] == 1);
    flags.setFin(bounds[2 * TCPFIN_IDX] == 1);
    flags.setUrg(bounds[2 * TCPURG_IDX] == 1);
    flags.setRst(bounds[2 * TCPRST_IDX] == 1);
    flags.setPsh(bounds[2 * TCPPSH_IDX] == 1);
    flags.setSyn(bounds[2 * TCPSYN_IDX] == 1);

    dstIps.add(new IpWildcard(new Ip(bounds[2 * DSTIP_IDX])));
    srcIps.add(new IpWildcard(new Ip(bounds[2 * SRCIP_IDX])));
    dstPorts.add(
        new SubRange((int) bounds[2 * DSTPORT_IDX], (int) bounds[2 * DSTPORT_IDX + 1] - 1));
    srcPorts.add(
        new SubRange((int) bounds[2 * SRCPORT_IDX], (int) bounds[2 * SRCPORT_IDX + 1] - 1));
    icmpTypes.add(
        new SubRange((int) bounds[2 * ICMPTYPE_IDX], (int) bounds[2 * ICMPTYPE_IDX + 1] - 1));
    icmpCodes.add(
        new SubRange((int) bounds[2 * ICMPCODE_IDX], (int) bounds[2 * ICMPCODE_IDX + 1] - 1));
    ipProtos.add(IpProtocol.fromNumber((int) bounds[2 * IPPROTO_IDX]));
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

  static GeometricSpace fromHeaderSpace(HeaderSpace h) {
    GeometricSpace acc = GeometricSpace.one();

    SortedSet<IpWildcard> allDstIps = new TreeSet<>(h.getDstIps());
    allDstIps.addAll(h.getSrcOrDstIps());
    if (!allDstIps.isEmpty()) {
      GeometricSpace dstIps = GeometricSpace.zero();
      for (IpWildcard wc : allDstIps) {
        Prefix p = wc.toPrefix();
        long start = p.getStartIp().asLong();
        long end = p.getEndIp().asLong() + 1;
        HyperRectangle r = GeometricSpace.fullSpace();
        r.getBounds()[2 * DSTIP_IDX] = start;
        r.getBounds()[2 * DSTIP_IDX + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        dstIps = GeometricSpace.or(dstIps, space);
      }
      acc = GeometricSpace.and(acc, dstIps);
    }

    SortedSet<IpWildcard> allSrcIps = new TreeSet<>(h.getSrcIps());
    allSrcIps.addAll(h.getSrcOrDstIps());
    if (!allSrcIps.isEmpty()) {
      GeometricSpace srcIps = GeometricSpace.zero();
      for (IpWildcard wc : allSrcIps) {
        Prefix p = wc.toPrefix();
        long start = p.getStartIp().asLong();
        long end = p.getEndIp().asLong() + 1;
        HyperRectangle r = GeometricSpace.fullSpace();
        r.getBounds()[2 * SRCIP_IDX] = start;
        r.getBounds()[2 * SRCIP_IDX + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        srcIps = GeometricSpace.or(srcIps, space);
      }
      acc = GeometricSpace.and(acc, srcIps);
    }

    SortedSet<SubRange> allDstPorts = new TreeSet<>(h.getDstPorts());
    allDstPorts.addAll(h.getSrcOrDstPorts());
    if (!allDstPorts.isEmpty()) {
      GeometricSpace dstPorts = GeometricSpace.zero();
      for (SubRange sr : allDstPorts) {
        long start = sr.getStart();
        long end = sr.getEnd() + 1;
        HyperRectangle r = GeometricSpace.fullSpace();
        r.getBounds()[2 * DSTPORT_IDX] = start;
        r.getBounds()[2 * DSTPORT_IDX + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        dstPorts = GeometricSpace.or(dstPorts, space);
      }
      acc = GeometricSpace.and(acc, dstPorts);
    }

    SortedSet<SubRange> allSrcPorts = new TreeSet<>(h.getSrcPorts());
    allSrcPorts.addAll(h.getSrcOrDstPorts());
    if (!allSrcPorts.isEmpty()) {
      GeometricSpace srcPorts = GeometricSpace.zero();
      for (SubRange sr : allSrcPorts) {
        long start = sr.getStart();
        long end = sr.getEnd() + 1;
        HyperRectangle r = GeometricSpace.fullSpace();
        r.getBounds()[2 * DSTPORT_IDX] = start;
        r.getBounds()[2 * DSTPORT_IDX + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        srcPorts = GeometricSpace.or(srcPorts, space);
      }
      acc = GeometricSpace.and(acc, srcPorts);
    }

    if (!h.getIpProtocols().isEmpty()) {
      GeometricSpace ipProtos = GeometricSpace.zero();
      for (IpProtocol proto : h.getIpProtocols()) {
        long start = proto.number();
        long end = proto.number() + 1;
        HyperRectangle r = GeometricSpace.fullSpace();
        r.getBounds()[2 * IPPROTO_IDX] = start;
        r.getBounds()[2 * IPPROTO_IDX + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        ipProtos = GeometricSpace.or(ipProtos, space);
      }
      acc = GeometricSpace.and(acc, ipProtos);
    }

    if (!h.getIcmpTypes().isEmpty()) {
      GeometricSpace icmpTypes = GeometricSpace.zero();
      for (SubRange sr : h.getIcmpTypes()) {
        long start = sr.getStart();
        long end = sr.getEnd() + 1;
        HyperRectangle r = GeometricSpace.fullSpace();
        r.getBounds()[2 * ICMPTYPE_IDX] = start;
        r.getBounds()[2 * ICMPTYPE_IDX + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        icmpTypes = GeometricSpace.or(icmpTypes, space);
      }
      acc = GeometricSpace.and(acc, icmpTypes);
    }

    if (!h.getIcmpCodes().isEmpty()) {
      GeometricSpace icmpCodes = GeometricSpace.zero();
      for (SubRange sr : h.getIcmpCodes()) {
        long start = sr.getStart();
        long end = sr.getEnd() + 1;
        HyperRectangle r = GeometricSpace.fullSpace();
        r.getBounds()[2 * ICMPCODE_IDX] = start;
        r.getBounds()[2 * ICMPCODE_IDX + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        icmpCodes = GeometricSpace.or(icmpCodes, space);
      }
      acc = GeometricSpace.and(acc, icmpCodes);
    }

    return acc;
  }

  static GeometricSpace fromAcl(IpAccessListLine aclLine) {
    return fromHeaderSpace(aclLine);
  }

  List<HyperRectangle> rectangles() {
    return this._rectangles;
  }
}

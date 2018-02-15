package org.batfish.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
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

class GeometricSpaceFactory {

  private EnumSet<PacketField> _fields;

  private EnumMap<PacketField, Integer> _fieldIndex;

  private EquivalenceClass _fullSpace;

  private GeometricSpace _one;

  private GeometricSpace _zero;

  GeometricSpaceFactory(EnumSet<PacketField> fields) {
    _fields = fields;
    _fullSpace = null;
    _fieldIndex = new EnumMap<>(PacketField.class);
    EnumMap<PacketField, Long> fieldLow = new EnumMap<>(PacketField.class);
    EnumMap<PacketField, Long> fieldHigh = new EnumMap<>(PacketField.class);

    long u32 = (long) Math.pow(2, 32);
    long u16 = (long) Math.pow(2, 16);
    long u8 = (long) Math.pow(2, 8);
    long u4 = (long) Math.pow(2, 4);
    long u1 = (long) Math.pow(2, 1);

    int i = 0;
    for (PacketField field : _fields) {
      _fieldIndex.put(field, i);
      i++;
      fieldLow.put(field, 0L);
      switch (field) {
        case SRCIP:
          fieldHigh.put(field, u32);
          break;
        case DSTIP:
          fieldHigh.put(field, u32);
          break;
        case SRCPORT:
          fieldHigh.put(field, u16);
          break;
        case DSTPORT:
          fieldHigh.put(field, u16);
          break;
        case IPPROTO:
          fieldHigh.put(field, u8);
          break;
        case ICMPTYPE:
          fieldHigh.put(field, u8);
          break;
        case ICMPCODE:
          fieldHigh.put(field, u4);
          break;
        case TCPACK:
          fieldHigh.put(field, u1);
          break;
        case TCPCWR:
          fieldHigh.put(field, u1);
          break;
        case TCPECE:
          fieldHigh.put(field, u1);
          break;
        case TCPFIN:
          fieldHigh.put(field, u1);
          break;
        case TCPPSH:
          fieldHigh.put(field, u1);
          break;
        case TCPRST:
          fieldHigh.put(field, u1);
          break;
        case TCPURG:
          fieldHigh.put(field, u1);
          break;
        case TCPSYN:
          fieldHigh.put(field, u1);
          break;
        default:
          break;
      }
    }

    long[] bounds = new long[2 * _fields.size()];
    for (Entry<PacketField, Integer> entry : _fieldIndex.entrySet()) {
      PacketField field = entry.getKey();
      Integer index = entry.getValue();
      long low = fieldLow.get(field);
      long high = fieldHigh.get(field);
      bounds[2 * index] = low;
      bounds[2 * index + 1] = high;
    }
    _fullSpace = new EquivalenceClass(bounds);
    List<EquivalenceClass> rects = new ArrayList<>();
    rects.add(_fullSpace);
    _one = new GeometricSpace(rects);
    _zero = new GeometricSpace(new ArrayList<>());
  }

  public int numFields() {
    return _fields.size();
  }

  public GeometricSpace one() {
    return _one;
  }

  public GeometricSpace zero() {
    return _zero;
  }

  public GeometricSpace and(GeometricSpace x, GeometricSpace y) {
    List<EquivalenceClass> rects = new ArrayList<>();
    for (EquivalenceClass r1 : x.rectangles()) {
      for (EquivalenceClass r2 : y.rectangles()) {
        EquivalenceClass overlap = r1.overlap(r2);
        if (overlap != null) {
          rects.add(overlap);
        }
      }
    }
    return new GeometricSpace(rects);
  }

  public GeometricSpace or(GeometricSpace x, GeometricSpace y) {
    List<EquivalenceClass> z = new ArrayList<>();
    Stack<EquivalenceClass> workListX = new Stack<>();
    Stack<EquivalenceClass> workListY = new Stack<>();
    workListX.addAll(x.rectangles());
    workListY.addAll(y.rectangles());
    while (!workListY.isEmpty()) {
      EquivalenceClass r1 = workListY.pop();
      boolean disjoint = true;
      for (EquivalenceClass r2 : x.rectangles()) {
        EquivalenceClass overlap = r1.overlap(r2);
        if (overlap != null) {
          disjoint = false;
          Collection<EquivalenceClass> newRects1 = r1.subtract(overlap);
          Collection<EquivalenceClass> newRects2 = r2.subtract(overlap);
          assert (newRects1 != null);
          assert (newRects2 != null);
          workListX.addAll(newRects2);
          for (EquivalenceClass newRect : newRects1) {
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

  public GeometricSpace not(GeometricSpace x) {
    GeometricSpace acc = _zero;
    for (EquivalenceClass r : x.rectangles()) {
      EquivalenceClass one = _one.rectangles().iterator().next();
      assert (one != null);
      Collection<EquivalenceClass> divided = one.subtract(r);
      assert (divided != null);
      divided.remove(r);
      List<EquivalenceClass> negated = new ArrayList<>(divided);
      GeometricSpace space = new GeometricSpace(negated);
      acc = or(acc, space);
    }
    return acc;
  }

  EquivalenceClass fullSpace() {
    return new EquivalenceClass(_fullSpace);
  }

  public HeaderSpace example(EquivalenceClass counterExample) {
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

    Integer idx;

    TcpFlags flags = new TcpFlags();
    idx = _fieldIndex.get(PacketField.TCPACK);
    if (idx != null) {
      flags.setAck(bounds[2 * idx] == 1);
    }
    idx = _fieldIndex.get(PacketField.TCPCWR);
    if (idx != null) {
      flags.setCwr(bounds[2 * idx] == 1);
    }
    idx = _fieldIndex.get(PacketField.TCPECE);
    if (idx != null) {
      flags.setEce(bounds[2 * idx] == 1);
    }
    idx = _fieldIndex.get(PacketField.TCPFIN);
    if (idx != null) {
      flags.setFin(bounds[2 * idx] == 1);
    }
    idx = _fieldIndex.get(PacketField.TCPURG);
    if (idx != null) {
      flags.setUrg(bounds[2 * idx] == 1);
    }
    idx = _fieldIndex.get(PacketField.TCPRST);
    if (idx != null) {
      flags.setRst(bounds[2 * idx] == 1);
    }
    idx = _fieldIndex.get(PacketField.TCPPSH);
    if (idx != null) {
      flags.setPsh(bounds[2 * idx] == 1);
    }
    idx = _fieldIndex.get(PacketField.TCPSYN);
    if (idx != null) {
      flags.setSyn(bounds[2 * idx] == 1);
    }

    idx = _fieldIndex.get(PacketField.DSTIP);
    if (idx != null) {
      dstIps.add(new IpWildcard(new Ip(bounds[2 * idx])));
    }
    idx = _fieldIndex.get(PacketField.SRCIP);
    if (idx != null) {
      srcIps.add(new IpWildcard(new Ip(bounds[2 * idx])));
    }
    idx = _fieldIndex.get(PacketField.DSTPORT);
    if (idx != null) {
      dstPorts.add(new SubRange((int) bounds[2 * idx], (int) bounds[2 * idx + 1] - 1));
    }
    idx = _fieldIndex.get(PacketField.SRCPORT);
    if (idx != null) {
      srcPorts.add(new SubRange((int) bounds[2 * idx], (int) bounds[2 * idx + 1] - 1));
    }
    idx = _fieldIndex.get(PacketField.ICMPTYPE);
    if (idx != null) {
      icmpTypes.add(new SubRange((int) bounds[2 * idx], (int) bounds[2 * idx + 1] - 1));
    }
    idx = _fieldIndex.get(PacketField.ICMPCODE);
    if (idx != null) {
      icmpCodes.add(new SubRange((int) bounds[2 * idx], (int) bounds[2 * idx + 1] - 1));
    }
    idx = _fieldIndex.get(PacketField.IPPROTO);
    if (idx != null) {
      ipProtos.add(IpProtocol.fromNumber((int) bounds[2 * idx]));
    }

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

  public GeometricSpace fromHeaderSpace(HeaderSpace h) {
    GeometricSpace acc = one();

    SortedSet<IpWildcard> allDstIps = new TreeSet<>(h.getDstIps());
    allDstIps.addAll(h.getSrcOrDstIps());
    if (!allDstIps.isEmpty() && _fields.contains(PacketField.DSTIP)) {
      GeometricSpace dstIps = zero();
      for (IpWildcard wc : allDstIps) {
        Prefix p = wc.toPrefix();
        long start = p.getStartIp().asLong();
        long end = p.getEndIp().asLong() + 1;
        EquivalenceClass r = fullSpace();
        int index = _fieldIndex.get(PacketField.DSTIP);
        r.getBounds()[2 * index] = start;
        r.getBounds()[2 * index + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        dstIps = or(dstIps, space);
      }
      acc = and(acc, dstIps);
    }

    SortedSet<IpWildcard> allSrcIps = new TreeSet<>(h.getSrcIps());
    allSrcIps.addAll(h.getSrcOrDstIps());
    if (!allSrcIps.isEmpty() && _fields.contains(PacketField.SRCIP)) {
      GeometricSpace srcIps = zero();
      for (IpWildcard wc : allSrcIps) {
        Prefix p = wc.toPrefix();
        long start = p.getStartIp().asLong();
        long end = p.getEndIp().asLong() + 1;
        EquivalenceClass r = fullSpace();
        int index = _fieldIndex.get(PacketField.SRCIP);
        r.getBounds()[2 * index] = start;
        r.getBounds()[2 * index + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        srcIps = or(srcIps, space);
      }
      acc = and(acc, srcIps);
    }

    SortedSet<SubRange> allDstPorts = new TreeSet<>(h.getDstPorts());
    allDstPorts.addAll(h.getSrcOrDstPorts());
    if (!allDstPorts.isEmpty() && _fields.contains(PacketField.DSTPORT)) {
      GeometricSpace dstPorts = zero();
      for (SubRange sr : allDstPorts) {
        long start = sr.getStart();
        long end = sr.getEnd() + 1;
        EquivalenceClass r = fullSpace();
        int index = _fieldIndex.get(PacketField.DSTPORT);
        r.getBounds()[2 * index] = start;
        r.getBounds()[2 * index + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        dstPorts = or(dstPorts, space);
      }
      acc = and(acc, dstPorts);
    }

    SortedSet<SubRange> allSrcPorts = new TreeSet<>(h.getSrcPorts());
    allSrcPorts.addAll(h.getSrcOrDstPorts());
    if (!allSrcPorts.isEmpty() && _fields.contains(PacketField.SRCPORT)) {
      GeometricSpace srcPorts = zero();
      for (SubRange sr : allSrcPorts) {
        long start = sr.getStart();
        long end = sr.getEnd() + 1;
        EquivalenceClass r = fullSpace();
        int index = _fieldIndex.get(PacketField.SRCPORT);
        r.getBounds()[2 * index] = start;
        r.getBounds()[2 * index + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        srcPorts = or(srcPorts, space);
      }
      acc = and(acc, srcPorts);
    }

    if (!h.getIpProtocols().isEmpty() && _fields.contains(PacketField.IPPROTO)) {
      GeometricSpace ipProtos = zero();
      for (IpProtocol proto : h.getIpProtocols()) {
        long start = proto.number();
        long end = proto.number() + 1;
        EquivalenceClass r = fullSpace();
        int index = _fieldIndex.get(PacketField.IPPROTO);
        r.getBounds()[2 * index] = start;
        r.getBounds()[2 * index + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        ipProtos = or(ipProtos, space);
      }
      acc = and(acc, ipProtos);
    }

    if (!h.getIcmpTypes().isEmpty() && _fields.contains(PacketField.ICMPTYPE)) {
      GeometricSpace icmpTypes = zero();
      for (SubRange sr : h.getIcmpTypes()) {
        long start = sr.getStart();
        long end = sr.getEnd() + 1;
        EquivalenceClass r = fullSpace();
        int index = _fieldIndex.get(PacketField.ICMPTYPE);
        r.getBounds()[2 * index] = start;
        r.getBounds()[2 * index + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        icmpTypes = or(icmpTypes, space);
      }
      acc = and(acc, icmpTypes);
    }

    if (!h.getIcmpCodes().isEmpty() && _fields.contains(PacketField.ICMPCODE)) {
      GeometricSpace icmpCodes = zero();
      for (SubRange sr : h.getIcmpCodes()) {
        long start = sr.getStart();
        long end = sr.getEnd() + 1;
        EquivalenceClass r = fullSpace();
        int index = _fieldIndex.get(PacketField.ICMPCODE);
        r.getBounds()[2 * index] = start;
        r.getBounds()[2 * index + 1] = end;
        GeometricSpace space = GeometricSpace.singleton(r);
        icmpCodes = or(icmpCodes, space);
      }
      acc = and(acc, icmpCodes);
    }

    return acc;
  }

  GeometricSpace fromAcl(IpAccessListLine aclLine) {
    return fromHeaderSpace(aclLine);
  }
}

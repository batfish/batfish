package org.batfish.symbolic.bdd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.symbolic.AstVisitor;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.OspfType;

public class BDDUtils {

  public static Set<Integer> findAllLocalPrefs(Graph g) {
    Set<Integer> prefs = new HashSet<>();
    AstVisitor v = new AstVisitor();
    v.visit(
        g.getConfigurations().values(),
        stmt -> {
          if (stmt instanceof SetLocalPreference) {
            SetLocalPreference slp = (SetLocalPreference) stmt;
            IntExpr ie = slp.getLocalPreference();
            if (ie instanceof LiteralInt) {
              LiteralInt l = (LiteralInt) ie;
              prefs.add(l.getValue());
            }
          }
        },
        expr -> {});
    return prefs;
  }

  public static Set<Long> findAllSetMetrics(Graph g) {
    Set<Long> meds = new HashSet<>();
    AstVisitor v = new AstVisitor();
    v.visit(
        g.getConfigurations().values(),
        stmt -> {
          if (stmt instanceof SetMetric) {
            SetMetric sm = (SetMetric) stmt;
            LongExpr le = sm.getMetric();
            if (le instanceof LiteralLong) {
              LiteralLong ll = (LiteralLong) le;
              meds.add(ll.getValue());
            }
          }
        },
        expr -> {});
    return meds;
  }

  public static Set<Long> findAllMeds(Graph g) {
    return findAllSetMetrics(g);
  }

  public static Set<Long> findAllAdminDistances(Graph g) {
    Set<Long> ads = findAllSetMetrics(g);
    for (Configuration conf : g.getConfigurations().values()) {
      ConfigurationFormat format = conf.getConfigurationFormat();
      try {
        ads.add((long) RoutingProtocol.CONNECTED.getDefaultAdministrativeCost(format));
        ads.add((long) RoutingProtocol.STATIC.getDefaultAdministrativeCost(format));
        ads.add((long) RoutingProtocol.OSPF.getDefaultAdministrativeCost(format));
        ads.add((long) RoutingProtocol.BGP.getDefaultAdministrativeCost(format));
        ads.add((long) RoutingProtocol.IBGP.getDefaultAdministrativeCost(format));
      } catch (Exception ignored) {

      }
      for (Vrf vrf : conf.getVrfs().values()) {
        for (GeneratedRoute gr : vrf.getGeneratedRoutes()) {
          ads.add((long) gr.getAdministrativeCost());
        }
      }
    }
    return ads;
  }

  public static Set<Ip> findAllNextHopIps(Graph g) {
    Set<Ip> ips = new HashSet<>();
    AstVisitor v = new AstVisitor();
    v.visit(
        g.getConfigurations().values(),
        stmt -> {
          if (stmt instanceof SetNextHop) {
            SetNextHop s = (SetNextHop) stmt;
            NextHopExpr e = s.getExpr();
            if (e instanceof IpNextHop) {
              IpNextHop x = (IpNextHop) e;
              ips.addAll(x.getIps());
            }
          }
        },
        expr -> {});
    for (Configuration conf : g.getConfigurations().values()) {
      for (Interface iface : conf.getInterfaces().values()) {
        ips.add(iface.getAddress().getIp());
      }
    }
    return ips;
  }

  public static int numBits(long size) {
    double log = Math.log((double) size);
    double base = Math.log((double) 2);
    if (size == 0) {
      return 0;
    } else {
      return (int) Math.ceil(log / base);
    }
  }

  public static BDD firstBitsEqual(BDDFactory factory, BDD[] bits, Ip ip, int length) {
    long b = ip.asLong();
    BDD acc = factory.one();
    for (int i = length - 1; i >= 0; i--) {
      boolean res = Ip.getBitAtPosition(b, i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc.andWith(bits[i].not());
      }
    }
    return acc;
  }

  public static BDD firstBitsEqual(BDDFactory factory, BDD[] bits, Prefix p, int length) {
    return firstBitsEqual(factory, bits, p.getStartIp(), length);
  }

  public static BDD prefixRangeToBdd(BDDFactory factory, BDDRoute record, PrefixRange range) {
    Prefix p = range.getPrefix();
    SubRange r = range.getLengthRange();
    int len = p.getPrefixLength();
    int lower = r.getStart();
    int upper = r.getEnd();
    BDD lowerBitsMatch = firstBitsEqual(factory, record.getPrefix().getBitvec(), p, len);
    BDD acc = factory.zero();
    if (lower == 0 && upper == 32) {
      acc = factory.one();
    } else {
      for (int i = lower; i <= upper; i++) {
        BDD equalLen = record.getPrefixLength().value(i);
        acc.orWith(equalLen);
      }
    }
    return acc.andWith(lowerBitsMatch);
  }

  public static BDD prefixToBdd(BDDFactory factory, BDDRoute record, Prefix p) {
    BDD bitsMatch = firstBitsEqual(factory, record.getPrefix().getBitvec(), p, 32);
    BDD correctLen = record.getPrefixLength().value(p.getPrefixLength());
    return bitsMatch.andWith(correctLen);
  }

  public static BDD headerspaceToBdd(BDDNetFactory factory, HeaderSpace hs) {
    BDDPacket pkt = factory.packetVariables();
    MatchHeaderSpace mhs = new MatchHeaderSpace(hs);
    AclLineMatchExprToBDD converter = new AclLineMatchExprToBDD(factory.getFactory(), pkt);
    return converter.toBDD(mhs);
  }

  /*
   * A useful function for debugging BDDs. It translates a BDD to the
   * graphviz dot format. The BDDNetFactory parameter is used to translate
   * the BDD variable indices back into meaningful names.
   */
  public static String dot(BDDNetFactory netFactory, BDD bdd) {
    StringBuilder sb = new StringBuilder();
    sb.append("digraph G {\n");
    if (!bdd.isOne()) {
      sb.append("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];\n");
    }
    if (!bdd.isZero()) {
      sb.append("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];\n");
    }
    dotRec(netFactory, sb, bdd, new HashSet<>());
    sb.append("}");
    return sb.toString();
  }

  private static Integer dotId(BDD bdd) {
    if (bdd.isZero()) {
      return 0;
    }
    if (bdd.isOne()) {
      return 1;
    }
    return bdd.hashCode() + 2;
  }

  private static void dotRec(
      BDDNetFactory netFactory, StringBuilder sb, BDD bdd, Set<BDD> visited) {
    if (bdd.isOne() || bdd.isZero() || visited.contains(bdd)) {
      return;
    }
    int val = dotId(bdd);
    int valLow = dotId(bdd.low());
    int valHigh = dotId(bdd.high());
    String rname = netFactory.routeVariables().getBitNames().get(bdd.var());
    String pname = netFactory.packetVariables().getBitNames().get(bdd.var());
    String name = (rname == null ? pname : rname);
    sb.append(val).append(" [label=\"").append(name).append("\"]\n");
    sb.append(val).append(" -> ").append(valLow).append("[style=dotted]\n");
    sb.append(val).append(" -> ").append(valHigh).append("[style=filled]\n");
    visited.add(bdd);
    dotRec(netFactory, sb, bdd.low(), visited);
    dotRec(netFactory, sb, bdd.high(), visited);
  }

  /*
   * Returns many satisfying assignments (examples) for the given BDD.
   * Specifically, it returns one assignment per path through the BDD
   * to the "true" terminal node. If there are "don't care" bits, it
   * will currently set them to false for each example, unless the
   * expand flag is true.
   */
  public static List<SatAssignment> allSat(
      BDDNetFactory netFactory, BDD x, boolean expandPrefix) {
    List<SatAssignment> entries = new ArrayList<>();
    @SuppressWarnings("unchecked")
    List<byte[]> assignments = (List<byte[]>) x.allsat();
    for (byte[] variables : assignments) {
      List<byte[]> concreteAssignments = new ArrayList<>();
      List<Integer> dontCare = new ArrayList<>();

      int startDstIp = netFactory.getIndexDstIp();
      int endDstIp = startDstIp + netFactory.getNumBitsDstIp();
      for (int i = startDstIp; i < endDstIp; i++) {
        byte b = variables[i];
        if (b == -1) {
          if (expandPrefix) {
            dontCare.add(i);
          }
        }
      }
      int startPfxLen = netFactory.getIndexPrefixLen();
      int endPfxLen = startDstIp + netFactory.getNumBitsPrefixLen();
      for (int i = startPfxLen; i < endPfxLen; i++) {
        byte b = variables[i];
        if (b == -1) {
          if (expandPrefix) {
            dontCare.add(i);
          }
        }
      }
      expand(dontCare, 0, variables, concreteAssignments);

      if (dontCare.isEmpty()) {
        concreteAssignments.add(variables);
      }

      for (byte[] concreteAssignment : concreteAssignments) {
        SatAssignment entry = sat(netFactory, concreteAssignment);
        entries.add(entry);
      }
    }
    return entries;
  }

  private static void expand(List<Integer> dontCareBits, int i, byte[] input, List<byte[]> output) {
    if (i >= dontCareBits.size()) {
      output.add(input);
      return;
    }
    int idx = dontCareBits.get(i);
    byte[] newInput1 = Arrays.copyOf(input, input.length);
    byte[] newInput2 = Arrays.copyOf(input, input.length);
    newInput1[idx] = 0;
    newInput2[idx] = 1;
    expand(dontCareBits, i + 1, newInput1, output);
    expand(dontCareBits, i + 1, newInput2, output);
  }

  /*
   * Returns a single satisfying assignment (example) for the given BDD.
   * This is useful for producing a counterexample to a property, such
   * as a packet matched by one ACL but not another.
   */
  @Nullable
  public static SatAssignment satOne(BDDNetFactory netFactory, BDD x) {
    List<SatAssignment> assigments = allSat(netFactory, x.satOne(), false);
    if (assigments.isEmpty()) {
      return null;
    }
    return assigments.get(0);
  }

  private static int byIndex(int index, int numBits, int i) {
    return 1 << ((numBits - 1) - i + index);
  }

  private static long byIndexL(int index, int numBits, int i) {
    return 1L << ((numBits - 1) - i + index);
  }

  private static SatAssignment sat(BDDNetFactory nf, byte[] variables) {
    int ipProtocol = 0;
    long dstIp = 0;
    long srcIp = 0;
    int prefixLen = 0;
    int proto = 0;
    int dstRouter = 0;
    int srcRouter = 0;
    int dstPort = 0;
    int srcPort = 0;
    int icmpCode = 0;
    int icmpType = 0;
    int adminDist = 0;
    int localPref = 0;
    int med = 0;
    int metric = 0;
    int ospfMetric = 0;
    int nextHop = 0;
    List<CommunityVar> cvars = new ArrayList<>();
    TcpFlags.Builder tcpFlags = TcpFlags.builder();
    for (int i = 0; i < variables.length; i++) {
      byte var = variables[i];
      boolean isTrue = (var == 1);
      if (isTrue) {
        if (i >= nf.getIndexIpProto() && i < nf.getIndexIpProto() + nf.getNumBitsIpProto()) {
          ipProtocol += byIndex(nf.getIndexIpProto(), nf.getNumBitsIpProto(), i);
        } else if (nf.getConfig().getKeepProtocol()
            && i >= nf.getIndexRoutingProtocol()
            && i < nf.getIndexRoutingProtocol() + nf.getNumBitsRoutingProtocol()) {
          proto += byIndex(nf.getIndexRoutingProtocol(), nf.getNumBitsRoutingProtocol(), i);
        } else if (i >= nf.getIndexPrefixLen()
            && i < nf.getIndexPrefixLen() + nf.getNumBitsPrefixLen()) {
          prefixLen += byIndex(nf.getIndexPrefixLen(), nf.getNumBitsPrefixLen(), i);
        } else if (i >= nf.getIndexAdminDist()
            && i < nf.getIndexAdminDist() + nf.getNumBitsAdminDist()) {
          adminDist += byIndex(nf.getIndexAdminDist(), nf.getNumBitsAdminDist(), i);
        } else if (i >= nf.getIndexLocalPref()
            && i < nf.getIndexLocalPref() + nf.getNumBitsLocalPref()) {
          localPref += byIndex(nf.getIndexLocalPref(), nf.getNumBitsLocalPref(), i);
        } else if (i >= nf.getIndexMed() && i < nf.getIndexMed() + nf.getNumBitsMed()) {
          med += byIndex(nf.getIndexMed(), nf.getNumBitsMed(), i);
        } else if (i >= nf.getIndexMetric() && i < nf.getIndexMetric() + nf.getNumBitsMetric()) {
          metric += byIndex(nf.getIndexMetric(), nf.getNumBitsMetric(), i);
        } else if (i >= nf.getIndexOspfMetric()
            && i < nf.getIndexOspfMetric() + nf.getNumBitsOspfMetric()) {
          ospfMetric += byIndex(nf.getIndexOspfMetric(), nf.getNumBitsOspfMetric(), i);
        } else if (i >= nf.getIndexCommunities()
            && i < nf.getIndexCommunities() + nf.getNumBitsCommunities()) {
          int j = i - nf.getIndexCommunities();
          cvars.add(nf.getAllCommunities().get(j));
        } else if (i >= nf.getIndexNextHopIp()
            && i < nf.getIndexNextHopIp() + nf.getNumBitsNextHopIp()) {
          nextHop += byIndex(nf.getIndexNextHopIp(), nf.getNumBitsNextHopIp(), i);
        } else if (i >= nf.getIndexDstIp() && i < nf.getIndexDstIp() + nf.getNumBitsDstIp()) {
          dstIp += byIndexL(nf.getIndexDstIp(), nf.getNumBitsDstIp(), i);
        } else if (i >= nf.getIndexSrcIp() && i < nf.getIndexSrcIp() + nf.getNumBitsSrcIp()) {
          srcIp += byIndexL(nf.getIndexSrcIp(), nf.getNumBitsSrcIp(), i);
        } else if (i >= nf.getIndexDstPort() && i < nf.getIndexDstPort() + nf.getNumBitsDstPort()) {
          dstPort += byIndex(nf.getIndexDstPort(), nf.getNumBitsDstPort(), i);
        } else if (i >= nf.getIndexSrcPort() && i < nf.getIndexSrcPort() + nf.getNumBitsSrcPort()) {
          srcPort += byIndex(nf.getIndexSrcPort(), nf.getNumBitsSrcPort(), i);
        } else if (i >= nf.getIndexIcmpCode()
            && i < nf.getIndexIcmpCode() + nf.getNumBitsIcmpCode()) {
          icmpCode += byIndex(nf.getIndexIcmpCode(), nf.getNumBitsIcmpCode(), i);
        } else if (i >= nf.getIndexIcmpType()
            && i < nf.getIndexIcmpType() + nf.getNumBitsIcmpType()) {
          icmpType += byIndex(nf.getIndexIcmpType(), nf.getNumBitsIcmpType(), i);
        } else if (i >= nf.getIndexTcpFlags()
            && i < nf.getIndexTcpFlags() + nf.getNumBitsTcpFlags()) {
          int j = i - nf.getIndexTcpFlags();
          switch (j) {
            case 0:
              tcpFlags.setAck(true);
              break;
            case 1:
              tcpFlags.setCwr(true);
              break;
            case 2:
              tcpFlags.setEce(true);
              break;
            case 3:
              tcpFlags.setFin(true);
              break;
            case 4:
              tcpFlags.setPsh(true);
              break;
            case 5:
              tcpFlags.setRst(true);
              break;
            case 6:
              tcpFlags.setSyn(true);
              break;
            case 7:
              tcpFlags.setUrg(true);
              break;
            default:
              break;
          }
        } else if (nf.getConfig().getKeepRouters()
            && i >= nf.getIndexDstRouter()
            && i < nf.getIndexDstRouter() + nf.getNumBitsRouters()) {
          dstRouter += byIndex(nf.getIndexDstRouter(), nf.getNumBitsRouters(), i);
        } else if (nf.getConfig().getKeepRouters()
            && i >= nf.getIndexSrcRouter()
            && i < nf.getIndexSrcRouter() + nf.getNumBitsRouters()) {
          srcRouter += byIndex(nf.getIndexSrcRouter(), nf.getNumBitsRouters(), i);
        }
      }
    }

    SatAssignment assignment = new SatAssignment();
    assignment.setIpProtocol(IpProtocol.fromNumber(ipProtocol));
    assignment.setDstIp(new Ip(dstIp));
    assignment.setSrcIp(new Ip(srcIp));
    assignment.setDstPort(dstPort);
    assignment.setSrcPort(srcPort);
    assignment.setIcmpCode(icmpCode);
    assignment.setIcmpType(icmpType);
    assignment.setTcpFlags(tcpFlags.build());
    assignment.setDstRouter(nf.getAllRouters().isEmpty() ? null : nf.getRouter(dstRouter));
    assignment.setSrcRouter(nf.getAllRouters().isEmpty() ? null : nf.getRouter(srcRouter));
    assignment.setRoutingProtocol(nf.getAllProtos().get(proto));
    assignment.setPrefixLen(prefixLen);
    assignment.setAdminDist((nf.getAllAdminDistances().get(adminDist)).intValue());
    assignment.setLocalPref(nf.getAllLocalPrefs().get(localPref));
    assignment.setMed((nf.getAllMeds().get(med)).intValue());
    assignment.setMetric(metric);
    assignment.setOspfMetric(OspfType.values()[ospfMetric]);
    assignment.setCommunities(cvars);
    assignment.setNextHopIp(nf.getAllIps().get(nextHop));
    return assignment;
  }

  // TODO: this is very inefficient without access to the BDD library
  public static List<SatAssignment> bestRoutes(BDDNetFactory nf, BDD input) {
    Set<BDD> processed = new HashSet<>();
    List<SatAssignment> result = new ArrayList<>();
    bestRoutesAux(nf, input, 0, 0, 0, 0, 0, processed, result);
    return result;
  }

  private static void bestRoutesAux(
      BDDNetFactory nf,
      BDD x,
      int adminDist,
      int localPref,
      int med,
      int metric,
      int ospfMetric,
      Set<BDD> processed,
      List<SatAssignment> result) {

    if (x.isZero()) {
      return;
    }

    int var = x.var();
    boolean beforeDecisionBits = (var < nf.getIndexAdminDist());
    boolean afterDecisionBits = (var > nf.getIndexOspfMetric() + nf.getNumBitsOspfMetric());
    if (x.isOne() || afterDecisionBits) {
      SatAssignment assignment = new SatAssignment();
      assignment.setAdminDist((nf.getAllAdminDistances().get(adminDist)).intValue());
      assignment.setLocalPref(nf.getAllLocalPrefs().get(localPref));
      assignment.setMetric(metric);
      assignment.setMed((nf.getAllMeds().get(med)).intValue());
      assignment.setOspfMetric(OspfType.values()[ospfMetric]);
      result.add(assignment);
      return;
    }

    if (processed.contains(x)) {
      return;
    }

    BDD low = x.low();
    bestRoutesAux(nf, low, adminDist, localPref, med, metric, ospfMetric, processed, result);
    if (low.isZero() || beforeDecisionBits) {
      BDD high = x.high();
      if (!beforeDecisionBits) {
        if (var >= nf.getIndexAdminDist()
            && var < nf.getIndexAdminDist() + nf.getNumBitsAdminDist()) {
          adminDist += byIndex(nf.getIndexAdminDist(), nf.getNumBitsAdminDist(), var);
        } else if (var >= nf.getIndexLocalPref()
            && var < nf.getIndexLocalPref() + nf.getNumBitsLocalPref()) {
          localPref += byIndex(nf.getIndexLocalPref(), nf.getNumBitsLocalPref(), var);
        } else if (var >= nf.getIndexMed() && var < nf.getIndexMed() + nf.getNumBitsMed()) {
          med += byIndex(nf.getIndexMed(), nf.getNumBitsMed(), var);
        } else if (var >= nf.getIndexMetric()
            && var < nf.getIndexMetric() + nf.getNumBitsMetric()) {
          metric += byIndex(nf.getIndexMetric(), nf.getNumBitsMetric(), var);
        } else if (var >= nf.getIndexOspfMetric()
            && var < nf.getIndexOspfMetric() + nf.getNumBitsOspfMetric()) {
          ospfMetric += byIndex(nf.getIndexOspfMetric(), nf.getNumBitsOspfMetric(), var);
        }
      }
      bestRoutesAux(nf, high, adminDist, localPref, med, metric, ospfMetric, processed, result);
    }

    if (beforeDecisionBits) {
      processed.add(x);
    }
  }

  public static BDD flowToBdd(BDDNetFactory factory, Flow flow) {
    BDDPacket pkt = factory.packetVariables();
    BDD dstIp = pkt.getDstIp().value(flow.getDstIp().asLong());
    BDD srcIp = pkt.getSrcIp().value(flow.getSrcIp().asLong());
    BDD dstPort = pkt.getDstPort().value(flow.getDstPort());
    BDD srcPort = pkt.getSrcPort().value(flow.getSrcPort());
    BDD proto = pkt.getIpProtocol().value(flow.getIpProtocol().number());
    BDD icmpCode = pkt.getIcmpCode().value(flow.getIcmpCode());
    BDD icmpType = pkt.getIcmpType().value(flow.getIcmpType());
    BDD tcpAck = flow.getTcpFlagsAck() == 0 ? pkt.getTcpAck().not() : pkt.getTcpAck();
    BDD tcpCwr = flow.getTcpFlagsCwr() == 0 ? pkt.getTcpCwr().not() : pkt.getTcpCwr();
    BDD tcpEce = flow.getTcpFlagsEce() == 0 ? pkt.getTcpEce().not() : pkt.getTcpEce();
    BDD tcpPsh = flow.getTcpFlagsPsh() == 0 ? pkt.getTcpPsh().not() : pkt.getTcpPsh();
    BDD tcpRst = flow.getTcpFlagsRst() == 0 ? pkt.getTcpRst().not() : pkt.getTcpRst();
    BDD tcpSyn = flow.getTcpFlagsSyn() == 0 ? pkt.getTcpSyn().not() : pkt.getTcpSyn();
    BDD tcpUrg = flow.getTcpFlagsUrg() == 0 ? pkt.getTcpUrg().not() : pkt.getTcpUrg();
    BDD tcpFin = flow.getTcpFlagsFin() == 0 ? pkt.getTcpFin().not() : pkt.getTcpFin();
    return dstIp
        .andWith(srcIp)
        .andWith(dstPort)
        .andWith(srcPort)
        .andWith(proto)
        .andWith(icmpCode)
        .andWith(icmpType)
        .andWith(tcpAck)
        .andWith(tcpCwr)
        .andWith(tcpEce)
        .andWith(tcpPsh)
        .andWith(tcpRst)
        .andWith(tcpSyn)
        .andWith(tcpUrg)
        .andWith(tcpFin);
  }

  /** Create a new {@link BDDFactory} object with {@param numVariables} boolean variables. */
  public static BDDFactory bddFactory(int numVariables) {
    BDDFactory factory = JFactory.init(10000, 1000);
    factory.disableReorder();
    factory.setCacheRatio(64);
    factory.setVarNum(numVariables); // reserve 32 1-bit variables
    return factory;
  }
}

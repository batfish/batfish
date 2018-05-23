package org.batfish.symbolic.bdd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.symbolic.AstVisitor;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.OspfType;
import org.batfish.symbolic.Protocol;

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

  public static int numBits(int size) {
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
  public String dot(BDDNetFactory netFactory, BDD bdd) {
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

  private Integer dotId(BDD bdd) {
    if (bdd.isZero()) {
      return 0;
    }
    if (bdd.isOne()) {
      return 1;
    }
    return bdd.hashCode() + 2;
  }

  private void dotRec(BDDNetFactory netFactory, StringBuilder sb, BDD bdd, Set<BDD> visited) {
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
   * will currently set them to false for each example.
   */
  public static List<SatAssignment> allSat(BDDNetFactory netFactory, BDD x) {
    List<SatAssignment> entries = new ArrayList<>();
    @SuppressWarnings("unchecked")
    List<byte[]> assignments = (List<byte[]>) x.allsat();
    for (byte[] variables : assignments) {
      SatAssignment entry = sat(netFactory, variables);
      entries.add(entry);
    }
    return entries;
  }

  /*
   * Returns a single satisfying assignment (example) for the given BDD.
   * This is useful for producing a counterexample to a property, such
   * as a packet matched by one ACL but not another.
   */
  @Nullable
  public static SatAssignment satOne(BDDNetFactory netFactory, BDD x) {
    List<SatAssignment> assigments = allSat(netFactory, x.satOne());
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
          proto += (1 << i - nf.getIndexRoutingProtocol());
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
    assignment.setRoutingProtocol(Protocol.toRoutingProtocol(nf.getAllProtos().get(proto)));
    assignment.setPrefixLen(prefixLen);
    assignment.setAdminDist(adminDist);
    assignment.setLocalPref(
        nf.getAllLocalPrefs().isEmpty() ? 100 : nf.getAllLocalPrefs().get(localPref));
    assignment.setMed(med);
    assignment.setMetric(metric);
    assignment.setOspfMetric(OspfType.values()[ospfMetric]);
    assignment.setCommunities(cvars);
    return assignment;
  }
}

package org.batfish.symbolic.smt;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.datamodel.questions.smt.EnvironmentType;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.datamodel.questions.smt.HeaderQuestion;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.CommunityVar.Type;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.abstraction.Abstraction;
import org.batfish.symbolic.abstraction.EquivalenceClass;
import org.batfish.symbolic.answers.SmtDeterminismAnswerElement;
import org.batfish.symbolic.answers.SmtManyAnswerElement;
import org.batfish.symbolic.answers.SmtOneAnswerElement;
import org.batfish.symbolic.answers.SmtReachabilityAnswerElement;
import org.batfish.symbolic.collections.Table2;
import org.batfish.symbolic.utils.PathRegexes;
import org.batfish.symbolic.utils.PatternUtils;
import org.batfish.symbolic.utils.Tuple;

/**
 * A collection of functions to checks if various properties hold in the network. The general flow
 * is to create a new encoder object for the network, instrument additional properties on top of the
 * model, and then assert the negation of the property of interest.
 *
 * @author Ryan Beckett
 */
public class PropertyChecker {

  /*
   * Compute the forwarding behavior for the network. This adds no additional
   * constraints on top of the base network encoding. Forwarding will be
   * determined only for a particular network environment, failure scenario,
   * and data plane packet.
   */
  public static AnswerElement computeForwarding(IBatfish batfish, HeaderQuestion q) {
    Encoder encoder = new Encoder(batfish, q);
    encoder.computeEncoding();
    addEnvironmentConstraints(encoder, q.getBaseEnvironmentType());
    VerificationResult result = encoder.verify().getFirst();
    // result.debug(encoder.getMainSlice(), true, null);
    SmtOneAnswerElement answer = new SmtOneAnswerElement();
    answer.setResult(result);
    return answer;
  }

  /*
   * Find the collection of relevant destination interfaces
   */
  private static Set<GraphEdge> findFinalInterfaces(Graph g, PathRegexes p) {
    Set<GraphEdge> edges = new HashSet<>();
    edges.addAll(PatternUtils.findMatchingEdges(g, p));
    return edges;
  }

  /*
   * From the destination interfaces, infer the relevant headerspace.
   * E.g., what range of destination IP is interesting for the query
   */
  private static void inferDestinationHeaderSpace(
      Graph g, Collection<GraphEdge> destPorts, HeaderLocationQuestion q) {
    // Infer relevant destination IP headerspace from interfaces
    if (q.getHeaderSpace().getDstIps().isEmpty()) {
      for (GraphEdge ge : destPorts) {
        // If there is an external interface, then
        // it can be any prefix, so we leave it unconstrained
        if (g.getEbgpNeighbors().containsKey(ge)) {
          q.getHeaderSpace().getDstIps().clear();
          q.getHeaderSpace().getNotDstIps().clear();
          break;
        }
        // If we don't know what is on the other end
        if (ge.getPeer() == null) {
          Prefix pfx = ge.getStart().getPrefix().getNetworkPrefix();
          IpWildcard dst = new IpWildcard(pfx);
          q.getHeaderSpace().getDstIps().add(dst);
        } else {
          // If host, add the subnet but not the neighbor's address
          if (g.isHost(ge.getRouter())) {
            Prefix pfx = ge.getStart().getPrefix().getNetworkPrefix();
            IpWildcard dst = new IpWildcard(pfx);
            q.getHeaderSpace().getDstIps().add(dst);
            Ip ip = ge.getEnd().getPrefix().getAddress();
            IpWildcard dst2 = new IpWildcard(ip);
            q.getHeaderSpace().getNotDstIps().add(dst2);
          } else {
            // Otherwise, we add the exact address
            Ip ip = ge.getStart().getPrefix().getAddress();
            IpWildcard dst = new IpWildcard(ip);
            q.getHeaderSpace().getDstIps().add(dst);
          }
        }
      }
    }
  }

  /*
   * Lookup the value of a variable from a z3 model
   */
  private static String evaluate(Model m, Expr e) {
    Expr val = m.evaluate(e, true);
    return val.toString();
  }

  private static boolean isTrue(Model m, Expr e) {
    return "true".equals(evaluate(m, e));
  }

  private static boolean isFalse(Model m, Expr e) {
    return "false".equals(evaluate(m, e));
  }

  private static int intVal(Model m, Expr e) {
    return Integer.parseInt(evaluate(m, e));
  }

  private static Ip ipVal(Model m, Expr e) {
    String s = evaluate(m, e);
    return new Ip(Long.parseLong(evaluate(m, e)));
  }

  /*
   * Constraint that encodes if two symbolic records are equal
   */
  private static BoolExpr equal(
      Encoder e, Configuration conf, SymbolicRecord r1, SymbolicRecord r2) {
    EncoderSlice main = e.getMainSlice();
    BoolExpr eq = main.equal(conf, Protocol.CONNECTED, r1, r2, null, true);
    BoolExpr samePermitted = e.mkEq(r1.getPermitted(), r2.getPermitted());
    return e.mkAnd(eq, samePermitted);
  }

  /*
   * Reconstruct the packet z3 found in the counterexample
   */
  private static Flow buildFlow(Model m, SymbolicPacket pkt, String router) {
    Ip srcIp = ipVal(m, pkt.getSrcIp());
    Ip dstIp = ipVal(m, pkt.getDstIp());
    Integer srcPort = intVal(m, pkt.getSrcPort());
    Integer dstPort = intVal(m, pkt.getDstPort());
    IpProtocol ipProtocol = IpProtocol.fromNumber(intVal(m, pkt.getIpProtocol()));
    Integer icmpType = intVal(m, pkt.getIcmpType());
    Integer icmpCode = intVal(m, pkt.getIcmpCode());
    Integer tcpFlagsCwr = isTrue(m, pkt.getTcpCwr()) ? 0 : 1;
    Integer tcpFlagsEce = isTrue(m, pkt.getTcpEce()) ? 0 : 1;
    Integer tcpFlagsUrg = isTrue(m, pkt.getTcpUrg()) ? 0 : 1;
    Integer tcpFlagsAck = isTrue(m, pkt.getTcpAck()) ? 0 : 1;
    Integer tcpFlagsPsh = isTrue(m, pkt.getTcpPsh()) ? 0 : 1;
    Integer tcpFlagsRst = isTrue(m, pkt.getTcpRst()) ? 0 : 1;
    Integer tcpFlagsSyn = isTrue(m, pkt.getTcpSyn()) ? 0 : 1;
    Integer tcpFlagsFin = isTrue(m, pkt.getTcpFin()) ? 0 : 1;

    Flow.Builder b = new Flow.Builder();
    b.setIngressNode(router);
    b.setSrcIp(srcIp);
    b.setDstIp(dstIp);
    b.setSrcPort(srcPort);
    b.setDstPort(dstPort);
    b.setIpProtocol(ipProtocol);
    b.setIcmpType(icmpType);
    b.setIcmpCode(icmpCode);
    b.setTcpFlagsCwr(tcpFlagsCwr);
    b.setTcpFlagsEce(tcpFlagsEce);
    b.setTcpFlagsUrg(tcpFlagsUrg);
    b.setTcpFlagsAck(tcpFlagsAck);
    b.setTcpFlagsPsh(tcpFlagsPsh);
    b.setTcpFlagsRst(tcpFlagsRst);
    b.setTcpFlagsSyn(tcpFlagsSyn);
    b.setTcpFlagsFin(tcpFlagsFin);
    b.setTag("SMT");
    return b.build();
  }

  /*
   * Convert an SMT GraphEdge to a Batfish Edge
   */
  private static Edge fromGraphEdge(GraphEdge ge) {
    String w = ge.getRouter();
    String x = ge.getStart() == null ? "none" : ge.getStart().getName();
    String y = ge.getPeer();
    String z = ge.getEnd() == null ? "none" : ge.getEnd().getName();
    return new Edge(w, x, y, z);
  }

  /*
   * From the model, reconstruct the collection of Edges in
   * the graph that have been failed.
   */
  private static SortedSet<Edge> buildFailedLinks(Encoder enc, Model m) {
    Set<GraphEdge> failed = new HashSet<>();
    Graph g = enc.getMainSlice().getGraph();
    g.getEdgeMap()
        .forEach(
            (router, edges) -> {
              for (GraphEdge ge : edges) {
                ArithExpr e = enc.getSymbolicFailures().getFailedVariable(ge);
                assert e != null;
                String s = evaluate(m, e);
                if (!"0".equals(s)) {
                  // Don't add both directions?
                  GraphEdge other = g.getOtherEnd().get(ge);
                  if (other == null || !failed.contains(other)) {
                    failed.add(ge);
                  }
                }
              }
            });
    // Convert to Batfish Edge type
    SortedSet<Edge> failedEdges = new TreeSet<>();
    for (GraphEdge ge : failed) {
      failedEdges.add(fromGraphEdge(ge));
    }
    return failedEdges;
  }

  private static SortedSet<BgpAdvertisement> buildEnvRoutingTable(Encoder enc, Model m) {
    SortedSet<BgpAdvertisement> routes = new TreeSet<>();
    EncoderSlice slice = enc.getMainSlice();
    LogicalGraph lg = slice.getLogicalGraph();
    lg.getEnvironmentVars()
        .forEach(
            (lge, record) -> {
              // If there is an external advertisement
              String s = evaluate(m, record.getPermitted());
              if ("true".equals(s)) {
                // If we actually use it
                GraphEdge ge = lge.getEdge();
                String router = ge.getRouter();
                SymbolicDecisions decisions = slice.getSymbolicDecisions();
                BoolExpr ctrFwd = decisions.getControlForwarding().get(router, ge);
                assert ctrFwd != null;
                s = evaluate(m, ctrFwd);
                if ("true".equals(s)) {
                  SymbolicRecord r = decisions.getBestNeighbor().get(router);
                  SymbolicPacket pkt = slice.getSymbolicPacket();
                  Flow f = buildFlow(m, pkt, router);
                  Prefix pfx = buildPrefix(r, m, f);
                  int pathLength = intVal(m, r.getMetric());

                  // Create dummy information
                  BgpNeighbor n = slice.getGraph().getEbgpNeighbors().get(lge.getEdge());
                  String srcNode = "as" + n.getRemoteAs();
                  Ip zeroIp = new Ip(0);
                  Ip dstIp = n.getLocalIp();

                  // Recover AS path
                  List<SortedSet<Integer>> asSets = new ArrayList<>();
                  for (int i = 0; i < pathLength; i++) {
                    SortedSet<Integer> asSet = new TreeSet<>();
                    asSet.add(-1);
                    asSets.add(asSet);
                  }
                  AsPath path = new AsPath(asSets);

                  // Recover communities
                  SortedSet<Long> communities = new TreeSet<>();
                  r.getCommunities()
                      .forEach(
                          (cvar, expr) -> {
                            if (cvar.getType() == Type.EXACT) {
                              String c = evaluate(m, expr);
                              if ("true".equals(c)) {
                                communities.add(cvar.asLong());
                              }
                            }
                          });

                  BgpAdvertisement adv =
                      new BgpAdvertisement(
                          BgpAdvertisementType.EBGP_RECEIVED,
                          pfx,
                          zeroIp,
                          srcNode,
                          "default",
                          zeroIp,
                          router,
                          "default",
                          dstIp,
                          RoutingProtocol.BGP,
                          OriginType.EGP,
                          100,
                          80,
                          zeroIp,
                          path,
                          communities,
                          null,
                          0);

                  routes.add(adv);
                }
              }
            });
    return routes;
  }

  /*
   * Build an individual flow hop along a path
   */
  private static FlowTraceHop buildFlowTraceHop(Flow f, GraphEdge ge, String route) {
    String node1 = ge.getRouter();
    String int1 = ge.getStart().getName();
    String node2 = ge.getPeer() == null ? "(none)" : ge.getPeer();
    String int2 = ge.getEnd() == null ? "null_interface" : ge.getEnd().getName();
    Edge edge = new Edge(node1, int1, node2, int2);
    SortedSet<String> routes = new TreeSet<>();
    routes.add(route);
    return new FlowTraceHop(edge, routes, null);
  }

  /*
   * Reconstruct the actual route used to forward the packet.
   */
  private static String buildRoute(Prefix pfx, Protocol proto, GraphEdge ge) {
    String type;
    String nhip;
    String nhint;
    if (proto.isConnected()) {
      type = "ConnectedRoute";
      nhip = "AUTO/NONE(-1l)";
      nhint = ge.getStart().getName();
    } else {
      type = StringUtils.capitalize(proto.name().toLowerCase()) + "Route";
      nhip = ge.getStart().getPrefix().getAddress().toString();
      nhint = "dynamic";
    }
    return String.format("%s<%s,nhip:%s,nhint:%s>", type, pfx.getNetworkPrefix(), nhip, nhint);
  }

  /*
   * Create a route from a graph edge
   */
  private static String buildRoute(EncoderSlice slice, Model m, GraphEdge ge) {
    String router = ge.getRouter();
    SymbolicDecisions decisions = slice.getSymbolicDecisions();
    SymbolicRecord r = decisions.getBestNeighbor().get(router);
    SymbolicPacket pkt = slice.getSymbolicPacket();
    Flow f = buildFlow(m, pkt, router);
    Prefix pfx = buildPrefix(r, m, f);
    Protocol proto = buildProcotol(r, m, slice, router);
    return buildRoute(pfx, proto, ge);
  }

  /*
   * Reconstruct the prefix from a symbolic record
   */
  private static Prefix buildPrefix(SymbolicRecord r, Model m, Flow f) {
    Integer pfxLen = intVal(m, r.getPrefixLength());
    return new Prefix(f.getDstIp(), pfxLen);
  }

  /*
   * Reconstruct the protocol from a symbolic record
   */
  private static Protocol buildProcotol(
      SymbolicRecord r, Model m, EncoderSlice slice, String router) {
    Protocol proto;
    if (r.getProtocolHistory().getBitVec() == null) {
      proto = slice.getProtocols().get(router).get(0);
    } else {
      Integer idx = intVal(m, r.getProtocolHistory().getBitVec());
      proto = r.getProtocolHistory().value(idx);
    }
    return proto;
  }

  /*
   * Build flow information for a given hop along a path
   */
  private static Tuple<Flow, FlowTrace> buildFlowTrace(Encoder enc, Model m, String router) {
    EncoderSlice slice = enc.getMainSlice();
    SymbolicPacket pkt = slice.getSymbolicPacket();
    SymbolicDecisions decisions = slice.getSymbolicDecisions();
    Flow f = buildFlow(m, pkt, router);

    SortedSet<String> visited = new TreeSet<>();
    List<FlowTraceHop> hops = new ArrayList<>();

    String current = router;
    while (true) {
      visited.add(current);
      // Get the forwarding variables
      Map<GraphEdge, BoolExpr> dfwd = decisions.getDataForwarding().get(current);
      Map<GraphEdge, BoolExpr> cfwd = decisions.getControlForwarding().get(current);
      Map<GraphEdge, BoolExpr> across = enc.getMainSlice().getForwardsAcross().get(current);
      // Find the route used
      SymbolicRecord r = decisions.getBestNeighbor().get(current);
      Protocol proto = buildProcotol(r, m, slice, current);
      Prefix pfx = buildPrefix(r, m, f);
      // pick the next router
      boolean found = false;
      for (Entry<GraphEdge, BoolExpr> entry : dfwd.entrySet()) {
        GraphEdge ge = entry.getKey();
        BoolExpr dexpr = entry.getValue();
        BoolExpr cexpr = cfwd.get(ge);
        BoolExpr aexpr = across.get(ge);
        String route = buildRoute(pfx, proto, ge);
        if (isTrue(m, dexpr)) {
          hops.add(buildFlowTraceHop(f, ge, route));
          if (ge.getPeer() != null && visited.contains(ge.getPeer())) {
            FlowTrace ft = new FlowTrace(FlowDisposition.LOOP, hops, "LOOP");
            return new Tuple<>(f, ft);
          }
          if (isFalse(m, aexpr)) {
            Interface i = ge.getEnd();
            IpAccessList acl = i.getIncomingFilter();
            FilterResult fr = acl.filter(f);
            IpAccessListLine line = acl.getLines().get(fr.getMatchLine());
            String note = String.format("DENIED_IN{%s}{%s}", acl.getName(), line.getName());
            FlowTrace ft = new FlowTrace(FlowDisposition.DENIED_IN, hops, note);
            return new Tuple<>(f, ft);
          }
          boolean isLoopback = slice.getGraph().isLoopback(ge);
          if (isLoopback) {
            FlowTrace ft = new FlowTrace(FlowDisposition.ACCEPTED, hops, "ACCEPTED");
            return new Tuple<>(f, ft);
          }
          if (ge.getPeer() == null) {
            boolean isBgpPeering = slice.getGraph().getEbgpNeighbors().get(ge) != null;
            if (isBgpPeering) {
              FlowTrace ft = new FlowTrace(FlowDisposition.ACCEPTED, hops, "ACCEPTED");
              return new Tuple<>(f, ft);
            } else {
              FlowTrace ft =
                  new FlowTrace(
                      FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
                      hops,
                      "NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK");
              return new Tuple<>(f, ft);
            }
          }
          if (slice.getGraph().isHost(ge.getPeer())) {
            FlowTrace ft = new FlowTrace(FlowDisposition.ACCEPTED, hops, "ACCEPTED");
            return new Tuple<>(f, ft);
          }

          current = ge.getPeer();
          found = true;
          break;

        } else if (isTrue(m, cexpr)) {
          hops.add(buildFlowTraceHop(f, ge, route));
          Interface i = ge.getStart();
          IpAccessList acl = i.getOutgoingFilter();
          FilterResult fr = acl.filter(f);
          IpAccessListLine line = acl.getLines().get(fr.getMatchLine());
          String note = String.format("DENIED_OUT{%s}{%s}", acl.getName(), line.getName());
          FlowTrace ft = new FlowTrace(FlowDisposition.DENIED_OUT, hops, note);
          return new Tuple<>(f, ft);
        }
      }
      if (!found) {
        BoolExpr permitted = r.getPermitted();
        String s1 = evaluate(m, permitted);
        if ("true".equals(s1)) {
          // Check if there is an accepting interface
          for (GraphEdge ge : slice.getGraph().getEdgeMap().get(current)) {
            Interface i = ge.getStart();
            Ip ip = i.getPrefix().getAddress();
            if (ip.equals(f.getDstIp())) {
              FlowTrace ft = new FlowTrace(FlowDisposition.ACCEPTED, hops, "ACCEPTED");
              return new Tuple<>(f, ft);
            }
          }
          FlowTrace ft =
              new FlowTrace(
                  FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK,
                  hops,
                  "NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK");
          return new Tuple<>(f, ft);
        }
        FlowTrace ft = new FlowTrace(FlowDisposition.NO_ROUTE, hops, "NO_ROUTE");
        return new Tuple<>(f, ft);
      }
    }
  }

  /*
   * Creates a trace-based example of what happens
   * to a packet (e.g., why it is not reachable).
   */
  private static FlowHistory buildFlowCounterExample(
      IBatfish batfish,
      VerificationResult res,
      Set<String> sourceRouters,
      Model model,
      Encoder enc,
      Map<String, BoolExpr> reach) {

    FlowHistory fh = new FlowHistory();
    if (!res.isVerified()) {
      for (String source : sourceRouters) {
        BoolExpr sourceVar = reach.get(source);
        if (isFalse(model, sourceVar)) {
          Tuple<Flow, FlowTrace> tup = buildFlowTrace(enc, model, source);
          SortedSet<Edge> failedLinks = buildFailedLinks(enc, model);
          SortedSet<BgpAdvertisement> envRoutes = buildEnvRoutingTable(enc, model);
          Environment baseEnv =
              new Environment(
                  "BASE", batfish.getTestrigName(), failedLinks, null, null, null, null, envRoutes);
          fh.addFlowTrace(tup.getFirst(), "BASE", baseEnv, tup.getSecond());
        }
      }
    }
    return fh;
  }

  /*
   * Create a trace-based counterexample demonstrating
   * the difference between two networks on a single packet.
   */
  private static FlowHistory buildFlowDiffCounterExample(
      IBatfish batfish,
      VerificationResult res,
      Set<String> sourceRouters,
      Model model,
      Encoder enc,
      Encoder enc2,
      Map<String, BoolExpr> reach,
      Map<String, BoolExpr> reach2) {

    FlowHistory fh = new FlowHistory();
    if (!res.isVerified()) {
      assert (reach2 != null);
      for (String source : sourceRouters) {
        BoolExpr sourceVar1 = reach.get(source);
        BoolExpr sourceVar2 = reach2.get(source);
        String val1 = evaluate(model, sourceVar1);
        String val2 = evaluate(model, sourceVar2);
        if (!Objects.equals(val1, val2)) {
          Tuple<Flow, FlowTrace> diff = buildFlowTrace(enc, model, source);
          Tuple<Flow, FlowTrace> base = buildFlowTrace(enc2, model, source);
          SortedSet<Edge> failedLinksDiff = buildFailedLinks(enc, model);
          SortedSet<Edge> failedLinksBase = buildFailedLinks(enc2, model);
          SortedSet<BgpAdvertisement> envRoutesDiff = buildEnvRoutingTable(enc, model);
          SortedSet<BgpAdvertisement> envRoutesBase = buildEnvRoutingTable(enc2, model);
          Environment baseEnv =
              new Environment(
                  "BASE",
                  batfish.getTestrigName(),
                  failedLinksBase,
                  null,
                  null,
                  null,
                  null,
                  envRoutesBase);
          Environment failedEnv =
              new Environment(
                  "DELTA",
                  batfish.getTestrigName(),
                  failedLinksDiff,
                  null,
                  null,
                  null,
                  null,
                  envRoutesDiff);
          fh.addFlowTrace(base.getFirst(), "BASE", baseEnv, base.getSecond());
          fh.addFlowTrace(diff.getFirst(), "DELTA", failedEnv, diff.getSecond());
        }
      }
    }
    return fh;
  }

  /*
   * Find the set of edges that the solver can consider to fail
   */
  private static Set<GraphEdge> failLinkSet(Graph g, HeaderLocationQuestion q) {
    Pattern p1 = Pattern.compile(q.getFailNode1Regex());
    Pattern p2 = Pattern.compile(q.getFailNode2Regex());
    Pattern p3 = Pattern.compile(q.getNotFailNode1Regex());
    Pattern p4 = Pattern.compile(q.getNotFailNode2Regex());
    Set<GraphEdge> failChoices = PatternUtils.findMatchingEdges(g, p1, p2);
    Set<GraphEdge> failChoices2 = PatternUtils.findMatchingEdges(g, p2, p1);
    Set<GraphEdge> notFailChoices = PatternUtils.findMatchingEdges(g, p3, p4);
    Set<GraphEdge> notFailChoices2 = PatternUtils.findMatchingEdges(g, p4, p3);
    failChoices.addAll(failChoices2);
    failChoices.removeAll(notFailChoices);
    failChoices.removeAll(notFailChoices2);
    return failChoices;
  }

  /*
   * Creates a boolean expression that relates the environments of
   * two separate network copies.
   */
  private static BoolExpr relateEnvironments(Encoder enc1, Encoder enc2) {
    // create a map for enc2 to lookup a related environment variable from enc
    Table2<GraphEdge, EdgeType, SymbolicRecord> relatedEnv = new Table2<>();
    enc2.getMainSlice()
        .getLogicalGraph()
        .getEnvironmentVars()
        .forEach((lge, r) -> relatedEnv.put(lge.getEdge(), lge.getEdgeType(), r));

    BoolExpr related = enc1.mkTrue();

    // relate environments if necessary
    Map<LogicalEdge, SymbolicRecord> map =
        enc1.getMainSlice().getLogicalGraph().getEnvironmentVars();
    for (Map.Entry<LogicalEdge, SymbolicRecord> entry : map.entrySet()) {
      LogicalEdge le = entry.getKey();
      SymbolicRecord r1 = entry.getValue();
      String router = le.getEdge().getRouter();
      Configuration conf = enc1.getMainSlice().getGraph().getConfigurations().get(router);

      // Lookup the same environment variable in the other copy
      // The copy will have a different name but the same edge and type
      SymbolicRecord r2 = relatedEnv.get(le.getEdge(), le.getEdgeType());
      assert r2 != null;
      BoolExpr x = equal(enc1, conf, r1, r2);
      related = enc1.mkAnd(related, x);
    }
    return related;
  }

  /*
   * Creates a boolean expression that relates the environments of
   * two separate network copies.
   */
  private static BoolExpr relateFailures(Encoder enc1, Encoder enc2) {
    BoolExpr related = enc1.mkTrue();
    for (GraphEdge ge : enc1.getMainSlice().getGraph().getAllRealEdges()) {
      ArithExpr a1 = enc1.getSymbolicFailures().getFailedVariable(ge);
      ArithExpr a2 = enc2.getSymbolicFailures().getFailedVariable(ge);
      assert a1 != null;
      assert a2 != null;
      related = enc1.mkEq(a1, a2);
    }
    return related;
  }

  /*
   * Relate the two packets from different network copies
   */
  private static BoolExpr relatePackets(Encoder enc1, Encoder enc2) {
    SymbolicPacket p1 = enc1.getMainSlice().getSymbolicPacket();
    SymbolicPacket p2 = enc2.getMainSlice().getSymbolicPacket();
    return p1.mkEqual(p2);
  }

  /*
   * Adds additional constraints that certain edges should not be failed to avoid
   * trivial false positives.
   */
  private static void addFailureConstraints(
      Encoder enc, Set<GraphEdge> dstPorts, Set<GraphEdge> failSet) {
    Graph graph = enc.getMainSlice().getGraph();
    graph
        .getEdgeMap()
        .forEach(
            (router, edges) -> {
              for (GraphEdge ge : edges) {
                ArithExpr f = enc.getSymbolicFailures().getFailedVariable(ge);
                assert f != null;
                if (!failSet.contains(ge)) {
                  enc.add(enc.mkEq(f, enc.mkInt(0)));
                } else if (dstPorts.contains(ge)) {
                  // Don't fail an interface if it is for the destination ip we are considering
                  // Otherwise, any failure can trivially make equivalence false
                  Prefix pfx = ge.getStart().getPrefix();
                  BitVecExpr dstIp = enc.getMainSlice().getSymbolicPacket().getDstIp();
                  BoolExpr relevant = enc.getMainSlice().isRelevantFor(pfx, dstIp);
                  BoolExpr notFailed = enc.mkEq(f, enc.mkInt(0));
                  enc.add(enc.mkImplies(relevant, notFailed));
                }
              }
            });
  }

  /*
   * Add constraints on the environment
   */
  private static void addEnvironmentConstraints(Encoder enc, EnvironmentType t) {
    LogicalGraph lg = enc.getMainSlice().getLogicalGraph();
    Context ctx = enc.getCtx();
    switch (t) {
      case ANY:
        break;
      case NONE:
        lg.getEnvironmentVars().forEach((le, vars) -> enc.add(ctx.mkNot(vars.getPermitted())));
        break;
      case SANE:
        lg.getEnvironmentVars()
            .forEach((le, vars) -> enc.add(ctx.mkLe(vars.getMetric(), ctx.mkInt(50))));
        break;
      default:
        break;
    }
  }

  /*
   * Returns an iterator that lazily generates new Equivalence classes to verify.
   * If the abstraction option is enabled, will create abstract networks on-the-fly.
   */
  private static Iterator<EquivalenceClass> findAllEquivalenceClasses(
      IBatfish batfish, HeaderQuestion q, Graph graph) {
    if (q.getUseAbstraction()) {
      Abstraction abs = Abstraction.create(batfish, new ArrayList<>(), q.getHeaderSpace());
      return abs.iterator();
    }
    List<EquivalenceClass> singleEc = new ArrayList<>();
    EquivalenceClass ec = new EquivalenceClass(q.getHeaderSpace(), graph, null);
    singleEc.add(ec);
    return singleEc.iterator();
  }

  /*
   * Apply mapping from concrete to abstract nodes
   */
  private static Set<String> mapNodes(EquivalenceClass ec, List<String> concreteNodes) {
    if (ec.getAbstraction() == null) {
      return new HashSet<>(concreteNodes);
    }
    Set<String> abstractNodes = new HashSet<>();
    for (String c : concreteNodes) {
      String a = ec.getAbstraction().get(c);
      abstractNodes.add(a);
    }
    return abstractNodes;
  }

  /*
   * Compute if a collection of source routers can reach a collection of destination
   * ports. This is broken up into multiple queries, one for each destination port.
   */
  public static AnswerElement computeReachability(IBatfish batfish, HeaderLocationQuestion q) {

    PathRegexes p = new PathRegexes(q);
    Graph graph = new Graph(batfish);
    Set<GraphEdge> destPorts = findFinalInterfaces(graph, p);
    List<String> sourceRouters = PatternUtils.findMatchingSourceNodes(graph, p);

    if (destPorts.isEmpty()) {
      throw new BatfishException("Set of valid destination interfaces is empty");
    }
    if (sourceRouters.isEmpty()) {
      throw new BatfishException("Set of valid ingress nodes is empty");
    }

    inferDestinationHeaderSpace(graph, destPorts, q);
    Set<GraphEdge> failOptions = failLinkSet(graph, q);

    Iterator<EquivalenceClass> it = findAllEquivalenceClasses(batfish, q, graph);
    VerificationResult res = null;

    long start = System.currentTimeMillis();
    while (it.hasNext()) {
      EquivalenceClass ec = it.next();
      graph = ec.getGraph();
      Set<String> srcRouters = mapNodes(ec, sourceRouters);

      Encoder enc = new Encoder(graph, q);
      enc.computeEncoding();

      // Add environment constraints for base case
      if (q.getDiffType() != null) {
        if (q.getEnvDiff()) {
          addEnvironmentConstraints(enc, q.getDeltaEnvironmentType());
        }
      } else {
        addEnvironmentConstraints(enc, q.getBaseEnvironmentType());
      }

      // Add reachability variables
      PropertyAdder pa = new PropertyAdder(enc.getMainSlice());
      Map<String, BoolExpr> reach = pa.instrumentReachability(destPorts);

      // If this is a equivalence query, we create a second copy of the network
      Encoder enc2 = null;
      Map<String, BoolExpr> reach2 = null;

      if (q.getDiffType() != null) {
        HeaderLocationQuestion q2 = new HeaderLocationQuestion(q);
        q2.setFailures(0);
        enc2 = new Encoder(enc, graph, q2);
        enc2.computeEncoding();
      }

      // TODO: Should equivalence be factored out separately?
      if (q.getDiffType() != null) {
        assert (enc2 != null);

        // create a map for enc2 to lookup a related environment variable from enc
        Table2<GraphEdge, EdgeType, SymbolicRecord> relatedEnv = new Table2<>();
        enc2.getMainSlice()
            .getLogicalGraph()
            .getEnvironmentVars()
            .forEach((lge, r) -> relatedEnv.put(lge.getEdge(), lge.getEdgeType(), r));

        BoolExpr related = enc.mkTrue();

        // Setup environment for the second copy
        addEnvironmentConstraints(enc2, q.getBaseEnvironmentType());

        if (!q.getEnvDiff()) {
          related = relateEnvironments(enc, enc2);
        }

        PropertyAdder pa2 = new PropertyAdder(enc2.getMainSlice());
        reach2 = pa2.instrumentReachability(destPorts);

        // Add diff constraints
        BoolExpr required = enc.mkTrue();
        for (String source : srcRouters) {
          BoolExpr sourceReachable1 = reach.get(source);
          BoolExpr sourceReachable2 = reach2.get(source);
          BoolExpr val;
          switch (q.getDiffType()) {
            case INCREASED:
              val = enc.mkImplies(sourceReachable1, sourceReachable2);
              break;
            case REDUCED:
              val = enc.mkImplies(sourceReachable2, sourceReachable1);
              break;
            case ANY:
              val = enc.mkEq(sourceReachable1, sourceReachable2);
              break;
            default:
              throw new BatfishException("Missing case: " + q.getDiffType());
          }
          required = enc.mkAnd(required, val);
        }

        // Ensure packets are equal
        related = enc.mkAnd(related, relatePackets(enc, enc2));

        // Assuming equal packets and environments, is reachability the same
        enc.add(related);
        enc.add(enc.mkNot(required));

      } else {
        BoolExpr allReach = enc.mkTrue();
        for (String router : srcRouters) {
          BoolExpr r = reach.get(router);
          allReach = enc.mkAnd(allReach, r);
        }
        enc.add(enc.mkNot(allReach));
      }

      // Only consider failures for allowed edges
      addFailureConstraints(enc, destPorts, failOptions);

      long startVerify = System.currentTimeMillis();
      Tuple<VerificationResult, Model> result = enc.verify();
      System.out.println("Verification time: " + (System.currentTimeMillis() - startVerify));

      res = result.getFirst();
      Model model = result.getSecond();

      // res.debug(enc.getMainSlice(), false, null);

      if (res.isVerified()) {
        continue;
      }

      FlowHistory fh;
      if (q.getDiffType() != null) {
        fh =
            buildFlowDiffCounterExample(
                batfish, res, srcRouters, model, enc, enc2, reach, reach2);
      } else {
        fh = buildFlowCounterExample(batfish, res, srcRouters, model, enc, reach);
      }

      System.out.println("Total time: " + (System.currentTimeMillis() - start));

      SmtReachabilityAnswerElement answer = new SmtReachabilityAnswerElement();
      answer.setResult(res);
      answer.setFlowHistory(fh);
      return answer;
    }

    System.out.println("Total time: " + (System.currentTimeMillis() - start));

    SmtReachabilityAnswerElement answer = new SmtReachabilityAnswerElement();
    answer.setResult(res);
    answer.setFlowHistory(new FlowHistory());
    return answer;
  }

  /*
   * Check if there exist multiple stable solutions to the netowrk.
   * If so, reports the forwarding differences between the two cases.
   */
  public static AnswerElement computeDeterminism(IBatfish batfish, HeaderQuestion q) {
    Graph graph = new Graph(batfish);
    Encoder enc1 = new Encoder(graph, q);
    Encoder enc2 = new Encoder(enc1, graph, q);
    enc1.computeEncoding();
    enc2.computeEncoding();
    addEnvironmentConstraints(enc1, q.getBaseEnvironmentType());

    BoolExpr relatedFailures = relateFailures(enc1, enc2);
    BoolExpr relatedEnvs = relateEnvironments(enc1, enc2);
    BoolExpr relatedPkts = relatePackets(enc1, enc2);
    BoolExpr related = enc1.mkAnd(relatedFailures, relatedEnvs, relatedPkts);
    BoolExpr required = enc1.mkTrue();
    for (GraphEdge ge : graph.getAllRealEdges()) {
      SymbolicDecisions d1 = enc1.getMainSlice().getSymbolicDecisions();
      SymbolicDecisions d2 = enc2.getMainSlice().getSymbolicDecisions();
      BoolExpr dataFwd1 = d1.getDataForwarding().get(ge.getRouter(), ge);
      BoolExpr dataFwd2 = d2.getDataForwarding().get(ge.getRouter(), ge);
      assert dataFwd1 != null;
      assert dataFwd2 != null;
      required = enc1.mkAnd(required, enc1.mkEq(dataFwd1, dataFwd2));
    }

    enc1.add(related);
    enc1.add(enc1.mkNot(required));

    Tuple<VerificationResult, Model> tup = enc1.verify();
    VerificationResult res = tup.getFirst();
    Model model = tup.getSecond();

    SortedSet<String> case1 = null;
    SortedSet<String> case2 = null;
    Flow flow = null;
    if (!res.isVerified()) {
      case1 = new TreeSet<>();
      case2 = new TreeSet<>();
      flow = buildFlow(model, enc1.getMainSlice().getSymbolicPacket(), "(none)");
      for (GraphEdge ge : graph.getAllRealEdges()) {
        SymbolicDecisions d1 = enc1.getMainSlice().getSymbolicDecisions();
        SymbolicDecisions d2 = enc2.getMainSlice().getSymbolicDecisions();
        BoolExpr dataFwd1 = d1.getDataForwarding().get(ge.getRouter(), ge);
        BoolExpr dataFwd2 = d2.getDataForwarding().get(ge.getRouter(), ge);
        String s1 = evaluate(model, dataFwd1);
        String s2 = evaluate(model, dataFwd2);
        if (!Objects.equals(s1, s2)) {
          if ("true".equals(s1)) {
            String route = buildRoute(enc1.getMainSlice(), model, ge);
            String msg = ge + " -- " + route;
            case1.add(msg);
          }
          if ("true".equals(s2)) {
            String route = buildRoute(enc2.getMainSlice(), model, ge);
            String msg = ge + " -- " + route;
            case2.add(msg);
          }
        }
      }
    }

    SmtDeterminismAnswerElement answer = new SmtDeterminismAnswerElement();
    answer.setResult(res);
    answer.setFlow(flow);
    answer.setForwardingCase1(case1);
    answer.setForwardingCase2(case2);
    return answer;
  }

  /*
   * Compute if there can ever be a black hole for routers that are
   * not at the edge of the network. This is almost certainly a bug.
   */
  public static AnswerElement computeBlackHole(IBatfish batfish, HeaderQuestion q) {
    Graph graph = new Graph(batfish);
    Encoder enc = new Encoder(graph, q);
    enc.computeEncoding();
    Context ctx = enc.getCtx();
    EncoderSlice slice = enc.getMainSlice();

    // Collect routers that have no host/environment edge
    List<String> toCheck = new ArrayList<>();
    graph
        .getEdgeMap()
        .forEach(
            (router, edges) -> {
              boolean check = true;
              for (GraphEdge edge : edges) {
                if (edge.getEnd() == null) {
                  check = false;
                  break;
                }
              }
              if (check) {
                toCheck.add(router);
              }
            });

    // Ensure the router never receives traffic and then drops the traffic
    BoolExpr someBlackHole = ctx.mkBool(false);

    for (String router : toCheck) {
      Map<GraphEdge, BoolExpr> edges = slice.getSymbolicDecisions().getDataForwarding().get(router);
      BoolExpr doesNotFwd = ctx.mkBool(true);
      for (Map.Entry<GraphEdge, BoolExpr> entry : edges.entrySet()) {
        BoolExpr dataFwd = entry.getValue();
        doesNotFwd = ctx.mkAnd(doesNotFwd, ctx.mkNot(dataFwd));
      }

      BoolExpr isFwdTo = ctx.mkBool(false);
      Set<String> neighbors = graph.getNeighbors().get(router);
      for (String n : neighbors) {
        for (Map.Entry<GraphEdge, BoolExpr> entry :
            slice.getSymbolicDecisions().getDataForwarding().get(n).entrySet()) {
          GraphEdge ge = entry.getKey();
          BoolExpr fwd = entry.getValue();
          if (router.equals(ge.getPeer())) {
            isFwdTo = ctx.mkOr(isFwdTo, fwd);
          }
        }
      }

      someBlackHole = ctx.mkOr(someBlackHole, ctx.mkAnd(isFwdTo, doesNotFwd));
    }

    enc.add(someBlackHole);

    VerificationResult result = enc.verify().getFirst();

    SmtOneAnswerElement answer = new SmtOneAnswerElement();
    answer.setResult(result);
    return answer;
  }

  /*
   * Compute whether the path length will always be bounded by a constant k
   * for a collection of source routers to any of a number of destination ports.
   */
  public static AnswerElement computeBoundedLength(
      IBatfish batfish, HeaderLocationQuestion q, int k) {

    PathRegexes p = new PathRegexes(q);
    Graph graph = new Graph(batfish);
    Set<GraphEdge> destPorts = findFinalInterfaces(graph, p);
    List<String> sourceRouters = PatternUtils.findMatchingSourceNodes(graph, p);
    inferDestinationHeaderSpace(graph, destPorts, q);

    Encoder enc = new Encoder(graph, q);
    enc.computeEncoding();
    EncoderSlice slice = enc.getMainSlice();

    PropertyAdder pa = new PropertyAdder(slice);
    Map<String, ArithExpr> lenVars = pa.instrumentPathLength(destPorts);
    Context ctx = enc.getCtx();

    // All routers bounded by a particular length
    BoolExpr allBounded = ctx.mkFalse();
    for (String router : sourceRouters) {
      ArithExpr len = lenVars.get(router);
      ArithExpr bound = ctx.mkInt(k);
      allBounded = ctx.mkOr(allBounded, ctx.mkGt(len, bound));
    }
    enc.add(allBounded);

    VerificationResult res = enc.verify().getFirst();
    SmtOneAnswerElement answer = new SmtOneAnswerElement();
    answer.setResult(res);
    return answer;
  }

  /*
   * Computes whether a collection of source routers will always have
   * equal path length to destination port(s).
   */
  public static AnswerElement computeEqualLength(IBatfish batfish, HeaderLocationQuestion q) {
    PathRegexes p = new PathRegexes(q);
    Graph graph = new Graph(batfish);
    Set<GraphEdge> destPorts = findFinalInterfaces(graph, p);
    List<String> sourceRouters = PatternUtils.findMatchingSourceNodes(graph, p);
    inferDestinationHeaderSpace(graph, destPorts, q);

    Encoder enc = new Encoder(graph, q);
    enc.computeEncoding();
    EncoderSlice slice = enc.getMainSlice();

    PropertyAdder pa = new PropertyAdder(slice);
    Map<String, ArithExpr> lenVars = pa.instrumentPathLength(destPorts);
    Context ctx = enc.getCtx();

    // All routers have the same length through transitivity
    List<Expr> lens = new ArrayList<>();
    for (String router : sourceRouters) {
      lens.add(lenVars.get(router));
    }
    BoolExpr allEqual = PropertyAdder.allEqual(ctx, lens);
    enc.add(ctx.mkNot(allEqual));

    VerificationResult res = enc.verify().getFirst();
    SmtOneAnswerElement answer = new SmtOneAnswerElement();
    answer.setResult(res);
    return answer;
  }

  /*
   * Computes whether load balancing for each source node in a collection is
   * within some threshold k of the each other.
   */
  public static AnswerElement computeLoadBalance(
      IBatfish batfish, HeaderLocationQuestion q, int k) {

    PathRegexes p = new PathRegexes(q);

    Graph graph = new Graph(batfish);
    List<GraphEdge> destinationPorts = PatternUtils.findMatchingEdges(graph, p);
    List<String> sourceRouters = PatternUtils.findMatchingSourceNodes(graph, p);
    Map<String, List<String>> peerRouters = new HashMap<>();
    Map<String, VerificationResult> result = new HashMap<>();

    List<String> pRouters = PatternUtils.findMatchingSourceNodes(graph, p);

    // TODO: refactor this out separately
    for (String router : sourceRouters) {
      List<String> list = new ArrayList<>();
      peerRouters.put(router, list);
      Set<String> neighbors = graph.getNeighbors().get(router);
      for (String peer : pRouters) {
        if (neighbors.contains(peer)) {
          list.add(peer);
        }
      }
    }

    for (GraphEdge ge : destinationPorts) {
      // Add the interface destination
      boolean addedDestination = false;
      if (q.getHeaderSpace().getDstIps().isEmpty()) {
        addedDestination = true;
        Prefix destination = ge.getStart().getPrefix();
        IpWildcard dst = new IpWildcard(destination);
        q.getHeaderSpace().getDstIps().add(dst);
      }

      Encoder enc = new Encoder(graph, q);
      enc.computeEncoding();

      EncoderSlice slice = enc.getMainSlice();

      PropertyAdder pa = new PropertyAdder(slice);
      Map<String, ArithExpr> loadVars = pa.instrumentLoad(ge);

      Context ctx = enc.getCtx();

      // TODO: add threshold
      // All routers bounded by a particular length
      List<Expr> peerLoads = new ArrayList<>();
      peerRouters.forEach(
          (router, allPeers) -> {
            // ArithExpr load = loadVars.get(router);
            for (String peer : allPeers) {
              peerLoads.add(loadVars.get(peer));
            }
          });
      BoolExpr evenLoads = PropertyAdder.allEqual(ctx, peerLoads);
      enc.add(ctx.mkNot(evenLoads));

      VerificationResult res = enc.verify().getFirst();
      result.put(ge.getRouter() + "," + ge.getStart().getName(), res);

      if (addedDestination) {
        q.getHeaderSpace().getDstIps().clear();
      }
    }

    SmtManyAnswerElement answer = new SmtManyAnswerElement();
    answer.setResult(result);
    return answer;
  }

  /*
   * Computes whether or not two routers are equivalent.
   * To be equivalent, each router must have identical intefaces.
   *
   * We then relate the environments on each interface for each router
   * so that they are required to be equal.
   *
   * We finally check that their forwarding decisions and exported messages
   * will be equal given their equal inputs.
   */
  public static AnswerElement computeLocalConsistency(
      IBatfish batfish, Pattern n, boolean strict, boolean fullModel) {
    Graph graph = new Graph(batfish);
    List<String> routers = PatternUtils.findMatchingNodes(graph, n, Pattern.compile(""));

    HeaderQuestion q = new HeaderQuestion();
    q.setFullModel(fullModel);
    q.setFailures(0);
    q.setBaseEnvironmentType(EnvironmentType.ANY);

    Collections.sort(routers);

    Map<String, VerificationResult> result = new HashMap<>();

    int len = routers.size();
    if (len <= 1) {
      SmtManyAnswerElement answer = new SmtManyAnswerElement();
      answer.setResult(new HashMap<>());
      return answer;
    }

    for (int i = 0; i < len - 1; i++) {
      String r1 = routers.get(i);
      String r2 = routers.get(i + 1);

      // TODO: reorder to encode after checking if we can compare them

      // Create transfer function for router 1
      Set<String> toModel1 = new TreeSet<>();
      toModel1.add(r1);
      Graph g1 = new Graph(batfish, null, toModel1);
      Encoder e1 = new Encoder(g1, q);
      e1.computeEncoding();

      Context ctx = e1.getCtx();

      // Create transfer function for router 2
      Set<String> toModel2 = new TreeSet<>();
      toModel2.add(r2);
      Graph g2 = new Graph(batfish, null, toModel2);
      Encoder e2 = new Encoder(e1, g2);
      e2.computeEncoding();

      EncoderSlice slice1 = e1.getMainSlice();
      EncoderSlice slice2 = e2.getMainSlice();

      // Ensure that the two routers have the same interfaces for comparison
      Pattern p = Pattern.compile(".*");
      Pattern neg = Pattern.compile("");
      List<GraphEdge> edges1 = PatternUtils.findMatchingEdges(g1, p, neg, p, neg);
      List<GraphEdge> edges2 = PatternUtils.findMatchingEdges(g2, p, neg, p, neg);
      Set<String> ifaces1 = interfaces(edges1);
      Set<String> ifaces2 = interfaces(edges2);

      if (!(ifaces1.containsAll(ifaces2) && ifaces2.containsAll(ifaces1))) {
        String msg = String.format("Routers %s and %s have different interfaces", r1, r2);
        System.out.println(msg);
        SmtManyAnswerElement answer = new SmtManyAnswerElement();
        answer.setResult(new TreeMap<>());
        return answer;
      }

      // TODO: check running same protocols?

      // Map<String, Map<Protocol, Map<String, EnumMap<EdgeType, LogicalEdge>>>>
      //        lgeMap1 = logicalEdgeMap(e1);
      Map<String, Map<Protocol, Map<String, EnumMap<EdgeType, LogicalEdge>>>> lgeMap2 =
          logicalEdgeMap(slice2);

      BoolExpr equalEnvs = ctx.mkBool(true);
      BoolExpr equalOutputs = ctx.mkBool(true);
      BoolExpr equalIncomingAcls = ctx.mkBool(true);

      Configuration conf1 = g1.getConfigurations().get(r1);
      Configuration conf2 = g2.getConfigurations().get(r2);

      // Set environments equal
      Set<String> communities = new HashSet<>();

      Set<SymbolicRecord> envRecords = new HashSet<>();

      for (Protocol proto1 : slice1.getProtocols().get(r1)) {
        for (ArrayList<LogicalEdge> es :
            slice1.getLogicalGraph().getLogicalEdges().get(r1).get(proto1)) {
          for (LogicalEdge lge1 : es) {

            String ifaceName = lge1.getEdge().getStart().getName();

            LogicalEdge lge2 = lgeMap2.get(r2).get(proto1).get(ifaceName).get(lge1.getEdgeType());

            if (lge1.getEdgeType() == EdgeType.IMPORT) {

              SymbolicRecord vars1 = slice1.getLogicalGraph().getEnvironmentVars().get(lge1);
              SymbolicRecord vars2 = slice2.getLogicalGraph().getEnvironmentVars().get(lge2);

              BoolExpr aclIn1 = slice1.getIncomingAcls().get(lge1.getEdge());
              BoolExpr aclIn2 = slice2.getIncomingAcls().get(lge2.getEdge());

              if (aclIn1 == null) {
                aclIn1 = ctx.mkBool(true);
              }
              if (aclIn2 == null) {
                aclIn2 = ctx.mkBool(true);
              }

              equalIncomingAcls = ctx.mkAnd(equalIncomingAcls, ctx.mkEq(aclIn1, aclIn2));

              boolean hasEnv1 = (vars1 != null);
              boolean hasEnv2 = (vars2 != null);

              if (hasEnv1 && hasEnv2) {
                BoolExpr samePermitted = ctx.mkEq(vars1.getPermitted(), vars2.getPermitted());

                // Set communities equal
                BoolExpr equalComms = e1.mkTrue();
                for (Map.Entry<CommunityVar, BoolExpr> entry : vars1.getCommunities().entrySet()) {
                  CommunityVar cvar = entry.getKey();
                  BoolExpr ce1 = entry.getValue();
                  BoolExpr ce2 = vars2.getCommunities().get(cvar);
                  if (ce2 != null) {
                    equalComms = e1.mkAnd(equalComms, e1.mkEq(ce1, ce2));
                  }
                }

                // Set communities belonging to one but not the other
                // off, but give a warning of the difference
                BoolExpr unsetComms = e1.mkTrue();

                for (Map.Entry<CommunityVar, BoolExpr> entry : vars1.getCommunities().entrySet()) {
                  CommunityVar cvar = entry.getKey();
                  BoolExpr ce1 = entry.getValue();
                  BoolExpr ce2 = vars2.getCommunities().get(cvar);
                  if (ce2 == null) {

                    if (!communities.contains(cvar.getValue())) {
                      communities.add(cvar.getValue());
                      /* String msg =
                       String.format(
                           "Warning: community %s found for router %s but not %s.",
                           cvar.getValue(), conf1.getEnvName(), conf2.getEnvName());
                      System.out.println(msg); */
                    }
                    unsetComms = e1.mkAnd(unsetComms, e1.mkNot(ce1));
                  }
                }

                // Do the same thing for communities missing from the other side
                for (Map.Entry<CommunityVar, BoolExpr> entry : vars2.getCommunities().entrySet()) {
                  CommunityVar cvar = entry.getKey();
                  BoolExpr ce2 = entry.getValue();
                  BoolExpr ce1 = vars1.getCommunities().get(cvar);
                  if (ce1 == null) {
                    if (!communities.contains(cvar.getValue())) {
                      communities.add(cvar.getValue());
                      /* String msg =
                       String.format(
                           "Warning: community %s found for router %s but not %s.",
                           cvar.getValue(), conf2.getEnvName(), conf1.getEnvName());
                      System.out.println(msg); */
                    }
                    unsetComms = e1.mkAnd(unsetComms, e1.mkNot(ce2));
                  }
                }

                envRecords.add(vars1);

                BoolExpr equalVars = slice1.equal(conf1, proto1, vars1, vars2, lge1, true);
                equalEnvs = ctx.mkAnd(equalEnvs, unsetComms, samePermitted, equalVars, equalComms);

                // System.out.println("Unset communities: ");
                // System.out.println(unsetComms);

              } else if (hasEnv1 || hasEnv2) {
                System.out.println("Edge1: " + lge1);
                System.out.println("Edge2: " + lge2);
                throw new BatfishException("one had environment");
              }

            } else {

              SymbolicRecord out1 = lge1.getSymbolicRecord();
              SymbolicRecord out2 = lge2.getSymbolicRecord();

              equalOutputs =
                  ctx.mkAnd(equalOutputs, slice1.equal(conf1, proto1, out1, out2, lge1, false));
            }
          }
        }
      }

      // Ensure that there is only one active environment message if we want to
      // check the stronger version of local equivalence
      if (strict) {
        for (SymbolicRecord env1 : envRecords) {
          for (SymbolicRecord env2 : envRecords) {
            if (!env1.equals(env2)) {
              BoolExpr c = e2.mkImplies(env1.getPermitted(), e2.mkNot(env2.getPermitted()));
              e2.add(c);
            }
          }
        }
      }

      // TODO: check both have same environment vars (e.g., screw up configuring peer connection)

      // Create assumptions
      BoolExpr validDest;
      validDest = ignoredDestinations(ctx, slice1, r1, conf1);
      validDest = ctx.mkAnd(validDest, ignoredDestinations(ctx, slice2, r2, conf2));
      SymbolicPacket p1 = slice1.getSymbolicPacket();
      SymbolicPacket p2 = slice2.getSymbolicPacket();
      BoolExpr equalPackets = p1.mkEqual(p2);
      BoolExpr assumptions = ctx.mkAnd(equalEnvs, equalPackets, validDest);

      // Create the requirements

      // Best choices should be the same
      BoolExpr required;
      if (strict) {
        SymbolicRecord best1 =
            e1.getMainSlice().getSymbolicDecisions().getBestNeighbor().get(conf1.getName());
        SymbolicRecord best2 =
            e2.getMainSlice().getSymbolicDecisions().getBestNeighbor().get(conf2.getName());
        // Just pick some protocol for defaults, shouldn't matter for best choice
        required = equal(e2, conf2, best1, best2);
      } else {
        // Forwarding decisions should be the sames
        Map<String, GraphEdge> geMap2 = interfaceMap(edges2);
        BoolExpr sameForwarding = ctx.mkBool(true);
        for (GraphEdge ge1 : edges1) {
          GraphEdge ge2 = geMap2.get(ge1.getStart().getName());
          BoolExpr dataFwd1 = slice1.getSymbolicDecisions().getDataForwarding().get(r1, ge1);
          BoolExpr dataFwd2 = slice2.getSymbolicDecisions().getDataForwarding().get(r2, ge2);
          assert (dataFwd1 != null);
          assert (dataFwd2 != null);
          sameForwarding = ctx.mkAnd(sameForwarding, ctx.mkEq(dataFwd1, dataFwd2));
        }
        required =
            ctx.mkAnd(
                sameForwarding,
                equalOutputs); // , equalOutputs); //, equalOutputs, equalIncomingAcls);
      }

      // System.out.println("Assumptions: ");
      // System.out.println(assumptions.simplify());

      // System.out.println("Required: ");
      // System.out.println(required.simplify());

      e2.add(assumptions);
      e2.add(ctx.mkNot(required));

      VerificationResult res = e2.verify().getFirst();

      // res.debug(e1.getMainSlice(), false, null);

      String name = r1 + "<-->" + r2;
      result.put(name, res);
    }

    SmtManyAnswerElement answer = new SmtManyAnswerElement();
    answer.setResult(result);
    return answer;
  }

  /*
   * Get the interfaces for a l
   */
  private static Set<String> interfaces(List<GraphEdge> edges) {
    Set<String> ifaces = new TreeSet<>();
    for (GraphEdge edge : edges) {
      ifaces.add(edge.getStart().getName());
    }
    return ifaces;
  }

  /*
   * Build the inverse map for each logical edge
   */
  private static Map<String, Map<Protocol, Map<String, EnumMap<EdgeType, LogicalEdge>>>>
      logicalEdgeMap(EncoderSlice enc) {

    Map<String, Map<Protocol, Map<String, EnumMap<EdgeType, LogicalEdge>>>> acc = new HashMap<>();
    enc.getLogicalGraph()
        .getLogicalEdges()
        .forEach(
            (router, map) -> {
              Map<Protocol, Map<String, EnumMap<EdgeType, LogicalEdge>>> mapAcc = new HashMap<>();
              acc.put(router, mapAcc);
              map.forEach(
                  (proto, edges) -> {
                    Map<String, EnumMap<EdgeType, LogicalEdge>> edgesMap = new HashMap<>();
                    mapAcc.put(proto, edgesMap);
                    for (ArrayList<LogicalEdge> xs : edges) {
                      for (LogicalEdge lge : xs) {
                        // Should have import since only connected to environment
                        String ifaceName = lge.getEdge().getStart().getName();
                        EnumMap<EdgeType, LogicalEdge> typeMap = edgesMap.get(ifaceName);
                        if (typeMap == null) {
                          EnumMap<EdgeType, LogicalEdge> m = new EnumMap<>(EdgeType.class);
                          m.put(lge.getEdgeType(), lge);
                          edgesMap.put(ifaceName, m);
                        } else {
                          typeMap.put(lge.getEdgeType(), lge);
                        }
                      }
                    }
                  });
            });
    return acc;
  }

  /*
   * Creates a boolean variable representing destinations we don't want
   * to consider due to local differences.
   */
  private static BoolExpr ignoredDestinations(
      Context ctx, EncoderSlice e1, String r1, Configuration conf1) {
    BoolExpr validDest = ctx.mkBool(true);
    for (Protocol proto1 : e1.getProtocols().get(r1)) {
      Set<Prefix> prefixes = Graph.getOriginatedNetworks(conf1, proto1);
      BoolExpr dest = e1.relevantOrigination(prefixes);
      validDest = ctx.mkAnd(validDest, ctx.mkNot(dest));
    }
    return validDest;
  }

  /*
   * Create a map from interface name to graph edge.
   */
  private static Map<String, GraphEdge> interfaceMap(List<GraphEdge> edges) {
    Map<String, GraphEdge> ifaceMap = new HashMap<>();
    for (GraphEdge edge : edges) {
      ifaceMap.put(edge.getStart().getName(), edge);
    }
    return ifaceMap;
  }

  /*
   * Computes multipath consistency, which ensures traffic that travels
   * multiple paths will be treated equivalently by each path
   * (i.e., dropped or accepted by each).
   */
  public static AnswerElement computeMultipathConsistency(
      IBatfish batfish, HeaderLocationQuestion q) {
    PathRegexes p = new PathRegexes(q);
    Graph graph = new Graph(batfish);
    Set<GraphEdge> destPorts = findFinalInterfaces(graph, p);
    inferDestinationHeaderSpace(graph, destPorts, q);

    Encoder enc = new Encoder(graph, q);
    enc.computeEncoding();
    EncoderSlice slice = enc.getMainSlice();

    PropertyAdder pa = new PropertyAdder(slice);
    Map<String, BoolExpr> reachableVars = pa.instrumentReachability(destPorts);

    BoolExpr acc = enc.mkFalse();
    for (Map.Entry<String, Configuration> entry : graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      BoolExpr reach = reachableVars.get(router);

      BoolExpr all = enc.mkTrue();
      for (GraphEdge edge : graph.getEdgeMap().get(router)) {
        BoolExpr dataFwd = slice.getForwardsAcross().get(router, edge);
        BoolExpr ctrFwd = slice.getSymbolicDecisions().getControlForwarding().get(router, edge);
        assert (ctrFwd != null);
        BoolExpr peerReach = enc.mkTrue();
        if (edge.getPeer() != null) {
          peerReach = reachableVars.get(edge.getPeer());
        }
        BoolExpr imp = enc.mkImplies(ctrFwd, enc.mkAnd(dataFwd, peerReach));

        all = enc.mkAnd(all, imp);
      }

      acc = enc.mkOr(acc, enc.mkNot(enc.mkImplies(reach, all)));
    }

    enc.add(acc);

    VerificationResult res = enc.verify().getFirst();

    SmtOneAnswerElement answer = new SmtOneAnswerElement();
    answer.setResult(res);
    return answer;
  }

  /*
   * Checks for routing loops in the network. For efficiency reasons,
   * we only check for loops with routers that use static routes since
   * these can override the usual loop-prevention mechanisms.
   */
  public static AnswerElement computeRoutingLoop(IBatfish batfish, HeaderQuestion q) {
    Graph graph = new Graph(batfish);

    // Collect all relevant destinations
    List<Prefix> prefixes = new ArrayList<>();
    graph
        .getStaticRoutes()
        .forEach(
            (router, ifaceMap) -> {
              ifaceMap.forEach(
                  (ifaceName, srs) -> {
                    for (StaticRoute sr : srs) {
                      prefixes.add(sr.getNetwork());
                    }
                  });
            });

    SortedSet<IpWildcard> pfxs = new TreeSet<>();
    for (Prefix prefix : prefixes) {
      pfxs.add(new IpWildcard(prefix));
    }
    q.getHeaderSpace().setDstIps(pfxs);

    // Collect all routers that use static routes as a
    // potential node along a loop
    List<String> routers = new ArrayList<>();
    graph
        .getConfigurations()
        .forEach(
            (router, conf) -> {
              if (conf.getDefaultVrf().getStaticRoutes().size() > 0) {
                routers.add(router);
              }
            });

    Encoder enc = new Encoder(graph, q);
    enc.computeEncoding();
    Context ctx = enc.getCtx();

    EncoderSlice slice = enc.getMainSlice();

    PropertyAdder pa = new PropertyAdder(slice);

    BoolExpr someLoop = ctx.mkBool(false);
    for (String router : routers) {
      BoolExpr hasLoop = pa.instrumentLoop(router);
      someLoop = ctx.mkOr(someLoop, hasLoop);
    }
    enc.add(someLoop);

    VerificationResult result = enc.verify().getFirst();

    SmtOneAnswerElement answer = new SmtOneAnswerElement();
    answer.setResult(result);
    return answer;
  }
}

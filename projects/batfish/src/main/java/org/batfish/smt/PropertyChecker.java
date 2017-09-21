package org.batfish.smt;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.datamodel.questions.smt.HeaderQuestion;
import org.batfish.smt.answers.SmtManyAnswerElement;
import org.batfish.smt.answers.SmtOneAnswerElement;
import org.batfish.smt.collections.Table2;
import org.batfish.smt.utils.PathRegexes;
import org.batfish.smt.utils.PatternUtils;

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
    VerificationResult result = encoder.verify();
    //result.debug(
    //    encoder.getMainSlice(), true, "0_SLICE-MAIN_as2core1_OSPF_SINGLE-EXPORT__permitted");
    SmtOneAnswerElement answer = new SmtOneAnswerElement();
    answer.setResult(result);
    return answer;
  }

  private static Set<GraphEdge> findFinalInterfaces(Graph g, PathRegexes p) {
    Set<GraphEdge> edges = new HashSet<>();
    for (GraphEdge ge : PatternUtils.findMatchingEdges(g, p)) {
      if (ge.getPeer() == null) {
        edges.add(ge);
      }
    }
    return edges;
  }

  private static void inferDestinationHeaderSpace(
      Graph g, Collection<GraphEdge> destPorts, HeaderLocationQuestion q) {
    // Infer relevant destination IP headerspace from interfaces
    for (GraphEdge ge : destPorts) {
      // If there is an external interface, then
      // it can be any prefix, so we leave it unconstrained
      if (g.getEbgpNeighbors().containsKey(ge)) {
        q.getHeaderSpace().getDstIps().clear();
        break;
      }
      // Otherwise, we add the destination IP range
      Prefix pfx = ge.getStart().getPrefix().getNetworkPrefix();
      IpWildcard dst = new IpWildcard(pfx);
      q.getHeaderSpace().getDstIps().add(dst);
    }
  }

  private static BoolExpr allReachable(
      Encoder enc, Set<GraphEdge> destPorts, List<String> sourceRouters) {
    EncoderSlice slice = enc.getMainSlice();
    PropertyAdder pa = new PropertyAdder(slice);
    Map<String, BoolExpr> reachableVars = pa.instrumentReachability(destPorts);
    BoolExpr allReach = enc.mkTrue();
    for (String router : sourceRouters) {
      BoolExpr reach = reachableVars.get(router);
      allReach = enc.mkAnd(allReach, reach);
    }
    return allReach;
  }

  /*
   * Constraint that encodes if two symbolic records are equal
   */
  private static BoolExpr equal(
      Encoder e, Configuration conf, SymbolicRecord r1, SymbolicRecord r2) {
    EncoderSlice main = e.getMainSlice();
    return main.equal(conf, Protocol.CONNECTED, r1, r2, null, true);
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
    inferDestinationHeaderSpace(graph, destPorts, q);

    Encoder enc = new Encoder(graph, q);
    enc.computeEncoding();

    // If this is a equivalence query, we create a second copy of the network
    Encoder enc2 = null;
    if (q.getEquivalence()) {
      HeaderLocationQuestion q2 = new HeaderLocationQuestion(q);
      q2.setFailures(0);
      enc2 = new Encoder(enc, graph, q2);
      enc2.computeEncoding();
    }

    // TODO: Should equivalence be factored out separately?
    if (q.getEquivalence()) {
      assert (enc2 != null);

      PropertyAdder pa = new PropertyAdder(enc.getMainSlice());
      Map<String, BoolExpr> reach = pa.instrumentReachability(destPorts);

      // create a map for enc2 to lookup a related environment variable from enc
      Table2<GraphEdge, EdgeType, SymbolicRecord> relatedEnv = new Table2<>();
      enc2.getMainSlice()
          .getLogicalGraph()
          .getEnvironmentVars()
          .forEach((lge, r) -> relatedEnv.put(lge.getEdge(), lge.getEdgeType(), r));

      BoolExpr related = enc.mkTrue();

      // relate environments
      Map<LogicalEdge, SymbolicRecord> map =
          enc.getMainSlice().getLogicalGraph().getEnvironmentVars();
      for (Map.Entry<LogicalEdge, SymbolicRecord> entry : map.entrySet()) {
        LogicalEdge le = entry.getKey();
        SymbolicRecord r1 = entry.getValue();
        String router = le.getEdge().getRouter();
        Configuration conf = enc.getMainSlice().getGraph().getConfigurations().get(router);

        // Lookup the same environment variable in the other copy
        // The copy will have a different name but the same edge and type
        SymbolicRecord r2 = relatedEnv.get(le.getEdge(), le.getEdgeType());
        assert r2 != null;
        BoolExpr x = equal(enc, conf, r1, r2);
        related = enc.mkAnd(related, x);
      }

      PropertyAdder pa2 = new PropertyAdder(enc2.getMainSlice());
      Map<String, BoolExpr> reach2 = pa2.instrumentReachability(destPorts);

      BoolExpr required = enc.mkTrue();
      for (String source : sourceRouters) {
        BoolExpr sourceReachable1 = reach.get(source);
        BoolExpr sourceReachable2 = reach2.get(source);
        required = enc.mkAnd(required, enc.mkEq(sourceReachable1, sourceReachable2));
      }

      // Ensure packets are equal
      SymbolicPacket p1 = enc.getMainSlice().getSymbolicPacket();
      SymbolicPacket p2 = enc2.getMainSlice().getSymbolicPacket();
      BoolExpr equalPackets = p1.mkEqual(p2);
      related = enc.mkAnd(related, equalPackets);

      // Assuming equal packets and environments, is reachability the same
      enc.add(related);
      enc.add(enc.mkNot(required));

    } else {
      BoolExpr allReach = allReachable(enc, destPorts, sourceRouters);
      enc.add(enc.mkNot(allReach));
    }

    // We don't really care about the case where the interface is directly failed
    for (GraphEdge ge : destPorts) {
      ArithExpr f = enc.getSymbolicFailures().getFailedVariable(ge);
      assert (f != null);
      enc.add(enc.mkEq(f, enc.mkInt(0)));
    }

    VerificationResult res = enc.verify();
    // res.debug(enc.getMainSlice(), true, null);
    SmtOneAnswerElement answer = new SmtOneAnswerElement();
    answer.setResult(res);
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

    VerificationResult result = enc.verify();

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

    VerificationResult res = enc.verify();
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

    VerificationResult res = enc.verify();
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

      VerificationResult res = enc.verify();
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
    q.setNoEnvironment(false);

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
      Graph g1 = new Graph(batfish, toModel1);
      Encoder e1 = new Encoder(g1, q);
      e1.computeEncoding();

      Context ctx = e1.getCtx();

      // Create transfer function for router 2
      Set<String> toModel2 = new TreeSet<>();
      toModel2.add(r2);
      Graph g2 = new Graph(batfish, toModel2);
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
                           cvar.getValue(), conf1.getName(), conf2.getName());
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
                           cvar.getValue(), conf2.getName(), conf1.getName());
                      System.out.println(msg); */
                    }
                    unsetComms = e1.mkAnd(unsetComms, e1.mkNot(ce2));
                  }
                }

                envRecords.add(vars1);

                BoolExpr equalVars = slice1.equal(conf1, proto1, vars1, vars2, lge1, true);
                equalEnvs = ctx.mkAnd(equalEnvs, unsetComms, samePermitted, equalVars, equalComms);

                //System.out.println("Unset communities: ");
                //System.out.println(unsetComms);

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
                equalOutputs); //, equalOutputs); //, equalOutputs, equalIncomingAcls);
      }

      // System.out.println("Assumptions: ");
      // System.out.println(assumptions.simplify());

      // System.out.println("Required: ");
      // System.out.println(required.simplify());

      e2.add(assumptions);
      e2.add(ctx.mkNot(required));

      VerificationResult res = e2.verify();

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
      List<Prefix> prefixes = e1.getOriginatedNetworks(conf1, proto1);
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

    VerificationResult res = enc.verify();

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

    VerificationResult result = enc.verify();

    SmtOneAnswerElement answer = new SmtOneAnswerElement();
    answer.setResult(result);
    return answer;
  }
}

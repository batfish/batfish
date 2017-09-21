package org.batfish.smt;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Instruments the network model with additional information that is useful for checking other
 * properties. This information can be things like reachability, path length, load, etc.
 *
 * @author Ryan Beckett
 */
public class PropertyAdder {

  private EncoderSlice _encoderSlice;

  public PropertyAdder(EncoderSlice encoderSlice) {
    _encoderSlice = encoderSlice;
  }

  /*
   * Ensure that all expressions in a list are equal
   */
  public static BoolExpr allEqual(Context ctx, List<Expr> exprs) {
    BoolExpr acc = ctx.mkBool(true);
    if (exprs.size() > 1) {
      for (int i = 0; i < exprs.size() - 1; i++) {
        Expr x = exprs.get(i);
        Expr y = exprs.get(i + 1);
        acc = ctx.mkAnd(acc, ctx.mkEq(x, y));
      }
    }
    return acc;
  }

  /*
   * Add reachability information to the network for a destination edge.
   * Each router will have a boolean variable determining if it can reach
   * the destination. A router is reachable if it has some neighbor that
   * is also reachable.
   */
  public Map<String, BoolExpr> instrumentReachability(Set<GraphEdge> ges) {
    Context ctx = _encoderSlice.getCtx();
    Solver solver = _encoderSlice.getSolver();
    String sliceName = _encoderSlice.getSliceName();

    // Initialize reachability variables
    Map<String, BoolExpr> reachableVars = new HashMap<>();
    _encoderSlice
        .getGraph()
        .getConfigurations()
        .forEach(
            (router, conf) -> {
              BoolExpr var =
                  ctx.mkBoolConst(
                      _encoderSlice.getEncoder().getId()
                          + "_"
                          + sliceName
                          + "_reachable_"
                          + router);
              reachableVars.put(router, var);
              _encoderSlice.getAllVariables().put(var.toString(), var);
            });

    _encoderSlice
        .getGraph()
        .getEdgeMap()
        .forEach(
            (router, edges) -> {
              // Add the base case, reachable if we forward to a directly connected interface
              BoolExpr hasDirectRoute = ctx.mkFalse();
              for (GraphEdge ge : edges) {
                if (!ge.isAbstract() && ges.contains(ge)) {
                  BoolExpr fwdIface = _encoderSlice.getForwardsAcross().get(ge.getRouter(), ge);
                  assert (fwdIface != null);
                  hasDirectRoute = ctx.mkOr(hasDirectRoute, fwdIface);
                }
              }
              // Now we add the recursive case, reachable if fwd to a reachable neighbor
              BoolExpr hasRecursiveRoute = ctx.mkFalse();
              for (GraphEdge edge : edges) {
                if (!edge.isAbstract()) {
                  BoolExpr fwd = _encoderSlice.getForwardsAcross().get(router, edge);
                  if (edge.getPeer() != null) {
                    BoolExpr peerReachable = reachableVars.get(edge.getPeer());
                    hasRecursiveRoute = ctx.mkOr(hasRecursiveRoute, ctx.mkAnd(fwd, peerReachable));
                  }
                }
              }
              // Reachable if either holds true
              BoolExpr reach = reachableVars.get(router);
              BoolExpr canReach = ctx.mkOr(hasDirectRoute, hasRecursiveRoute);
              solver.add(ctx.mkEq(reach, canReach));
            });

    return reachableVars;
  }

  /*
   * Also instruments reachability, but to a destination router
   * rather than a destination port.
   */
  public Map<String, BoolExpr> instrumentReachability(String router) {
    Context ctx = _encoderSlice.getCtx();
    Solver solver = _encoderSlice.getSolver();
    String sliceName = _encoderSlice.getSliceName();

    Map<String, BoolExpr> reachableVars = new HashMap<>();
    _encoderSlice
        .getGraph()
        .getConfigurations()
        .forEach(
            (r, conf) -> {
              BoolExpr var =
                  ctx.mkBoolConst(
                      _encoderSlice.getEncoder().getId() + "_" + sliceName + "_reachable_" + r);
              reachableVars.put(r, var);
              _encoderSlice.getAllVariables().put(var.toString(), var);
            });

    BoolExpr baseRouterReachable = reachableVars.get(router);
    _encoderSlice.add(baseRouterReachable);

    _encoderSlice
        .getGraph()
        .getEdgeMap()
        .forEach(
            (r, edges) -> {
              if (!r.equals(router)) {
                BoolExpr var = reachableVars.get(r);
                BoolExpr acc = ctx.mkBool(false);
                for (GraphEdge edge : edges) {
                  if (!edge.isAbstract()) {
                    BoolExpr fwd = _encoderSlice.getForwardsAcross().get(r, edge);
                    if (edge.getPeer() != null) {
                      BoolExpr peerReachable = reachableVars.get(edge.getPeer());
                      acc = ctx.mkOr(acc, ctx.mkAnd(fwd, peerReachable));
                    }
                  }
                }
                solver.add(ctx.mkEq(var, acc));
              }
            });

    return reachableVars;
  }

  /*
   * Instruments the network with path length information to a
   * destination port corresponding to a graph edge ge.
   * A router has a path of length n if some neighbor has a path
   * with length n-1.
   */
  public Map<String, ArithExpr> instrumentPathLength(Set<GraphEdge> ges) {
    Context ctx = _encoderSlice.getCtx();
    Solver solver = _encoderSlice.getSolver();
    String sliceName = _encoderSlice.getSliceName();

    // Initialize path length variables
    Map<String, ArithExpr> lenVars = new HashMap<>();
    _encoderSlice
        .getGraph()
        .getConfigurations()
        .forEach(
            (router, conf) -> {
              String name =
                  _encoderSlice.getEncoder().getId() + "_" + sliceName + "_path-length_" + router;
              ArithExpr var = ctx.mkIntConst(name);
              lenVars.put(router, var);
              _encoderSlice.getAllVariables().put(var.toString(), var);
            });

    // Lower bound for all lengths
    lenVars.forEach((name, var) -> solver.add(ctx.mkGe(var, ctx.mkInt(-1))));

    ArithExpr zero = ctx.mkInt(0);
    ArithExpr minusOne = ctx.mkInt(-1);

    // If no peer has a path, then I don't have a path
    // Otherwise I choose 1 + somePeer value to capture all possible lengths
    _encoderSlice
        .getGraph()
        .getEdgeMap()
        .forEach(
            (router, edges) -> {
              ArithExpr length = lenVars.get(router);

              // If there is a direct route, then we have length 0
              BoolExpr acc = ctx.mkFalse();
              for (GraphEdge ge : edges) {
                if (!ge.isAbstract() && ges.contains(ge)) {
                  BoolExpr fwdIface = _encoderSlice.getForwardsAcross().get(ge.getRouter(), ge);
                  assert (fwdIface != null);
                  acc = ctx.mkOr(acc, fwdIface);
                }
              }

              // Otherwise, we find length recursively
              BoolExpr accNone = ctx.mkTrue();
              BoolExpr accSome = ctx.mkFalse();
              for (GraphEdge edge : edges) {
                if (!edge.isAbstract()) {
                  if (edge.getPeer() != null) {
                    BoolExpr dataFwd = _encoderSlice.getForwardsAcross().get(router, edge);
                    assert (dataFwd != null);
                    ArithExpr peerLen = lenVars.get(edge.getPeer());
                    accNone =
                        ctx.mkAnd(accNone, ctx.mkOr(ctx.mkLt(peerLen, zero), ctx.mkNot(dataFwd)));
                    ArithExpr newVal = ctx.mkAdd(peerLen, ctx.mkInt(1));
                    BoolExpr fwd =
                        ctx.mkAnd(ctx.mkGe(peerLen, zero), dataFwd, ctx.mkEq(length, newVal));
                    accSome = ctx.mkOr(accSome, fwd);
                  }
                }
                BoolExpr cond1 = _encoderSlice.mkIf(accNone, ctx.mkEq(length, minusOne), accSome);
                BoolExpr cond2 = _encoderSlice.mkIf(acc, ctx.mkEq(length, zero), cond1);
                solver.add(cond2);
              }
            });

    return lenVars;
  }

  /*
   * Instruments the network with load balancing information to destination
   * port for graph edge ge. Each router will split load according to the
   * number of neighbors it actively uses to get to ge.
   */
  public Map<String, ArithExpr> instrumentLoad(GraphEdge ge) {
    Context ctx = _encoderSlice.getCtx();
    Solver solver = _encoderSlice.getSolver();
    String sliceName = _encoderSlice.getSliceName();

    BoolExpr fwdIface = _encoderSlice.getForwardsAcross().get(ge.getRouter(), ge);
    assert (fwdIface != null);

    Map<String, ArithExpr> loadVars = new HashMap<>();
    _encoderSlice
        .getGraph()
        .getConfigurations()
        .forEach(
            (router, conf) -> {
              String name =
                  _encoderSlice.getEncoder().getId() + "_" + sliceName + "_load_" + router;
              ArithExpr var = ctx.mkIntConst(name);
              loadVars.put(router, var);
              _encoderSlice.getAllVariables().put(var.toString(), var);
            });

    // Lower bound for all lengths
    loadVars.forEach(
        (name, var) -> {
          solver.add(ctx.mkGe(var, ctx.mkInt(0)));
        });

    // Root router has load 1 if it uses the interface
    ArithExpr zero = ctx.mkInt(0);
    ArithExpr one = ctx.mkInt(1);
    ArithExpr baseRouterLoad = loadVars.get(ge.getRouter());
    solver.add(ctx.mkImplies(fwdIface, ctx.mkEq(baseRouterLoad, one)));

    _encoderSlice
        .getGraph()
        .getEdgeMap()
        .forEach(
            (router, edges) -> {
              if (!router.equals(ge.getRouter())) {
                ArithExpr var = loadVars.get(router);
                ArithExpr acc = ctx.mkInt(0);
                for (GraphEdge edge : edges) {
                  if (!edge.isAbstract()) {
                    BoolExpr fwd = _encoderSlice.getForwardsAcross().get(router, edge);
                    assert (fwd != null);
                    if (edge.getPeer() != null) {
                      ArithExpr peerLoad = loadVars.get(edge.getPeer());
                      ArithExpr x = (ArithExpr) ctx.mkITE(fwd, peerLoad, zero);
                      acc = ctx.mkAdd(acc, x);
                    }
                  }
                }
                solver.add(ctx.mkEq(var, acc));
              }
            });

    return loadVars;
  }

  /*
   * Instruments the network to check if a router will be part
   * of a routing loop.
   */
  public BoolExpr instrumentLoop(String router) {
    Context ctx = _encoderSlice.getCtx();
    Solver solver = _encoderSlice.getSolver();
    String sliceName = _encoderSlice.getSliceName();

    // Add on-loop variables to track a loop
    Map<String, BoolExpr> onLoop = new HashMap<>();
    _encoderSlice
        .getGraph()
        .getConfigurations()
        .forEach(
            (r, conf) -> {
              String name =
                  _encoderSlice.getEncoder().getId()
                      + "_"
                      + sliceName
                      + "_on-loop_"
                      + router
                      + "_"
                      + r;
              BoolExpr var = ctx.mkBoolConst(name);
              onLoop.put(r, var);
              _encoderSlice.getAllVariables().put(var.toString(), var);
            });

    // Transitive closure for other routers
    _encoderSlice
        .getGraph()
        .getEdgeMap()
        .forEach(
            (r, edges) -> {
              BoolExpr var = onLoop.get(r);
              BoolExpr acc = ctx.mkBool(false);
              for (GraphEdge edge : edges) {
                if (!edge.isAbstract()) {
                  BoolExpr fwd = _encoderSlice.getForwardsAcross().get(r, edge);
                  String peer = edge.getPeer();
                  if (peer != null) {
                    if (peer.equals(router)) {
                      // If next hop is static route router, then on loop
                      acc = ctx.mkOr(acc, fwd);
                    } else {
                      // Otherwise check if next hop also is on the loop
                      BoolExpr peerOnLoop = onLoop.get(peer);
                      acc = ctx.mkOr(acc, ctx.mkAnd(fwd, peerOnLoop));
                    }
                  }
                }
              }
              solver.add(ctx.mkEq(var, acc));
            });

    return onLoop.get(router);
  }
}

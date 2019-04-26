package org.batfish.bddreachability;

import javax.annotation.Nullable;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.z3.state.NodeInterfaceExitsNetwork;
import org.batfish.z3.state.NodeInterfaceInsufficientInfo;
import org.batfish.z3.state.NodeInterfaceNeighborUnreachable;
import org.batfish.z3.state.OriginateInterfaceLink;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostInInterface;
import org.batfish.z3.state.PostInInterfacePostNat;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutEdgePostNat;
import org.batfish.z3.state.PreOutInterfaceDeliveredToSubnet;
import org.batfish.z3.state.PreOutInterfaceExitsNetwork;
import org.batfish.z3.state.PreOutInterfaceInsufficientInfo;
import org.batfish.z3.state.PreOutInterfaceNeighborUnreachable;
import org.batfish.z3.state.PreOutVrf;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

/**
 * Converts successful flow termination states from the forward pass of a bidirectional reachability
 * query to the corresponding origination state for the return pass (if any). If the state does not
 * have a corresponding origination state, returns null.
 *
 * <p>{@link NodeInterfaceDeliveredToSubnet} and {@link NodeInterfaceExitsNetwork} states are mapped
 * to corresponding {@link OriginateInterfaceLink} states.
 *
 * <p>TODO: handle ACCEPT disposition
 *
 * <p>Flows accepted by a VRF in the forward pass should be originated by {@link OriginateVrf} in
 * the return pass. This isn't implemented yet, because there is no corresponding AcceptVrf state.
 * We can either add one, or else split the packets accepted at a {@link NodeAccept} state into
 * multiple {@link OriginateVrf} states by dst IP.
 */
public class ReversePassOriginationState implements GenericStateExprVisitor<StateExpr> {
  private static final ReversePassOriginationState INSTANCE = new ReversePassOriginationState();

  private ReversePassOriginationState() {}

  public static @Nullable StateExpr reverseTraceOriginationState(StateExpr expr) {
    return expr.accept(INSTANCE);
  }

  @Override
  public StateExpr visitAccept() {
    return null;
  }

  @Override
  public StateExpr visitDropAclIn() {
    return null;
  }

  @Override
  public StateExpr visitDropAclOut() {
    return null;
  }

  @Override
  public StateExpr visitDropNoRoute() {
    return null;
  }

  @Override
  public StateExpr visitDropNullRoute() {
    return null;
  }

  @Override
  public StateExpr visitExitsNetwork() {
    return null;
  }

  @Override
  public StateExpr visitDeliveredToSubnet() {
    return null;
  }

  @Override
  public StateExpr visitInsufficientInfo() {
    return null;
  }

  @Override
  public StateExpr visitNeighborUnreachable() {
    return null;
  }

  @Override
  public StateExpr visitNodeAccept(NodeAccept nodeAccept) {
    return null;
  }

  @Override
  public StateExpr visitNodeDropAclIn(NodeDropAclIn nodeDropAclIn) {
    return null;
  }

  @Override
  public StateExpr visitNodeDropAclOut(NodeDropAclOut nodeDropAclOut) {
    return null;
  }

  @Override
  public StateExpr visitNodeDropNoRoute(NodeDropNoRoute nodeDropNoRoute) {
    return null;
  }

  @Override
  public StateExpr visitNodeDropNullRoute(NodeDropNullRoute nodeDropNullRoute) {
    return null;
  }

  @Override
  public StateExpr visitNodeInterfaceDeliveredToSubnet(NodeInterfaceDeliveredToSubnet state) {
    return new OriginateInterfaceLink(state.getHostname(), state.getIface());
  }

  @Override
  public StateExpr visitNodeInterfaceExitsNetwork(NodeInterfaceExitsNetwork state) {
    return new OriginateInterfaceLink(state.getHostname(), state.getIface());
  }

  @Override
  public StateExpr visitNodeInterfaceInsufficientInfo(
      NodeInterfaceInsufficientInfo nodeInterfaceInsufficientInfo) {
    return null;
  }

  @Override
  public StateExpr visitNodeInterfaceNeighborUnreachable(
      NodeInterfaceNeighborUnreachable nodeInterfaceNeighborUnreachable) {
    return null;
  }

  @Override
  public StateExpr visitOriginateInterfaceLink(OriginateInterfaceLink originateInterfaceLink) {
    return null;
  }

  @Override
  public StateExpr visitOriginateVrf(OriginateVrf originateVrf) {
    return null;
  }

  @Override
  public StateExpr visitPostInInterface(PostInInterface postInInterface) {
    return null;
  }

  @Override
  public StateExpr visitPostInInterfacePostNat(PostInInterfacePostNat postInInterfacePostNat) {
    return null;
  }

  @Override
  public StateExpr visitPostInVrf(PostInVrf postInVrf) {
    return null;
  }

  @Override
  public StateExpr visitPreInInterface(PreInInterface preInInterface) {
    return null;
  }

  @Override
  public StateExpr visitPreOutVrf(PreOutVrf preOutVrf) {
    return null;
  }

  @Override
  public StateExpr visitPreOutEdge(PreOutEdge preOutEdge) {
    return null;
  }

  @Override
  public StateExpr visitPreOutEdgePostNat(PreOutEdgePostNat preOutInterface) {
    return null;
  }

  @Override
  public StateExpr visitPreOutInterfaceDeliveredToSubnet(
      PreOutInterfaceDeliveredToSubnet preOutInterfaceDeliveredToSubnet) {
    return null;
  }

  @Override
  public StateExpr visitPreOutInterfaceExitsNetwork(
      PreOutInterfaceExitsNetwork preOutInterfaceExitsNetwork) {
    return null;
  }

  @Override
  public StateExpr visitPreOutInterfaceInsufficientInfo(
      PreOutInterfaceInsufficientInfo preOutInterfaceInsufficientInfo) {
    return null;
  }

  @Override
  public StateExpr visitPreOutInterfaceNeighborUnreachable(
      PreOutInterfaceNeighborUnreachable preOutInterfaceNeighborUnreachable) {
    return null;
  }

  @Override
  public StateExpr visitQuery() {
    return null;
  }
}

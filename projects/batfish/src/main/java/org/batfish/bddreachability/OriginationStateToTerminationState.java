package org.batfish.bddreachability;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.symbolic.state.NodeAccept;
import org.batfish.symbolic.state.NodeDropAclIn;
import org.batfish.symbolic.state.NodeDropAclOut;
import org.batfish.symbolic.state.NodeDropNoRoute;
import org.batfish.symbolic.state.NodeDropNullRoute;
import org.batfish.symbolic.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.NodeInterfaceExitsNetwork;
import org.batfish.symbolic.state.NodeInterfaceInsufficientInfo;
import org.batfish.symbolic.state.NodeInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.PostInInterface;
import org.batfish.symbolic.state.PostInInterfacePostNat;
import org.batfish.symbolic.state.PostInVrf;
import org.batfish.symbolic.state.PreInInterface;
import org.batfish.symbolic.state.PreOutEdge;
import org.batfish.symbolic.state.PreOutEdgePostNat;
import org.batfish.symbolic.state.PreOutInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.PreOutInterfaceExitsNetwork;
import org.batfish.symbolic.state.PreOutInterfaceInsufficientInfo;
import org.batfish.symbolic.state.PreOutInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.PreOutVrf;
import org.batfish.symbolic.state.StateExpr;
import org.batfish.symbolic.state.StateExprVisitor;

/**
 * Convert origination states to their corresponding termination states. Returns null for
 * non-origination states.
 */
public class OriginationStateToTerminationState implements StateExprVisitor<List<StateExpr>> {
  private static final OriginationStateToTerminationState INSTANCE =
      new OriginationStateToTerminationState();

  private OriginationStateToTerminationState() {}

  static @Nullable List<StateExpr> originationStateToTerminationState(StateExpr expr) {
    return expr.accept(INSTANCE);
  }

  @Override
  public List<StateExpr> visitAccept() {
    return null;
  }

  @Override
  public List<StateExpr> visitDropAclIn() {
    return null;
  }

  @Override
  public List<StateExpr> visitDropAclOut() {
    return null;
  }

  @Override
  public List<StateExpr> visitDropNoRoute() {
    return null;
  }

  @Override
  public List<StateExpr> visitDropNullRoute() {
    return null;
  }

  @Override
  public List<StateExpr> visitExitsNetwork() {
    return null;
  }

  @Override
  public List<StateExpr> visitDeliveredToSubnet() {
    return null;
  }

  @Override
  public List<StateExpr> visitInsufficientInfo() {
    return null;
  }

  @Override
  public List<StateExpr> visitNeighborUnreachable() {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeAccept(NodeAccept nodeAccept) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeDropAclIn(NodeDropAclIn nodeDropAclIn) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeDropAclOut(NodeDropAclOut nodeDropAclOut) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeDropNoRoute(NodeDropNoRoute nodeDropNoRoute) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeDropNullRoute(NodeDropNullRoute nodeDropNullRoute) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeInterfaceDeliveredToSubnet(
      NodeInterfaceDeliveredToSubnet nodeInterfaceDeliveredToSubnet) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeInterfaceExitsNetwork(
      NodeInterfaceExitsNetwork nodeInterfaceExitsNetwork) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeInterfaceInsufficientInfo(
      NodeInterfaceInsufficientInfo nodeInterfaceInsufficientInfo) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeInterfaceNeighborUnreachable(
      NodeInterfaceNeighborUnreachable nodeInterfaceNeighborUnreachable) {
    return null;
  }

  @Override
  public List<StateExpr> visitOriginateInterfaceLink(OriginateInterfaceLink state) {
    String hostname = state.getHostname();
    String iface = state.getInterface();
    return ImmutableList.of(
        new NodeInterfaceDeliveredToSubnet(hostname, iface),
        new NodeInterfaceExitsNetwork(hostname, iface),
        new NodeInterfaceNeighborUnreachable(hostname, iface),
        new NodeInterfaceInsufficientInfo(hostname, iface));
  }

  @Override
  public List<StateExpr> visitOriginateVrf(OriginateVrf originateVrf) {
    // TODO: we need a NodeVrfAccept here.
    return ImmutableList.of(new NodeAccept(originateVrf.getHostname()));
  }

  @Override
  public List<StateExpr> visitPostInInterface(PostInInterface postInInterface) {
    return null;
  }

  @Override
  public List<StateExpr> visitPostInInterfacePostNat(
      PostInInterfacePostNat postInInterfacePostNat) {
    return null;
  }

  @Override
  public List<StateExpr> visitPostInVrf(PostInVrf postInVrf) {
    return null;
  }

  @Override
  public List<StateExpr> visitPreInInterface(PreInInterface preInInterface) {
    return null;
  }

  @Override
  public List<StateExpr> visitPreOutVrf(PreOutVrf preOutVrf) {
    return null;
  }

  @Override
  public List<StateExpr> visitPreOutEdge(PreOutEdge preOutEdge) {
    return null;
  }

  @Override
  public List<StateExpr> visitPreOutEdgePostNat(PreOutEdgePostNat preOutInterface) {
    return null;
  }

  @Override
  public List<StateExpr> visitPreOutInterfaceDeliveredToSubnet(
      PreOutInterfaceDeliveredToSubnet preOutInterfaceDeliveredToSubnet) {
    return null;
  }

  @Override
  public List<StateExpr> visitPreOutInterfaceExitsNetwork(
      PreOutInterfaceExitsNetwork preOutInterfaceExitsNetwork) {
    return null;
  }

  @Override
  public List<StateExpr> visitPreOutInterfaceInsufficientInfo(
      PreOutInterfaceInsufficientInfo preOutInterfaceInsufficientInfo) {
    return null;
  }

  @Override
  public List<StateExpr> visitPreOutInterfaceNeighborUnreachable(
      PreOutInterfaceNeighborUnreachable preOutInterfaceNeighborUnreachable) {
    return null;
  }

  @Override
  public List<StateExpr> visitQuery() {
    return null;
  }
}

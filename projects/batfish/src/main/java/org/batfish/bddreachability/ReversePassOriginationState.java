package org.batfish.bddreachability;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.batfish.symbolic.state.InterfaceAccept;
import org.batfish.symbolic.state.NodeAccept;
import org.batfish.symbolic.state.NodeDropAclIn;
import org.batfish.symbolic.state.NodeDropAclOut;
import org.batfish.symbolic.state.NodeDropNoRoute;
import org.batfish.symbolic.state.NodeDropNullRoute;
import org.batfish.symbolic.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.NodeInterfaceExitsNetwork;
import org.batfish.symbolic.state.NodeInterfaceInsufficientInfo;
import org.batfish.symbolic.state.NodeInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.OriginateInterface;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.PbrFibLookup;
import org.batfish.symbolic.state.PostInInterface;
import org.batfish.symbolic.state.PostInInterfacePostNat;
import org.batfish.symbolic.state.PostInVrf;
import org.batfish.symbolic.state.PostInVrfSession;
import org.batfish.symbolic.state.PreInInterface;
import org.batfish.symbolic.state.PreOutEdge;
import org.batfish.symbolic.state.PreOutEdgePostNat;
import org.batfish.symbolic.state.PreOutInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.PreOutInterfaceExitsNetwork;
import org.batfish.symbolic.state.PreOutInterfaceInsufficientInfo;
import org.batfish.symbolic.state.PreOutInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.PreOutVrf;
import org.batfish.symbolic.state.PreOutVrfSession;
import org.batfish.symbolic.state.StateExpr;
import org.batfish.symbolic.state.StateExprVisitor;
import org.batfish.symbolic.state.VrfAccept;

/**
 * Converts successful flow termination states from the forward pass of a bidirectional reachability
 * query to the corresponding origination state for the return pass (if any). If the state does not
 * have a corresponding origination state, or if the state should be filtered out because it does
 * not correspond to a permitted final node, returns {@code null}.
 *
 * <p>{@link NodeInterfaceDeliveredToSubnet} and {@link NodeInterfaceExitsNetwork} states are mapped
 * to corresponding {@link OriginateInterfaceLink} states.
 *
 * <p>{@link VrfAccept} states are mapped to corresponding {@link OriginateVrf} states.
 */
public class ReversePassOriginationState implements StateExprVisitor<StateExpr> {
  private final @Nonnull Predicate<String> _isFinalNode;

  /**
   * Construct a {@link ReversePassOriginationState}.
   *
   * @param isFinalNode Only return non-null origination states when the state being visited is for
   *     a final node as determined by {@code isFinalNode}.
   */
  public ReversePassOriginationState(Predicate<String> isFinalNode) {
    _isFinalNode = isFinalNode;
  }

  @Override
  public StateExpr visitAccept() {
    return null;
  }

  @Override
  public StateExpr visitBlackHole() {
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
  public StateExpr visitInterfaceAccept(InterfaceAccept interfaceAccept) {
    String hostname = interfaceAccept.getHostname();
    return _isFinalNode.test(hostname)
        ? new OriginateInterface(hostname, interfaceAccept.getInterface())
        : null;
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
    String hostname = state.getHostname();
    return _isFinalNode.test(hostname)
        ? new OriginateInterfaceLink(hostname, state.getInterface())
        : null;
  }

  @Override
  public StateExpr visitNodeInterfaceExitsNetwork(NodeInterfaceExitsNetwork state) {
    String hostname = state.getHostname();
    return _isFinalNode.test(hostname)
        ? new OriginateInterfaceLink(hostname, state.getInterface())
        : null;
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
  public StateExpr visitOriginateInterface(OriginateInterface originateInterface) {
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
  public StateExpr visitPbrFibLookup(PbrFibLookup pbrFibLookup) {
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

  @Override
  public StateExpr visitPostInVrfSession(PostInVrfSession postInVrfSession) {
    return null;
  }

  @Override
  public StateExpr visitPreOutVrfSession(PreOutVrfSession preOutVrfSession) {
    return null;
  }

  @Override
  public StateExpr visitVrfAccept(VrfAccept vrfAccept) {
    String hostname = vrfAccept.getHostname();
    return _isFinalNode.test(hostname) ? new OriginateVrf(hostname, vrfAccept.getVrf()) : null;
  }
}

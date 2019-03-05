package org.batfish.bddreachability;

import javax.annotation.Nullable;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.TransformationExpr;
import org.batfish.z3.expr.TransformationStepExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.AclDeny;
import org.batfish.z3.state.AclLineIndependentMatch;
import org.batfish.z3.state.AclLineMatch;
import org.batfish.z3.state.AclLineNoMatch;
import org.batfish.z3.state.AclPermit;
import org.batfish.z3.state.Debug;
import org.batfish.z3.state.DeliveredToSubnet;
import org.batfish.z3.state.Drop;
import org.batfish.z3.state.DropAcl;
import org.batfish.z3.state.DropAclIn;
import org.batfish.z3.state.DropAclOut;
import org.batfish.z3.state.DropNoRoute;
import org.batfish.z3.state.DropNullRoute;
import org.batfish.z3.state.ExitsNetwork;
import org.batfish.z3.state.InsufficientInfo;
import org.batfish.z3.state.NeighborUnreachable;
import org.batfish.z3.state.NeighborUnreachableOrExitsNetwork;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDrop;
import org.batfish.z3.state.NodeDropAcl;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.z3.state.NodeInterfaceExitsNetwork;
import org.batfish.z3.state.NodeInterfaceInsufficientInfo;
import org.batfish.z3.state.NodeInterfaceNeighborUnreachable;
import org.batfish.z3.state.NodeInterfaceNeighborUnreachableOrExitsNetwork;
import org.batfish.z3.state.NodeNeighborUnreachableOrExitsNetwork;
import org.batfish.z3.state.NumberedQuery;
import org.batfish.z3.state.OriginateInterfaceLink;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostInInterface;
import org.batfish.z3.state.PostInInterfacePostNat;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PostOutEdge;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutEdgePostNat;
import org.batfish.z3.state.PreOutInterfaceDeliveredToSubnet;
import org.batfish.z3.state.PreOutInterfaceExitsNetwork;
import org.batfish.z3.state.PreOutInterfaceInsufficientInfo;
import org.batfish.z3.state.PreOutInterfaceNeighborUnreachable;
import org.batfish.z3.state.PreOutVrf;
import org.batfish.z3.state.Query;
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
  public StateExpr castToGenericStateExprVisitorReturnType(Object o) {
    return (StateExpr) o;
  }

  @Override
  public StateExpr visitAccept(Accept accept) {
    return null;
  }

  @Override
  public StateExpr visitAclDeny(AclDeny aclDeny) {
    return null;
  }

  @Override
  public StateExpr visitAclLineIndependentMatch(AclLineIndependentMatch aclLineIndependentMatch) {
    return null;
  }

  @Override
  public StateExpr visitAclLineMatch(AclLineMatch aclLineMatch) {
    return null;
  }

  @Override
  public StateExpr visitAclLineNoMatch(AclLineNoMatch aclLineNoMatch) {
    return null;
  }

  @Override
  public StateExpr visitAclPermit(AclPermit aclPermit) {
    return null;
  }

  @Override
  public StateExpr visitDebug(Debug debug) {
    return null;
  }

  @Override
  public StateExpr visitDrop(Drop drop) {
    return null;
  }

  @Override
  public StateExpr visitDropAcl(DropAcl dropAcl) {
    return null;
  }

  @Override
  public StateExpr visitDropAclIn(DropAclIn dropAclIn) {
    return null;
  }

  @Override
  public StateExpr visitDropAclOut(DropAclOut dropAclOut) {
    return null;
  }

  @Override
  public StateExpr visitDropNoRoute(DropNoRoute dropNoRoute) {
    return null;
  }

  @Override
  public StateExpr visitDropNullRoute(DropNullRoute dropNullRoute) {
    return null;
  }

  @Override
  public StateExpr visitExitsNetwork(ExitsNetwork exitsNetwork) {
    return null;
  }

  @Override
  public StateExpr visitDeliveredToSubnet(DeliveredToSubnet deliveredToSubnet) {
    return null;
  }

  @Override
  public StateExpr visitInsufficientInfo(InsufficientInfo insufficientInfo) {
    return null;
  }

  @Override
  public StateExpr visitNeighborUnreachable(NeighborUnreachable neighborUnreachable) {
    return null;
  }

  @Override
  public StateExpr visitNeighborUnreachableOrExitsNetwork(
      NeighborUnreachableOrExitsNetwork neighborUnreachableOrExitsNetwork) {
    return null;
  }

  @Override
  public StateExpr visitNodeAccept(NodeAccept nodeAccept) {
    return null;
  }

  @Override
  public StateExpr visitNodeDrop(NodeDrop nodeDrop) {
    return null;
  }

  @Override
  public StateExpr visitNodeDropAcl(NodeDropAcl nodeDropAcl) {
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
  public StateExpr visitNodeInterfaceNeighborUnreachableOrExitsNetwork(
      NodeInterfaceNeighborUnreachableOrExitsNetwork nodeNeighborUnreachable) {
    return null;
  }

  @Override
  public StateExpr visitNodeNeighborUnreachableOrExitsNetwork(
      NodeNeighborUnreachableOrExitsNetwork nodeNeighborUnreachableOrExitsNetwork) {
    return null;
  }

  @Override
  public StateExpr visitNumberedQuery(NumberedQuery numberedQuery) {
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
  public StateExpr visitPostOutEdge(PostOutEdge preOutInterface) {
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
  public StateExpr visitTransformation(TransformationExpr transformationExpr) {
    return null;
  }

  @Override
  public StateExpr visitTransformationStep(TransformationStepExpr transformationStepExpr) {
    return null;
  }

  @Override
  public StateExpr visitQuery(Query query) {
    return null;
  }
}

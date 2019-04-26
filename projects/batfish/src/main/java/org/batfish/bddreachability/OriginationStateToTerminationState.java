package org.batfish.bddreachability;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.TransformationExpr;
import org.batfish.z3.expr.TransformationStepExpr;
import org.batfish.z3.state.AclDeny;
import org.batfish.z3.state.AclLineIndependentMatch;
import org.batfish.z3.state.AclLineMatch;
import org.batfish.z3.state.AclLineNoMatch;
import org.batfish.z3.state.AclPermit;
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
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

/**
 * Convert origination states to their corresponding termination states. Returns null for
 * non-origination states.
 */
public class OriginationStateToTerminationState
    implements GenericStateExprVisitor<List<StateExpr>> {
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
  public List<StateExpr> visitAclDeny(AclDeny aclDeny) {
    return null;
  }

  @Override
  public List<StateExpr> visitAclLineIndependentMatch(
      AclLineIndependentMatch aclLineIndependentMatch) {
    return null;
  }

  @Override
  public List<StateExpr> visitAclLineMatch(AclLineMatch aclLineMatch) {
    return null;
  }

  @Override
  public List<StateExpr> visitAclLineNoMatch(AclLineNoMatch aclLineNoMatch) {
    return null;
  }

  @Override
  public List<StateExpr> visitAclPermit(AclPermit aclPermit) {
    return null;
  }

  @Override
  public List<StateExpr> visitDebug() {
    return null;
  }

  @Override
  public List<StateExpr> visitDrop() {
    return null;
  }

  @Override
  public List<StateExpr> visitDropAcl() {
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
  public List<StateExpr> visitNeighborUnreachableOrExitsNetwork() {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeAccept(NodeAccept nodeAccept) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeDrop(NodeDrop nodeDrop) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeDropAcl(NodeDropAcl nodeDropAcl) {
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
  public List<StateExpr> visitNodeInterfaceNeighborUnreachableOrExitsNetwork(
      NodeInterfaceNeighborUnreachableOrExitsNetwork nodeNeighborUnreachable) {
    return null;
  }

  @Override
  public List<StateExpr> visitNodeNeighborUnreachableOrExitsNetwork(
      NodeNeighborUnreachableOrExitsNetwork nodeNeighborUnreachableOrExitsNetwork) {
    return null;
  }

  @Override
  public List<StateExpr> visitNumberedQuery(NumberedQuery numberedQuery) {
    return null;
  }

  @Override
  public List<StateExpr> visitOriginateInterfaceLink(OriginateInterfaceLink state) {
    String hostname = state.getHostname();
    String iface = state.getIface();
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
  public List<StateExpr> visitPostOutEdge(PostOutEdge preOutInterface) {
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
  public List<StateExpr> visitTransformation(TransformationExpr transformationExpr) {
    return null;
  }

  @Override
  public List<StateExpr> visitTransformationStep(TransformationStepExpr transformationStepExpr) {
    return null;
  }

  @Override
  public List<StateExpr> visitQuery() {
    return null;
  }
}

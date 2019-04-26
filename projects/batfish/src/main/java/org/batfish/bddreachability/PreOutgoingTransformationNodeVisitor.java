package org.batfish.bddreachability;

import org.batfish.datamodel.collections.NodeInterfacePair;
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
 * If the input node occurs right before an outgoing transformation is applied (possibly with an
 * intermediate ACL), return the node/interface of that transformation. Otherwise, return null.
 */
public class PreOutgoingTransformationNodeVisitor
    implements GenericStateExprVisitor<NodeInterfacePair> {
  public static final PreOutgoingTransformationNodeVisitor INSTANCE =
      new PreOutgoingTransformationNodeVisitor();

  private PreOutgoingTransformationNodeVisitor() {}

  @Override
  public NodeInterfacePair visitAccept() {
    return null;
  }

  @Override
  public NodeInterfacePair visitAclDeny(AclDeny aclDeny) {
    return null;
  }

  @Override
  public NodeInterfacePair visitAclLineIndependentMatch(
      AclLineIndependentMatch aclLineIndependentMatch) {
    return null;
  }

  @Override
  public NodeInterfacePair visitAclLineMatch(AclLineMatch aclLineMatch) {
    return null;
  }

  @Override
  public NodeInterfacePair visitAclLineNoMatch(AclLineNoMatch aclLineNoMatch) {
    return null;
  }

  @Override
  public NodeInterfacePair visitAclPermit(AclPermit aclPermit) {
    return null;
  }

  @Override
  public NodeInterfacePair visitDebug() {
    return null;
  }

  @Override
  public NodeInterfacePair visitDrop() {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropAcl() {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropAclIn() {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropAclOut() {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropNoRoute() {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropNullRoute() {
    return null;
  }

  @Override
  public NodeInterfacePair visitExitsNetwork() {
    return null;
  }

  @Override
  public NodeInterfacePair visitDeliveredToSubnet() {
    return null;
  }

  @Override
  public NodeInterfacePair visitInsufficientInfo() {
    return null;
  }

  @Override
  public NodeInterfacePair visitNeighborUnreachable() {
    return null;
  }

  @Override
  public NodeInterfacePair visitNeighborUnreachableOrExitsNetwork() {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeAccept(NodeAccept nodeAccept) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeDrop(NodeDrop nodeDrop) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeDropAcl(NodeDropAcl nodeDropAcl) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeDropAclIn(NodeDropAclIn nodeDropAclIn) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeDropAclOut(NodeDropAclOut nodeDropAclOut) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeDropNoRoute(NodeDropNoRoute nodeDropNoRoute) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeDropNullRoute(NodeDropNullRoute nodeDropNullRoute) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeInterfaceDeliveredToSubnet(
      NodeInterfaceDeliveredToSubnet nodeInterfaceDeliveredToSubnet) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeInterfaceExitsNetwork(
      NodeInterfaceExitsNetwork nodeInterfaceExitsNetwork) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeInterfaceInsufficientInfo(
      NodeInterfaceInsufficientInfo nodeInterfaceInsufficientInfo) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeInterfaceNeighborUnreachable(
      NodeInterfaceNeighborUnreachable nodeInterfaceNeighborUnreachable) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeInterfaceNeighborUnreachableOrExitsNetwork(
      NodeInterfaceNeighborUnreachableOrExitsNetwork nodeNeighborUnreachable) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNodeNeighborUnreachableOrExitsNetwork(
      NodeNeighborUnreachableOrExitsNetwork nodeNeighborUnreachableOrExitsNetwork) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNumberedQuery(NumberedQuery numberedQuery) {
    return null;
  }

  @Override
  public NodeInterfacePair visitOriginateInterfaceLink(
      OriginateInterfaceLink originateInterfaceLink) {
    return null;
  }

  @Override
  public NodeInterfacePair visitOriginateVrf(OriginateVrf originateVrf) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPostInInterface(PostInInterface postInInterface) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPostInInterfacePostNat(
      PostInInterfacePostNat postInInterfacePostNat) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPostInVrf(PostInVrf postInVrf) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPostOutEdge(PostOutEdge preOutInterface) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPreInInterface(PreInInterface preInInterface) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPreOutVrf(PreOutVrf preOutVrf) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPreOutEdge(PreOutEdge preOutEdge) {
    return new NodeInterfacePair(preOutEdge.getSrcNode(), preOutEdge.getSrcIface());
  }

  @Override
  public NodeInterfacePair visitPreOutEdgePostNat(PreOutEdgePostNat preOutInterface) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPreOutInterfaceDeliveredToSubnet(
      PreOutInterfaceDeliveredToSubnet state) {
    return new NodeInterfacePair(state.getHostname(), state.getInterface());
  }

  @Override
  public NodeInterfacePair visitPreOutInterfaceExitsNetwork(PreOutInterfaceExitsNetwork state) {
    return new NodeInterfacePair(state.getHostname(), state.getInterface());
  }

  @Override
  public NodeInterfacePair visitPreOutInterfaceInsufficientInfo(
      PreOutInterfaceInsufficientInfo state) {
    return new NodeInterfacePair(state.getHostname(), state.getInterface());
  }

  @Override
  public NodeInterfacePair visitPreOutInterfaceNeighborUnreachable(
      PreOutInterfaceNeighborUnreachable state) {
    return new NodeInterfacePair(state.getHostname(), state.getInterface());
  }

  @Override
  public NodeInterfacePair visitTransformation(TransformationExpr transformationExpr) {
    return null;
  }

  @Override
  public NodeInterfacePair visitTransformationStep(TransformationStepExpr transformationStepExpr) {
    return null;
  }

  @Override
  public NodeInterfacePair visitQuery() {
    return null;
  }
}

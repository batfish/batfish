package org.batfish.bddreachability;

import org.batfish.datamodel.collections.NodeInterfacePair;
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
 * For {@link StateExpr StateExprs} that correspond to points where a firewall session would get
 * installed, return the (node,outgoing interface) of that point. Return null for all other
 * StateExprs.
 *
 * <p>Currently the session StateExprs are {@link NodeInterfaceDeliveredToSubnet}, {@link
 * NodeInterfaceExitsNetwork}, and {@link PreOutEdgePostNat}. Other ARP-failure StateExprs like
 * {@link NodeInterfaceNeighborUnreachable} are excluded because they are failure cases (we don't
 * need to track sessions for failures).
 */
final class SessionCreationNodeVisitor implements GenericStateExprVisitor<NodeInterfacePair> {
  public static final SessionCreationNodeVisitor INSTANCE = new SessionCreationNodeVisitor();

  private SessionCreationNodeVisitor() {}

  @Override
  public NodeInterfacePair castToGenericStateExprVisitorReturnType(Object o) {
    return (NodeInterfacePair) o;
  }

  @Override
  public NodeInterfacePair visitAccept(Accept accept) {
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
  public NodeInterfacePair visitDebug(Debug debug) {
    return null;
  }

  @Override
  public NodeInterfacePair visitDrop(Drop drop) {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropAcl(DropAcl dropAcl) {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropAclIn(DropAclIn dropAclIn) {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropAclOut(DropAclOut dropAclOut) {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropNoRoute(DropNoRoute dropNoRoute) {
    return null;
  }

  @Override
  public NodeInterfacePair visitDropNullRoute(DropNullRoute dropNullRoute) {
    return null;
  }

  @Override
  public NodeInterfacePair visitExitsNetwork(ExitsNetwork exitsNetwork) {
    return null;
  }

  @Override
  public NodeInterfacePair visitDeliveredToSubnet(DeliveredToSubnet deliveredToSubnet) {
    return null;
  }

  @Override
  public NodeInterfacePair visitInsufficientInfo(InsufficientInfo insufficientInfo) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNeighborUnreachable(NeighborUnreachable neighborUnreachable) {
    return null;
  }

  @Override
  public NodeInterfacePair visitNeighborUnreachableOrExitsNetwork(
      NeighborUnreachableOrExitsNetwork neighborUnreachableOrExitsNetwork) {
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
      NodeInterfaceDeliveredToSubnet expr) {
    return new NodeInterfacePair(expr.getHostname(), expr.getIface());
  }

  @Override
  public NodeInterfacePair visitNodeInterfaceExitsNetwork(NodeInterfaceExitsNetwork expr) {
    return new NodeInterfacePair(expr.getHostname(), expr.getIface());
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
      NodeInterfaceNeighborUnreachableOrExitsNetwork expr) {
    return new NodeInterfacePair(expr.getHostname(), expr.getIface());
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
    return null;
  }

  @Override
  public NodeInterfacePair visitPreOutEdgePostNat(PreOutEdgePostNat expr) {
    return new NodeInterfacePair(expr.getSrcNode(), expr.getSrcIface());
  }

  @Override
  public NodeInterfacePair visitPreOutInterfaceDeliveredToSubnet(
      PreOutInterfaceDeliveredToSubnet preOutInterfaceDeliveredToSubnet) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPreOutInterfaceExitsNetwork(
      PreOutInterfaceExitsNetwork preOutInterfaceExitsNetwork) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPreOutInterfaceInsufficientInfo(
      PreOutInterfaceInsufficientInfo preOutInterfaceInsufficientInfo) {
    return null;
  }

  @Override
  public NodeInterfacePair visitPreOutInterfaceNeighborUnreachable(
      PreOutInterfaceNeighborUnreachable preOutInterfaceNeighborUnreachable) {
    return null;
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
  public NodeInterfacePair visitQuery(Query query) {
    return null;
  }
}

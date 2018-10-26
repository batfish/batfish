package org.batfish.z3.state.visitors;

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
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PostOutEdge;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutEdgePostNat;
import org.batfish.z3.state.PreOutVrf;
import org.batfish.z3.state.Query;

public interface GenericStateExprVisitor<R> {

  R castToGenericStateExprVisitorReturnType(Object o);

  R visitAccept(Accept accept);

  R visitAclDeny(AclDeny aclDeny);

  R visitAclLineIndependentMatch(AclLineIndependentMatch aclLineIndependentMatch);

  R visitAclLineMatch(AclLineMatch aclLineMatch);

  R visitAclLineNoMatch(AclLineNoMatch aclLineNoMatch);

  R visitAclPermit(AclPermit aclPermit);

  R visitDebug(Debug debug);

  R visitDrop(Drop drop);

  R visitDropAcl(DropAcl dropAcl);

  R visitDropAclIn(DropAclIn dropAclIn);

  R visitDropAclOut(DropAclOut dropAclOut);

  R visitDropNoRoute(DropNoRoute dropNoRoute);

  R visitDropNullRoute(DropNullRoute dropNullRoute);

  R visitExitsNetwork(ExitsNetwork exitsNetwork);

  R visitDeliveredToSubnet(DeliveredToSubnet deliveredToSubnet);

  R visitInsufficientInfo(InsufficientInfo insufficientInfo);

  R visitNeighborUnreachable(NeighborUnreachable neighborUnreachable);

  R visitNeighborUnreachableOrExitsNetwork(
      NeighborUnreachableOrExitsNetwork neighborUnreachableOrExitsNetwork);

  R visitNodeAccept(NodeAccept nodeAccept);

  R visitNodeDrop(NodeDrop nodeDrop);

  R visitNodeDropAcl(NodeDropAcl nodeDropAcl);

  R visitNodeDropAclIn(NodeDropAclIn nodeDropAclIn);

  R visitNodeDropAclOut(NodeDropAclOut nodeDropAclOut);

  R visitNodeDropNoRoute(NodeDropNoRoute nodeDropNoRoute);

  R visitNodeDropNullRoute(NodeDropNullRoute nodeDropNullRoute);

  R visitNodeInterfaceDeliveredToSubnet(
      NodeInterfaceDeliveredToSubnet nodeInterfaceDeliveredToSubnet);

  R visitNodeInterfaceExitsNetwork(NodeInterfaceExitsNetwork nodeInterfaceExitsNetwork);

  R visitNodeInterfaceInsufficientInfo(NodeInterfaceInsufficientInfo nodeInterfaceInsufficientInfo);

  R visitNodeInterfaceNeighborUnreachable(
      NodeInterfaceNeighborUnreachable nodeInterfaceNeighborUnreachable);

  R visitNodeInterfaceNeighborUnreachableOrExitsNetwork(
      NodeInterfaceNeighborUnreachableOrExitsNetwork nodeNeighborUnreachable);

  R visitNodeNeighborUnreachableOrExitsNetwork(
      NodeNeighborUnreachableOrExitsNetwork nodeNeighborUnreachableOrExitsNetwork);

  R visitNumberedQuery(NumberedQuery numberedQuery);

  R visitOriginateInterfaceLink(OriginateInterfaceLink originateInterfaceLink);

  R visitOriginateVrf(OriginateVrf originateVrf);

  R visitPostInInterface(PostInInterface postInInterface);

  R visitPostInVrf(PostInVrf postInVrf);

  R visitPostOutEdge(PostOutEdge preOutInterface);

  R visitPreInInterface(PreInInterface preInInterface);

  R visitPreOutVrf(PreOutVrf preOutVrf);

  R visitPreOutEdge(PreOutEdge preOutEdge);

  R visitPreOutEdgePostNat(PreOutEdgePostNat preOutInterface);

  R visitQuery(Query query);
}

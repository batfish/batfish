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
import org.batfish.z3.state.NodeNeighborUnreachableOrExitsNetwork.State;
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

public interface StateVisitor {

  void visitAccept(Accept.State accept);

  void visitAclDeny(AclDeny.State aclDeny);

  void visitAclLineIndependentMatch(AclLineIndependentMatch.State state);

  void visitAclLineMatch(AclLineMatch.State aclLineMatch);

  void visitAclLineNoMatch(AclLineNoMatch.State aclLineNoMatch);

  void visitAclPermit(AclPermit.State aclPermit);

  void visitDebug(Debug.State debug);

  void visitDrop(Drop.State drop);

  void visitDropAcl(DropAcl.State dropAcl);

  void visitDropAclIn(DropAclIn.State dropAclIn);

  void visitDropAclOut(DropAclOut.State dropAclOut);

  void visitDropNoRoute(DropNoRoute.State dropNoRoute);

  void visitDropNullRoute(DropNullRoute.State dropNullRoute);

  void visitExitsNetwork(ExitsNetwork.State state);

  void visitDeliveredToSubnet(DeliveredToSubnet.State state);

  void visitInsufficientInfo(InsufficientInfo.State state);

  void visitNeighborUnreachable(NeighborUnreachable.State state);

  void visitNeighborUnreachableOrExitsNetwork(NeighborUnreachableOrExitsNetwork.State state);

  void visitNodeAccept(NodeAccept.State nodeAccept);

  void visitNodeDrop(NodeDrop.State nodeDrop);

  void visitNodeDropAcl(NodeDropAcl.State nodeDropAcl);

  void visitNodeDropAclIn(NodeDropAclIn.State nodeDropAclIn);

  void visitNodeDropAclOut(NodeDropAclOut.State nodeDropAclOut);

  void visitNodeDropNoRoute(NodeDropNoRoute.State nodeDropNoRoute);

  void visitNodeDropNullRoute(NodeDropNullRoute.State nodeDropNullRoute);

  void visitNodeInterfaceExitsNetwork(NodeInterfaceExitsNetwork.State state);

  void visitNodeInterfaceDeliveredToSubnet(NodeInterfaceDeliveredToSubnet.State state);

  void visitNodeInterfaceInsufficientInfo(NodeInterfaceInsufficientInfo.State state);

  void visitNodeInterfaceNeighborUnreachable(NodeInterfaceNeighborUnreachable.State state);

  void visitNodeInterfaceNeighborUnreachableOrExitsNetwork(
      NodeInterfaceNeighborUnreachableOrExitsNetwork.State state);

  void visitNodeNeighborUnreachableOrExitsNetwork(State state);

  void visitNumberedQuery(NumberedQuery.State numberedQuery);

  void visitOriginateInterfaceLink(OriginateInterfaceLink.State state);

  void visitOriginateVrf(OriginateVrf.State originateVrf);

  void visitPostInInterface(PostInInterface.State postInInterface);

  void visitPostInVrf(PostInVrf.State postInVrf);

  void visitPostOutEdge(PostOutEdge.State state);

  void visitPreInInterface(PreInInterface.State preInInterface);

  void visitPreOutVrf(PreOutVrf.State preOut);

  void visitPreOutEdge(PreOutEdge.State preOutEdge);

  void visitPreOutEdgePostNat(PreOutEdgePostNat.State state);

  void visitQuery(Query.State query);
}

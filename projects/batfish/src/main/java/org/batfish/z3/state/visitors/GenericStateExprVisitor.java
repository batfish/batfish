package org.batfish.z3.state.visitors;

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

public interface GenericStateExprVisitor<R> {

  R visitAccept();

  R visitDropAclIn();

  R visitDropAclOut();

  R visitDropNoRoute();

  R visitDropNullRoute();

  R visitExitsNetwork();

  R visitDeliveredToSubnet();

  R visitInsufficientInfo();

  R visitNeighborUnreachable();

  R visitNodeAccept(NodeAccept nodeAccept);

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

  R visitOriginateInterfaceLink(OriginateInterfaceLink originateInterfaceLink);

  R visitOriginateVrf(OriginateVrf originateVrf);

  R visitPostInInterface(PostInInterface postInInterface);

  R visitPostInInterfacePostNat(PostInInterfacePostNat postInInterfacePostNat);

  R visitPostInVrf(PostInVrf postInVrf);

  R visitPreInInterface(PreInInterface preInInterface);

  R visitPreOutVrf(PreOutVrf preOutVrf);

  R visitPreOutEdge(PreOutEdge preOutEdge);

  R visitPreOutEdgePostNat(PreOutEdgePostNat preOutInterface);

  R visitPreOutInterfaceDeliveredToSubnet(
      PreOutInterfaceDeliveredToSubnet preOutInterfaceDeliveredToSubnet);

  R visitPreOutInterfaceExitsNetwork(PreOutInterfaceExitsNetwork preOutInterfaceExitsNetwork);

  R visitPreOutInterfaceInsufficientInfo(
      PreOutInterfaceInsufficientInfo preOutInterfaceInsufficientInfo);

  R visitPreOutInterfaceNeighborUnreachable(
      PreOutInterfaceNeighborUnreachable preOutInterfaceNeighborUnreachable);

  R visitQuery();
}

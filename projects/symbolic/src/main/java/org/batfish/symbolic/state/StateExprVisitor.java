package org.batfish.symbolic.state;

public interface StateExprVisitor<R> {

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

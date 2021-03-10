package org.batfish.symbolic.state;

public interface StateExprVisitor<R> {

  R visitAccept();

  R visitBlackHole();

  R visitDeliveredToSubnet();

  R visitDropAclIn();

  R visitDropAclOut();

  R visitDropNoRoute();

  R visitDropNullRoute();

  R visitExitsNetwork();

  R visitInsufficientInfo();

  R visitInterfaceAccept(InterfaceAccept interfaceAccept);

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

  R visitOriginateInterface(OriginateInterface originateInterface);

  R visitOriginateInterfaceLink(OriginateInterfaceLink originateInterfaceLink);

  R visitOriginateVrf(OriginateVrf originateVrf);

  R visitPbrFibLookup(PbrFibLookup pbrFibLookup);

  R visitPostInInterface(PostInInterface postInInterface);

  R visitPostInInterfacePostNat(PostInInterfacePostNat postInInterfacePostNat);

  R visitPostInVrf(PostInVrf postInVrf);

  R visitPostInVrfSession(PostInVrfSession postInVrfSession);

  R visitPreInInterface(PreInInterface preInInterface);

  R visitPreOutEdge(PreOutEdge preOutEdge);

  R visitPreOutEdgePostNat(PreOutEdgePostNat preOutInterface);

  R visitPreOutInterfaceDeliveredToSubnet(
      PreOutInterfaceDeliveredToSubnet preOutInterfaceDeliveredToSubnet);

  R visitPreOutInterfaceExitsNetwork(PreOutInterfaceExitsNetwork preOutInterfaceExitsNetwork);

  R visitPreOutInterfaceInsufficientInfo(
      PreOutInterfaceInsufficientInfo preOutInterfaceInsufficientInfo);

  R visitPreOutInterfaceNeighborUnreachable(
      PreOutInterfaceNeighborUnreachable preOutInterfaceNeighborUnreachable);

  R visitPreOutVrf(PreOutVrf preOutVrf);

  R visitPreOutVrfSession(PreOutVrfSession preOutVrfSession);

  R visitQuery();

  R visitVrfAccept(VrfAccept vrfAccept);
}

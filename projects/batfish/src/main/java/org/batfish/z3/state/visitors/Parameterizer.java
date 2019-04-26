package org.batfish.z3.state.visitors;

import static org.batfish.z3.state.StateParameter.Type.INTERFACE;
import static org.batfish.z3.state.StateParameter.Type.NODE;
import static org.batfish.z3.state.StateParameter.Type.VRF;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.z3.expr.StateExpr;
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
import org.batfish.z3.state.StateParameter;

public class Parameterizer implements GenericStateExprVisitor<List<StateParameter>> {

  public static List<StateParameter> getParameters(StateExpr stateExpr) {
    Parameterizer visitor = new Parameterizer();
    return stateExpr.accept(visitor);
  }

  private Parameterizer() {}

  @Override
  public List<StateParameter> visitAccept() {
    return ImmutableList.of();
  }

  @Override
  public List<StateParameter> visitDropAclIn() {
    return ImmutableList.of();
  }

  @Override
  public List<StateParameter> visitDropAclOut() {
    return ImmutableList.of();
  }

  @Override
  public List<StateParameter> visitDropNoRoute() {
    return ImmutableList.of();
  }

  @Override
  public List<StateParameter> visitDropNullRoute() {
    return ImmutableList.of();
  }

  @Override
  public List<StateParameter> visitExitsNetwork() {
    return ImmutableList.of();
  }

  @Override
  public List<StateParameter> visitDeliveredToSubnet() {
    return ImmutableList.of();
  }

  @Override
  public List<StateParameter> visitInsufficientInfo() {
    return ImmutableList.of();
  }

  @Override
  public List<StateParameter> visitNeighborUnreachable() {
    return ImmutableList.of();
  }

  @Override
  public List<StateParameter> visitNodeAccept(NodeAccept nodeAccept) {
    return ImmutableList.of(new StateParameter(nodeAccept.getHostname(), NODE));
  }

  @Override
  public List<StateParameter> visitNodeDropAclIn(NodeDropAclIn nodeDropAclIn) {
    return ImmutableList.of(new StateParameter(nodeDropAclIn.getHostname(), NODE));
  }

  @Override
  public List<StateParameter> visitNodeDropAclOut(NodeDropAclOut nodeDropAclOut) {
    return ImmutableList.of(new StateParameter(nodeDropAclOut.getHostname(), NODE));
  }

  @Override
  public List<StateParameter> visitNodeDropNoRoute(NodeDropNoRoute nodeDropNoRoute) {
    return ImmutableList.of(new StateParameter(nodeDropNoRoute.getHostname(), NODE));
  }

  @Override
  public List<StateParameter> visitNodeDropNullRoute(NodeDropNullRoute nodeDropNullRoute) {
    return ImmutableList.of(new StateParameter(nodeDropNullRoute.getHostname(), NODE));
  }

  @Override
  public List<StateParameter> visitNodeInterfaceDeliveredToSubnet(
      NodeInterfaceDeliveredToSubnet nodeIfaceDeliveredToSubnet) {
    return ImmutableList.of(
        new StateParameter(nodeIfaceDeliveredToSubnet.getHostname(), NODE),
        new StateParameter(nodeIfaceDeliveredToSubnet.getIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitNodeInterfaceExitsNetwork(
      NodeInterfaceExitsNetwork nodeIfaceExitsNetwork) {
    return ImmutableList.of(
        new StateParameter(nodeIfaceExitsNetwork.getHostname(), NODE),
        new StateParameter(nodeIfaceExitsNetwork.getIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitNodeInterfaceInsufficientInfo(
      NodeInterfaceInsufficientInfo nodeIfaceInsufficientInfo) {
    return ImmutableList.of(
        new StateParameter(nodeIfaceInsufficientInfo.getHostname(), NODE),
        new StateParameter(nodeIfaceInsufficientInfo.getIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitNodeInterfaceNeighborUnreachable(
      NodeInterfaceNeighborUnreachable nodeInterfaceNeighborUnreachable) {
    return ImmutableList.of(
        new StateParameter(nodeInterfaceNeighborUnreachable.getHostname(), NODE),
        new StateParameter(nodeInterfaceNeighborUnreachable.getIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitOriginateInterfaceLink(
      OriginateInterfaceLink originateInterfaceLink) {
    return ImmutableList.of(
        new StateParameter(originateInterfaceLink.getHostname(), NODE),
        new StateParameter(originateInterfaceLink.getIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitOriginateVrf(OriginateVrf originateVrf) {
    return ImmutableList.of(
        new StateParameter(originateVrf.getHostname(), NODE),
        new StateParameter(originateVrf.getVrf(), VRF));
  }

  @Override
  public List<StateParameter> visitPostInInterface(PostInInterface postInInterface) {
    return ImmutableList.of(
        new StateParameter(postInInterface.getHostname(), NODE),
        new StateParameter(postInInterface.getIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitPostInInterfacePostNat(
      PostInInterfacePostNat postInInterfacePostNat) {
    return ImmutableList.of(
        new StateParameter(postInInterfacePostNat.getHostname(), NODE),
        new StateParameter(postInInterfacePostNat.getIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitPostInVrf(PostInVrf postInVrf) {
    return ImmutableList.of(
        new StateParameter(postInVrf.getHostname(), NODE),
        new StateParameter(postInVrf.getVrf(), VRF));
  }

  @Override
  public List<StateParameter> visitPreInInterface(PreInInterface preInInterface) {
    return ImmutableList.of(
        new StateParameter(preInInterface.getHostname(), NODE),
        new StateParameter(preInInterface.getIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitPreOutVrf(PreOutVrf preOutVrf) {
    return ImmutableList.of(
        new StateParameter(preOutVrf.getHostname(), NODE),
        new StateParameter(preOutVrf.getVrf(), VRF));
  }

  @Override
  public List<StateParameter> visitPreOutEdge(PreOutEdge preOutEdge) {
    return ImmutableList.of(
        new StateParameter(preOutEdge.getSrcNode(), NODE),
        new StateParameter(preOutEdge.getSrcIface(), INTERFACE),
        new StateParameter(preOutEdge.getDstNode(), NODE),
        new StateParameter(preOutEdge.getDstIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitPreOutEdgePostNat(PreOutEdgePostNat preOutEdgePostNat) {
    return ImmutableList.of(
        new StateParameter(preOutEdgePostNat.getSrcNode(), NODE),
        new StateParameter(preOutEdgePostNat.getSrcIface(), INTERFACE),
        new StateParameter(preOutEdgePostNat.getDstNode(), NODE),
        new StateParameter(preOutEdgePostNat.getDstIface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitPreOutInterfaceDeliveredToSubnet(
      PreOutInterfaceDeliveredToSubnet state) {
    return ImmutableList.of(
        new StateParameter(state.getHostname(), NODE),
        new StateParameter(state.getInterface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitPreOutInterfaceExitsNetwork(PreOutInterfaceExitsNetwork state) {
    return ImmutableList.of(
        new StateParameter(state.getHostname(), NODE),
        new StateParameter(state.getInterface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitPreOutInterfaceInsufficientInfo(
      PreOutInterfaceInsufficientInfo state) {
    return ImmutableList.of(
        new StateParameter(state.getHostname(), NODE),
        new StateParameter(state.getInterface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitPreOutInterfaceNeighborUnreachable(
      PreOutInterfaceNeighborUnreachable state) {
    return ImmutableList.of(
        new StateParameter(state.getHostname(), NODE),
        new StateParameter(state.getInterface(), INTERFACE));
  }

  @Override
  public List<StateParameter> visitQuery() {
    return ImmutableList.of();
  }
}

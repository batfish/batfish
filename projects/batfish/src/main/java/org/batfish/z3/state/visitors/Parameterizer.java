package org.batfish.z3.state.visitors;

import static org.batfish.z3.state.StateParameter.Type.ACL;
import static org.batfish.z3.state.StateParameter.Type.ACL_LINE;
import static org.batfish.z3.state.StateParameter.Type.INTERFACE;
import static org.batfish.z3.state.StateParameter.Type.NODE;
import static org.batfish.z3.state.StateParameter.Type.QUERY_NUMBER;
import static org.batfish.z3.state.StateParameter.Type.VRF;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.AclDeny;
import org.batfish.z3.state.AclLineMatch;
import org.batfish.z3.state.AclLineNoMatch;
import org.batfish.z3.state.AclPermit;
import org.batfish.z3.state.Debug;
import org.batfish.z3.state.Drop;
import org.batfish.z3.state.DropAcl;
import org.batfish.z3.state.DropAclIn;
import org.batfish.z3.state.DropAclOut;
import org.batfish.z3.state.DropNoRoute;
import org.batfish.z3.state.DropNullRoute;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDrop;
import org.batfish.z3.state.NodeDropAcl;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.NodeTransit;
import org.batfish.z3.state.NumberedQuery;
import org.batfish.z3.state.Originate;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostIn;
import org.batfish.z3.state.PostInInterface;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PostOutInterface;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOut;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutInterface;
import org.batfish.z3.state.Query;
import org.batfish.z3.state.StateParameter;

public class Parameterizer implements StateExprVisitor {

  public static List<StateParameter> getParameters(StateExpr stateExpr) {
    Parameterizer visitor = new Parameterizer();
    stateExpr.accept(visitor);
    return visitor._parameters;
  }

  private List<StateParameter> _parameters;

  private Parameterizer() {}

  @Override
  public void visitAccept(Accept accept) {
    _parameters = ImmutableList.of();
  }

  @Override
  public void visitAclDeny(AclDeny aclDeny) {
    _parameters =
        ImmutableList.of(
            new StateParameter(aclDeny.getHostname(), NODE),
            new StateParameter(aclDeny.getAcl(), ACL));
  }

  @Override
  public void visitAclLineMatch(AclLineMatch aclLineMatch) {
    _parameters =
        ImmutableList.of(
            new StateParameter(aclLineMatch.getHostname(), NODE),
            new StateParameter(aclLineMatch.getAcl(), ACL),
            new StateParameter(Integer.toString(aclLineMatch.getLine()), ACL_LINE));
  }

  @Override
  public void visitAclLineNoMatch(AclLineNoMatch aclLineNoMatch) {
    _parameters =
        ImmutableList.of(
            new StateParameter(aclLineNoMatch.getHostname(), NODE),
            new StateParameter(aclLineNoMatch.getAcl(), ACL),
            new StateParameter(Integer.toString(aclLineNoMatch.getLine()), ACL_LINE));
  }

  @Override
  public void visitAclPermit(AclPermit aclPermit) {
    _parameters =
        ImmutableList.of(
            new StateParameter(aclPermit.getHostname(), NODE),
            new StateParameter(aclPermit.getAcl(), ACL));
  }

  @Override
  public void visitDebug(Debug debug) {
    _parameters = ImmutableList.of();
  }

  @Override
  public void visitDrop(Drop drop) {
    _parameters = ImmutableList.of();
  }

  @Override
  public void visitDropAcl(DropAcl dropAcl) {
    _parameters = ImmutableList.of();
  }

  @Override
  public void visitDropAclIn(DropAclIn dropAclIn) {
    _parameters = ImmutableList.of();
  }

  @Override
  public void visitDropAclOut(DropAclOut dropAclOut) {
    _parameters = ImmutableList.of();
  }

  @Override
  public void visitDropNoRoute(DropNoRoute dropNoRoute) {
    _parameters = ImmutableList.of();
  }

  @Override
  public void visitDropNullRoute(DropNullRoute dropNullRoute) {
    _parameters = ImmutableList.of();
  }

  @Override
  public void visitNodeAccept(NodeAccept nodeAccept) {
    _parameters = ImmutableList.of(new StateParameter(nodeAccept.getHostname(), NODE));
  }

  @Override
  public void visitNodeDrop(NodeDrop nodeDrop) {
    _parameters = ImmutableList.of(new StateParameter(nodeDrop.getHostname(), NODE));
  }

  @Override
  public void visitNodeDropAcl(NodeDropAcl nodeDropAcl) {
    _parameters = ImmutableList.of(new StateParameter(nodeDropAcl.getHostname(), NODE));
  }

  @Override
  public void visitNodeDropAclIn(NodeDropAclIn nodeDropAclIn) {
    _parameters = ImmutableList.of(new StateParameter(nodeDropAclIn.getHostname(), NODE));
  }

  @Override
  public void visitNodeDropAclOut(NodeDropAclOut nodeDropAclOut) {
    _parameters = ImmutableList.of(new StateParameter(nodeDropAclOut.getHostname(), NODE));
  }

  @Override
  public void visitNodeDropNoRoute(NodeDropNoRoute nodeDropNoRoute) {
    _parameters = ImmutableList.of(new StateParameter(nodeDropNoRoute.getHostname(), NODE));
  }

  @Override
  public void visitNodeDropNullRoute(NodeDropNullRoute nodeDropNullRoute) {
    _parameters = ImmutableList.of(new StateParameter(nodeDropNullRoute.getHostname(), NODE));
  }

  @Override
  public void visitNodeTransit(NodeTransit nodeTransit) {
    _parameters = ImmutableList.of(new StateParameter(nodeTransit.getHostname(), NODE));
  }

  @Override
  public void visitNumberedQuery(NumberedQuery numberedQuery) {
    _parameters =
        ImmutableList.of(
            new StateParameter(Integer.toString(numberedQuery.getLine()), QUERY_NUMBER));
  }

  @Override
  public void visitOriginate(Originate originate) {
    _parameters = ImmutableList.of(new StateParameter(originate.getHostname(), NODE));
  }

  @Override
  public void visitOriginateVrf(OriginateVrf originateVrf) {
    _parameters =
        ImmutableList.of(
            new StateParameter(originateVrf.getHostname(), NODE),
            new StateParameter(originateVrf.getVrf(), VRF));
  }

  @Override
  public void visitPostIn(PostIn postIn) {
    _parameters = ImmutableList.of(new StateParameter(postIn.getHostname(), NODE));
  }

  @Override
  public void visitPostInInterface(PostInInterface postInInterface) {
    _parameters =
        ImmutableList.of(
            new StateParameter(postInInterface.getHostname(), NODE),
            new StateParameter(postInInterface.getIface(), INTERFACE));
  }

  @Override
  public void visitPostInVrf(PostInVrf postInVrf) {
    _parameters =
        ImmutableList.of(
            new StateParameter(postInVrf.getHostname(), NODE),
            new StateParameter(postInVrf.getVrf(), VRF));
  }

  @Override
  public void visitPostOutInterface(PostOutInterface postOutInterface) {
    _parameters =
        ImmutableList.of(
            new StateParameter(postOutInterface.getHostname(), NODE),
            new StateParameter(postOutInterface.getIface(), INTERFACE));
  }

  @Override
  public void visitPreInInterface(PreInInterface preInInterface) {
    _parameters =
        ImmutableList.of(
            new StateParameter(preInInterface.getHostname(), NODE),
            new StateParameter(preInInterface.getIface(), INTERFACE));
  }

  @Override
  public void visitPreOut(PreOut preOut) {
    _parameters = ImmutableList.of(new StateParameter(preOut.getHostname(), NODE));
  }

  @Override
  public void visitPreOutEdge(PreOutEdge preOutEdge) {
    _parameters =
        ImmutableList.of(
            new StateParameter(preOutEdge.getSrcNode(), NODE),
            new StateParameter(preOutEdge.getSrcIface(), INTERFACE),
            new StateParameter(preOutEdge.getDstNode(), NODE),
            new StateParameter(preOutEdge.getDstIface(), INTERFACE));
  }

  @Override
  public void visitPreOutInterface(PreOutInterface preOutInterface) {
    _parameters =
        ImmutableList.of(
            new StateParameter(preOutInterface.getHostname(), NODE),
            new StateParameter(preOutInterface.getIface(), INTERFACE));
  }

  @Override
  public void visitQuery(Query query) {
    _parameters = ImmutableList.of();
  }
}

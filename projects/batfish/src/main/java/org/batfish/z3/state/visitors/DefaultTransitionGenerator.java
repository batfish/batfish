package org.batfish.z3.state.visitors;

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

public class DefaultTransitionGenerator implements StateVisitor {

  @Override
  public void visitAccept(Accept accept) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitAclDeny(AclDeny aclDeny) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitAclLineMatch(AclLineMatch aclLineMatch) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitAclLineNoMatch(AclLineNoMatch aclLineNoMatch) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitAclPermit(AclPermit aclPermit) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitDebug(Debug debug) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitDrop(Drop drop) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitDropAcl(DropAcl dropAcl) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitDropAclIn(DropAclIn dropAclIn) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitDropAclOut(DropAclOut dropAclOut) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitDropNoRoute(DropNoRoute dropNoRoute) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitDropNullRoute(DropNullRoute dropNullRoute) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNodeAccept(NodeAccept nodeAccept) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNodeDrop(NodeDrop nodeDrop) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNodeDropAcl(NodeDropAcl nodeDropAcl) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNodeDropAclIn(NodeDropAclIn nodeDropAclIn) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNodeDropAclOut(NodeDropAclOut nodeDropAclOut) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNodeDropNoRoute(NodeDropNoRoute nodeDropNoRoute) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNodeDropNullRoute(NodeDropNullRoute nodeDropNullRoute) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNodeTransit(NodeTransit nodeTransit) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitNumberedQuery(NumberedQuery numberedQuery) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitOriginate(Originate originate) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitOriginateVrf(OriginateVrf originateVrf) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitPostIn(PostIn postIn) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitPostInInterface(PostInInterface postInInterface) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitPostInVrf(PostInVrf postInVrf) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitPostOutInterface(PostOutInterface postOutInterface) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitPreInInterface(PreInInterface preInInterface) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitPreOut(PreOut preOut) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitPreOutEdge(PreOutEdge preOutEdge) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitPreOutInterface(PreOutInterface preOutInterface) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void visitQuery(Query query) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}

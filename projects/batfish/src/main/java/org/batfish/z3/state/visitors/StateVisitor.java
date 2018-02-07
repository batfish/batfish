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

public interface StateVisitor {

  void visitAccept(Accept accept);

  void visitAclDeny(AclDeny aclDeny);

  void visitAclLineMatch(AclLineMatch aclLineMatch);

  void visitAclLineNoMatch(AclLineNoMatch aclLineNoMatch);

  void visitAclPermit(AclPermit aclPermit);

  void visitDebug(Debug debug);

  void visitDrop(Drop drop);

  void visitDropAcl(DropAcl dropAcl);

  void visitDropAclIn(DropAclIn dropAclIn);

  void visitDropAclOut(DropAclOut dropAclOut);

  void visitDropNoRoute(DropNoRoute dropNoRoute);

  void visitDropNullRoute(DropNullRoute dropNullRoute);

  void visitNodeAccept(NodeAccept nodeAccept);

  void visitNodeDrop(NodeDrop nodeDrop);

  void visitNodeDropAcl(NodeDropAcl nodeDropAcl);

  void visitNodeDropAclIn(NodeDropAclIn nodeDropAclIn);

  void visitNodeDropAclOut(NodeDropAclOut nodeDropAclOut);

  void visitNodeDropNoRoute(NodeDropNoRoute nodeDropNoRoute);

  void visitNodeDropNullRoute(NodeDropNullRoute nodeDropNullRoute);

  void visitNodeTransit(NodeTransit nodeTransit);

  void visitNumberedQuery(NumberedQuery numberedQuery);

  void visitOriginate(Originate originate);

  void visitOriginateVrf(OriginateVrf originateVrf);

  void visitPostIn(PostIn postIn);

  void visitPostInInterface(PostInInterface postInInterface);

  void visitPostInVrf(PostInVrf postInVrf);

  void visitPostOutInterface(PostOutInterface postOutInterface);

  void visitPreInInterface(PreInInterface preInInterface);

  void visitPreOut(PreOut preOut);

  void visitPreOutEdge(PreOutEdge preOutEdge);

  void visitPreOutInterface(PreOutInterface preOutInterface);

  void visitQuery(Query query);
}

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

public interface GenericStateExprVisitor<R> {

  R castToGenericStateExprVisitorReturnType(Object o);

  R visitAccept(Accept accept);

  R visitAclDeny(AclDeny aclDeny);

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

  R visitNodeAccept(NodeAccept nodeAccept);

  R visitNodeDrop(NodeDrop nodeDrop);

  R visitNodeDropAcl(NodeDropAcl nodeDropAcl);

  R visitNodeDropAclIn(NodeDropAclIn nodeDropAclIn);

  R visitNodeDropAclOut(NodeDropAclOut nodeDropAclOut);

  R visitNodeDropNoRoute(NodeDropNoRoute nodeDropNoRoute);

  R visitNodeDropNullRoute(NodeDropNullRoute nodeDropNullRoute);

  R visitNodeTransit(NodeTransit nodeTransit);

  R visitNumberedQuery(NumberedQuery numberedQuery);

  R visitOriginate(Originate originate);

  R visitOriginateVrf(OriginateVrf originateVrf);

  R visitPostIn(PostIn postIn);

  R visitPostInInterface(PostInInterface postInInterface);

  R visitPostInVrf(PostInVrf postInVrf);

  R visitPostOutInterface(PostOutInterface postOutInterface);

  R visitPreInInterface(PreInInterface preInInterface);

  R visitPreOut(PreOut preOut);

  R visitPreOutEdge(PreOutEdge preOutEdge);

  R visitPreOutInterface(PreOutInterface preOutInterface);

  R visitQuery(Query query);
}

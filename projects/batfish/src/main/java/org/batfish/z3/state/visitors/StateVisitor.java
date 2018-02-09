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

  void visitAccept(Accept.State accept);

  void visitAclDeny(AclDeny.State aclDeny);

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

  void visitNodeAccept(NodeAccept.State nodeAccept);

  void visitNodeDrop(NodeDrop.State nodeDrop);

  void visitNodeDropAcl(NodeDropAcl.State nodeDropAcl);

  void visitNodeDropAclIn(NodeDropAclIn.State nodeDropAclIn);

  void visitNodeDropAclOut(NodeDropAclOut.State nodeDropAclOut);

  void visitNodeDropNoRoute(NodeDropNoRoute.State nodeDropNoRoute);

  void visitNodeDropNullRoute(NodeDropNullRoute.State nodeDropNullRoute);

  void visitNodeTransit(NodeTransit.State nodeTransit);

  void visitNumberedQuery(NumberedQuery.State numberedQuery);

  void visitOriginate(Originate.State originate);

  void visitOriginateVrf(OriginateVrf.State originateVrf);

  void visitPostIn(PostIn.State postIn);

  void visitPostInInterface(PostInInterface.State postInInterface);

  void visitPostInVrf(PostInVrf.State postInVrf);

  void visitPostOutInterface(PostOutInterface.State postOutInterface);

  void visitPreInInterface(PreInInterface.State preInInterface);

  void visitPreOut(PreOut.State preOut);

  void visitPreOutEdge(PreOutEdge.State preOutEdge);

  void visitPreOutInterface(PreOutInterface.State preOutInterface);

  void visitQuery(Query.State query);
}

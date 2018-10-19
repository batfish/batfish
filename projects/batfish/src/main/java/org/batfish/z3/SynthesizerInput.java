package org.batfish.z3;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.state.AclPermit;
import org.batfish.z3.state.StateParameter.Type;

/**
 * The input to reachability program synthesis, including settings and marshalled information from
 * configurations and data-plane
 */
public interface SynthesizerInput {

  int getNodeInterfaceId(String node, String iface);

  /**
   * Mapping: hostname -&gt; aclName -&gt; lineNumber -&gt; lineAction <br>
   * This mapping contains only the acls that are required for the operation this {@link
   * SynthesizerInput} is being used for. Specifically, for ACL-reachability this should contain all
   * ACLs, while for other reachability queries this will only contain ACLs that a packet could
   * reasonably encounter (e.g. ACLs assigned to interfaces).
   */
  Map<String, Map<String, List<LineAction>>> getAclActions();

  /**
   * Mapping: hostname -&gt; aclName -&gt; lineNumber -&gt; lineConditions <br>
   * lineConditions is a boolean expression representing the constraints on a header necessary for
   * that line to be matched.
   */
  Map<String, Map<String, List<BooleanExpr>>> getAclConditions();

  /**
   * Mapping: hostname -&gt; vrfName -&gt; outInterface -&gt; recvNode -&gt; recvInterface -&gt;
   * dstIpConstraintForWhichArpReplySent
   */
  Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>> getArpTrueEdge();

  Set<Edge> getEnabledEdges();

  /** Mapping: hostname -&gt; interfaces */
  Map<String, Set<String>> getEnabledInterfaces();

  /** Mapping: hostname -&gt; vrf -&gt; interfaces */
  Map<String, Map<String, Set<String>>> getEnabledInterfacesByNodeVrf();

  Set<String> getEnabledNodes();

  /** Mapping: hostname -&gt; vrfs */
  Map<String, Set<String>> getEnabledVrfs();

  /** Mapping: hostname -&gt; interface-&gt; incomingAcl */
  Map<String, Map<String, String>> getIncomingAcls();

  /** Ingress locations grouped by the required constraint on src IP */
  Map<IngressLocation, BooleanExpr> getSrcIpConstraints();

  /** Mapping: hostname -&gt; ipsOwnedByHostname */
  Map<String, Set<Ip>> getIpsByHostname();

  /** Mapping: hostname -&gt; vrf -&gt; ipsOwnedByVrf */
  Map<String, Map<String, Set<Ip>>> getIpsByNodeVrf();

  /** Mapping: hostname -&gt; IpSpace name -&gt; IpSpace */
  Map<String, Map<String, IpSpace>> getNamedIpSpaces();

  /**
   * Mapping: hostname -&gt; vrfName -&gt; outInterface -&gt; dstIpConstraintForWhichNoArpReplySent
   */
  Map<String, Map<String, Map<String, BooleanExpr>>> getNeighborUnreachableOrExitsNetwork();

  /** Set of hostnames of nodes that have a firewall with a MatchSrcInterface AclLineMatchExpr */
  Set<String> getNodesWithSrcInterfaceConstraints();

  /** Set of nodes that should not be transited */
  Set<String> getNonTransitNodes();

  /** Mapping: hostname -&gt; vrfName -&gt; nullRoutedIps */
  Map<String, Map<String, BooleanExpr>> getNullRoutedIps();

  /** Mapping: hostname -&gt; interface-&gt; outgoingAcl */
  Map<String, Map<String, String>> getOutgoingAcls();

  /** Mapping: hostname -&gt; vrfName -&gt; routableIps */
  Map<String, Map<String, BooleanExpr>> getRoutableIps();

  /** Whether to run simplifier on AST after rule generation */
  boolean getSimplify();

  /**
   * Mapping: hostname -&gt; interface -&gt; [(preconditionPreTransformationState,
   * transformationToApply)]
   */
  Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>> getSourceNats();

  /** The set of nodes for which we should track whether they are transited */
  Set<String> getTransitNodes();

  /** Mapping: hostname -&gt; interfacesAllowedToBelongToAnEdge */
  Map<String, Set<String>> getTraversableInterfaces();

  /**
   * Set of parameter types that should be vectorized rather than baked into name of relations.<br>
   * Applies to NoD only.
   */
  Set<Type> getVectorizedParameters();

  /** @return A map from node to list of interface names in sorted order */
  Map<String, List<String>> getNodeInterfaces();

  /**
   * Get the field, if any, used to track the src interface for MatchSrcInterface AclMatchExprs. The
   * field is present only if the network has no MatchSrcInterface AclMatchExprs.
   */
  Field getSourceInterfaceField();

  /** Mapping: hostname -&gt; interface -&gt; constraint on transformed source interface field */
  Map<String, Map<String, IntExpr>> getSourceInterfaceFieldValues();

  /** Whether it's synthesizing data plane rules */
  boolean isDataPlane();
}

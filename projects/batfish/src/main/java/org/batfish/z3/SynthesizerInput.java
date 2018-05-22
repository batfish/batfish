package org.batfish.z3;

import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.specifier.Location;
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
   * Mapping: hostname -> aclName -> lineNumber -> lineAction <br>
   * This mapping contains only the acls that are required for the operation this {@link
   * SynthesizerInput} is being used for. Specifically, for ACL-reachability this should contain all
   * ACLs, while for other reachability queries this will only contain ACLs that a packet could
   * reasonably encounter (e.g. ACLs assigned to interfaces).
   */
  Map<String, Map<String, List<LineAction>>> getAclActions();

  /**
   * Mapping: hostname -> aclName -> lineNumber -> lineConditions <br>
   * lineConditions is a boolean expression representing the constraints on a header necessary for
   * that line to be matched.
   */
  Map<String, Map<String, List<BooleanExpr>>> getAclConditions();

  /**
   * Mapping: hostname -> vrfName -> outInterface -> recvNode -> recvInterface ->
   * dstIpConstraintForWhichArpReplySent
   */
  Map<String, Map<String, Map<String, Map<String, Map<String, BooleanExpr>>>>> getArpTrueEdge();

  Set<Edge> getEnabledEdges();

  /** Mapping: hostname -> interfaces */
  Map<String, Set<String>> getEnabledInterfaces();

  /** Mapping: hostname -> vrf -> interfaces */
  Map<String, Map<String, Set<String>>> getEnabledInterfacesByNodeVrf();

  Set<String> getEnabledNodes();

  /** Mapping: hostname -> vrfs */
  Map<String, Set<String>> getEnabledVrfs();

  /** Mapping: hostname -> interface-> incomingAcl */
  Map<String, Map<String, String>> getIncomingAcls();

  /** Ingress locations grouped by the required constraint on src IP */
  Multimap<BooleanExpr, Location> getIngressLocationsBySrcIpConstraint();

  /** Mapping: hostname -> ipsOwnedByHostname */
  Map<String, Set<Ip>> getIpsByHostname();

  /** Mapping: hostname -> vrf -> ipsOwnedByVrf */
  Map<String, Map<String, Set<Ip>>> getIpsByNodeVrf();

  /** Mapping: hostname -> IpSpace name -> IpSpace */
  Map<String, Map<String, IpSpace>> getNamedIpSpaces();

  /** Mapping: hostname -> vrfName -> outInterface -> dstIpConstraintForWhichNoArpReplySent */
  Map<String, Map<String, Map<String, BooleanExpr>>> getNeighborUnreachable();

  /** Set of hostnames of nodes that have a firewall with a MatchSrcInterface AclLineMatchExpr */
  Set<String> getNodesWithSrcInterfaceConstraints();

  /** Set of nodes that should not be transited */
  Set<String> getNonTransitNodes();

  /** Mapping: hostname -> vrfName -> nullRoutedIps */
  Map<String, Map<String, BooleanExpr>> getNullRoutedIps();

  /** Mapping: hostname -> interface-> outgoingAcl */
  Map<String, Map<String, String>> getOutgoingAcls();

  /** Mapping: hostname -> vrfName -> routableIps */
  Map<String, Map<String, BooleanExpr>> getRoutableIps();

  /** Whether to run simplifier on AST after rule generation */
  boolean getSimplify();

  /**
   * Mapping: hostname -> interface -> [(preconditionPreTransformationState, transformationToApply)]
   */
  Map<String, Map<String, List<Entry<AclPermit, BooleanExpr>>>> getSourceNats();

  /** The set of nodes for which we should track whether they are transited */
  Set<String> getTransitNodes();

  /** Mapping: hostname -> interfacesAllowedToBelongToAnEdge */
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

  /** Mapping: hostname -> interface -> constraint on transformed source interface field */
  Map<String, Map<String, IntExpr>> getSourceInterfaceFieldValues();

  /** Whether it's synthesizing data plane rules */
  boolean isDataPlane();
}

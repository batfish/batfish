package org.batfish.z3;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.state.StateParameter.Type;

public interface SynthesizerInput {

  /**
   * Mapping: hostname -> aclName -> lineNumber -> lineConditions <br>
   * lineConditions is a boolean expression representing the constraints on a header necessary for
   * that line to be matched.
   */
  Map<String, Map<String, Map<Integer, BooleanExpr>>> getAclConditions();

  /**
   * Mapping: hostname -> aclName -> lineNumber -> lineAction <br>
   * This mapping contains only the acls that are required for the operation this {@link
   * SynthesizerInput} is being used for. Specifically, for ACL-reachability this should contain all
   * ACLs, while for other reachability queries this will only contain ACLs that a packet could
   * reasonably encounter (e.g. ACLs assigned to interfaces).
   */
  Map<String, Map<String, Map<Integer, LineAction>>> getAclActions();

  Set<Edge> getEnabledEdges();

  Set<NodeInterfacePair> getEnabledFlowSinks();

  Map<String, Map<String, Interface>> getEnabledInterfaces();

  Map<String, Configuration> getEnabledNodes();

  Map<String, Map<String, Vrf>> getEnabledVrfs();

  Map<String, Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>>> getFibConditions();

  Map<String, Map<String, SortedSet<FibRow>>> getFibs();

  Map<String, Set<Ip>> getIpsByHostname();

  boolean getSimplify();

  Map<String, Set<Interface>> getTopologyInterfaces();

  Set<Type> getVectorizedParameters();
}

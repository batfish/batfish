package org.batfish.minesweeper.bdd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.CommunityVar.Type;
import org.batfish.minesweeper.Graph;

/**
 * Decides when certain routing information is unimportant when determining behavioral equivalence.
 *
 * <p>For example, if a set of configurations adds a community, but then never uses that community
 * in a match statement, then the community is unimportant for deciding the forwarding behavior. For
 * queries like reachability, this is a sound optimization
 *
 * <p>The set of optimizations can be expanded in the future. Right now it only optimized based on
 * BGP community tags
 */
public class PolicyQuotient {

  public static boolean ASSUME_COMMUNITY_LOCALITY = true;

  private Set<CommunityVar> _commsAssignedButNotMatched;

  /**
   * Set of REGEX community vars that are matched but not assigned (i.e. they have only OTHER
   * dependencies.
   */
  private Set<CommunityVar> _commsMatchedButNotAssigned;

  private Set<CommunityVar> _commsUsedOnlyLocally;

  public PolicyQuotient() {
    _commsAssignedButNotMatched = new HashSet<>();
    _commsMatchedButNotAssigned = new HashSet<>();
    _commsUsedOnlyLocally = new HashSet<>();
  }

  public PolicyQuotient(Graph graph) {
    _commsAssignedButNotMatched = new HashSet<>();

    // Compute the communities assigned but never matched
    Set<CommunityVar> usedComms = new HashSet<>();
    for (Entry<CommunityVar, List<CommunityVar>> entry :
        graph.getCommunityDependencies().entrySet()) {
      for (CommunityVar cvar : entry.getValue()) {
        if (cvar.getType() == Type.EXACT) {
          usedComms.add(cvar);
        }
      }
    }
    for (CommunityVar cvar : graph.getAllCommunities()) {
      if (cvar.getType() == Type.EXACT && !usedComms.contains(cvar)) {
        _commsAssignedButNotMatched.add(cvar);
      }
    }

    /*
     * Compute the communities that are matched but not assigned, i.e. their dependencies are
     * all OTHERs
     */
    _commsMatchedButNotAssigned = new HashSet<>();
    for (Entry<CommunityVar, List<CommunityVar>> entry :
        graph.getCommunityDependencies().entrySet()) {
      boolean isOnlyOtherType = true;
      for (CommunityVar cvar : entry.getValue()) {
        if (cvar.getType() != Type.OTHER) {
          isOnlyOtherType = false;
        }
      }
      if (isOnlyOtherType) {
        _commsMatchedButNotAssigned.add(entry.getKey());
      }
    }

    // Compute the communities that are just used locally
    _commsUsedOnlyLocally = new HashSet<>();
    if (ASSUME_COMMUNITY_LOCALITY) {
      Map<CommunityVar, Integer> communityCount = new HashMap<>();
      for (String router : graph.getRouters()) {
        Set<CommunityVar> routerComms = graph.findAllCommunities(router);
        for (CommunityVar cvar : routerComms) {
          Integer count = communityCount.computeIfAbsent(cvar, k -> 0);
          communityCount.put(cvar, count + 1);
        }
      }
      for (Entry<CommunityVar, Integer> entry : communityCount.entrySet()) {
        if (entry.getValue() == 1) {
          CommunityVar cvar = entry.getKey();
          _commsUsedOnlyLocally.add(cvar);
          if (cvar.getType() == Type.REGEX) {
            List<CommunityVar> deps = graph.getCommunityDependencies().get(cvar);
            _commsUsedOnlyLocally.addAll(deps);
          }
        }
      }
    }
  }

  Set<CommunityVar> getCommsAssignedButNotMatched() {
    return _commsAssignedButNotMatched;
  }

  public Set<CommunityVar> getCommsMatchedButNotAssigned() {
    return _commsMatchedButNotAssigned;
  }

  public Set<CommunityVar> getCommsUsedOnlyLocally() {
    return _commsUsedOnlyLocally;
  }
}

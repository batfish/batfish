package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.LineAction;

public final class BDDOps {
  private final BDDFactory _factory;

  public BDDOps(BDDFactory factory) {
    _factory = factory;
  }

  public @Nonnull BDD and(BDD... conjuncts) {
    return _factory.andAll(conjuncts);
  }

  public @Nonnull BDD and(Iterable<BDD> conjuncts) {
    return _factory.andAll(conjuncts);
  }

  public @Nonnull BDD or(BDD... disjuncts) {
    return _factory.orAll(disjuncts);
  }

  public @Nonnull BDD or(Iterable<BDD> disjuncts) {
    return _factory.orAll(disjuncts);
  }

  /**
   * Helper function to compile the line BDDs of an ACL-like object (e.g. an {@link AclIpSpace} or
   * an {@link org.batfish.datamodel.IpAccessList} to a single BDD.
   */
  public BDD bddAclLines(List<BDD> lineBdds, List<LineAction> actions) {
    checkArgument(lineBdds.size() == actions.size(), "Line BDDs and line actions must be 1:1.");

    BDD result = _factory.zero();

    LineAction currentAction = LineAction.PERMIT;
    List<BDD> lineBddsWithCurrentAction = new LinkedList<>();

    Iterator<BDD> bddIter = Lists.reverse(lineBdds).iterator();
    Iterator<LineAction> actionIter = Lists.reverse(actions).iterator();

    while (bddIter.hasNext()) {
      LineAction lineAction = actionIter.next();

      // The line action is going to change. Combine lineBddsWithCurrentAction into result
      if (lineAction != currentAction) {
        if (currentAction == LineAction.PERMIT) {
          // matched by any of the permit lines, or permitted by the rest of the acl.
          lineBddsWithCurrentAction.add(result);
          result = or(lineBddsWithCurrentAction);
        } else {
          // permitted by the rest of the acl and not matched by any of the deny lines.
          result = result.diffWith(or(lineBddsWithCurrentAction));
        }
        currentAction = lineAction;
        lineBddsWithCurrentAction.clear();
      }

      // Start a new batch of lines with the new action.
      BDD lineBdd = bddIter.next();
      lineBddsWithCurrentAction.add(lineBdd);
    }

    // Reached the start of the ACL. Combine the last batch of lines into the result.
    if (currentAction == LineAction.PERMIT) {
      lineBddsWithCurrentAction.add(result);
      result = or(lineBddsWithCurrentAction);
    } else {
      result = result.diffWith(or(lineBddsWithCurrentAction));
    }
    return result;
  }

  /**
   * Helper function to compile the line BDDs of an ACL-like object (e.g. an {@link AclIpSpace} or
   * an {@link org.batfish.datamodel.IpAccessList} to a single {@link PermitAndDenyBdds}
   * representing the BDDs of all explicitly permitted and explicitly denied flows.
   *
   * @param bdds {@link PermitAndDenyBdds} representing flows explicitly matched by each ACL line
   */
  public PermitAndDenyBdds bddAclLines(List<PermitAndDenyBdds> bdds) {
    // TODO Optimizations likely possible. Compare with other bddAclLines() method.
    // For each line, BDD of the flows that reach that line and are permitted
    List<BDD> reachAndPermitBdds = new ArrayList<>();

    // For each line, BDD of the flows that reach that line and are denied
    List<BDD> reachAndDenyBdds = new ArrayList<>();

    // BDD of the flows matched by any previously checked line
    BDD alreadyMatched = _factory.zero();

    for (PermitAndDenyBdds currentLine : bdds) {
      // Find BDDs of flows that reach and match current line with each action
      BDD reachAndPermit = currentLine.getPermitBdd().diff(alreadyMatched);
      BDD reachAndDeny = currentLine.getDenyBdd().diff(alreadyMatched);
      reachAndPermitBdds.add(reachAndPermit);
      reachAndDenyBdds.add(reachAndDeny);

      alreadyMatched = alreadyMatched.or(currentLine.getMatchBdd());
    }

    return new PermitAndDenyBdds(or(reachAndPermitBdds), or(reachAndDenyBdds));
  }
}

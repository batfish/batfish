package org.batfish.common.bdd;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.LineAction;

public final class BDDOps {
  private final BDDFactory _factory;

  public BDDOps(BDDFactory factory) {
    _factory = factory;
  }

  public BDD and(BDD... conjuncts) {
    return and(Arrays.asList(conjuncts));
  }

  public BDD and(Iterable<BDD> conjuncts) {
    return firstNonNull(andNull(conjuncts), _factory.one());
  }

  /** A variant of {@link #and(BDD...)} that returns {@code null} when all conjuncts are null. */
  public static BDD andNull(BDD... conjuncts) {
    return andNull(Arrays.asList(conjuncts));
  }

  public static BDD andNull(Iterable<BDD> conjuncts) {
    BDD result = null;
    for (BDD conjunct : conjuncts) {
      if (conjunct != null) {
        result = result == null ? conjunct : result.and(conjunct);
      }
    }
    return result;
  }

  public BDD orAll(Collection<BDD> bdds) {
    return _factory.orAll(bdds);
  }

  public BDD orAll(BDD... bdds) {
    return _factory.orAll(bdds);
  }

  /** Returns bdd.not() or {@code null} if given {@link BDD} is null. */
  public static BDD negateIfNonNull(BDD bdd) {
    return bdd == null ? bdd : bdd.not();
  }

  public BDD or(BDD... disjuncts) {
    return or(Arrays.asList(disjuncts));
  }

  public BDD or(Iterable<BDD> disjuncts) {
    return firstNonNull(orNull(disjuncts), _factory.zero());
  }

  /** A variant of {@link #or(BDD...)} that returns {@code null} when all disjuncts are null. */
  public static BDD orNull(BDD... disjuncts) {
    return orNull(Arrays.asList(disjuncts));
  }

  public static BDD orNull(Iterable<BDD> disjuncts) {
    BDD result = null;
    for (BDD disjunct : disjuncts) {
      if (disjunct != null) {
        result = result == null ? disjunct : result.or(disjunct);
      }
    }
    return result;
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
          result = orAll(lineBddsWithCurrentAction);
        } else {
          // permitted by the rest of the acl and not matched by any of the deny lines.
          result = result.diff(orAll(lineBddsWithCurrentAction));
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
      result = orAll(lineBddsWithCurrentAction);
    } else {
      result = result.diff(orAll(lineBddsWithCurrentAction));
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

    return new PermitAndDenyBdds(orAll(reachAndPermitBdds), orAll(reachAndDenyBdds));
  }
}

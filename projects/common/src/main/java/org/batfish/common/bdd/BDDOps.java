package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toArrayList;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.LineAction;

public final class BDDOps implements Serializable {
  private final BDDFactory _factory;

  public BDDOps(BDDFactory factory) {
    _factory = factory;
  }

  /**
   * Convert all {@code objs} to {@link BDD} and or them together. If {@code objs} is {@code null}
   * or empty, returns {@code null}. Consumes all input BDDs.
   */
  public static @Nullable <T> BDD mapAndOrAllNull(
      @Nullable Collection<T> objs, Function<T, BDD> objToBdd) {
    if (objs == null || objs.isEmpty()) {
      return null;
    }
    int size = objs.size();
    if (size == 1) {
      return objToBdd.apply(objs.iterator().next());
    }
    if (size == 2) {
      Iterator<T> iter = objs.iterator();
      return objToBdd.apply(iter.next()).orWith(objToBdd.apply(iter.next()));
    }
    List<BDD> bdds = toArrayList(objs, objToBdd);
    return bdds.get(0).getFactory().orAllAndFree(bdds);
  }

  public @Nonnull <T> BDD mapAndOrAll(@Nullable Collection<T> objs, Function<T, BDD> objToBdd) {
    @Nullable BDD bddOrNull = mapAndOrAllNull(objs, objToBdd);
    return bddOrNull == null ? _factory.zero() : bddOrNull;
  }

  public @Nonnull BDD and(BDD... conjuncts) {
    return _factory.andAll(conjuncts);
  }

  public @Nonnull BDD and(Collection<BDD> conjuncts) {
    return _factory.andAll(conjuncts);
  }

  public @Nonnull BDD or(BDD... disjuncts) {
    return _factory.orAll(disjuncts);
  }

  public @Nonnull BDD or(Collection<BDD> disjuncts) {
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
          result = _factory.orAllAndFree(lineBddsWithCurrentAction);
        } else {
          // permitted by the rest of the acl and not matched by any of the deny lines.
          result = result.diffWith(_factory.orAllAndFree(lineBddsWithCurrentAction));
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
      result = _factory.orAllAndFree(lineBddsWithCurrentAction);
    } else {
      result = result.diffWith(_factory.orAllAndFree(lineBddsWithCurrentAction));
    }
    return result;
  }

  /**
   * Helper function to compile the line BDDs of an ACL-like object (e.g. an {@link AclIpSpace} or
   * an {@link org.batfish.datamodel.IpAccessList} to a single {@link PermitAndDenyBdds}
   * representing the BDDs of all explicitly permitted and explicitly denied flows.
   *
   * @param permitAndDenyBdds {@link PermitAndDenyBdds} representing flows explicitly matched by
   *     each ACL line
   */
  public PermitAndDenyBdds bddAclLines(List<PermitAndDenyBdds> permitAndDenyBdds) {
    BDD permitBdd = _factory.zero();
    BDD denyBdd = _factory.zero();

    LineAction currentAction = LineAction.PERMIT;
    List<BDD> lineBddsWithCurrentAction = new LinkedList<>();

    BiFunction<BDD, BDD, Void> finalizeBlock =
        (BDD sameActionBdd, BDD otherActionBdd) -> {
          BDD blockBdd = _factory.orAllAndFree(lineBddsWithCurrentAction);
          otherActionBdd.diffEq(blockBdd);
          sameActionBdd.orWith(blockBdd);
          lineBddsWithCurrentAction.clear();
          return null;
        };

    for (PermitAndDenyBdds line : Lists.reverse(permitAndDenyBdds)) {
      BDD linePermitBdd = line.getPermitBdd();
      BDD lineDenyBdd = line.getDenyBdd();

      switch (currentAction) {
        case PERMIT:
          if (lineDenyBdd.isZero()) {
            lineBddsWithCurrentAction.add(linePermitBdd);
          } else {
            if (!linePermitBdd.isZero()) {
              // line permits and denies (i.e. AclAclLine)
              lineBddsWithCurrentAction.add(linePermitBdd);
            }
            finalizeBlock.apply(permitBdd, denyBdd);

            // start a new deny block
            currentAction = LineAction.DENY;
            lineBddsWithCurrentAction.add(lineDenyBdd);
          }
          break;
        case DENY:
          if (linePermitBdd.isZero()) {
            lineBddsWithCurrentAction.add(lineDenyBdd);
          } else {
            if (!lineDenyBdd.isZero()) {
              // line permits and denies (i.e. AclAclLine)
              lineBddsWithCurrentAction.add(lineDenyBdd);
            }
            finalizeBlock.apply(denyBdd, permitBdd);

            // start a new permit block
            currentAction = LineAction.PERMIT;
            lineBddsWithCurrentAction.add(linePermitBdd);
          }
          break;
      }
    }

    // complete the last piece
    switch (currentAction) {
      case PERMIT:
        finalizeBlock.apply(permitBdd, denyBdd);
        break;
      case DENY:
        finalizeBlock.apply(denyBdd, permitBdd);
        break;
    }

    return new PermitAndDenyBdds(permitBdd, denyBdd);
  }
}

package org.batfish.datamodel.acl.normalize;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.util.NonRecursiveSupplier;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.acl.explanation.ConjunctsBuilder;
import org.batfish.datamodel.acl.explanation.DisjunctsBuilder;

/**
 * Reduce an {@link org.batfish.datamodel.IpAccessList} to a single {@link AclLineMatchExpr}.
 *
 * <p>All references to other {@link org.batfish.datamodel.IpAccessList ACLs} are inlined. For each
 * {@link LineAction#PERMIT} line, we generate an expression of the form "matches that line AND does
 * not match any earlier {@link LineAction#DENY} line". We then combine all those expressions into a
 * single {@link OrMatchExpr}. Note that the line expressions may overlap. This is OK because we are
 * only trying to express the space of the entire ACL as an {@link AclLineMatchExpr}. If you're
 * interested in an {@link AclLineMatchExpr} that describes exactly what is matched by a particular
 * line, you want something of the form "matches that line AND does not match ANY early line ({@link
 * LineAction#PERMIT} or {@link LineAction#DENY})". This class does not do that.
 *
 * <p>We use {@link BDD BDDs} to keep the expressions small. This refines the generated line
 * expressions to the form "matches that line AND does not match any early OVERLAPPING {@link
 * LineAction#DENY} line".
 */
public final class AclToAclLineMatchExpr {

  /** Reduce an entire {@link IpAccessList} to a single {@link AclLineMatchExpr}. */
  public static AclLineMatchExpr toAclLineMatchExpr(
      @Nonnull IpAccessListToBdd ipAccessListToBdd,
      IpAccessList acl,
      Map<String, IpAccessList> namedAcls) {
    return new AclToAclLineMatchExpr(ipAccessListToBdd, namedAcls).toAclLineMatchExpr(acl);
  }

  private final Map<String, Supplier<AclLineMatchExpr>> _namedAclThunks;

  private IpAccessListToBdd _ipAccessListToBdd;

  @VisibleForTesting
  AclToAclLineMatchExpr(
      @Nonnull IpAccessListToBdd ipAccessListToBdd, Map<String, IpAccessList> namedAcls) {
    _ipAccessListToBdd = ipAccessListToBdd;
    _namedAclThunks = createThunks(namedAcls);
  }

  private Map<String, Supplier<AclLineMatchExpr>> createThunks(
      Map<String, IpAccessList> namedAcls) {
    ImmutableMap.Builder<String, Supplier<AclLineMatchExpr>> thunks = ImmutableMap.builder();
    namedAcls.forEach(
        (name, acl) ->
            thunks.put(name, new NonRecursiveSupplier<>(() -> this.toAclLineMatchExpr(acl))));
    return thunks.build();
  }

  private AclLineMatchExpr toAclLineMatchExpr(IpAccessList acl) {
    return AclToExprConverter.toAclLineMatchExpr(acl, _namedAclThunks, _ipAccessListToBdd);
  }

  /** For testing purposes only */
  @VisibleForTesting
  AclToExprConverter getConverterInstance() {
    return new AclToExprConverter(_namedAclThunks, _ipAccessListToBdd);
  }

  /**
   * Visitor to convert an {@link IpAccessList} into an {@link AclLineMatchExpr}. Separated into its
   * own class because the same instance should never be reused to convert a new ACL: it will have
   * dirty state. (But do not create a separate converter for referenced ACLs.)
   */
  @VisibleForTesting
  static class AclToExprConverter
      implements GenericAclLineMatchExprVisitor<AclLineMatchExpr>, GenericAclLineVisitor<Void> {

    /**
     * Assembles an {@link AclLineMatchExpr} representing the behavior of the given {@link
     * IpAccessList}. To avoid polluting the assembler's state, this should be the only method that
     * creates an instance of {@link AclToExprConverter}.
     */
    static AclLineMatchExpr toAclLineMatchExpr(
        IpAccessList acl,
        Map<String, Supplier<AclLineMatchExpr>> namedAclThunks,
        IpAccessListToBdd ipAccessListToBdd) {
      AclToExprConverter converter = new AclToExprConverter(namedAclThunks, ipAccessListToBdd);
      acl.getLines().forEach(converter::visit);
      return converter.getExpr();
    }

    private final Map<String, Supplier<AclLineMatchExpr>> _namedAclThunks;
    private final IpAccessListToBdd _ipAccessListToBdd;
    private final DisjunctsBuilder _disjunctsBuilder;
    private final List<AclLineMatchExpr> _earlierDenyLineExprs;

    private AclToExprConverter(
        Map<String, Supplier<AclLineMatchExpr>> namedAclThunks,
        IpAccessListToBdd ipAccessListToBdd) {
      _namedAclThunks = namedAclThunks;
      _ipAccessListToBdd = ipAccessListToBdd;
      /*
       * We're going to construct an OrMatchExpr with a disjunct per PERMIT line in the ACL. We use
       * a disjuncts builder to remove redundant disjuncts.
       */
      _disjunctsBuilder = new DisjunctsBuilder(ipAccessListToBdd);
      /*
       * The disjunct for each PERMIT line is the expression "match the line but do not match any
       * earlier DENY line". As we walk the ACL we remember the expressions for earlier deny lines in
       * this list.
       */
      _earlierDenyLineExprs = new ArrayList<>();
    }

    private AclLineMatchExpr getExpr() {
      return _disjunctsBuilder.build();
    }

    /* AclLine visit methods */

    @Override
    public Void visitExprAclLine(ExprAclLine exprAclLine) {
      // Inline the line's match expr
      AclLineMatchExpr expr = visit(exprAclLine.getMatchCondition());

      if (exprAclLine.getAction() == LineAction.PERMIT) {
        /*
         * This is a PERMIT line, so the output is going to include a disjunct for it. The disjunct
         * is an AndMatchExpr -- matches this line, and doesn't match each previous DENY line. We
         * use a ConjunctsBuilder to remove redundant conjuncts.
         */
        ConjunctsBuilder conjunctsBuilder = new ConjunctsBuilder(_ipAccessListToBdd);
        // matches this PERMIT line
        conjunctsBuilder.add(expr);
        // does not match any earlier DENY line.
        _earlierDenyLineExprs.forEach(conjunctsBuilder::add);

        _disjunctsBuilder.add(conjunctsBuilder.build());
      } else {
        /* this is a DENY line, so add it to our list of DENY line expressions. */
        _earlierDenyLineExprs.add(not(expr));
      }
      return null;
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return and(
          andMatchExpr.getConjuncts().stream()
              .map(this::visit)
              .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())));
    }

    @Override
    public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
      return falseExpr;
    }

    @Override
    public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return matchHeaderSpace;
    }

    @Override
    public AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return matchSrcInterface;
    }

    @Override
    public AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return new NotMatchExpr(notMatchExpr.getOperand().accept(this));
    }

    @Override
    public AclLineMatchExpr visitOriginatingFromDevice(
        OriginatingFromDevice originatingFromDevice) {
      return originatingFromDevice;
    }

    @Override
    public AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return or(
          orMatchExpr.getDisjuncts().stream()
              .map(this::visit)
              .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())));
    }

    @Override
    public AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      return _namedAclThunks.get(permittedByAcl.getAclName()).get();
    }

    @Override
    public AclLineMatchExpr visitTrueExpr(TrueExpr trueExpr) {
      return trueExpr;
    }
  }
}

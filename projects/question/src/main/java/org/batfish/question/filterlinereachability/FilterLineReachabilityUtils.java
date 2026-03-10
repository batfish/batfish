package org.batfish.question.filterlinereachability;

import static org.batfish.common.bdd.PermitAndDenyBdds.takeDifferentActions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.PermitAndDenyBdds;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.MatchDestinationPort;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchIpProtocol;
import org.batfish.datamodel.acl.MatchSourceIp;
import org.batfish.datamodel.acl.MatchSourcePort;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.answers.AclSpecs;

/** Utils for extracting referenced ACLs and interfaces from an {@link AclLine} */
public class FilterLineReachabilityUtils {
  private static final ReferencedAclsCollector ACLS_COLLECTOR = new ReferencedAclsCollector();
  private static final ReferencedInterfacesCollector INTERFACES_COLLECTOR =
      new ReferencedInterfacesCollector();

  private FilterLineReachabilityUtils() {}

  public static Set<String> getReferencedAcls(AclLine line) {
    return ACLS_COLLECTOR.visit(line).collect(ImmutableSet.toImmutableSet());
  }

  public static Set<String> getReferencedInterfaces(AclLine line) {
    return INTERFACES_COLLECTOR.visit(line).collect(ImmutableSet.toImmutableSet());
  }

  public static Stream<UnreachableFilterLine> computeUnreachableFilterLines(
      AclSpecs aclSpec, BDDPacket bddPacket) {
    BDDFactory bddFactory = bddPacket.getFactory();
    BDDSourceManager sourceMgr =
        BDDSourceManager.forInterfaces(bddPacket, aclSpec.acl.getInterfaces());
    IpAccessListToBdd ipAccessListToBdd =
        new IpAccessListToBddImpl(
            bddPacket, sourceMgr, aclSpec.acl.getDependencies(), ImmutableMap.of());

    /* Convert every line to permit and deny BDDs. */
    List<PermitAndDenyBdds> linePermitAndDenyBdds =
        aclSpec.acl.getSanitizedAcl().getLines().stream()
            .map(ipAccessListToBdd::toPermitAndDenyBdds)
            .collect(Collectors.toList());

    /* Pass over BDDs to classify each as unmatchable, unreachable, or (implicitly) reachable. */
    BDD unmatchedPackets = bddFactory.one(); // The packets that are not yet matched by the ACL.
    return Streams.mapWithIndex(
            linePermitAndDenyBdds.stream(),
            (lineBdds, lineNum) -> {
              @Nullable
              UnreachableFilterLine unreachableLine =
                  computeUnreachableFilterLine(
                      aclSpec, (int) lineNum, linePermitAndDenyBdds, unmatchedPackets);
              unmatchedPackets.diffWith(lineBdds.getMatchBdd().id());
              return unreachableLine;
            })
        .filter(Objects::nonNull);
  }

  private static @Nullable UnreachableFilterLine computeUnreachableFilterLine(
      AclSpecs aclSpecs,
      int lineNum,
      List<PermitAndDenyBdds> linePermitAndDenyBdds,
      BDD unmatchedPackets) {
    PermitAndDenyBdds lineBDDs = linePermitAndDenyBdds.get(lineNum);
    if (lineBDDs.isZero()) {
      // This line is unmatchable
      return UnreachableFilterLine.forUnmatchableLine(aclSpecs, lineNum);
    }

    if (unmatchedPackets.isZero() || !lineBDDs.getMatchBdd().andSat(unmatchedPackets)) {
      // No unmatched packets in the ACL match this line, so this line is unreachable.
      BlockingProperties blockingProps = findBlockingPropsForLine(lineNum, linePermitAndDenyBdds);
      return new BlockedFilterLine(
          aclSpecs, lineNum, blockingProps.getBlockingLineNums(), blockingProps.getDiffAction());
    }

    // line is reachable
    return null;
  }

  @VisibleForTesting
  static BlockingProperties findBlockingPropsForLine(
      int blockedLineNum, List<PermitAndDenyBdds> bdds) {
    PermitAndDenyBdds blockedLine = bdds.get(blockedLineNum);

    ImmutableSortedSet.Builder<LineAndWeight> linesByWeight =
        ImmutableSortedSet.orderedBy(LineAndWeight.COMPARATOR);

    // First, we find all lines before blockedLine that actually terminate any packets
    // blockedLine intends to. These, collectively, are the (partially-)blocking lines.
    //
    // In this same loop, we also compute the overlap of each such line with the blocked line
    // and weight each blocking line by that overlap.
    //
    // Finally, we record whether any of these lines has a different action than the blocked line.
    PermitAndDenyBdds restOfLine = blockedLine;
    boolean diffAction = false; // true if some partially-blocking line has a different action.
    for (int prevLineNum = 0; prevLineNum < blockedLineNum && !restOfLine.isZero(); prevLineNum++) {
      PermitAndDenyBdds prevLine = bdds.get(prevLineNum);

      if (!prevLine.getMatchBdd().andSat(restOfLine.getMatchBdd())) {
        continue;
      }

      BDD blockedLineOverlap = prevLine.getMatchBdd().and(blockedLine.getMatchBdd());
      linesByWeight.add(new LineAndWeight(prevLineNum, blockedLineOverlap.satCount()));
      diffAction = diffAction || takeDifferentActions(prevLine, restOfLine);
      restOfLine = restOfLine.diff(prevLine.getMatchBdd());
    }

    // In this second loop, we compute the answer:
    // * include partially-blocking lines in weight order until the blocked line is fully blocked by
    //   this subset.
    // * also include the largest blocking line with a different action than the blocked line, if
    //   not already in the above subset.
    ImmutableSortedSet.Builder<Integer> answerLines = ImmutableSortedSet.naturalOrder();
    restOfLine = blockedLine;
    boolean needDiffAction = diffAction;
    for (LineAndWeight line : linesByWeight.build()) {
      int curLineNum = line.getLine();
      PermitAndDenyBdds curLine = bdds.get(curLineNum);
      boolean curDiff = takeDifferentActions(curLine, blockedLine);

      // The original line is still not blocked, or this is the first line with a different action.
      if (!restOfLine.isZero() || needDiffAction && curDiff) {
        restOfLine = restOfLine.diff(curLine.getMatchBdd());
        answerLines.add(curLineNum);
        needDiffAction = needDiffAction && !curDiff;
      }

      // The original line is blocked and we have a line with a different action (if such exists).
      if (restOfLine.isZero() && !needDiffAction) {
        break;
      }
    }

    return new BlockingProperties(answerLines.build(), diffAction);
  }

  /**
   * Collects names of all ACLs directly referenced in an {@link AclLineMatchExpr} or {@link
   * AclLine}. Does not recurse into referenced ACLs.
   */
  private static class ReferencedAclsCollector
      implements GenericAclLineMatchExprVisitor<Stream<String>>,
          GenericAclLineVisitor<Stream<String>> {

    /* AclLine visit methods */

    @Override
    public Stream<String> visitAclAclLine(AclAclLine aclAclLine) {
      return Stream.of(aclAclLine.getAclName());
    }

    @Override
    public Stream<String> visitExprAclLine(ExprAclLine exprAclLine) {
      return visit(exprAclLine.getMatchCondition());
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public Stream<String> visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return andMatchExpr.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      return Stream.of(deniedByAcl.getAclName());
    }

    @Override
    public Stream<String> visitFalseExpr(FalseExpr falseExpr) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchDestinationPort(MatchDestinationPort matchDestinationPort) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitMatchIpProtocol(MatchIpProtocol matchIpProtocol) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchSourceIp(MatchSourceIp matchSourceIp) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchSourcePort(MatchSourcePort matchSourcePort) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return visit(notMatchExpr.getOperand());
    }

    @Override
    public Stream<String> visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return orMatchExpr.getDisjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      return Stream.of(permittedByAcl.getAclName());
    }

    @Override
    public Stream<String> visitTrueExpr(TrueExpr trueExpr) {
      return Stream.of();
    }
  }

  /**
   * Collects names of all interfaces directly referenced in an {@link AclLineMatchExpr} or {@link
   * AclLine}. Does not recurse into referenced ACLs.
   */
  private static class ReferencedInterfacesCollector
      implements GenericAclLineMatchExprVisitor<Stream<String>>,
          GenericAclLineVisitor<Stream<String>> {

    /* AclLine visit methods */

    @Override
    public Stream<String> visitAclAclLine(AclAclLine aclAclLine) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitExprAclLine(ExprAclLine exprAclLine) {
      return visit(exprAclLine.getMatchCondition());
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public Stream<String> visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return andMatchExpr.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitFalseExpr(FalseExpr falseExpr) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchDestinationPort(MatchDestinationPort matchDestinationPort) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitMatchIpProtocol(MatchIpProtocol matchIpProtocol) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchSourceIp(MatchSourceIp matchSourceIp) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchSourcePort(MatchSourcePort matchSourcePort) {
      return Stream.empty();
    }

    @Override
    public Stream<String> visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return matchSrcInterface.getSrcInterfaces().stream();
    }

    @Override
    public Stream<String> visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return visit(notMatchExpr.getOperand());
    }

    @Override
    public Stream<String> visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return orMatchExpr.getDisjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitTrueExpr(TrueExpr trueExpr) {
      return Stream.of();
    }
  }

  private static class LineAndWeight {
    private final int _line;
    private final double _weight;

    public LineAndWeight(int line, double weight) {
      _line = line;
      _weight = weight;
    }

    private int getLine() {
      return _line;
    }

    private double getWeight() {
      return _weight;
    }

    private static final Comparator<LineAndWeight> COMPARATOR =
        Comparator.comparing(LineAndWeight::getWeight)
            .reversed()
            .thenComparing(LineAndWeight::getLine);
  }

  /**
   * Info about how some ACL line is blocked: which lines block it and whether any of them treat any
   * packet differently than the blocked line would
   */
  public static class BlockingProperties {
    private final @Nonnull List<Integer> _blockingLineNums;
    private final boolean _diffAction;

    public BlockingProperties(@Nonnull Collection<Integer> blockingLineNums, boolean diffAction) {
      _blockingLineNums = ImmutableList.copyOf(blockingLineNums);
      _diffAction = diffAction;
    }

    @Nonnull
    List<Integer> getBlockingLineNums() {
      return _blockingLineNums;
    }

    /**
     * If true, indicates that some packet that matches the blocked line would be treated
     * differently by some blocking line. (Does not require that the packet be able to reach that
     * blocking line.)
     */
    boolean getDiffAction() {
      return _diffAction;
    }
  }
}

package org.batfish.datamodel.acl;

import static org.batfish.datamodel.IpAccessListLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBDD;
import org.batfish.common.bdd.MemoizedIpAccessListToBDD;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.normalize.AclToAclLineMatchExpr;

/**
 * Generate an explanation of the headerspace permitted by an {@link IpAccessList}. The explanation
 * is a single {@link AclLineMatchExpr} in a simplified normal form. First we normalize to something
 * analogous to Disjunctive Normal Form, then simplify further if possible.
 */
public final class AclExplainer {
  private AclExplainer() {}

  /**
   * Explain the flow space permitted by one {@link IpAccessList} ({@code permitAcl}) but denied by
   * another ({@code denyAcl}). The {@code invariantExp} allows scoping the explanation to a space
   * of interest (use {@link TrueExpr} to explain the entire difference).
   */
  public static AclLineMatchExpr explainDifferential(
      BDDPacket bddPacket,
      BDDSourceManager mgr,
      AclLineMatchExpr invariantExpr,
      IpAccessList denyAcl,
      Map<String, IpAccessList> denyNamedAcls,
      Map<String, IpSpace> denyNamedIpSpaces,
      IpAccessList permitAcl,
      Map<String, IpAccessList> permitNamedAcls,
      Map<String, IpSpace> permitNamedIpSpaces) {
    return explainDifferentialWithProvenance(
            bddPacket,
            mgr,
            invariantExpr,
            denyAcl,
            denyNamedAcls,
            denyNamedIpSpaces,
            permitAcl,
            permitNamedAcls,
            permitNamedIpSpaces)
        .getMatchExpr();
  }

  /**
   * Explain the flow space permitted by one {@link IpAccessList} ({@code permitAcl}) but denied by
   * another ({@code denyAcl}). Along with the explanation is its provenance: a map from each
   * literal in the explanation to the set of ACL lines on which the literal depends.
   */
  public static AclLineMatchExprWithProvenance<IpAccessListLineIndex>
      explainDifferentialWithProvenance(
          BDDPacket bddPacket,
          BDDSourceManager mgr,
          AclLineMatchExpr invariantExpr,
          IpAccessList denyAcl,
          Map<String, IpAccessList> denyNamedAcls,
          Map<String, IpSpace> denyNamedIpSpaces,
          IpAccessList permitAcl,
          Map<String, IpAccessList> permitNamedAcls,
          Map<String, IpSpace> permitNamedIpSpaces) {
    // Construct an ACL that permits the difference of the two ACLs.
    DifferentialIpAccessList differentialIpAccessList =
        DifferentialIpAccessList.create(
            denyAcl,
            denyNamedAcls,
            denyNamedIpSpaces,
            permitAcl,
            permitNamedAcls,
            permitNamedIpSpaces);

    IpAccessListToBDD ipAccessListToBDD =
        MemoizedIpAccessListToBDD.create(
            bddPacket,
            mgr,
            differentialIpAccessList.getNamedAcls(),
            differentialIpAccessList.getNamedIpSpaces());

    return explainWithProvenance(
        ipAccessListToBDD,
        scopedAcl(invariantExpr, differentialIpAccessList.getAcl()),
        differentialIpAccessList.getNamedAcls());
  }

  /**
   * Explain the flow space permitted by an {@link IpAccessList}. The {@code invariantExp} allows
   * scoping the explanation to a space of interest (use {@link TrueExpr} to explain the entire
   * space).
   */
  public static AclLineMatchExpr explain(
      BDDPacket bddPacket,
      BDDSourceManager mgr,
      AclLineMatchExpr invariantExpr,
      IpAccessList acl,
      Map<String, IpAccessList> namedAcls,
      Map<String, IpSpace> namedIpSpaces) {
    return explainWithProvenance(bddPacket, mgr, invariantExpr, acl, namedAcls, namedIpSpaces)
        .getMatchExpr();
  }

  /**
   * Explain the flow space permitted by an {@link IpAccessList}. Along with the explanation is its
   * provenance: a map from each literal in the explanation to the set of ACL lines on which the
   * literal depends.
   */
  public static AclLineMatchExprWithProvenance<IpAccessListLineIndex> explainWithProvenance(
      BDDPacket bddPacket,
      BDDSourceManager mgr,
      AclLineMatchExpr invariantExpr,
      IpAccessList acl,
      Map<String, IpAccessList> namedAcls,
      Map<String, IpSpace> namedIpSpaces) {
    IpAccessListToBDD ipAccessListToBDD =
        MemoizedIpAccessListToBDD.create(bddPacket, mgr, namedAcls, namedIpSpaces);

    IpAccessList aclWithInvariant = scopedAcl(invariantExpr, acl);

    return explainWithProvenance(ipAccessListToBDD, aclWithInvariant, namedAcls);
  }

  private static AclLineMatchExprWithProvenance<IpAccessListLineIndex> explainWithProvenance(
      IpAccessListToBDD ipAccessListToBDD, IpAccessList acl, Map<String, IpAccessList> namedAcls) {

    // Create a map from each literal in the given acls to the acl and index that it came from.
    IdentityHashMap<AclLineMatchExpr, IpAccessListLineIndex> literalsToLines =
        new IdentityHashMap<>();
    List<IpAccessList> allAcls = new LinkedList<>(namedAcls.values());
    allAcls.add(acl);
    allAcls.forEach(
        currAcl -> {
          List<IpAccessListLine> lines = currAcl.getLines();
          IntStream.range(0, lines.size())
              .forEach(
                  i ->
                      AclLineMatchExprLiterals.getLiterals(lines.get(i).getMatchCondition())
                          .forEach(
                              lit ->
                                  literalsToLines.put(lit, new IpAccessListLineIndex(currAcl, i))));
        });

    // Convert acl to a single expression.
    AclLineMatchExpr aclExpr =
        AclToAclLineMatchExpr.toAclLineMatchExpr(ipAccessListToBDD, acl, namedAcls);

    // Reduce that expression to normal form.
    AclLineMatchExpr aclExprNf = AclLineMatchExprNormalizer.normalize(ipAccessListToBDD, aclExpr);

    // Simplify the normal form
    AclLineMatchExprWithProvenance<AclLineMatchExpr> aclExprNfExplained =
        AclExplanation.explainNormalForm(aclExprNf);

    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> disjunctsToLines =
        new IdentityHashMap<>();

    for (Map.Entry<AclLineMatchExpr, Set<AclLineMatchExpr>> entry :
        aclExprNfExplained.getProvenance().entrySet()) {
      AclLineMatchExpr disjunct = entry.getKey();
      disjunctsToLines.put(
          disjunct,
          entry
              .getValue()
              .stream()
              .map(literalsToLines::get)
              .collect(ImmutableSet.toImmutableSet()));
    }

    return new AclLineMatchExprWithProvenance<IpAccessListLineIndex>(
        aclExprNfExplained.getMatchExpr(), disjunctsToLines);
  }

  /**
   * Scope the headerspace permitted by an {@link IpAccessList} to those flows that also match
   * {@code invariantExpr}.
   */
  private static IpAccessList scopedAcl(AclLineMatchExpr invariantExpr, IpAccessList acl) {
    return IpAccessList.builder()
        .setName(acl.getName())
        .setLines(
            ImmutableList.<IpAccessListLine>builder()
                .add(rejecting(not(invariantExpr)))
                .addAll(acl.getLines())
                .build())
        .build();
  }
}

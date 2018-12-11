package org.batfish.datamodel.acl;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.IpAccessListLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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

  static final String INVARIANT_ACL_NAME = " ~~ Invariant ACL Name ~~ ";

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

    // add the newly created differential acl to the list of named acls, because below we are going
    // to create a new top-level acl to take into account the given invariant
    Map<String, IpAccessList> namedAcls = new TreeMap<>(differentialIpAccessList.getNamedAcls());
    IpAccessList diffAcl = differentialIpAccessList.getAcl();
    namedAcls.put(diffAcl.getName(), diffAcl);
    namedAcls = ImmutableMap.copyOf(namedAcls);

    IpAccessList diffAclWithInvariant = scopedAcl(invariantExpr, diffAcl);

    IpAccessListToBDD ipAccessListToBDD =
        MemoizedIpAccessListToBDD.create(
            bddPacket, mgr, namedAcls, differentialIpAccessList.getNamedIpSpaces());

    IdentityHashMap<AclLineMatchExpr, IpAccessListLineIndex> literalsToLines =
        differentialIpAccessList.getLiteralsToLines();
    // add the newly created ACL's literals here to get provenance tracking for the
    // user-provided invariant as well
    literalsToLines.putAll(AclLineMatchExprLiterals.literalsToLines(diffAclWithInvariant));

    return explainWithProvenance(
        ipAccessListToBDD,
        diffAclWithInvariant,
        namedAcls,
        differentialIpAccessList.getLiteralsToLines());
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
    Preconditions.checkArgument(
        namedAcls.getOrDefault(acl.getName(), acl).equals(acl),
        "namedAcls contains a different ACL with the same name as acl");

    IpAccessListToBDD ipAccessListToBDD =
        MemoizedIpAccessListToBDD.create(bddPacket, mgr, namedAcls, namedIpSpaces);

    // add the top-level acl to the list of named acls, because we are going to create
    // a new top-level acl to take into account the given invariant
    Map<String, IpAccessList> finalNamedAcls = new TreeMap<>(namedAcls);
    finalNamedAcls.putIfAbsent(acl.getName(), acl);
    IpAccessList aclWithInvariant = scopedAcl(invariantExpr, acl);

    IdentityHashMap<AclLineMatchExpr, IpAccessListLineIndex> literalsToLines =
        AclLineMatchExprLiterals.literalsToLines(finalNamedAcls.values());
    // add the newly created ACL's literals here to get provenance tracking for the
    // user-provided invariant as well
    literalsToLines.putAll(AclLineMatchExprLiterals.literalsToLines(aclWithInvariant));

    return explainWithProvenance(
        ipAccessListToBDD, aclWithInvariant, ImmutableMap.copyOf(finalNamedAcls), literalsToLines);
  }

  private static AclLineMatchExprWithProvenance<IpAccessListLineIndex> explainWithProvenance(
      IpAccessListToBDD ipAccessListToBDD,
      IpAccessList acl,
      Map<String, IpAccessList> namedAcls,
      IdentityHashMap<AclLineMatchExpr, IpAccessListLineIndex> literalsToLines) {

    // Convert acl to a single expression.
    AclLineMatchExpr aclExpr =
        AclToAclLineMatchExpr.toAclLineMatchExpr(ipAccessListToBDD, acl, namedAcls);

    // Reduce that expression to normal form.
    AclLineMatchExpr aclExprNf = AclLineMatchExprNormalizer.normalize(ipAccessListToBDD, aclExpr);

    // Simplify the normal form
    AclLineMatchExprWithProvenance<AclLineMatchExpr> aclExprNfExplained =
        AclExplanation.explainNormalForm(aclExprNf);

    // join the provenance information from the normal form with the literalsToLines mapping
    // above to obtain provenance back to the original acl lines
    IdentityHashMap<AclLineMatchExpr, Set<IpAccessListLineIndex>> conjunctsToLines =
        new IdentityHashMap<>();
    for (Map.Entry<AclLineMatchExpr, Set<AclLineMatchExpr>> entry :
        aclExprNfExplained.getProvenance().entrySet()) {
      AclLineMatchExpr conjunct = entry.getKey();
      conjunctsToLines.put(
          conjunct,
          entry
              .getValue()
              .stream()
              .map(literalsToLines::get)
              .collect(ImmutableSet.toImmutableSet()));
    }

    return new AclLineMatchExprWithProvenance<>(
        aclExprNfExplained.getMatchExpr(), conjunctsToLines);
  }

  /**
   * Scope the headerspace permitted by an {@link IpAccessList} to those flows that also match
   * {@code invariantExpr}.
   */
  @VisibleForTesting
  static IpAccessList scopedAcl(AclLineMatchExpr invariantExpr, IpAccessList acl) {
    return IpAccessList.builder()
        .setName(INVARIANT_ACL_NAME)
        .setLines(
            ImmutableList.<IpAccessListLine>builder()
                .add(rejecting(not(invariantExpr)))
                .add(accepting(new PermittedByAcl(acl.getName())))
                .build())
        .build();
  }
}

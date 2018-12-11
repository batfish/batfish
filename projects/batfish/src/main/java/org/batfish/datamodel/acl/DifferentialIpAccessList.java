package org.batfish.datamodel.acl;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.IpAccessListLine.rejecting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.visitors.IpSpaceRenamer;

/**
 * Given two {@link IpAccessList ACLs} {@code denyAcl} and {@code permitAcl}, generate a new ACL
 * that permits exactly those flows that are permitted by {@code permitAcl} and denied by {@code
 * denyAcl} -- i.e. the difference between them.
 *
 * <p>Constructing such an ACL is simple when they have the same context (i.e. the same sets of
 * named {@link IpAccessList ACLs} and named {@link IpSpace IP spaces}. Where it gets tricky is when
 * they have possibly different sets, possibly with the name collisions. This is a common situation
 * when comparing two versions of the same ACL (e.g. from two different snapshots. We do rename the
 * {@code denyAcl}'s context to avoid collisions.
 */
public final class DifferentialIpAccessList {
  @VisibleForTesting static final String DIFFERENTIAL_ACL_NAME = " ~~ Differential ACL Name ~~ ";

  @VisibleForTesting
  static final Function<String, String> RENAMER = name -> String.format("~~ Deny ACL: %s ~~", name);

  private final IpAccessList _acl;

  private final Map<String, IpAccessList> _namedAcls;

  private final Map<String, IpSpace> _namedIpSpaces;

  private final IdentityHashMap<AclLineMatchExpr, IpAccessListLineIndex> _literalsToLines;

  private DifferentialIpAccessList(
      IpAccessList acl,
      IdentityHashMap<AclLineMatchExpr, IpAccessListLineIndex> literalsToLines,
      Map<String, IpAccessList> namedAcls,
      Map<String, IpSpace> namedIpSpaces) {
    _acl = acl;
    _literalsToLines = literalsToLines;
    _namedAcls = ImmutableMap.copyOf(namedAcls);
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
  }

  /**
   * Create a new {@link IpAccessList} that permits the difference between two other {@link
   * IpAccessList IpAccessLists}.
   *
   * @param denyAcl The {@link IpAccessList} subtracted in the difference.
   * @param denyNamedAcls The named {@link IpAccessList IpAccessLists} in scope for {@code denyAcl}.
   * @param denyNamedIpSpaces The named {@link IpSpace IpSpaces} in scope for {@code denyAcl}.
   * @param permitAcl The {@link IpAccessList} that is subtracted from in the difference.
   * @param permitNamedAcls The named {@link IpAccessList IpAccessLists} in scope for {@code
   *     permitAcl}.
   * @param permitNamedIpSpaces The named {@link IpSpace IpSpaces} in scope for {@code permitAcl}.
   */
  public static DifferentialIpAccessList create(
      IpAccessList denyAcl,
      Map<String, IpAccessList> denyNamedAcls,
      Map<String, IpSpace> denyNamedIpSpaces,
      IpAccessList permitAcl,
      Map<String, IpAccessList> permitNamedAcls,
      Map<String, IpSpace> permitNamedIpSpaces) {

    Preconditions.checkArgument(
        denyNamedAcls.getOrDefault(denyAcl.getName(), denyAcl).equals(denyAcl),
        "denyNamedAcls contains a different ACL with the same name as denyAcl");
    Preconditions.checkArgument(
        permitNamedAcls.getOrDefault(permitAcl.getName(), permitAcl).equals(permitAcl),
        "permitNamedAcls contains a different ACL with the same name as permitAcl");

    IpSpaceRenamer ipSpaceRenamer = new IpSpaceRenamer(RENAMER);
    IpAccessListRenamer aclRenamer = new IpAccessListRenamer(RENAMER, ipSpaceRenamer);
    /*
     * Create a new ACL for "matched by permitAcl but not by denyAcl"
     */
    IpAccessList differentialAcl =
        IpAccessList.builder()
            .setName(DIFFERENTIAL_ACL_NAME)
            .setLines(
                ImmutableList.<IpAccessListLine>builder()
                    // reject if permitted by denyAcl
                    .add(rejecting(new PermittedByAcl(RENAMER.apply(denyAcl.getName()))))
                    .add(accepting(new PermittedByAcl(permitAcl.getName())))
                    .build())
            .build();
    /*
     * Create namedAcls map for differentialAcl
     */
    // first add the top-level ACLs to the map of named ACLs if they are not already there
    Map<String, IpAccessList> finalDenyNamedAcls =
        denyNamedAcls.containsKey(denyAcl.getName())
            ? denyNamedAcls
            : ImmutableMap.<String, IpAccessList>builder()
                .putAll(denyNamedAcls)
                .put(denyAcl.getName(), denyAcl)
                .build();
    Map<String, IpAccessList> finalPermitNamedAcls =
        permitNamedAcls.containsKey(permitAcl.getName())
            ? permitNamedAcls
            : ImmutableMap.<String, IpAccessList>builder()
                .putAll(permitNamedAcls)
                .put(permitAcl.getName(), permitAcl)
                .build();

    Map<String, IpAccessList> namedAcls =
        ImmutableMap.<String, IpAccessList>builder()
            // include all the renamed finalDenyNamedAcls
            .putAll(
                finalDenyNamedAcls
                    .entrySet()
                    .stream()
                    .map(
                        entry ->
                            Maps.immutableEntry(
                                RENAMER.apply(entry.getKey()), aclRenamer.apply(entry.getValue())))
                    .collect(Collectors.toList()))
            // include all the finalPermitNamedAcls (no need to rename).
            .putAll(finalPermitNamedAcls)
            .build();

    /*
     * Create a map from literals to their original ACL lines.
     */
    // start with the map for the permit named ACLs and the permit ACL
    IdentityHashMap<AclLineMatchExpr, IpAccessListLineIndex> literalsToLines =
        AclLineMatchExprLiterals.literalsToLines(finalPermitNamedAcls.values());
    // include the map for the deny ACLs, but change the keys to use the new literals
    // from the renamed versions of the deny ACLs
    IdentityHashMap<AclLineMatchExpr, IpAccessListLineIndex> denyLiteralsToLines =
        AclLineMatchExprLiterals.literalsToLines(finalDenyNamedAcls.values());
    for (Map.Entry<AclLineMatchExpr, IpAccessListLineIndex> entry :
        denyLiteralsToLines.entrySet()) {
      AclLineMatchExpr newLit = entry.getKey();
      if (aclRenamer.getLiteralsMap().containsKey(newLit)) {
        newLit = aclRenamer.getLiteralsMap().get(newLit);
      }
      literalsToLines.put(newLit, entry.getValue());
    }

    /*
     * Create namedIpSpaces map for differentialAcl
     */
    Map<String, IpSpace> namedIpSpaces =
        ImmutableMap.<String, IpSpace>builder()
            // include all the renamed denyIpSpaces
            .putAll(
                denyNamedIpSpaces
                    .entrySet()
                    .stream()
                    .map(
                        entry ->
                            Maps.immutableEntry(
                                RENAMER.apply(entry.getKey()),
                                ipSpaceRenamer.apply(entry.getValue())))
                    .collect(Collectors.toList()))
            // include add the permitIpSpaces (no need to rename).
            .putAll(permitNamedIpSpaces)
            .build();
    return new DifferentialIpAccessList(differentialAcl, literalsToLines, namedAcls, namedIpSpaces);
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  public IdentityHashMap<AclLineMatchExpr, IpAccessListLineIndex> getLiteralsToLines() {
    return _literalsToLines;
  }

  public Map<String, IpAccessList> getNamedAcls() {
    return _namedAcls;
  }

  public Map<String, IpSpace> getNamedIpSpaces() {
    return _namedIpSpaces;
  }
}

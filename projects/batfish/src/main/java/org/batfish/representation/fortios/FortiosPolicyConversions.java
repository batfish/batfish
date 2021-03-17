package org.batfish.representation.fortios;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.representation.fortios.FortiosConfiguration.computeViPolicyName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.vendor.VendorStructureId;

final class FortiosPolicyConversions {

  static void convertPolicy(
      Policy policy,
      Configuration c,
      Map<String, AclLineMatchExpr> convertedServices,
      String filename,
      Warnings w) {
    if (policy.getStatusEffective() != Policy.Status.ENABLE) {
      return;
    }

    String number = policy.getNumber();
    @Nullable String name = policy.getName();
    String numAndName = name == null ? number : String.format("%s (%s)", number, name);

    ExprAclLine.Builder line;
    switch (policy.getActionEffective()) {
      case ALLOW:
        line = ExprAclLine.accepting();
        break;
      case DENY:
        line = ExprAclLine.rejecting();
        break;
      default: // TODO: Support policies with action IPSEC
        w.redFlag(
            String.format(
                "Ignoring policy %s: Action %s is not supported",
                numAndName, policy.getActionEffective()));
        return;
    }

    // TODO Incorporate policy.getComments()
    Set<String> srcAddrs = policy.getSrcAddr();
    Set<String> dstAddrs = policy.getDstAddr();
    Set<String> services = policy.getService();

    // Make sure references were finalized
    assert srcAddrs != null && dstAddrs != null && services != null;

    // Note that src/dst interface filtering will be done in generated export policies.
    ImmutableList.Builder<AclLineMatchExpr> matchConjuncts = ImmutableList.builder();

    // Match src addresses, dst addresses, and services
    List<AclLineMatchExpr> srcAddrExprs =
        Sets.intersection(srcAddrs, c.getIpSpaces().keySet()).stream()
            .map(
                addr -> {
                  HeaderSpace hs =
                      HeaderSpace.builder().setSrcIps(new IpSpaceReference(addr)).build();
                  VendorStructureId vsi =
                      new VendorStructureId(
                          filename, FortiosStructureType.ADDRESS.getDescription(), addr);
                  return new MatchHeaderSpace(
                      hs, TraceElement.builder().add("Match source address", vsi).build());
                })
            .collect(ImmutableList.toImmutableList());
    List<AclLineMatchExpr> dstAddrExprs =
        Sets.intersection(dstAddrs, c.getIpSpaces().keySet()).stream()
            .map(
                addr -> {
                  HeaderSpace hs =
                      HeaderSpace.builder().setDstIps(new IpSpaceReference(addr)).build();
                  VendorStructureId vsi =
                      new VendorStructureId(
                          filename, FortiosStructureType.ADDRESS.getDescription(), addr);
                  return new MatchHeaderSpace(
                      hs, TraceElement.builder().add("Match destination address", vsi).build());
                })
            .collect(ImmutableList.toImmutableList());
    List<AclLineMatchExpr> svcExprs =
        Sets.intersection(services, convertedServices.keySet()).stream()
            .map(convertedServices::get)
            .collect(ImmutableList.toImmutableList());
    if (srcAddrExprs.isEmpty() || dstAddrExprs.isEmpty() || services.isEmpty()) {
      String emptyField =
          srcAddrExprs.isEmpty()
              ? "source addresses"
              : dstAddrExprs.isEmpty() ? "destination addresses" : "services";
      w.redFlag(
          String.format(
              "Policy %s will not match any packets because none of its %s were successfully"
                  + " converted",
              numAndName, emptyField));
    }
    matchConjuncts.add(or(srcAddrExprs));
    matchConjuncts.add(or(dstAddrExprs));
    matchConjuncts.add(or(svcExprs)); // TODO confirm services should be disjoined

    line.setMatchCondition(and(matchConjuncts.build()));
    String viName = computeViPolicyName(policy);
    IpAccessList.builder().setOwner(c).setName(viName).setLines(line.build()).build();
  }
}

package org.batfish.question.loop;

import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExprs;

public class LoopNetwork {
  /*
   * Create a network with a forwarding loop. Optionally include an ACL that denies the traffic that
   * loops.
   */
  public static SortedMap<String, Configuration> testLoopNetwork(boolean includeLoop) {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    ConcreteInterfaceAddress c1Addr = ConcreteInterfaceAddress.parse("1.0.0.0/31");
    ConcreteInterfaceAddress c2Addr = ConcreteInterfaceAddress.parse("1.0.0.1/31");
    Interface i1 =
        nf.interfaceBuilder()
            .setOwner(c1)
            .setVrf(v1)
            .setAddress(c1Addr)
            .setType(InterfaceType.PHYSICAL)
            .build();
    Prefix loopPrefix = Prefix.parse("2.0.0.0/32");
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(loopPrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(i1.getName())
                .setNextHopIp(c2Addr.getIp())
                .build()));
    Configuration c2 = cb.build();
    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    Interface i2 =
        nf.interfaceBuilder()
            .setOwner(c2)
            .setVrf(v2)
            .setAddress(c2Addr)
            .setType(InterfaceType.PHYSICAL)
            .build();
    Prefix natPoolIp = Prefix.parse("5.5.5.5/32");

    if (!includeLoop) {
      // stop the loop by adding an ingress ACL that filters NATted traffic
      i2.setIncomingFilter(
          nf.aclBuilder()
              .setOwner(c2)
              .setLines(
                  ImmutableList.of(ExprAclLine.rejecting(AclLineMatchExprs.matchSrc(natPoolIp))))
              .build());
    }

    i2.setOutgoingTransformation(
        always().apply(assignSourceIp(natPoolIp.getStartIp(), natPoolIp.getStartIp())).build());
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(loopPrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(i2.getName())
                .setNextHopIp(c1Addr.getIp())
                .build()));

    return ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
  }
}

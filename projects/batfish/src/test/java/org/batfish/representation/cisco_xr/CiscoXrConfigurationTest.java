package org.batfish.representation.cisco_xr;

import static org.batfish.representation.cisco_xr.CiscoXrConfiguration.isIpv4AclUsedForAbf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.junit.Test;

public class CiscoXrConfigurationTest {
  @Test
  public void testIsIpv4AclUsedForAbf() {
    Ipv4AccessListLine nonAbfLine =
        Ipv4AccessListLine.builder()
            .setName("non-abf line")
            .setAction(LineAction.PERMIT)
            .setSrcAddressSpecifier(new WildcardAddressSpecifier(IpWildcard.ANY))
            .setDstAddressSpecifier(new WildcardAddressSpecifier(IpWildcard.ANY))
            .setServiceSpecifier(
                SimpleExtendedAccessListServiceSpecifier.builder()
                    .setProtocol(IpProtocol.TCP)
                    .build())
            .build();
    Ipv4AccessListLine abfLine =
        Ipv4AccessListLine.builder()
            .setName("abf line")
            .setAction(LineAction.PERMIT)
            .setSrcAddressSpecifier(new WildcardAddressSpecifier(IpWildcard.ANY))
            .setDstAddressSpecifier(new WildcardAddressSpecifier(IpWildcard.ANY))
            .setServiceSpecifier(
                SimpleExtendedAccessListServiceSpecifier.builder()
                    .setProtocol(IpProtocol.TCP)
                    .build())
            .setNexthop1(new Ipv4Nexthop(Ip.ZERO, null))
            .build();

    Ipv4AccessList nonAbfAcl = new Ipv4AccessList("nonAbfAcl");
    nonAbfAcl.addLine(nonAbfLine);
    Ipv4AccessList abfAcl = new Ipv4AccessList("abfAcl");
    abfAcl.addLine(nonAbfLine);
    abfAcl.addLine(abfLine);

    assertFalse(isIpv4AclUsedForAbf(null));
    assertFalse(isIpv4AclUsedForAbf(nonAbfAcl));
    assertTrue(isIpv4AclUsedForAbf(abfAcl));
  }
}

package org.batfish.representation.palo_alto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;

import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.palo_alto.PaloAltoCombinedParser;
import org.batfish.grammar.palo_alto.PaloAltoControlPlaneExtractor;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.BatfishTestUtils;
import org.junit.Test;
import org.antlr.v4.runtime.ParserRuleContext;

public class PaloAltoStaticRouteTest {

    private PaloAltoConfiguration parsePaloAltoConfig(String content) {
        Settings settings = new Settings();
        BatfishTestUtils.configureBatfishTestSettings(settings);
        PaloAltoCombinedParser parser = new PaloAltoCombinedParser(content, settings, null);
        ParserRuleContext tree = org.batfish.main.Batfish.parse(parser,
                new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
        Warnings parseWarnings = new Warnings();
        PaloAltoControlPlaneExtractor extractor = new PaloAltoControlPlaneExtractor(content, parser, parseWarnings,
                new SilentSyntaxCollection());
        extractor.processParseTree(org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);
        PaloAltoConfiguration pac = (PaloAltoConfiguration) extractor.getVendorConfiguration();
        pac.setVendor(ConfigurationFormat.PALO_ALTO);
        pac.setWarnings(parseWarnings);
        return pac;
    }

    @Test
    public void testStaticRouteWithNextHopIp() {
        String content = "set deviceconfig system hostname FW1\n"
                + "set network virtual-router VR1 routing-table ip static-route ROUTE-NAME nexthop ip-address 10.10.10.10/24\n"
                + "set network virtual-router VR1 routing-table ip static-route ROUTE-NAME destination 1.1.1.0/24\n";

        PaloAltoConfiguration config = parsePaloAltoConfig(content);

        // Check that the static route was created in the VS model
        VirtualRouter vr = config.getVirtualRouters().get("VR1");
        assertNotNull("VR1 should exist", vr);

        StaticRoute route = vr.getStaticRoutes().get("ROUTE-NAME");
        assertNotNull("ROUTE-NAME should exist", route);

        assertThat(route.getNextHopIp(), equalTo(Ip.parse("10.10.10.10")));
        assertThat(route.getDestination(), equalTo(Prefix.parse("1.1.1.0/24")));

        // Now convert to VendorIndependent model to verify the issue
        // The issue is that it FAILS to convert or indicates redflag
        config.toVendorIndependentConfigurations();

        // Check for redflags or verify VI route existence
        // If the bug exists, this might log a redflag and NOT produce the route in VI
        // model,
        // OR it might produce a route with no next hop which would be invalid.

        org.batfish.datamodel.Configuration viConfig = config.toVendorIndependentConfigurations().get(0);
        org.batfish.datamodel.Vrf viVrf = viConfig.getVrfs().get("VR1");
        assertNotNull(viVrf);

        // logic in PaloAltoConfiguration.java:2883 checks for null next hops and flags
        // it.
        // If it's working correctly, it should find the route
        boolean found = false;
        for (org.batfish.datamodel.StaticRoute sr : viVrf.getStaticRoutes()) {
            if (sr.getNetwork().equals(Prefix.parse("1.1.1.0/24"))
                    && sr.getNextHopIp().equals(Ip.parse("10.10.10.10"))) {
                found = true;
                break;
            }
        }

        assertThat("Should find converted static route with IP next hop", found);
    }

    @Test
    public void testStaticRouteWithNextHopIpUnmasked() {
        String content = "set deviceconfig system hostname FW1\n"
                + "set network virtual-router VR1 routing-table ip static-route ROUTE-NAME nexthop ip-address 10.10.10.10\n"
                + "set network virtual-router VR1 routing-table ip static-route ROUTE-NAME destination 1.1.1.0/24\n";

        PaloAltoConfiguration config = parsePaloAltoConfig(content);

        org.batfish.datamodel.Configuration viConfig = config.toVendorIndependentConfigurations().get(0);
        org.batfish.datamodel.Vrf viVrf = viConfig.getVrfs().get("VR1");

        boolean found = false;
        for (org.batfish.datamodel.StaticRoute sr : viVrf.getStaticRoutes()) {
            if (sr.getNetwork().equals(Prefix.parse("1.1.1.0/24"))
                    && sr.getNextHopIp().equals(Ip.parse("10.10.10.10"))) {
                found = true;
                break;
            }
        }
        assertThat("Should find converted static route with unmasked IP next hop", found);
    }
}

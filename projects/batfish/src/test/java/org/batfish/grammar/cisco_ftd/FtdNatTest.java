package org.batfish.grammar.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.cisco_ftd.FtdConfiguration;
import org.junit.Test;

public class FtdNatTest extends FtdGrammarTest {

        @Test
        public void testGbEInterfaceParsing() throws IOException {
                String config = join(
                                "interface GigabitEthernet0/0",
                                " nameif inside",
                                "interface GigabitEthernet0/1",
                                " nameif outside",
                                "object network REAL_1",
                                " host 10.0.0.1",
                                "object network MAPPED_1",
                                " host 192.0.2.1",
                                "nat (inside,outside) source static REAL_1 MAPPED_1");

                FtdConfiguration vc = parseVendorConfig(config);
                Configuration c = vc.toVendorIndependentConfigurations().get(0);

                // Verify interfaces exist
                assertThat(c.getAllInterfaces().get("GigabitEthernet0/0"), notNullValue());
                assertThat(c.getAllInterfaces().get("GigabitEthernet0/1"), notNullValue());

                // Verify NAT
                Transformation expectedOutgoing = Transformation.when(
                                AclLineMatchExprs.and(
                                                AclLineMatchExprs.matchSrcInterface("GigabitEthernet0/0"),
                                                AclLineMatchExprs.matchSrc(Ip.parse("10.0.0.1").toIpSpace())))
                                .apply(TransformationStep.assignSourceIp(Ip.parse("192.0.2.1")))
                                .build();

                assertThat(
                                c.getAllInterfaces().get("GigabitEthernet0/0").getOutgoingTransformation(),
                                equalTo(expectedOutgoing));
        }

        @Test
        public void testManualTwiceNatConversion() throws IOException {
                String config = join(
                                "interface GigabitEthernet0/0",
                                " nameif inside",
                                "interface GigabitEthernet0/1",
                                " nameif outside",
                                "object network REAL_SRC",
                                " host 10.0.0.10",
                                "object network MAPPED_SRC",
                                " host 192.0.2.10",
                                "object network REAL_DST",
                                " host 203.0.113.5",
                                "object network MAPPED_DST",
                                " host 198.51.100.5",
                                "nat (inside,outside) after-auto source static REAL_SRC MAPPED_SRC destination static REAL_DST MAPPED_DST");

                FtdConfiguration vc = parseVendorConfig(config);
                Configuration c = vc.toVendorIndependentConfigurations().get(0);

                Transformation expectedOutgoing = Transformation.when(
                                AclLineMatchExprs.and(
                                                AclLineMatchExprs.matchSrcInterface("GigabitEthernet0/0"),
                                                AclLineMatchExprs.matchSrc(Ip.parse("10.0.0.10").toIpSpace()),
                                                AclLineMatchExprs.matchDst(Ip.parse("203.0.113.5").toIpSpace())))
                                .apply(
                                                TransformationStep.assignSourceIp(Ip.parse("192.0.2.10")),
                                                TransformationStep.assignDestinationIp(Ip.parse("198.51.100.5")))
                                .build();

                assertThat(
                                c.getAllInterfaces().get("GigabitEthernet0/0").getOutgoingTransformation(),
                                equalTo(expectedOutgoing));

                Transformation expectedIncoming = Transformation.when(
                                AclLineMatchExprs.and(
                                                AclLineMatchExprs.matchSrcInterface("GigabitEthernet0/1"),
                                                AclLineMatchExprs.matchDst(Ip.parse("198.51.100.5").toIpSpace()),
                                                AclLineMatchExprs.matchSrc(Ip.parse("192.0.2.10").toIpSpace())))
                                .apply(
                                                TransformationStep.assignSourceIp(Ip.parse("10.0.0.10")),
                                                TransformationStep.assignDestinationIp(Ip.parse("203.0.113.5")))
                                .build();

                assertThat(
                                c.getAllInterfaces().get("GigabitEthernet0/1").getIncomingTransformation(),
                                equalTo(expectedIncoming));
        }
}

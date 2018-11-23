package org.batfish.question.namedstructures;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.NamedStructureSpecifier;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

public class NamedStructuresAnswererTest {

  @Test
  public void testRawAnswer() {

    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    RoutingPolicy rp1 = nf.routingPolicyBuilder().setOwner(c).setName("rp1").build();
    RoutingPolicy rp2 = nf.routingPolicyBuilder().setOwner(c).setName("rp2").build();
    nf.vrfBuilder().setOwner(c).build();

    Map<String, Configuration> configurations = ImmutableMap.of("node1", c);

    Multiset<Row> rows =
        NamedStructuresAnswerer.rawAnswer(
            ImmutableSet.of(NamedStructureSpecifier.ROUTING_POLICY), // only get routing policies
            ImmutableSet.of("node1"),
            configurations,
            NamedStructuresAnswerer.createMetadata(new NamedStructuresQuestion(null, null))
                .toColumnMap());

    Multiset<Row> expected =
        HashMultiset.create(
            ImmutableList.of(
                Row.builder()
                    .put(NamedStructuresAnswerer.COL_NODE, new Node("node1"))
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_TYPE,
                        NamedStructureSpecifier.ROUTING_POLICY)
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_NAME, "rp1")
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_DEFINITION, rp1)
                    .build(),
                Row.builder()
                    .put(NamedStructuresAnswerer.COL_NODE, new Node("node1"))
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_TYPE,
                        NamedStructureSpecifier.ROUTING_POLICY)
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_NAME, "rp2")
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_DEFINITION, rp2)
                    .build()));

    assertThat(rows, equalTo(expected));
  }
}

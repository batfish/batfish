package org.batfish.question.namedstructures;

import static org.batfish.question.namedstructures.NamedStructuresAnswerer.getAllStructureNamesOfType;
import static org.batfish.question.namedstructures.NamedStructuresAnswerer.insertedObject;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

public class NamedStructuresAnswererTest {

  private static final String ALL_NODES = ".*";

  @Test
  public void testGetAllStructureNamesOfType() {
    NetworkFactory nf = new NetworkFactory();

    // c1 has both routing policies
    Configuration c1 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.routingPolicyBuilder().setOwner(c1).setName("rp1").build();
    nf.routingPolicyBuilder().setOwner(c1).setName("rp2").build();

    // c2 has only one routing policy
    Configuration c2 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.routingPolicyBuilder().setOwner(c1).setName("rp1").build();

    Map<String, Configuration> configurations = ImmutableMap.of("node1", c1, "node2", c2);

    // both policies should be returned
    assertThat(
        getAllStructureNamesOfType(
            NamedStructurePropertySpecifier.ROUTING_POLICY,
            configurations.keySet(),
            configurations),
        equalTo(ImmutableSet.of("rp1", "rp2")));
  }

  @Test
  public void testRawAnswerDefinition() {

    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    RoutingPolicy rp1 = nf.routingPolicyBuilder().setOwner(c).setName("rp1").build();
    RoutingPolicy rp2 = nf.routingPolicyBuilder().setOwner(c).setName("rp2").build();
    nf.vrfBuilder().setOwner(c).build();

    Map<String, Configuration> configurations = ImmutableMap.of("node1", c);

    // only get routing policies
    NamedStructuresQuestion question =
        new NamedStructuresQuestion(
            ALL_NODES, NamedStructurePropertySpecifier.ROUTING_POLICY, null, null, false);

    Multiset<Row> rows =
        NamedStructuresAnswerer.rawAnswer(
            question,
            configurations.keySet(),
            configurations,
            NamedStructuresAnswerer.createMetadata(question).toColumnMap());

    Multiset<Row> expected =
        HashMultiset.create(
            ImmutableList.of(
                Row.builder()
                    .put(NamedStructuresAnswerer.COL_NODE, new Node("node1"))
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_TYPE,
                        NamedStructurePropertySpecifier.ROUTING_POLICY)
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_NAME, "rp1")
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_DEFINITION,
                        insertedObject(rp1, NamedStructurePropertySpecifier.ROUTING_POLICY))
                    .build(),
                Row.builder()
                    .put(NamedStructuresAnswerer.COL_NODE, new Node("node1"))
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_TYPE,
                        NamedStructurePropertySpecifier.ROUTING_POLICY)
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_NAME, "rp2")
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_DEFINITION,
                        insertedObject(rp2, NamedStructurePropertySpecifier.ROUTING_POLICY))
                    .build()));

    assertThat(rows, equalTo(expected));
  }

  @Test
  public void testRawAnswerIgnoreGenerated() {

    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    RoutingPolicy rp1 = nf.routingPolicyBuilder().setOwner(c).setName("rp1").build();
    nf.routingPolicyBuilder().setOwner(c).setName("~rp2").build();

    Map<String, Configuration> configurations = ImmutableMap.of("node1", c);

    NamedStructuresQuestion question =
        new NamedStructuresQuestion(ALL_NODES, "/.*/", null, true, null);

    Multiset<Row> rows =
        NamedStructuresAnswerer.rawAnswer(
            question,
            configurations.keySet(),
            configurations,
            NamedStructuresAnswerer.createMetadata(question).toColumnMap());

    Multiset<Row> expected =
        HashMultiset.create(
            ImmutableList.of(
                Row.builder()
                    .put(NamedStructuresAnswerer.COL_NODE, new Node("node1"))
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_TYPE,
                        NamedStructurePropertySpecifier.ROUTING_POLICY)
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_NAME, "rp1")
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_DEFINITION,
                        insertedObject(rp1, NamedStructurePropertySpecifier.ROUTING_POLICY))
                    .build()));

    assertThat(rows, equalTo(expected));
  }

  @Test
  public void testRawAnswerPresence() {

    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.routingPolicyBuilder().setOwner(c1).setName("rp1").build();

    Configuration c2 =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();

    Map<String, Configuration> configurations = ImmutableMap.of("node1", c1, "node2", c2);

    NamedStructuresQuestion question =
        new NamedStructuresQuestion(ALL_NODES, "/.*/", null, null, true);

    Multiset<Row> rows =
        NamedStructuresAnswerer.rawAnswer(
            question,
            configurations.keySet(),
            configurations,
            NamedStructuresAnswerer.createMetadata(question).toColumnMap());

    Multiset<Row> expected =
        HashMultiset.create(
            ImmutableList.of(
                Row.builder()
                    .put(NamedStructuresAnswerer.COL_NODE, new Node("node1"))
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_TYPE,
                        NamedStructurePropertySpecifier.ROUTING_POLICY)
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_NAME, "rp1")
                    .put(NamedStructuresAnswerer.COL_PRESENT_ON_NODE, true)
                    .build(),
                Row.builder()
                    .put(NamedStructuresAnswerer.COL_NODE, new Node("node2"))
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_TYPE,
                        NamedStructurePropertySpecifier.ROUTING_POLICY)
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_NAME, "rp1")
                    .put(NamedStructuresAnswerer.COL_PRESENT_ON_NODE, false)
                    .build()));

    assertThat(rows, equalTo(expected));
  }

  @Test
  public void testRawAnswerStructureNameRegex() {

    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    RoutingPolicy rp1 = nf.routingPolicyBuilder().setOwner(c).setName("selected-rp1").build();
    nf.routingPolicyBuilder().setOwner(c).setName("leftout-rp2").build();

    Map<String, Configuration> configurations = ImmutableMap.of("node1", c);

    NamedStructuresQuestion question =
        new NamedStructuresQuestion(ALL_NODES, "/.*/", "selected.*", false, null);

    Multiset<Row> rows =
        NamedStructuresAnswerer.rawAnswer(
            question,
            configurations.keySet(),
            configurations,
            NamedStructuresAnswerer.createMetadata(question).toColumnMap());

    Multiset<Row> expected =
        HashMultiset.create(
            ImmutableList.of(
                Row.builder()
                    .put(NamedStructuresAnswerer.COL_NODE, new Node("node1"))
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_TYPE,
                        NamedStructurePropertySpecifier.ROUTING_POLICY)
                    .put(NamedStructuresAnswerer.COL_STRUCTURE_NAME, "selected-rp1")
                    .put(
                        NamedStructuresAnswerer.COL_STRUCTURE_DEFINITION,
                        insertedObject(rp1, NamedStructurePropertySpecifier.ROUTING_POLICY))
                    .build()));

    assertThat(rows, equalTo(expected));
  }
}

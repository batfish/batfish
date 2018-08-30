package org.batfish.question.edges;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.edges.EdgesAnswerer.COL_AS_NUMBER;
import static org.batfish.question.edges.EdgesAnswerer.COL_INTERFACE;
import static org.batfish.question.edges.EdgesAnswerer.COL_IP;
import static org.batfish.question.edges.EdgesAnswerer.COL_IPS;
import static org.batfish.question.edges.EdgesAnswerer.COL_NODE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_AS_NUMBER;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_INTERFACE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_IP;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_IPS;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_NODE;
import static org.batfish.question.edges.EdgesAnswerer.COL_REMOTE_VLAN;
import static org.batfish.question.edges.EdgesAnswerer.COL_VLAN;
import static org.batfish.question.edges.EdgesAnswerer.eigrpEdgeToRow;
import static org.batfish.question.edges.EdgesAnswerer.getBgpEdgeRow;
import static org.batfish.question.edges.EdgesAnswerer.getOspfEdgeRow;
import static org.batfish.question.edges.EdgesAnswerer.getRipEdgeRow;
import static org.batfish.question.edges.EdgesAnswerer.getTableMetadata;
import static org.batfish.question.edges.EdgesAnswerer.isisEdgeToRow;
import static org.batfish.question.edges.EdgesAnswerer.layer1EdgeToRow;
import static org.batfish.question.edges.EdgesAnswerer.layer2EdgeToRow;
import static org.batfish.question.edges.EdgesAnswerer.layer3EdgeToRow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer2Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.EdgeType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpEdge;
import org.batfish.datamodel.eigrp.EigrpInterface;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.isis.IsisNode;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link EdgesAnswerer} */
public class EdgesAnswererTest {
  private Configuration _host1;
  private Configuration _host2;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    _host1 = cb.setHostname("host1").build();
    _host1.setInterfaces(
        ImmutableSortedMap.of(
            "int1",
            Interface.builder()
                .setName("int1")
                .setAddress(new InterfaceAddress(new Ip("1.1.1.1"), 24))
                .build()));
    _host2 = cb.setHostname("host2").build();
    _host2.setInterfaces(
        ImmutableSortedMap.of(
            "int2",
            Interface.builder()
                .setName("int2")
                .setAddress(new InterfaceAddress(new Ip("2.2.2.2"), 24))
                .build()));
  }

  @Test
  public void testEigrpToRow() {
    EigrpEdge testEdge =
        new EigrpEdge(
            new EigrpInterface("host1", "int1", "vrf1"),
            new EigrpInterface("host2", "int2", "vrf2"));
    Row row = eigrpEdgeToRow(testEdge);

    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testOspfToRow() {
    Row row = getOspfEdgeRow("host1", "int1", "host2", "int2");

    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testIsisToRow() {
    IsisEdge testEdge =
        new IsisEdge(
            IsisLevel.LEVEL_1, new IsisNode("host1", "int1"), new IsisNode("host2", "int2"));
    Row row = isisEdgeToRow(testEdge);

    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testRipToRow() {
    Row row = getRipEdgeRow("host1", "int1", "host2", "int2");

    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testBgpToRow() {
    Row row = getBgpEdgeRow("host1", new Ip("1.1.1.1"), 1L, "host2", new Ip("2.2.2.2"), 2L);
    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
            hasColumn(COL_IP, equalTo(new Ip("1.1.1.1")), Schema.IP),
            hasColumn(COL_AS_NUMBER, equalTo("1"), Schema.STRING),
            hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
            hasColumn(COL_REMOTE_IP, equalTo(new Ip("2.2.2.2")), Schema.IP),
            hasColumn(COL_REMOTE_AS_NUMBER, equalTo("2"), Schema.STRING)));
  }

  @Test
  public void testLayer1ToRow() {
    Row row = layer1EdgeToRow(new Layer1Edge("host1", "int1", "host2", "int2"));
    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE)));
  }

  @Test
  public void testLayer2ToRow() {
    Row row = layer2EdgeToRow(new Layer2Edge("host1", "int1", 1, "host2", "int2", 2, 12));
    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(COL_VLAN, equalTo("1"), Schema.STRING),
            hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE),
            hasColumn(COL_REMOTE_VLAN, equalTo("2"), Schema.STRING)));
  }

  @Test
  public void testLayer3ToRow() {
    Map<String, Configuration> configurationMap =
        ImmutableSortedMap.of("host1", _host1, "host2", _host2);
    Row row = layer3EdgeToRow(configurationMap, new Edge("host1", "int1", "host2", "int2"));
    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("host1")), Schema.NODE),
            hasColumn(
                COL_INTERFACE, equalTo(new NodeInterfacePair("host1", "int1")), Schema.INTERFACE),
            hasColumn(COL_IPS, equalTo(ImmutableSet.of(new Ip("1.1.1.1"))), Schema.set(Schema.IP)),
            hasColumn(COL_REMOTE_NODE, equalTo(new Node("host2")), Schema.NODE),
            hasColumn(
                COL_REMOTE_INTERFACE,
                equalTo(new NodeInterfacePair("host2", "int2")),
                Schema.INTERFACE),
            hasColumn(
                COL_REMOTE_IPS,
                equalTo(ImmutableSet.of(new Ip("2.2.2.2"))),
                Schema.set(Schema.IP))));
  }

  @Test
  public void testTableMetadataLayer3() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(EdgeType.LAYER3).getColumnMetadata();
    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(
            COL_NODE,
            COL_INTERFACE,
            COL_IPS,
            COL_REMOTE_NODE,
            COL_REMOTE_INTERFACE,
            COL_REMOTE_IPS));

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(
            Schema.NODE,
            Schema.INTERFACE,
            Schema.set(Schema.IP),
            Schema.NODE,
            Schema.INTERFACE,
            Schema.set(Schema.IP)));
  }

  @Test
  public void testTableMetadataLayer2() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(EdgeType.LAYER2).getColumnMetadata();
    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(
            COL_NODE,
            COL_INTERFACE,
            COL_VLAN,
            COL_REMOTE_NODE,
            COL_REMOTE_INTERFACE,
            COL_REMOTE_VLAN));

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(
            Schema.NODE,
            Schema.INTERFACE,
            Schema.STRING,
            Schema.NODE,
            Schema.INTERFACE,
            Schema.STRING));
  }

  @Test
  public void testTableMetadataBgp() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(EdgeType.BGP).getColumnMetadata();
    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(
            COL_NODE, COL_IP, COL_AS_NUMBER, COL_REMOTE_NODE, COL_REMOTE_IP, COL_REMOTE_AS_NUMBER));

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(Schema.NODE, Schema.IP, Schema.STRING, Schema.NODE, Schema.IP, Schema.STRING));
  }

  @Test
  public void testTableMetadataOthers() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(EdgeType.OSPF).getColumnMetadata();
    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(COL_NODE, COL_INTERFACE, COL_REMOTE_NODE, COL_REMOTE_INTERFACE));

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(Schema.NODE, Schema.INTERFACE, Schema.NODE, Schema.INTERFACE));
  }
}

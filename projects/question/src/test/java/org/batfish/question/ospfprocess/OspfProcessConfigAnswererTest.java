package org.batfish.question.ospfprocess;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.AREAS;
import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.AREA_BORDER_ROUTER;
import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.EXPORT_POLICY_SOURCES;
import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.REFERENCE_BANDWIDTH;
import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.ROUTER_ID;
import static org.batfish.question.ospfprocess.OspfProcessConfigurationAnswerer.COLUMNS_FROM_PROP_SPEC;
import static org.batfish.question.ospfprocess.OspfProcessConfigurationAnswerer.COL_NODE;
import static org.batfish.question.ospfprocess.OspfProcessConfigurationAnswerer.COL_PROCESS_ID;
import static org.batfish.question.ospfprocess.OspfProcessConfigurationAnswerer.COL_VRF;
import static org.batfish.question.ospfprocess.OspfProcessConfigurationAnswerer.getRow;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import java.util.List;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.NameNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.junit.Test;

/** Test for {@link OspfProcessConfigurationAnswerer} */
public class OspfProcessConfigAnswererTest {

  @Test
  public void testGetProperties() {
    NetworkFactory nf = new NetworkFactory();
    Configuration configuration =
        nf.configurationBuilder()
            .setHostname("test_conf")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf vrf = nf.vrfBuilder().setName("test_vrf").setOwner(configuration).build();

    nf.ospfProcessBuilder()
        .setProcessId("ospf_1")
        .setAreas(
            ImmutableSortedMap.of(
                1L, nf.ospfAreaBuilder().setNumber(1).setInjectDefaultRoute(true).build()))
        .setExportPolicySources(ImmutableSortedSet.of("export_policy_source"))
        .setExportPolicyName("export_policy")
        .setReferenceBandwidth(12d)
        .setRouterId(Ip.parse("1.1.1.1"))
        .setVrf(vrf)
        .build();

    NodeSpecifier nodeSpecifier = new NameNodeSpecifier(configuration.getHostname());
    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of(configuration.getHostname(), configuration))
            .build();

    Multiset<Row> rows =
        OspfProcessConfigurationAnswerer.getProperties(
            COLUMNS_FROM_PROP_SPEC,
            ctxt,
            nodeSpecifier,
            OspfProcessConfigurationAnswerer.createTableMetadata(null, COLUMNS_FROM_PROP_SPEC)
                .toColumnMap());
    assertThat(
        rows.iterator().next(),
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("test_conf")), Schema.NODE),
            hasColumn(COL_VRF, equalTo("test_vrf"), Schema.STRING),
            hasColumn(COL_PROCESS_ID, equalTo("ospf_1"), Schema.STRING),
            hasColumn(AREAS, equalTo(ImmutableSet.of("1")), Schema.set(Schema.STRING)),
            hasColumn(REFERENCE_BANDWIDTH, equalTo(12d), Schema.DOUBLE)));
    assertThat(
        rows.iterator().next(),
        allOf(
            hasColumn(ROUTER_ID, equalTo(Ip.parse("1.1.1.1")), Schema.IP),
            hasColumn(
                EXPORT_POLICY_SOURCES,
                equalTo(ImmutableSet.of("export_policy_source")),
                Schema.set(Schema.STRING)),
            hasColumn(AREA_BORDER_ROUTER, equalTo(false), Schema.BOOLEAN)));
  }

  @Test
  public void testGetRow() {
    NetworkFactory nf = new NetworkFactory();
    OspfProcess ospfProcess =
        nf.ospfProcessBuilder()
            .setProcessId("ospf_1")
            .setAreas(
                ImmutableSortedMap.of(
                    1L, nf.ospfAreaBuilder().setNumber(1).setInjectDefaultRoute(true).build()))
            .setExportPolicySources(ImmutableSortedSet.of("export_policy_source"))
            .setExportPolicyName("export_policy")
            .setReferenceBandwidth(12d)
            .setRouterId(Ip.parse("1.1.1.1"))
            .build();

    Row row =
        getRow(
            "node",
            "vrf",
            ospfProcess,
            COLUMNS_FROM_PROP_SPEC,
            OspfProcessConfigurationAnswerer.createTableMetadata(null, COLUMNS_FROM_PROP_SPEC)
                .toColumnMap());
    assertThat(
        row,
        allOf(
            hasColumn(COL_NODE, equalTo(new Node("node")), Schema.NODE),
            hasColumn(COL_VRF, equalTo("vrf"), Schema.STRING),
            hasColumn(COL_PROCESS_ID, equalTo("ospf_1"), Schema.STRING),
            hasColumn(AREAS, equalTo(ImmutableSet.of("1")), Schema.set(Schema.STRING)),
            hasColumn(REFERENCE_BANDWIDTH, equalTo(12d), Schema.DOUBLE)));
    assertThat(
        row,
        allOf(
            hasColumn(ROUTER_ID, equalTo(Ip.parse("1.1.1.1")), Schema.IP),
            hasColumn(
                EXPORT_POLICY_SOURCES,
                equalTo(ImmutableSet.of("export_policy_source")),
                Schema.set(Schema.STRING)),
            hasColumn(AREA_BORDER_ROUTER, equalTo(false), Schema.BOOLEAN)));
  }

  @Test
  public void testMetaData() {
    List<ColumnMetadata> metas =
        OspfProcessConfigurationAnswerer.createColumnMetadata(COLUMNS_FROM_PROP_SPEC);

    assertThat(
        metas.stream().map(ColumnMetadata::getName).collect(ImmutableList.toImmutableList()),
        equalTo(
            ImmutableList.builder()
                .add(COL_NODE)
                .add(COL_VRF)
                .add(COL_PROCESS_ID)
                .addAll(COLUMNS_FROM_PROP_SPEC)
                .build()));
  }
}

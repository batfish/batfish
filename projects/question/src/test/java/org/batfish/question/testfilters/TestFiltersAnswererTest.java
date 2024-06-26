package org.batfish.question.testfilters;

import static org.batfish.datamodel.ExprAclLine.acceptingHeaderSpace;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.batfish.datamodel.matchers.DataModelMatchers.forAll;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_FILTER_NAME;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_NODE;
import static org.batfish.specifier.Location.interfaceLocation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.specifier.LocationInfo;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestFiltersAnswererTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private NetworkFactory _nf = new NetworkFactory();
  private Configuration.Builder _cb;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
  }

  private static final class MockBatfish extends IBatfishTestAdapter {

    final SortedMap<String, Configuration> _configurations;
    final SpecifierContext _specifierContext;

    public MockBatfish(SortedMap<String, Configuration> configurations) {
      this(configurations, null);
    }

    public MockBatfish(SortedMap<String, Configuration> configurations, SpecifierContext ctxt) {
      _configurations = configurations;
      if (ctxt != null) {
        _specifierContext = ctxt;
      } else {
        _specifierContext = MockSpecifierContext.builder().setConfigs(_configurations).build();
      }
    }

    @Override
    public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
      assertThat(snapshot, equalTo(getSnapshot()));
      return _configurations;
    }

    @Override
    public SpecifierContext specifierContext(NetworkSnapshot snapshot) {
      assertThat(snapshot, equalTo(getSnapshot()));
      return _specifierContext;
    }
  }

  @Test
  public void testIndirection() {
    Configuration c = _cb.build();
    IpAccessList.Builder aclb = _nf.aclBuilder().setOwner(c);

    /*
    Reference ACL contains 1 line: Permit 1.0.0.0/32
    Main ACL contains 2 lines:
    0. Permit anything that reference ACL permits
    1. Permit 1.0.0.0/24
     */

    IpAccessList referencedAcl =
        aclb.setLines(
                ImmutableList.of(
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/32").toIpSpace())
                            .build())))
            .setName("acl1")
            .build();
    IpAccessList acl =
        aclb.setLines(
                ImmutableList.of(
                    ExprAclLine.accepting()
                        .setMatchCondition(new PermittedByAcl(referencedAcl.getName()))
                        .build(),
                    acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setSrcIps(Prefix.parse("1.0.0.0/24").toIpSpace())
                            .build())))
            .setName("acl2")
            .build();

    MockBatfish batfish = new MockBatfish(ImmutableSortedMap.of(c.getHostname(), c));

    assertThat(c, hasIpAccessLists(hasEntry(referencedAcl.getName(), referencedAcl)));
    assertThat(c, hasIpAccessLists(hasEntry(acl.getName(), acl)));

    TestFiltersQuestion question =
        new TestFiltersQuestion(
            null, null, PacketHeaderConstraints.builder().setSrcIp("1.0.0.4").build(), null);
    TestFiltersAnswerer answerer = new TestFiltersAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer(batfish.getSnapshot());

    /*
     * Trace should be present for referencing acl with one event:
     * Permitted by referencing acl
     *
     * NO event for referenced ACL since its action was not match (default deny)
     */
    assertThat(
        answer,
        hasRows(
            forAll(
                hasColumn(COL_FILTER_NAME, equalTo(acl.getName()), Schema.STRING),
                hasColumn(
                    TestFiltersAnswerer.COL_TRACE, empty(), Schema.list(Schema.TRACE_TREE)))));
    /* Trace should be present for referenced acl with one event: not matching the referenced acl */
    assertThat(
        answer,
        hasRows(
            forAll(
                hasColumn(COL_FILTER_NAME, equalTo(referencedAcl.getName()), Schema.STRING),
                hasColumn(
                    TestFiltersAnswerer.COL_TRACE, empty(), Schema.list(Schema.TRACE_TREE)))));
  }

  @Test
  public void testOneRowPerFilterPerConfig() {
    IpAccessList.Builder aclb = _nf.aclBuilder();
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();
    Configuration c3 = _cb.build();

    // Create 2 ACLs for each of 3 configs.
    aclb.setOwner(c1).build();
    aclb.build();
    aclb.setOwner(c2).build();
    aclb.build();
    aclb.setOwner(c3).build();
    aclb.build();

    IBatfish batfish =
        new MockBatfish(
            ImmutableSortedMap.of(
                c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3));
    TestFiltersQuestion question = new TestFiltersQuestion(null, null, null, null);
    TestFiltersAnswerer answerer = new TestFiltersAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer(batfish.getSnapshot());

    // There should be 6 rows
    assertThat(answer.getRows(), hasSize(6));
  }

  @Test
  public void testOneRowPerInterface() {
    Configuration c1 = _cb.setHostname("c1").build();
    IpAccessList.Builder aclb = _nf.aclBuilder();
    aclb.setOwner(c1).build();

    Interface iface1 =
        Interface.builder()
            .setName("iface1")
            .setOwner(c1)
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24"))
            .setVrf(new Vrf("default"))
            .build();
    Interface iface2 =
        Interface.builder()
            .setName("iface2")
            .setOwner(c1)
            .setAddress(ConcreteInterfaceAddress.parse("2.2.2.2/24"))
            .setVrf(new Vrf("default"))
            .build();

    c1.setInterfaces(ImmutableSortedMap.of("iface1", iface1, "iface2", iface2));

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(c1.getHostname(), c1);
    IBatfish batfish =
        new MockBatfish(
            configs,
            MockSpecifierContext.builder()
                .setConfigs(configs)
                .setLocationInfo(
                    ImmutableMap.of(
                        interfaceLocation(iface1),
                        new LocationInfo(
                            true,
                            iface1.getConcreteAddress().getIp().toIpSpace(),
                            EmptyIpSpace.INSTANCE),
                        interfaceLocation(iface2),
                        new LocationInfo(
                            true,
                            iface2.getConcreteAddress().getIp().toIpSpace(),
                            EmptyIpSpace.INSTANCE)))
                .build());

    TestFiltersQuestion question = new TestFiltersQuestion(null, null, null, null);
    TestFiltersAnswerer answerer = new TestFiltersAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer(batfish.getSnapshot());

    // There should be 2 rows
    assertThat(answer.getRows(), hasSize(2));
  }

  @Test
  public void testFilteringByNodeAndFilterName() {
    IpAccessList.Builder aclb = _nf.aclBuilder();
    Configuration c1 = _cb.build();
    Configuration c2 = _cb.build();

    aclb.setName("acl1").setOwner(c1).build();
    aclb.setName("acl2").build();
    aclb.setName("acl1").setOwner(c2).build();
    aclb.setName("acl2").build();

    IBatfish batfish =
        new MockBatfish(ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2));

    // Test that filtering by node gives only that node's ACLs
    TestFiltersQuestion question = new TestFiltersQuestion(c1.getHostname(), null, null, null);
    TestFiltersAnswerer answerer = new TestFiltersAnswerer(question, batfish);
    Rows rows = answerer.answer(batfish.getSnapshot()).getRows();

    assertThat(rows, hasSize(2));
    for (Row row : rows.getData()) {
      assertThat(((Node) row.get(COL_NODE, Schema.NODE)).getName(), equalTo(c1.getHostname()));
    }

    // Test that filtering by ACL name gives only matching ACLs
    question = new TestFiltersQuestion(null, "acl1", null, null);
    answerer = new TestFiltersAnswerer(question, batfish);
    rows = answerer.answer(batfish.getSnapshot()).getRows();

    assertThat(rows, hasSize(2));
    for (Row row : rows.getData()) {
      assertThat(row.getString(COL_FILTER_NAME), equalTo("acl1"));
    }

    // Test that filtering by both gives only the one matching ACL
    question = new TestFiltersQuestion(c1.getHostname(), "acl1", null, null);
    answerer = new TestFiltersAnswerer(question, batfish);
    rows = answerer.answer(batfish.getSnapshot()).getRows();

    assertThat(rows, hasSize(1));
  }

  @Test
  public void testErrorForNoMatchingFlows() {
    Configuration c1 = _cb.setHostname("c1").build();
    _nf.aclBuilder().setName("acl1").setOwner(c1).build();
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(c1.getHostname(), c1);
    IBatfish batfish =
        new MockBatfish(configs, MockSpecifierContext.builder().setConfigs(configs).build());

    // if no filters are matched, no error -- just an empty table
    {
      TestFiltersQuestion question =
          new TestFiltersQuestion(c1.getHostname(), "acl2", null, "nonExistentLocation");
      TestFiltersAnswerer answerer = new TestFiltersAnswerer(question, batfish);
      TableAnswerElement answer = answerer.answer(batfish.getSnapshot());
      assertThat(answer.getRows().getData(), empty());
    }

    // if filters are matched, but we can't find a flow, we get an error
    {
      // Test that exception is thrown if node is found, but no filters match
      TestFiltersQuestion question =
          new TestFiltersQuestion(c1.getHostname(), "acl1", null, "nonExistentLocation");
      TestFiltersAnswerer answerer = new TestFiltersAnswerer(question, batfish);

      _thrown.expect(BatfishException.class);
      _thrown.expectMessage("No valid flow found");
      answerer.answer(batfish.getSnapshot());
    }
  }
}

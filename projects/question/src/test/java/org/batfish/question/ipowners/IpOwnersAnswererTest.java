package org.batfish.question.ipowners;

import static org.batfish.datamodel.Prefix.MAX_PREFIX_LENGTH;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_ACTIVE;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_INTERFACE_NAME;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_IP;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_MASK;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_NODE;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_VRFNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link IpOwnersAnswerer} */
public class IpOwnersAnswererTest {
  private static final Ip _uniqueIp = Ip.parse("1.1.1.1");
  private static final Ip _duplicateIp = Ip.parse("2.2.2.2");
  private static final Ip _noOwnersIp = Ip.parse("3.3.3.3");
  private static final Ip _secondaryUniqueIp = Ip.parse("4.4.4.4");
  private ImmutableMap<Ip, Set<String>> _ownersMap;
  private Map<String, Set<Interface>> _interfaceMap;

  /** Setup the maps that the answerer processes, with mix of active and inactive interfaces */
  @Before
  public void setup() {
    Vrf vrf = new Vrf(Configuration.DEFAULT_VRF_NAME);
    Interface.Builder ib = TestInterface.builder().setVrf(vrf);
    ConcreteInterfaceAddress uniqueAddr =
        ConcreteInterfaceAddress.create(_uniqueIp, MAX_PREFIX_LENGTH - 1);
    InterfaceAddress secondaryUniqueAddr =
        ConcreteInterfaceAddress.create(_secondaryUniqueIp, MAX_PREFIX_LENGTH - 1);
    ConcreteInterfaceAddress duplicateAddr =
        ConcreteInterfaceAddress.create(_duplicateIp, MAX_PREFIX_LENGTH);
    _ownersMap =
        ImmutableMap.of(
            _uniqueIp,
            ImmutableSet.of("n2"),
            _duplicateIp,
            ImmutableSet.of("n1", "n2"),
            _noOwnersIp,
            ImmutableSet.of(),
            _secondaryUniqueIp,
            ImmutableSet.of("n2"));
    _interfaceMap =
        ImmutableMap.of(
            "n1",
            ImmutableSet.of(
                ib.setAdminUp(false).setAddress(duplicateAddr).setName("Eth1/1").build()),
            "n2",
            ImmutableSet.of(
                ib.setAdminUp(true).setAddress(duplicateAddr).setName("Eth2/1").build(),
                ib.setAdminUp(true)
                    // VRRP-like scenario
                    .setAddresses(uniqueAddr, secondaryUniqueAddr)
                    .setName("Eth2/2")
                    .build()));
  }

  /** Test that rows show up correctly if duplicatesOnly is false, and we can handle empty maps */
  @Test
  public void testRowGenerationAllIps() {
    // Generate rows with no IP owners, no Interface Ips
    Multiset<Row> rows =
        IpOwnersAnswerer.generateRows(
            ImmutableMap.of(), ImmutableMap.of(), UniverseIpSpace.INSTANCE, false);
    assertThat(rows, empty());

    // Generate rows for actual data
    rows =
        IpOwnersAnswerer.generateRows(_ownersMap, _interfaceMap, UniverseIpSpace.INSTANCE, false);

    /*
     * Test that:
     * - We have rows for each IP
     * - Masks, nodes, interfaces names are as expected
     * - All IPs per interface are displayed
     */
    assertThat(
        rows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(
                    hasColumn(COL_IP, equalTo(_uniqueIp), Schema.IP),
                    hasColumn(COL_MASK, equalTo(MAX_PREFIX_LENGTH - 1), Schema.INTEGER),
                    hasColumn(COL_NODE, equalTo(new Node("n2")), Schema.NODE),
                    hasColumn(COL_INTERFACE_NAME, equalTo("Eth2/2"), Schema.STRING)),
                allOf(
                    hasColumn(COL_IP, equalTo(_secondaryUniqueIp), Schema.IP),
                    hasColumn(COL_MASK, equalTo(MAX_PREFIX_LENGTH - 1), Schema.INTEGER),
                    hasColumn(COL_NODE, equalTo(new Node("n2")), Schema.NODE),
                    hasColumn(COL_INTERFACE_NAME, equalTo("Eth2/2"), Schema.STRING)),
                allOf(
                    hasColumn(COL_IP, equalTo(_duplicateIp), Schema.IP),
                    hasColumn(COL_MASK, equalTo(MAX_PREFIX_LENGTH), Schema.INTEGER),
                    hasColumn(COL_NODE, equalTo(new Node("n1")), Schema.NODE),
                    hasColumn(COL_INTERFACE_NAME, equalTo("Eth1/1"), Schema.STRING)),
                allOf(
                    hasColumn(COL_IP, equalTo(_duplicateIp), Schema.IP),
                    hasColumn(COL_MASK, equalTo(MAX_PREFIX_LENGTH), Schema.INTEGER),
                    hasColumn(COL_NODE, equalTo(new Node("n2")), Schema.NODE),
                    hasColumn(COL_INTERFACE_NAME, equalTo("Eth2/1"), Schema.STRING)))));
  }

  @Test
  public void testRowGenerationDuplicatesOnly() {

    // Expect two rows for _duplicateIp only
    Multiset<Row> rows =
        IpOwnersAnswerer.generateRows(_ownersMap, _interfaceMap, UniverseIpSpace.INSTANCE, true);
    assertThat(rows, hasSize(2));
    for (Row row : rows) {
      assertThat(row.getIp(COL_IP), equalTo(_duplicateIp));
    }
  }

  @Test
  public void testRowGenerationIpSpace() {
    // expect one row for _uniqueIp
    Multiset<Row> rows =
        IpOwnersAnswerer.generateRows(_ownersMap, _interfaceMap, _uniqueIp.toIpSpace(), false);
    assertThat(Iterables.getOnlyElement(rows).getIp(COL_IP), equalTo(_uniqueIp));
  }

  @Test
  public void testColumnPresence() {
    assertThat(
        IpOwnersAnswerer.getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(Collectors.toList()),
        contains(COL_NODE, COL_VRFNAME, COL_INTERFACE_NAME, COL_IP, COL_MASK, COL_ACTIVE));
  }
}

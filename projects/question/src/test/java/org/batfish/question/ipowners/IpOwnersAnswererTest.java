package org.batfish.question.ipowners;

import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_ACTIVE;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_INTERFACE_NAME;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_IP;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_MASK;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_NODE;
import static org.batfish.question.ipowners.IpOwnersAnswerer.COL_VRFNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link IpOwnersAnswerer} */
public class IpOwnersAnswererTest {
  private Ip _uniqueIp = new Ip("1.1.1.1");
  private Ip _duplicateIp = new Ip("2.2.2.2");
  private Ip _noOwnersIp = new Ip("3.3.3.3");
  private ImmutableMap<Ip, Set<String>> _ownersMap;
  private Map<String, Set<Interface>> _interfaceMap;

  /** Setup the maps that the answerer processes, with mix of active and inactive interfaces */
  @Before
  public void setup() {
    Vrf vrf = new Vrf(Configuration.DEFAULT_VRF_NAME);
    Interface.Builder ib = Interface.builder().setVrf(vrf);
    InterfaceAddress uniqueAddr = new InterfaceAddress(_uniqueIp, Prefix.MAX_PREFIX_LENGTH - 1);
    InterfaceAddress duplicateAddr = new InterfaceAddress(_duplicateIp, Prefix.MAX_PREFIX_LENGTH);
    _ownersMap =
        ImmutableMap.of(
            _uniqueIp,
            ImmutableSet.of("n1"),
            _duplicateIp,
            ImmutableSet.of("n1", "n2"),
            _noOwnersIp,
            ImmutableSet.of());
    _interfaceMap =
        ImmutableMap.of(
            "n1",
            ImmutableSet.of(
                ib.setActive(false).setAddress(duplicateAddr).setName("Eth1/1").build()),
            "n2",
            ImmutableSet.of(
                ib.setActive(true).setAddress(duplicateAddr).setName("Eth2/1").build(),
                ib.setActive(true).setAddress(uniqueAddr).setName("Eth2/2").build()));
  }

  /** Test that rows show up correctly if duplicatesOnly is false, and we can handle empty maps */
  @Test
  public void testRowGenerationAllIps() {
    // Generate rows with no IP owners, no Interface Ips
    Multiset<Row> rows = IpOwnersAnswerer.generateRows(ImmutableMap.of(), ImmutableMap.of(), false);
    assertThat(rows, empty());

    // Generate rows for actual data
    rows = IpOwnersAnswerer.generateRows(_ownersMap, _interfaceMap, false);

    /*
     * Test that:
     * - We have three rows, one for _uniqueIP, two for _duplicateIp
     * - Masks are as expected
     */
    assertThat(rows, hasSize(3));
    Map<Ip, Long> counts =
        rows.stream()
            .map(r -> r.getIp(COL_IP))
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    assertThat(counts, hasEntry(_uniqueIp, 1L));
    assertThat(counts, hasEntry(_duplicateIp, 2L));

    List<Integer> masks =
        rows.stream().map(r -> r.getInteger(COL_MASK)).collect(Collectors.toList());
    assertThat(
        masks,
        containsInAnyOrder(
            Prefix.MAX_PREFIX_LENGTH - 1, Prefix.MAX_PREFIX_LENGTH, Prefix.MAX_PREFIX_LENGTH));
  }

  @Test
  public void testRowGenerationDuplicatesOnly() {

    // Expect two rows for _duplicateIp only
    Multiset<Row> rows = IpOwnersAnswerer.generateRows(_ownersMap, _interfaceMap, true);
    assertThat(rows, hasSize(2));
    for (Row row : rows) {
      assertThat(row.getIp(COL_IP), equalTo(_duplicateIp));
    }
  }

  @Test
  public void testColumnPresence() {
    assertThat(
        IpOwnersAnswerer.getColumnMetadata()
            .stream()
            .map(ColumnMetadata::getName)
            .collect(Collectors.toList()),
        contains(COL_NODE, COL_VRFNAME, COL_INTERFACE_NAME, COL_IP, COL_MASK, COL_ACTIVE));
  }
}

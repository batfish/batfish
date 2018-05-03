package org.batfish.question.ipowners;

import static org.batfish.question.ipowners.IpOwnersAnswerElement.COL_IP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.table.Row;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link IpOwnersAnswerer} */
public class IpOwnersAnswererTest {
  private Ip _uniqueIp = new Ip("1.1.1.1");
  private Ip _duplicateIp = new Ip("2.2.2.2");
  private Ip _noOwnersIp = new Ip("3.3.3.3");
  private Map<Ip, Map<String, Set<String>>> _ownersMap;
  private Map<Ip, Map<String, Set<String>>> _interaceMap;

  /** Setup the maps as would be computed by the */
  @Before
  public void setup() {
    _ownersMap =
        ImmutableMap.of(
            _uniqueIp,
            ImmutableMap.of("n1", ImmutableSet.of(Configuration.DEFAULT_VRF_NAME)),
            _duplicateIp,
            ImmutableMap.of(
                "n1",
                ImmutableSet.of(Configuration.DEFAULT_VRF_NAME),
                "n2",
                ImmutableSet.of(Configuration.DEFAULT_VRF_NAME)),
            _noOwnersIp,
            ImmutableMap.of());
    _interaceMap =
        ImmutableMap.of(
            _uniqueIp,
            ImmutableMap.of("n1", ImmutableSet.of("Eth0")),
            _duplicateIp,
            ImmutableMap.of("n1", ImmutableSet.of("Eth0"), "n2", ImmutableSet.of("Eth0")));
  }

  /** Test that rows show up correctly if duplicatesOnly is false, and we can handle empty maps */
  @Test
  public void testRowGenerationAllIps() {
    // Do generation with no IP owners, no Interface Ips
    Multiset<Row> rows = IpOwnersAnswerer.generateRows(ImmutableMap.of(), ImmutableMap.of(), false);
    assertThat(rows, empty());

    // Do generation for actual data, expect three rows, one for _uniqueIP, two for _duplicateIp
    rows = IpOwnersAnswerer.generateRows(_ownersMap, _interaceMap, false);
    assertThat(rows, hasSize(3));
  }

  @Test
  public void testRowGenerationDuplicatesOnly() {

    // Expect two rows for _duplicateIp only
    Multiset<Row> rows = IpOwnersAnswerer.generateRows(_ownersMap, _interaceMap, true);
    assertThat(rows, hasSize(2));
    for (Row row : rows) {
      assertThat(row.get(COL_IP, Ip.class), equalTo(_duplicateIp));
    }
  }
}

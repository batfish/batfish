package org.batfish.question.lpmroutes;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.lpmroutes.LpmRoutesAnswerer.COL_IP;
import static org.batfish.question.lpmroutes.LpmRoutesAnswerer.COL_NETWORK;
import static org.batfish.question.lpmroutes.LpmRoutesAnswerer.COL_NODE;
import static org.batfish.question.lpmroutes.LpmRoutesAnswerer.COL_NUM_ROUTES;
import static org.batfish.question.lpmroutes.LpmRoutesAnswerer.COL_VRF;
import static org.batfish.question.lpmroutes.LpmRoutesAnswerer.getColumnMetadata;
import static org.batfish.question.lpmroutes.LpmRoutesAnswerer.getRows;
import static org.batfish.question.lpmroutes.LpmRoutesAnswerer.toRow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MockRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

/** Tests for {@link LpmRoutesAnswerer} */
public class LpmRoutesAnswererTest {

  private static MockRib getMockRib(Ip ip) {
    return MockRib.builder()
        .setLongestPrefixMatchResults(
            ImmutableMap.of(
                ip,
                ImmutableSet.of(
                    new AnnotatedRoute<>(
                        new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Eth0"), "vrf"),
                    new AnnotatedRoute<>(
                        new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Eth1"), "vrf"))))
        .build();
  }

  @Test
  public void testGetColumnMetadata() {
    List<ColumnMetadata> metadata = getColumnMetadata();
    assertThat(
        metadata,
        equalTo(
            ImmutableList.of(
                new ColumnMetadata(
                    COL_NODE, Schema.NODE, "Node where the route is present", true, false),
                new ColumnMetadata(
                    COL_VRF, Schema.STRING, "VRF where the route is present", true, false),
                new ColumnMetadata(COL_IP, Schema.IP, "IP that was being matched on", true, false),
                new ColumnMetadata(
                    COL_NETWORK,
                    Schema.PREFIX,
                    "The longest-prefix network that matched",
                    false,
                    true),
                new ColumnMetadata(
                    COL_NUM_ROUTES,
                    Schema.INTEGER,
                    "Number of routes that matched (in case of ECMP)",
                    false,
                    true))));
  }

  @Test
  public void testToRow() {
    final ImmutableMap<String, ColumnMetadata> columnMap =
        getColumnMetadata().stream()
            .collect(ImmutableMap.toImmutableMap(ColumnMetadata::getName, Function.identity()));
    Row row =
        toRow(
                ImmutableSet.of(
                    new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Eth0"),
                    new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "Eth1")),
                "node1",
                "vrf",
                Ip.parse("1.1.1.1"),
                columnMap)
            .get();
    assertThat(
        row,
        equalTo(
            Row.builder(columnMap)
                .put(COL_NODE, new Node("node1"))
                .put(COL_VRF, "vrf")
                .put(COL_IP, Ip.parse("1.1.1.1"))
                .put(COL_NETWORK, Prefix.parse("1.1.1.0/24"))
                .put(COL_NUM_ROUTES, 2)
                .build()));
  }

  @Test
  public void testFilteringByNodes() {
    final ImmutableMap<String, ColumnMetadata> columnMap =
        getColumnMetadata().stream()
            .collect(ImmutableMap.toImmutableMap(ColumnMetadata::getName, Function.identity()));

    final Ip lpmIp = Ip.parse("1.1.1.1");
    List<Row> rows =
        getRows(
            ImmutableSortedMap.of(
                "node1",
                ImmutableSortedMap.of("vrf1", getMockRib(lpmIp)),
                "node2",
                ImmutableSortedMap.of("vrf2", getMockRib(lpmIp))),
            lpmIp,
            ImmutableSet.of("node2"),
            Pattern.compile(".*"),
            columnMap);

    assertThat(rows, contains(hasColumn(COL_NODE, equalTo(new Node("node2")), Schema.NODE)));
  }

  @Test
  public void testFilteringByVrfs() {
    final ImmutableMap<String, ColumnMetadata> columnMap =
        getColumnMetadata().stream()
            .collect(ImmutableMap.toImmutableMap(ColumnMetadata::getName, Function.identity()));

    final Ip lpmIp = Ip.parse("1.1.1.1");
    List<Row> rows =
        getRows(
            ImmutableSortedMap.of(
                "node1",
                ImmutableSortedMap.of("vrf1", getMockRib(lpmIp)),
                "node2",
                ImmutableSortedMap.of("vrf2", getMockRib(lpmIp))),
            lpmIp,
            ImmutableSet.of("node1", "node2"),
            Pattern.compile("vrf1"),
            columnMap);

    assertThat(
        rows,
        contains(
            allOf(
                hasColumn(COL_NODE, equalTo(new Node("node1")), Schema.NODE),
                hasColumn(COL_VRF, equalTo("vrf1"), Schema.STRING))));
  }
}

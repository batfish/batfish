package org.batfish.question.routes;

import static org.batfish.question.routes.RoutesAnswerer.COL_NETWORK;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NEXT_HOP_IP;
import static org.batfish.question.routes.RoutesAnswerer.COL_NODE;
import static org.batfish.question.routes.RoutesAnswerer.COL_PROTOCOL;
import static org.batfish.question.routes.RoutesAnswerer.COL_VRF_NAME;
import static org.batfish.question.routes.RoutesAnswerer.getMainRibRoutes;
import static org.batfish.question.routes.RoutesAnswerer.getTableMetadata;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.junit.Test;

/** Tests of {@link RoutesAnswerer}. */
public class RoutesAnswererTest {
  @Test
  public void testGetMainRibRoutesWhenEmptyRib() {
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        ImmutableSortedMap.of(
            "n1", ImmutableSortedMap.of(Configuration.DEFAULT_VRF_NAME, new MockRib<>()));

    Multiset<Row> actual = getMainRibRoutes(ribs, ImmutableSet.of("n1"), ".*");

    assertThat(actual, hasSize(0));
  }

  @Test
  public void testHasNodeFiltering() {
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        ImmutableSortedMap.of(
            "n1",
            ImmutableSortedMap.of(
                Configuration.DEFAULT_VRF_NAME,
                new MockRib<>(
                    ImmutableSet.of(
                        StaticRoute.builder()
                            .setNetwork(Prefix.parse("1.1.1.0/24"))
                            .setNextHopInterface("Null")
                            .build()))));

    Multiset<Row> actual = getMainRibRoutes(ribs, ImmutableSet.of("differentNode"), ".*");

    assertThat(actual, hasSize(0));
  }

  @Test
  public void testHasVrfFiltering() {
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs =
        ImmutableSortedMap.of(
            "n1",
            ImmutableSortedMap.of(
                Configuration.DEFAULT_VRF_NAME,
                new MockRib<>(
                    ImmutableSet.of(
                        StaticRoute.builder()
                            .setNetwork(Prefix.parse("1.1.1.0/24"))
                            .setNextHopInterface("Null")
                            .build())),
                "notDefaultVrf",
                new MockRib<>(
                    ImmutableSet.of(
                        StaticRoute.builder()
                            .setNetwork(Prefix.parse("2.2.2.0/24"))
                            .setNextHopInterface("Null")
                            .build()))));

    Multiset<Row> actual = getMainRibRoutes(ribs, ImmutableSet.of("n1"), "^not.*");

    assertThat(actual, hasSize(1));
    assertThat(
        actual.iterator().next().getPrefix(COL_NETWORK), equalTo(Prefix.parse("2.2.2.0/24")));
  }

  @Test
  public void testGetTableMetadataProtocolAll() {
    List<ColumnMetadata> columnMetadata = getTableMetadata(RibProtocol.ALL).getColumnMetadata();

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableList.toImmutableList()),
        contains(COL_NODE, COL_VRF_NAME, COL_NETWORK, COL_PROTOCOL, COL_NEXT_HOP, COL_NEXT_HOP_IP));

    assertThat(
        columnMetadata
            .stream()
            .map(ColumnMetadata::getSchema)
            .collect(ImmutableList.toImmutableList()),
        contains(
            Schema.NODE, Schema.STRING, Schema.PREFIX, Schema.STRING, Schema.STRING, Schema.IP));
  }
}

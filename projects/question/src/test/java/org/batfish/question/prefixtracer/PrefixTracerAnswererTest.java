package org.batfish.question.prefixtracer;

import static org.batfish.question.prefixtracer.PrefixTracerAnswerer.COL_ACTION;
import static org.batfish.question.prefixtracer.PrefixTracerAnswerer.COL_NODE;
import static org.batfish.question.prefixtracer.PrefixTracerAnswerer.COL_PEER;
import static org.batfish.question.prefixtracer.PrefixTracerAnswerer.COL_PREFIX;
import static org.batfish.question.prefixtracer.PrefixTracerAnswerer.COL_VRF;
import static org.batfish.question.prefixtracer.PrefixTracerAnswerer.getRows;
import static org.batfish.question.prefixtracer.PrefixTracerAnswerer.getTableMetadata;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link PrefixTracerAnswerer} */
public class PrefixTracerAnswererTest {

  // Node -> VRF -> Prefix -> Action -> Set(peer hostname)
  private SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      _prefixTracingInfo;

  @Before
  public void setup() {
    // Setup sample data that would be extracted from the dataplane
    _prefixTracingInfo =
        ImmutableSortedMap.of(
            "n1",
            ImmutableSortedMap.of(
                "default",
                ImmutableMap.of(
                    Prefix.parse("1.1.1.1/32"), ImmutableMap.of("sent", ImmutableSet.of("n2")))),
            "n2",
            ImmutableSortedMap.of(
                "default",
                ImmutableMap.of(
                    Prefix.parse("1.1.1.1/32"),
                    ImmutableMap.of("received", ImmutableSet.of("n1")),
                    Prefix.parse("2.2.2.2/32"),
                    ImmutableMap.of("received", ImmutableSet.of("n3")))));
  }

  @Test
  public void testMatchesAllPrefixesWithoutSpecifying() {
    Multiset<Row> answer = getRows(_prefixTracingInfo, null, _prefixTracingInfo.keySet());
    assertThat(answer, hasSize(3));
  }

  @Test
  public void testPrefixAndNodeSpecifierIsConjunction() {
    Multiset<Row> answer =
        getRows(_prefixTracingInfo, Prefix.parse("1.1.1.1/32"), ImmutableSet.of("n2"));
    assertThat(answer, hasSize(1));
  }

  @Test
  public void testHasColumns() {
    // Set because order not important
    Set<String> columnNames =
        getTableMetadata().getColumnMetadata().stream()
            .map(ColumnMetadata::getName)
            .collect(ImmutableSet.toImmutableSet());

    assertThat(
        columnNames, equalTo(ImmutableSet.of(COL_NODE, COL_VRF, COL_PEER, COL_ACTION, COL_PREFIX)));
  }
}

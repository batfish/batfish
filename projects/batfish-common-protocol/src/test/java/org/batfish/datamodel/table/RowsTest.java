package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public final class RowsTest {

  @Test
  public void testEquals() {
    Row r1 = Row.of("val", 1);
    Row r2 = Row.of("val", 2);
    new EqualsTester()
        .addEqualityGroup(new Rows().add(r1), new Rows().add(r1))
        .addEqualityGroup(new Rows().add(r1).add(r1), new Rows().add(r1).add(r1))
        .addEqualityGroup(new Rows().add(r1).add(r2), new Rows().add(r2).add(r1))
        .testEquals();
  }

  @Test
  public void testIterationAndSerializationPreservation() {
    // Rows are insertion-ordered, with duplicates appearing next to their first instance.
    List<Row> outOfOrderRows =
        ImmutableList.of(Row.of("val", 1), Row.of("val", 2), Row.of("val", 1));
    List<Row> inOrderRows = ImmutableList.of(Row.of("val", 1), Row.of("val", 1), Row.of("val", 2));

    {
      Rows rows = new Rows();
      outOfOrderRows.forEach(rows::add);
      assertThat(ImmutableList.copyOf(rows.iterator()), equalTo(inOrderRows));
      // Survives cloning
      assertThat(
          ImmutableList.copyOf(SerializationUtils.clone(rows).iterator()), equalTo(inOrderRows));
      assertThat(
          ImmutableList.copyOf(BatfishObjectMapper.clone(rows, Rows.class).iterator()),
          equalTo(inOrderRows));
    }

    // Reverse insertion.
    {
      List<Row> reversedRows = Lists.reverse(inOrderRows);
      Rows reverse = new Rows();
      reversedRows.forEach(reverse::add);
      assertThat(ImmutableList.copyOf(reverse.iterator()), equalTo(reversedRows));
      // Survives cloning
      assertThat(
          ImmutableList.copyOf(SerializationUtils.clone(reverse).iterator()),
          equalTo(reversedRows));
      assertThat(
          ImmutableList.copyOf(BatfishObjectMapper.clone(reverse, Rows.class).iterator()),
          equalTo(reversedRows));
    }
  }
}

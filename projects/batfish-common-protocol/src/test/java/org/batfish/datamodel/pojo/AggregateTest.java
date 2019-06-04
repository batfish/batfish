package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.pojo.Aggregate.AggregateType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link Aggregate}. */
public class AggregateTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testJsonSerialization() throws IOException {
    Aggregate a = new Aggregate("someAgg", AggregateType.REGION);
    a.setContents(ImmutableSet.of("id1", "id2"));

    assertThat(a, equalTo(BatfishObjectMapper.clone(a, Aggregate.class)));
  }
}

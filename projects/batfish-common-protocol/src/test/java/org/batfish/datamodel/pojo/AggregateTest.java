package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
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

  @Test
  public void testEquals() throws IOException {
    Aggregate emptyCloud = new Aggregate("cloud", AggregateType.CLOUD);
    Aggregate emptyCloudCloned = BatfishObjectMapper.clone(emptyCloud, Aggregate.class);
    Aggregate diffName = new Aggregate("cloud2", AggregateType.CLOUD);
    Aggregate diffType = BatfishObjectMapper.clone(emptyCloud, Aggregate.class);
    diffType.setType(AggregateType.REGION);
    Aggregate cloudWithStuff = BatfishObjectMapper.clone(emptyCloud, Aggregate.class);
    cloudWithStuff.setContents(ImmutableSet.of("stuff"));

    new EqualsTester()
        .addEqualityGroup(emptyCloud, emptyCloudCloned)
        .addEqualityGroup(diffName)
        .addEqualityGroup(diffType)
        .addEqualityGroup(cloudWithStuff)
        .testEquals();
  }
}

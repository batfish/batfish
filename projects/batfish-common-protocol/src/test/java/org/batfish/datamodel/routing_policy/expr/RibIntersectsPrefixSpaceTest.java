package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.junit.Test;

/** Tests of {@link RibIntersectsPrefixSpace} */
@ParametersAreNonnullByDefault
public final class RibIntersectsPrefixSpaceTest {

  @Test
  public void testJavaSerialization() {
    RibIntersectsPrefixSpace obj =
        new RibIntersectsPrefixSpace(
            MainRib.instance(), new ExplicitPrefixSet(new PrefixSpace(ImmutableList.of())));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJsonSerialization() {
    RibIntersectsPrefixSpace obj =
        new RibIntersectsPrefixSpace(
            MainRib.instance(), new ExplicitPrefixSet(new PrefixSpace(ImmutableList.of())));
    assertThat(BatfishObjectMapper.clone(obj, RibIntersectsPrefixSpace.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new RibIntersectsPrefixSpace(
                MainRib.instance(), new ExplicitPrefixSet(new PrefixSpace(ImmutableList.of()))))
        .addEqualityGroup(
            new RibIntersectsPrefixSpace(
                MainRib.instance(),
                new ExplicitPrefixSet(new PrefixSpace(ImmutableList.of(PrefixRange.ALL)))))
        .testEquals();
  }
}

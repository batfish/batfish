package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link StandardCommunityHighLowExprs}. */
public final class StandardCommunityHighLowExprsTest {

  private static final StandardCommunityHighLowExprs OBJ =
      new StandardCommunityHighLowExprs(new LiteralInt(1), new LiteralInt(1));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, StandardCommunityHighLowExprs.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            OBJ, OBJ, new StandardCommunityHighLowExprs(new LiteralInt(1), new LiteralInt(1)))
        .addEqualityGroup(new StandardCommunityHighLowExprs(new LiteralInt(1), new LiteralInt(2)))
        .addEqualityGroup(new StandardCommunityHighLowExprs(new LiteralInt(2), new LiteralInt(2)))
        .testEquals();
  }
}

package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link LiteralInt}. */
public final class LiteralIntTest {

  private static final LiteralInt OBJ = new LiteralInt(1);

  @Test
  public void testJacksonSerialization() throws IOException {
    assertThat(BatfishObjectMapper.clone(OBJ, LiteralInt.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(OBJ, OBJ, new LiteralInt(1))
        .addEqualityGroup(new LiteralInt(2))
        .testEquals();
  }
}

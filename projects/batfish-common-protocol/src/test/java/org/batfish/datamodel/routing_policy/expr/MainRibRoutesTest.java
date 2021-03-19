package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link MainRib} */
@ParametersAreNonnullByDefault
public final class MainRibRoutesTest {

  @Test
  public void testJavaSerialization() {
    MainRib obj = MainRib.instance();
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJsonSerialization() {
    MainRib obj = MainRib.instance();
    assertThat(BatfishObjectMapper.clone(obj, MainRib.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    new EqualsTester().addEqualityGroup(MainRib.instance(), MainRib.instance()).testEquals();
  }
}

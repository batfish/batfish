package org.batfish.datamodel.routing_policy.as_path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link InputAsPath}. */
public final class InputAsPathTest {

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(InputAsPath.instance()), equalTo(InputAsPath.instance()));
  }

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(InputAsPath.instance(), InputAsPath.class),
        equalTo(InputAsPath.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(InputAsPath.instance(), InputAsPath.instance())
        .testEquals();
  }
}

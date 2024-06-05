package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ReceivedFromSelf}. */
public final class ReceivedFromSelfTest {

  @Test
  public void testJacksonSerialiation() {
    ReceivedFrom obj = ReceivedFromSelf.instance();
    assertThat(BatfishObjectMapper.clone(obj, ReceivedFrom.class), sameInstance(obj));
  }

  @Test
  public void testJavaSerialization() {
    ReceivedFrom obj = ReceivedFromSelf.instance();
    assertThat(SerializationUtils.clone(obj), sameInstance(obj));
  }

  @Test
  public void testEquals() {
    ReceivedFrom obj = ReceivedFromSelf.instance();
    new EqualsTester().addEqualityGroup(obj, ReceivedFromSelf.instance()).testEquals();
  }
}

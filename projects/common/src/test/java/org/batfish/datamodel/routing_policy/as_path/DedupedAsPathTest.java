package org.batfish.datamodel.routing_policy.as_path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link DedupedAsPath}. */
public final class DedupedAsPathTest {

  @Test
  public void testJavaSerialization() {
    DedupedAsPath obj = DedupedAsPath.of(InputAsPath.instance());
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    DedupedAsPath obj = DedupedAsPath.of(InputAsPath.instance());
    assertThat(BatfishObjectMapper.clone(obj, DedupedAsPath.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    DedupedAsPath obj = DedupedAsPath.of(InputAsPath.instance());
    new EqualsTester()
        .addEqualityGroup(obj, DedupedAsPath.of(InputAsPath.instance()))
        .addEqualityGroup(DedupedAsPath.of(DedupedAsPath.of(InputAsPath.instance())))
        .testEquals();
  }
}

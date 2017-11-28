package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestrigMetadataTest {
  @Test
  public void testTimestampSerialization() {
    TestrigMetadata metadata = new TestrigMetadata(java.time.Instant.ofEpochMilli((758949005001L)));
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    try {
      assertThat(
          mapper.writeValueAsString(metadata),
          equalTo("{\n  \"creationTimestamp\" : \"1994-01-19T03:10:05.001Z\"\n}"));
    } catch (Exception e) {
      throw new BatfishException("Cannot process the timestamp into json", e);
    }
  }
}

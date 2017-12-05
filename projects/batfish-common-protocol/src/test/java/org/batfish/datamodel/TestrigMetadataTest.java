package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestrigMetadataTest {
  @Test
  public void testTimestampSerialization() throws JsonProcessingException {
    TestrigMetadata metadata = new TestrigMetadata(Instant.ofEpochMilli((758949005001L)));
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    assertThat(
        mapper.writeValueAsString(metadata),
        equalTo("{\n  \"creationTimestamp\" : \"1994-01-19T03:10:05.001Z\"\n}"));
  }
}

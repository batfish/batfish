package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestrigMetadataTest {
  @Test
  public void serialization() throws JsonProcessingException {
    TestrigMetadata metadata = new TestrigMetadata(Instant.ofEpochMilli((758949005001L)));
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    JsonNode jsonNode = mapper.valueToTree(metadata);
    assertThat(jsonNode.get("creationTimestamp").asText(), equalTo("1994-01-19T03:10:05.001Z"));
    assertThat(
        jsonNode
            .get("environments")
            .get(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME)
            .get("processingStatus")
            .asText(),
        equalTo(ProcessingStatus.UNINITIALIZED.toString()));
  }
}

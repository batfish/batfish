package org.batfish.datamodel;

import static org.batfish.datamodel.TestrigMetadata.PROP_CREATION_TIMESTAMP;
import static org.batfish.datamodel.TestrigMetadata.PROP_ENVIRONMENTS;
import static org.batfish.datamodel.TestrigMetadata.PROP_PARENT_SNAPSHOT_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;
import org.batfish.identifiers.SnapshotId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestrigMetadataTest {
  @Test
  public void serialization() {
    TestrigMetadata metadata =
        new TestrigMetadata(
            Instant.ofEpochMilli(758949005001L),
            BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME,
            new SnapshotId("blah"));
    JsonNode jsonNode = BatfishObjectMapper.mapper().valueToTree(metadata);
    assertThat(jsonNode.get(PROP_CREATION_TIMESTAMP).asText(), equalTo("1994-01-19T03:10:05.001Z"));
    assertThat(jsonNode.get(PROP_PARENT_SNAPSHOT_ID).asText(), equalTo("blah"));
    assertThat(
        jsonNode
            .get(PROP_ENVIRONMENTS)
            .get(BfConsts.RELPATH_DEFAULT_ENVIRONMENT_NAME)
            .get("currentStatus")
            .asText(),
        equalTo(ProcessingStatus.UNINITIALIZED.toString()));
  }
}

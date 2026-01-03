package org.batfish.datamodel;

import static org.batfish.datamodel.SnapshotMetadata.PROP_CREATION_TIMESTAMP;
import static org.batfish.datamodel.SnapshotMetadata.PROP_INITIALIZATION_METADATA;
import static org.batfish.datamodel.SnapshotMetadata.PROP_PARENT_SNAPSHOT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.InitializationMetadata.ProcessingStatus;
import org.batfish.identifiers.SnapshotId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class SnapshotMetadataTest {
  @Test
  public void serialization() {
    SnapshotMetadata metadata =
        new SnapshotMetadata(Instant.ofEpochMilli(758949005001L), new SnapshotId("blah"));
    JsonNode jsonNode = BatfishObjectMapper.mapper().valueToTree(metadata);
    assertThat(jsonNode.get(PROP_CREATION_TIMESTAMP).asText(), equalTo("1994-01-19T03:10:05.001Z"));
    assertThat(jsonNode.get(PROP_PARENT_SNAPSHOT_ID).asText(), equalTo("blah"));
    assertThat(
        jsonNode.get(PROP_INITIALIZATION_METADATA).get("currentStatus").asText(),
        equalTo(ProcessingStatus.UNINITIALIZED.toString()));
  }
}

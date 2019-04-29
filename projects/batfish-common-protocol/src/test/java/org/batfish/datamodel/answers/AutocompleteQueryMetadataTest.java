package org.batfish.datamodel.answers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class AutocompleteQueryMetadataTest {

  @Test
  public void testEquals() {
    AutocompleteQueryMetadata metadata = new AutocompleteQueryMetadata(false, null, null);

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(metadata, metadata, new AutocompleteQueryMetadata(false, null, null))
        .addEqualityGroup(new AutocompleteQueryMetadata(true, null, null))
        .addEqualityGroup(new AutocompleteQueryMetadata(true, "description", null))
        .addEqualityGroup(
            new AutocompleteQueryMetadata(true, "description", ImmutableList.of("expansion")))
        .addEqualityGroup(
            new AutocompleteQueryMetadata(true, "description2", ImmutableList.of("expansion")))
        .addEqualityGroup(
            new AutocompleteQueryMetadata(true, "description", ImmutableList.of("expansion2")))
        .addEqualityGroup(
            new AutocompleteQueryMetadata(false, "description", ImmutableList.of("expansion")))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    AutocompleteQueryMetadata queryMetadata =
        new AutocompleteQueryMetadata(
            true, "some description", ImmutableList.of("expansion1", "expansion2"));

    assertThat(
        BatfishObjectMapper.clone(queryMetadata, AutocompleteQueryMetadata.class),
        equalTo(queryMetadata));
  }
}

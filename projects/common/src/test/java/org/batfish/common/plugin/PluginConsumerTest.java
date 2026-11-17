package org.batfish.common.plugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PluginConsumerTest {
  @TempDir public File _folder;

  @Test
  void testSerializingAndDeserializing() throws Exception {
    Path serializeFile = File.createTempFile("junit", null, _folder).toPath();

    int[] ints = new int[] {1, 2, 3};
    PluginConsumer.serializeObject(ints, serializeFile);

    int[] value = PluginConsumer.deserializeObject(serializeFile, int[].class);
    assertThat(value, equalTo(ints));
  }
}

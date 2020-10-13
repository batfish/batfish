package org.batfish.common.plugin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PluginConsumerTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testSerializingAndDeserializing() throws Exception {
    Path serializeFile = _folder.newFile().toPath();

    int[] ints = new int[] {1, 2, 3};
    PluginConsumer.serializeObject(ints, serializeFile);

    int[] value = PluginConsumer.deserializeObject(serializeFile, int[].class);
    assertThat(value, equalTo(ints));
  }
}

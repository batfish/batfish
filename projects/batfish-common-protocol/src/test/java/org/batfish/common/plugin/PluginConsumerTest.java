package org.batfish.common.plugin;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PluginConsumerTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private void runSerializationTest() throws Exception {
    Path serializeFile = _folder.newFile().toPath();

    int[] ints = new int[] {1, 2, 3};
    PluginConsumer.serializeObject(ints, serializeFile);

    int[] value = PluginConsumer.deserializeObject(serializeFile, int[].class);
    assertThat(value, equalTo(ints));
  }

  @Test
  public void testSerializingAndDeserializingJava() throws Exception {
    runSerializationTest();
  }

  @Test
  public void testSerializingAndDeserializingText() throws Exception {
    runSerializationTest();
  }
}

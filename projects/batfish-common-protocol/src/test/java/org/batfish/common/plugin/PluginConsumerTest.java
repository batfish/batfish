package org.batfish.common.plugin;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import org.batfish.common.BatfishLogger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PluginConsumerTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static class TestPluginConsumer extends PluginConsumer {
    private TestPluginConsumer(boolean serializeToText) {
      super(serializeToText);
    }

    @Override
    public PluginClientType getType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public BatfishLogger getLogger() {
      throw new UnsupportedOperationException();
    }
  }

  private void runSerializationTest(boolean serializeToText) throws Exception {
    Path serializeFile = _folder.newFile().toPath();
    TestPluginConsumer consumer = new TestPluginConsumer(serializeToText);

    int[] ints = new int[] {1, 2, 3};
    consumer.serializeObject(ints, serializeFile);

    int[] value = consumer.deserializeObject(serializeFile, int[].class);
    assertThat(value, equalTo(ints));
  }

  @Test
  public void testSerializingAndDeserializingJava() throws Exception {
    runSerializationTest(false);
  }

  @Test
  public void testSerializingAndDeserializingText() throws Exception {
    runSerializationTest(true);
  }
}

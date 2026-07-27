package org.batfish.main;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.main.StreamDecoder.decodeStreamAndAppendNewline;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.primitives.Bytes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.io.ByteOrderMark;
import org.junit.Test;

/** Tests of {@link StreamDecoder}. */
public final class StreamDecoderTest {

  @Test
  public void testDecodeStreamAndAppendNewlineEmpty() throws IOException {
    assertThat(decodeStreamAndAppendNewline(new ByteArrayInputStream(new byte[0])), equalTo(""));
  }

  @Test
  public void testDecodeStreamAndAppendNewlineUtf8() throws IOException {
    String text = "café";
    assertThat(
        decodeStreamAndAppendNewline(new ByteArrayInputStream(text.getBytes(UTF_8))),
        equalTo(text + "\n"));
  }

  @Test
  public void testDecodeStreamAndAppendNewlineUtf16LeBom() throws IOException {
    String text = "café";
    byte[] bytes = Bytes.concat(ByteOrderMark.UTF_16LE.getBytes(), text.getBytes(UTF_16LE));
    assertThat(decodeStreamAndAppendNewline(new ByteArrayInputStream(bytes)), equalTo(text + "\n"));
  }
}

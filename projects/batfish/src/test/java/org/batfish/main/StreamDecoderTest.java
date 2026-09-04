package org.batfish.main;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.main.StreamDecoder.decodeStreamAndAppendNewline;
import static org.batfish.main.StreamDecoder.isPrintableAscii;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.primitives.Bytes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
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
  public void testDecodeStreamAndAppendNewlineAscii() throws IOException {
    String text = "hostname r1\r\n!\tinterface e1\n";
    assertThat(
        decodeStreamAndAppendNewline(new ByteArrayInputStream(text.getBytes(US_ASCII))),
        equalTo(text + "\n"));
  }

  @Test
  public void testIsPrintableAscii() {
    assertTrue(isPrintableAscii("hostname r1\r\n!\tinterface e1\f\n".getBytes(US_ASCII)));
    assertTrue(isPrintableAscii(new byte[0]));
    // Latin-1 and UTF-8 bytes above 0x7F.
    assertFalse(isPrintableAscii("café".getBytes(ISO_8859_1)));
    assertFalse(isPrintableAscii("café".getBytes(UTF_8)));
    // A BOM, a NUL as in UTF-16, an ESC as in ISO-2022, and DEL.
    assertFalse(isPrintableAscii(ByteOrderMark.UTF_8.getBytes()));
    assertFalse(isPrintableAscii(new byte[] {'a', 0, 'b', 0}));
    assertFalse(isPrintableAscii(new byte[] {'a', 0x1B, '$', 'B'}));
    assertFalse(isPrintableAscii(new byte[] {'a', 0x7F}));
    // A bad byte at every position of inputs spanning the word-at-a-time and tail paths.
    for (int length = 1; length <= 20; length++) {
      for (int position = 0; position < length; position++) {
        for (byte bad : new byte[] {0, 0x08, 0x0E, 0x1B, 0x1F, 0x7F, (byte) 0x80, (byte) 0xFF}) {
          byte[] bytes = new byte[length];
          Arrays.fill(bytes, (byte) 'x');
          bytes[position] = bad;
          assertFalse(length + "/" + position + "/" + bad, isPrintableAscii(bytes));
          bytes[position] = position % 2 == 0 ? (byte) '\t' : (byte) '~';
          assertTrue(isPrintableAscii(bytes));
        }
      }
    }
  }

  @Test
  public void testDecodeStreamAndAppendNewlineUtf16LeBom() throws IOException {
    String text = "café";
    byte[] bytes = Bytes.concat(ByteOrderMark.UTF_16LE.getBytes(), text.getBytes(UTF_16LE));
    assertThat(decodeStreamAndAppendNewline(new ByteArrayInputStream(bytes)), equalTo(text + "\n"));
  }
}

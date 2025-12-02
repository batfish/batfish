package org.batfish.main;

import com.ibm.icu.text.CharsetDetector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

/** Utility class for decoding streams of unknown charset to strings. */
final class StreamDecoder {

  /**
   * Automatically detects charset of the input stream, reads it, decodes it, and returns the
   * resulting string with a newline appended if the original stream is non-empty. Does not close
   * the provided input stream.
   *
   * @throws IOException if there is an error
   */
  static @Nonnull String decodeStreamAndAppendNewline(@Nonnull InputStream inputStream)
      throws IOException {
    byte[] rawBytes = inputStream.readAllBytes();
    if (rawBytes.length == 0) {
      return "";
    }
    Charset cs = Charset.forName(new CharsetDetector().setText(rawBytes).detect().getName());
    try (BOMInputStream bomStream = bomInputStream(new ByteArrayInputStream(rawBytes))) {
      return new String(bomStream.readAllBytes(), cs) + "\n";
    }
  }

  private static @Nonnull BOMInputStream bomInputStream(@Nonnull InputStream inputStream) {
    return new BOMInputStream(
        inputStream,
        ByteOrderMark.UTF_8,
        ByteOrderMark.UTF_16BE,
        ByteOrderMark.UTF_16LE,
        ByteOrderMark.UTF_32BE,
        ByteOrderMark.UTF_32LE);
  }

  private StreamDecoder() {}
}

package org.batfish.main;

import com.google.common.io.Closer;
import com.ibm.icu.text.CharsetDetector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
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
    Charset cs = Charset.forName(new CharsetDetector().setText(rawBytes).detect().getName());
    try (Closer closer = Closer.create()) {
      InputStream inputByteStream =
          closer.register(bomInputStream(new ByteArrayInputStream(rawBytes)));
      InputStream finalInputStream =
          closer.register(
              rawBytes.length > 0
                  ? new SequenceInputStream(
                      inputByteStream,
                      closer.register(bomInputStream(new ByteArrayInputStream("\n".getBytes(cs)))))
                  : inputByteStream);
      return new String(finalInputStream.readAllBytes(), cs);
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

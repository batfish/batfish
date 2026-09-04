package org.batfish.main;

import com.google.common.annotations.VisibleForTesting;
import com.ibm.icu.text.CharsetDetector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    if (isPrintableAscii(rawBytes)) {
      // Charset detection is a large fraction of the cost of reading a file. Every charset it can
      // report for such bytes (there are no escape sequences for ISO-2022 and no BOM or byte
      // pattern for UTF-16 or UTF-32) decodes them to the same characters, so skip it.
      return new String(rawBytes, StandardCharsets.US_ASCII) + "\n";
    }
    Charset cs = Charset.forName(new CharsetDetector().setText(rawBytes).detect().getName());
    try (BOMInputStream bomStream = bomInputStream(new ByteArrayInputStream(rawBytes))) {
      return new String(bomStream.readAllBytes(), cs) + "\n";
    }
  }

  private static final VarHandle LONGS =
      MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);

  /** Whether every byte is a printable ASCII character or one of tab, LF, VT, FF, CR. */
  @VisibleForTesting
  static boolean isPrintableAscii(byte[] bytes) {
    int n = bytes.length;
    int i = 0;
    // Eight bytes at a time: a word with no byte 0x80 or above, none below 0x20, and no 0x7F needs
    // no closer look. The subtraction borrows into the high bit of exactly the bytes below 0x20
    // once the high bits are known clear; the xor makes 0x7F bytes zero for the same test.
    for (; i + 8 <= n; i += 8) {
      long w = (long) LONGS.get(bytes, i);
      if ((w & 0x8080808080808080L) != 0) {
        return false;
      }
      long control = (w - 0x2020202020202020L) & 0x8080808080808080L;
      long del = ((w ^ 0x7F7F7F7F7F7F7F7FL) - 0x0101010101010101L) & 0x8080808080808080L;
      if ((control | del) != 0 && !isPrintableAscii(bytes, i, i + 8)) {
        return false;
      }
    }
    return isPrintableAscii(bytes, i, n);
  }

  private static boolean isPrintableAscii(byte[] bytes, int from, int to) {
    for (int i = from; i < to; i++) {
      byte b = bytes[i];
      if ((b < 0x20 && (b < 0x09 || b > 0x0D)) || b >= 0x7F) {
        return false;
      }
    }
    return true;
  }

  private static @Nonnull BOMInputStream bomInputStream(@Nonnull InputStream inputStream)
      throws IOException {
    return BOMInputStream.builder()
        .setInputStream(inputStream)
        .setByteOrderMarks(
            ByteOrderMark.UTF_8,
            ByteOrderMark.UTF_16BE,
            ByteOrderMark.UTF_16LE,
            ByteOrderMark.UTF_32BE,
            ByteOrderMark.UTF_32LE)
        .get();
  }

  private StreamDecoder() {}
}

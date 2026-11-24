package org.batfish.common.util;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.CommonUtil.readFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests of {@link CommonUtil}. */
final class CommonUtilTest {

  @TempDir public File _folder;

  @Test
  void testDetectCharsetUtf8RoundTrip() {
    String text = "snowman ☃";
    byte[] bytes = text.getBytes(UTF_8);
    assertThat(new String(bytes, CommonUtil.detectCharset(bytes)), equalTo(text));
  }

  @Test
  void testReadFileIso88591RoundTrip() throws IOException {
    String text = "café piñata";
    Path file = _folder.toPath().resolve("latin1.txt");
    Files.write(file, text.getBytes(ISO_8859_1));
    assertThat(readFile(file), equalTo(text));
  }
}

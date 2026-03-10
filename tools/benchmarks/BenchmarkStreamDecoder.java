package tools.benchmarks;

import com.google.common.io.Closer;
import com.ibm.icu.text.CharsetDetector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class BenchmarkStreamDecoder {

  @Param({"10", "25", "50", "100"})
  private int sizeMiB;

  @Param({"UTF-8", "UTF-16", "ISO-8859-1"})
  private String charsetName;

  private byte[] testData;

  @Setup(Level.Trial)
  public void setUp() {
    Charset charset = Charset.forName(charsetName);
    int targetBytes = sizeMiB * 1024 * 1024;

    // Generate random printable ASCII text, then encode in target charset
    Random rng = new Random(42); // fixed seed for reproducibility
    StringBuilder sb = new StringBuilder();
    while (sb.length() < targetBytes) {
      // Generate lines of random printable ASCII
      int lineLen = 40 + rng.nextInt(80);
      for (int i = 0; i < lineLen; i++) {
        sb.append((char) (32 + rng.nextInt(95))); // printable ASCII
      }
      sb.append('\n');
    }
    testData = sb.toString().getBytes(charset);
  }

  @Benchmark
  public String benchOriginal() throws IOException {
    return decodeOriginal(new ByteArrayInputStream(testData));
  }

  @Benchmark
  public String benchSimplified() throws IOException {
    return decodeSimplified(new ByteArrayInputStream(testData));
  }

  // Original implementation
  private static String decodeOriginal(InputStream inputStream) throws IOException {
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

  // Simplified implementation
  private static String decodeSimplified(InputStream inputStream) throws IOException {
    byte[] rawBytes = inputStream.readAllBytes();
    if (rawBytes.length == 0) {
      return "";
    }
    Charset cs = Charset.forName(new CharsetDetector().setText(rawBytes).detect().getName());
    try (BOMInputStream bomStream = bomInputStream(new ByteArrayInputStream(rawBytes))) {
      return new String(bomStream.readAllBytes(), cs) + "\n";
    }
  }

  private static BOMInputStream bomInputStream(InputStream inputStream) {
    return new BOMInputStream(
        inputStream,
        ByteOrderMark.UTF_8,
        ByteOrderMark.UTF_16BE,
        ByteOrderMark.UTF_16LE,
        ByteOrderMark.UTF_32BE,
        ByteOrderMark.UTF_32LE);
  }
}

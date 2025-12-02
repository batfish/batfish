package org.batfish.common.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;

/** Utility class for reading resources in the classpath */
public final class Resources {

  /**
   * Returns the contents of the resource at the provided path as a string with the given charset.
   */
  public static @Nonnull String readResource(String resourcePath, Charset charset) {
    return new String(readResourceBytes(resourcePath), charset);
  }

  /** Returns the contents of the resource at the provided path as a byte array. */
  public static @Nonnull byte[] readResourceBytes(@Nonnull String resourcePath) {
    try (InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
      checkArgument(is != null, "Error opening resource: '%s'", resourcePath);
      return is.readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not open resource: '" + resourcePath + "'", e);
    }
  }

  private Resources() {}
}

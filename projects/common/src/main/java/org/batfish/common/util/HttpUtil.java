package org.batfish.common.util;

import javax.annotation.Nonnull;
import javax.ws.rs.BadRequestException;

public final class HttpUtil {

  /**
   * If {@code precondition} is {@code false}, throw {@link BadRequestException} with message
   * constructed from {@code format} string and {@code args}.
   */
  public static void checkClientArgument(
      boolean precondition, @Nonnull String format, @Nonnull Object... args) {
    if (!precondition) {
      throw new BadRequestException(String.format(format, args));
    }
  }

  private HttpUtil() {}
}

package org.batfish.common.util;

import static org.batfish.common.util.HttpUtil.checkClientArgument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

final class HttpUtilTest {

  @Test
  void testCheckClientArgumentFalseNoVarArgs() {
    Throwable exception =
        assertThrows(BadRequestException.class, () -> checkClientArgument(false, "message"));
    assertThat(exception.getMessage(), containsString("message"));
  }

  @Test
  void testCheckClientArgumentFalseVarArgs() {
    Throwable exception =
        assertThrows(
            BadRequestException.class, () -> checkClientArgument(false, "%s%s", "var", "args"));
    assertThat(exception.getMessage(), containsString("varargs"));
  }

  @Test
  void testCheckClientArgumentTrue() {
    checkClientArgument(true, "");
  }
}

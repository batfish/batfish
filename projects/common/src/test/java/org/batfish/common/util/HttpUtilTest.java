package org.batfish.common.util;

import static org.batfish.common.util.HttpUtil.checkClientArgument;

import javax.ws.rs.BadRequestException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class HttpUtilTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testCheckClientArgumentFalseNoVarArgs() {
    _thrown.expect(BadRequestException.class);
    _thrown.expectMessage("message");
    checkClientArgument(false, "message");
  }

  @Test
  public void testCheckClientArgumentFalseVarArgs() {
    _thrown.expect(BadRequestException.class);
    _thrown.expectMessage("varargs");
    checkClientArgument(false, "%s%s", "var", "args");
  }

  @Test
  public void testCheckClientArgumentTrue() {
    checkClientArgument(true, "");
  }
}

package org.batfish.z3;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.microsoft.z3.Context;
import com.microsoft.z3.Params;
import org.batfish.config.Settings;
import org.junit.Test;

/** Tests of {@link Z3ContextJob}. */
public class Z3ContextJobTest {
  @Test
  public void testTimeoutIsPropagated() {
    Settings settings = new Settings(new String[] {"-z3timeout=1"});
    assertThat(settings.getZ3timeout(), equalTo(1));

    try (Context ctx = new Context()) {
      Params p = Z3ContextJob.makeZ3Params(ctx, settings, true);
      assertThat(p.toString(), containsString("params timeout 1"));
    }
  }
}

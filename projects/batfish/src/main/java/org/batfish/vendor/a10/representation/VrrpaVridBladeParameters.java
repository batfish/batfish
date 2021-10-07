package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration for a vrrp-a vrid's blade-parameters. */
public final class VrrpaVridBladeParameters implements Serializable {

  public @Nullable String getFailOverPolicyTemplate() {
    return _failOverPolicyTemplate;
  }

  public void setFailOverPolicyTemplate(@Nullable String failOverPolicyTemplate) {
    _failOverPolicyTemplate = failOverPolicyTemplate;
  }

  public @Nullable Integer getPriority() {
    return _priority;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  private @Nullable String _failOverPolicyTemplate;
  private @Nullable Integer _priority;
}

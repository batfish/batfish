package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** OSPF process configuration for Huawei device. */
public class HuaweiOspfProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  private long _processId;
  private @Nullable Ip _routerId;
  private final @Nonnull Map<Long, HuaweiOspfArea> _areas;

  public HuaweiOspfProcess(long processId) {
    _processId = processId;
    _areas = new HashMap<>();
  }

  public long getProcessId() {
    return _processId;
  }

  public void setProcessId(long processId) {
    _processId = processId;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public @Nonnull Map<Long, HuaweiOspfArea> getAreas() {
    return _areas;
  }
}

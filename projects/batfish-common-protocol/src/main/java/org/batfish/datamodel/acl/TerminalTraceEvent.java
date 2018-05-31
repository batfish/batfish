package org.batfish.datamodel.acl;

import org.batfish.datamodel.FilterResult;

public interface TerminalTraceEvent extends TraceEvent {
  FilterResult toFilterResult();
}

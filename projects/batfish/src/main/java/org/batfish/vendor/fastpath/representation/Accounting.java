package org.batfish.vendor.fastpath.representation;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;

/** A single {@code aaa accounting {exec|commands|dot1x}} method list. */
public final class Accounting implements Serializable {

  /** When accounting notices are emitted. */
  public enum RecordType {
    NONE,
    START_STOP,
    STOP_ONLY
  }

  private final @Nonnull AccountingType _type;
  private final @Nonnull String _name;
  private final @Nonnull RecordType _recordType;
  private final @Nonnull List<AaaMethod> _methods;

  public Accounting(
      AccountingType type, String name, RecordType recordType, List<AaaMethod> methods) {
    _type = type;
    _name = name;
    _recordType = recordType;
    _methods = ImmutableList.copyOf(methods);
  }

  public @Nonnull AccountingType getType() {
    return _type;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull RecordType getRecordType() {
    return _recordType;
  }

  public @Nonnull List<AaaMethod> getMethods() {
    return _methods;
  }
}

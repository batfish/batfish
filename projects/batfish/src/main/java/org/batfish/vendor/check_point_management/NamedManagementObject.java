package org.batfish.vendor.check_point_management;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Abstract class representing a management object with a name and UID. */
public abstract class NamedManagementObject extends ManagementObject implements HasName {

  protected static final String PROP_NAME = "name";

  protected NamedManagementObject(String name, Uid uid) {
    super(uid);
    _name = name;
  }

  @Override
  public final @Nonnull String getName() {
    return _name;
  }

  @Override
  protected boolean baseEquals(Object o) {
    if (!super.baseEquals(o)) {
      return false;
    }
    NamedManagementObject that = (NamedManagementObject) o;
    return _name.equals(that._name);
  }

  @Override
  protected int baseHashcode() {
    return Objects.hash(super.baseHashcode(), _name);
  }

  @Override
  protected @Nonnull ToStringHelper baseToStringHelper() {
    return super.baseToStringHelper().add(PROP_NAME, _name);
  }

  private final @Nonnull String _name;
}

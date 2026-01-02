package org.batfish.datamodel.vendor_family.cisco_xr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.common.util.ComparableStructure;

public class User extends ComparableStructure<String> {

  private static final String PROP_PASSWORD = "password";
  private static final String PROP_ROLE = "role";

  private @Nullable String _password;

  private @Nullable String _role;

  @JsonCreator
  public User(@JsonProperty(PROP_NAME) @Nullable String name) {
    super(name);
  }

  @JsonProperty(PROP_PASSWORD)
  public @Nullable String getPassword() {
    return _password;
  }

  @JsonProperty(PROP_ROLE)
  public @Nullable String getRole() {
    return _role;
  }

  @JsonProperty(PROP_PASSWORD)
  public void setPassword(@Nullable String password) {
    _password = password;
  }

  @JsonProperty(PROP_ROLE)
  public void setRole(@Nullable String role) {
    _role = role;
  }
}

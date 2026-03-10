package org.batfish.datamodel.vendor_family.juniper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.AuthenticationMethod;
import org.batfish.datamodel.Line;

public class JuniperFamily implements Serializable {
  private static final String PROP_LINES = "lines";
  private static final String PROP_ROOT_AUTHENTICATION_ENCRYPTED_PASSWORD =
      "rootAuthenticationEncryptedPassword";
  private static final String PROP_SYSTEM_AUTHENTICATION_ORDER = "systemAuthenticationOrder";
  private static final String PROP_TACPLUS_SERVERS = "tacplusServers";

  @VisibleForTesting public static final String CONSOLE_LINE_NAME = "console";
  @VisibleForTesting public static final String AUXILIARY_LINE_NAME = "auxiliary";

  private SortedMap<String, Line> _lines;

  private String _rootAuthenticationEncryptedPassword;

  private AaaAuthenticationLoginList _systemAuthenticationOrder;

  private SortedMap<String, TacplusServer> _tacplusServers;

  public JuniperFamily() {
    _lines = new TreeMap<>();
    _tacplusServers = new TreeMap<>();
    _systemAuthenticationOrder = // default authentication order is just password authentication
        new AaaAuthenticationLoginList(
            Collections.singletonList(AuthenticationMethod.PASSWORD), true);

    // Juniper has by default the console and aux lines enabled
    Line console = new Line(CONSOLE_LINE_NAME);
    console.setAaaAuthenticationLoginList(_systemAuthenticationOrder);
    _lines.put(CONSOLE_LINE_NAME, console);

    Line aux = new Line(AUXILIARY_LINE_NAME);
    aux.setAaaAuthenticationLoginList(_systemAuthenticationOrder);
    _lines.put(AUXILIARY_LINE_NAME, aux);
  }

  @JsonProperty(PROP_LINES)
  public SortedMap<String, Line> getLines() {
    return _lines;
  }

  @JsonProperty(PROP_ROOT_AUTHENTICATION_ENCRYPTED_PASSWORD)
  public String getRootAuthenticationEncryptedPassword() {
    return _rootAuthenticationEncryptedPassword;
  }

  @JsonProperty(PROP_SYSTEM_AUTHENTICATION_ORDER)
  public AaaAuthenticationLoginList getSystemAuthenticationOrder() {
    return _systemAuthenticationOrder;
  }

  @JsonProperty(PROP_TACPLUS_SERVERS)
  public SortedMap<String, TacplusServer> getTacplusServers() {
    return _tacplusServers;
  }

  @JsonProperty(PROP_LINES)
  public void setLines(SortedMap<String, Line> lines) {
    _lines = lines;
  }

  @JsonProperty(PROP_ROOT_AUTHENTICATION_ENCRYPTED_PASSWORD)
  public void setRootAuthenticationEncryptedPassword(String rootAuthenticationEncryptedPassword) {
    _rootAuthenticationEncryptedPassword = rootAuthenticationEncryptedPassword;
  }

  @JsonProperty(PROP_SYSTEM_AUTHENTICATION_ORDER)
  public void setSystemAuthenticationOrder(AaaAuthenticationLoginList authenticationOrder) {
    _systemAuthenticationOrder = authenticationOrder;
  }

  @JsonProperty(PROP_TACPLUS_SERVERS)
  public void setTacplusServers(SortedMap<String, TacplusServer> tacplusServers) {
    _tacplusServers = tacplusServers;
  }
}

package org.batfish.datamodel.vendor_family.juniper;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.AuthenticationMethod;
import org.batfish.datamodel.Line;

public class JuniperFamily implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

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

  public SortedMap<String, Line> getLines() {
    return _lines;
  }

  public String getRootAuthenticationEncryptedPassword() {
    return _rootAuthenticationEncryptedPassword;
  }

  public AaaAuthenticationLoginList getSystemAuthenticationOrder() {
    return _systemAuthenticationOrder;
  }

  public SortedMap<String, TacplusServer> getTacplusServers() {
    return _tacplusServers;
  }

  public void setLines(SortedMap<String, Line> lines) {
    _lines = lines;
  }

  public void setRootAuthenticationEncryptedPassword(String rootAuthenticationEncryptedPassword) {
    _rootAuthenticationEncryptedPassword = rootAuthenticationEncryptedPassword;
  }

  public void setSystemAuthenticationOrder(AaaAuthenticationLoginList authenticationOrder) {
    _systemAuthenticationOrder = authenticationOrder;
  }

  public void setTacplusServers(SortedMap<String, TacplusServer> tacplusServers) {
    _tacplusServers = tacplusServers;
  }
}

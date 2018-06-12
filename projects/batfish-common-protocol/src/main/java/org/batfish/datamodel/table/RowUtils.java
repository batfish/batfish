package org.batfish.datamodel.table;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;

@ParametersAreNonnullByDefault
public class RowUtils {

  public static Integer getInteger(Row row, String column) {
    return (Integer) row.get(column, Schema.INTEGER);
  }

  public static NodeInterfacePair getInterface(Row row, String column) {
    return (NodeInterfacePair) row.get(column, Schema.INTERFACE);
  }

  public static Ip getIp(Row row, String column) {
    return (Ip) row.get(column, Schema.IP);
  }

  public static Node getNode(Row row, String column) {
    return (Node) row.get(column, Schema.NODE);
  }

  public static Prefix getPrefix(Row row, String column) {
    return (Prefix) row.get(column, Schema.PREFIX);
  }

  public static String getString(Row row, String column) {
    return (String) row.get(column, Schema.STRING);
  }
}

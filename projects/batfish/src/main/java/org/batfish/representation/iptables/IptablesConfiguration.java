package org.batfish.representation.iptables;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.representation.iptables.IptablesChain.ChainPolicy;
import org.batfish.vendor.VendorConfiguration;

public abstract class IptablesConfiguration extends VendorConfiguration {

  private final @Nonnull Map<String, IptablesTable> _tables = new HashMap<>();

  public void addChain(String tableName, String chainName) {
    addTable(tableName);
    _tables.get(tableName).addChain(chainName);
  }

  public void addRule(String tableName, String chainName, IptablesRule rule, int index) {
    addTable(tableName);
    _tables.get(tableName).addRule(chainName, rule, index);
  }

  public void addTable(String tableName) {
    if (!_tables.containsKey(tableName)) {
      _tables.put(tableName, new IptablesTable(tableName));
    }
  }

  public void setChainPolicy(String tableName, String chainName, ChainPolicy policy) {
    addTable(tableName);
    _tables.get(tableName).setChainPolicy(chainName, policy);
  }

  public @Nonnull Map<String, IptablesTable> getTables() {
    return _tables;
  }
}

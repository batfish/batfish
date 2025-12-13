package jdd.bdd;

import jdd.util.JDDConsole;

/**
 * Additional tests and debug functions for NodeTable. These where moved here to make NodeTable
 * simpler and cleaner.
 */
public class NodeTableChecker {
  private NodeTable nt;

  public NodeTableChecker(NodeTable nt) {
    this.nt = nt;
  }

  // --------------------------------------------------------------------

  private void show_tuple(int i) {
    JDDConsole.out.printf(
        "%d\t%d\t%d\t%d\n", i, nt.getVar(i), nt.getLow(i), nt.getHigh(i), nt.getRef(i));
  }

  public void showTable(boolean complete) {
    final int size = nt.debug_table_size();
    JDDConsole.out.println(complete ? "Node-table (complete):" : "Node-table:");
    for (int i = 0; i < size; i++) {
      if (complete || nt.isValid(i)) show_tuple(i);
    }
  }

  // --------------------------------------------------------------------

  /** check if one node is okay */
  public String checkNode(int node, String msg) {
    if (node < 2) return null;

    if (!nt.isValid(node)) {
      show_tuple(node);
      return "Node " + node + " invalid " + ((msg != null) ? msg : "");
    }

    // XXX: we should mark nodes so we dont check them multiple
    //      times if we are doing anything recursive!
    String err = checkNode(nt.getLow(node), msg);
    if (err != null) err = checkNode(nt.getHigh(node), msg);
    return err;
  }

  /** check if all nodes are ok. it will test validaty and ref-count of nodes in the table */
  public String checkAllNodes(String msg) {
    String err = null;

    for (int i = 0; err != null && i < nt.debug_nstack_size(); i++)
      err = checkNode(nt.debug_nstack_item(i), msg);

    for (int i = 0; err != null && i < nt.debug_table_size(); i++)
      if (nt.isValid(i) && nt.getRefPlain(i) > 0) err = checkNode(i, msg);
    return err;
  }

  public String check() {
    final int table_size = nt.debug_table_size();
    final int free_nodes_count = nt.debug_free_nodes_count();

    // see if the number of free nodes is correct
    int c = 2, b = 0;
    for (int i = 2; i < table_size; i++)
      if (nt.isValid(i)) c++;
      else b++;

    if (table_size - c != free_nodes_count)
      return "Invalid # of free nodes: #live= "
          + c
          + ", table_size="
          + table_size
          + ", free_nodes_count="
          + free_nodes_count;

    // see if a nodes children point to invalid entries:
    for (int i = 0; i < table_size; i++) {
      if (nt.isValid(i)) {
        if (nt.getLow(i) < 0 || nt.getHigh(i) < 0) {
          show_tuple(i);
          return "Invalied node entry " + i;
        }

        int low = nt.getLow(i);
        int high = nt.getHigh(i);
        if ((low > 1 && !nt.isValid(low)) || (high > 1 && !nt.isValid(high))) {
          show_tuple(i);
          show_tuple(low);
          show_tuple(high);
          return "Children of " + i + " are not valid: " + low + "/" + high;
        }
      }
    }

    if (table_size > 100) {
      // out.println("(omitting slow parts of NodeTable.check(), table too large)");
      return null;
    }

    // slow O(N * N) test to see if there are two of any nodes in the table
    for (int i = 0; i < table_size; i++) {
      if (!nt.isValid(i)) continue;

      int var = nt.getVar(i);
      int low = nt.getLow(i);
      int high = nt.getHigh(i);

      for (int j = i + 1; j < table_size; j++) {
        if (var == nt.getVar(j) && low == nt.getLow(j) && high == nt.getHigh(j)) {
          show_tuple(i);
          show_tuple(j);
          return "Duplicate entries in NodeTable (" + i + " and " + j + "): ";
        }
      }
    }

    return null;
  }
}

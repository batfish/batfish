package jdd.examples;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import jdd.bdd.debug.BDDTrace;
import jdd.util.JDDConsole;
import jdd.util.Options;
import jdd.util.TextAreaTarget;

/** TraceApp is a simple AWT app for running traces */
public class TraceApp extends Frame implements ActionListener, WindowListener {
  private TextArea msg, code;
  private Button bRun, bClear, bLoad;
  private Checkbox cbVerbose;
  private Choice initialNodes;

  private String initial_text =
      "MODULE c17\n"
          + "INPUT\n"
          + "	1gat,2gat,3gat,6gat,7gat;\n"
          + "OUTPUT\n"
          + "	22gat,23gat;\n"
          + "STRUCTURE\n"
          + "	10gat = nand(1gat, 3gat);\n"
          + "	11gat = nand(3gat, 6gat);\n"
          + "	16gat = nand(2gat, 11gat);\n"
          + "	19gat = nand(11gat, 7gat);\n"
          + "	22gat = nand(10gat, 16gat);\n"
          + "	23gat = nand(16gat, 19gat);\n"
          + "	print_bdd(23gat);\n"
          + "ENDMODULE\n";

  public TraceApp() {
    setLayout(new BorderLayout());

    Panel p = new Panel(new FlowLayout(FlowLayout.LEFT));
    add(p, BorderLayout.NORTH);
    p.add(bRun = new Button("Run"));
    p.add(bLoad = new Button("Load file"));
    p.add(bClear = new Button("Clear"));
    bRun.addActionListener(this);
    bLoad.addActionListener(this);
    bClear.addActionListener(this);

    p.add(new Label("  Initial node-base"));
    p.add(initialNodes = new Choice());
    initialNodes.add("10");
    initialNodes.add("100");
    initialNodes.add("1000");
    initialNodes.add("10000");
    initialNodes.add("100000");
    initialNodes.select(3);
    p.add(cbVerbose = new Checkbox("verbose", false));

    add(code = new TextArea(25, 80), BorderLayout.CENTER);
    add(msg = new TextArea(16, 80), BorderLayout.SOUTH);
    msg.setEditable(false);
    msg.setText("\n       This is C17, from Yirng-An Chen's ISCAS'85 traces.\n\n");
    msg.setFont(new Font(null, 0, 10));
    JDDConsole.out = new TextAreaTarget(msg);

    code.setFont(new Font("Monospaced", 0, 16));
    code.setBackground(Color.yellow);
    code.setForeground(Color.red);
    code.setText(initial_text);

    addWindowListener(this);
    pack();
  }

  // ----------------------------------
  public void windowActivated(WindowEvent e) {}

  public void windowClosed(WindowEvent e) {}

  public void windowClosing(WindowEvent e) {
    setVisible(false);
    dispose();
  }

  public void windowDeactivated(WindowEvent e) {}

  public void windowDeiconified(WindowEvent e) {}

  public void windowIconified(WindowEvent e) {}

  public void windowOpened(WindowEvent e) {}

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src == bRun) doRun();
    else if (src == bClear) doClear();
    else if (src == bLoad) doLoad();
  }

  private void doClear() {
    msg.setText("");
  }

  private void doRun() {
    BDDTrace.verbose = Options.verbose = cbVerbose.getState();
    StringBufferInputStream sbis = new StringBufferInputStream(code.getText());
    int nodes = Integer.parseInt(initialNodes.getSelectedItem());
    try {
      BDDTrace bt = new BDDTrace("(memory)", sbis, nodes);
    } catch (IOException exx) {
      JDDConsole.out.println("ERROR: " + exx);
    }
  }

  private void doLoad() {
    FileDialog fd = new FileDialog(this, "Load trace file", FileDialog.LOAD);
    fd.setVisible(true);

    try {
      for (File f : fd.getFiles()) {

        // load all file conetnts: File -> String
        InputStream is = new FileInputStream(f);
        StringBuilder sb = new StringBuilder();
        for (int i = is.read(); i != -1; i = is.read()) sb.append((char) i);
        is.close();
        code.setText(sb.toString());
        return;
      }
    } catch (Exception ex) {
      JDDConsole.out.println("ERROR: " + ex);
    }
  }

  public static void main(String[] args) {
    TraceApp app = new TraceApp();
    app.setVisible(true);
  }
}

package jdd.examples;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import jdd.util.Array;
import jdd.util.JDDConsole;
import jdd.util.Options;
import jdd.util.TextAreaTarget;

/** QueensApp is a simple AWT app for testing N-queens */
public class QueensApp extends Frame implements WindowListener, ActionListener {
  private TextArea msg;
  private Button bSolve, bClear;
  private Choice cSize, cSolver;
  private Checkbox cbVerbose;
  private ChessBoard board;

  public QueensApp() {
    setLayout(new BorderLayout());

    Panel p = new Panel(new FlowLayout(FlowLayout.LEFT));
    add(p, BorderLayout.NORTH);
    p.add(bSolve = new Button("Solve!"));
    p.add(bClear = new Button("Clear"));

    p.add(new Label("        N = "));
    p.add(cSize = new Choice());
    for (int i = 4; i < 14; i++) cSize.add("" + i);
    cSize.select(5);

    p.add(new Label("        Solver: "));
    p.add(cSolver = new Choice());
    cSolver.add("BDD");
    cSolver.add("ZDD");
    cSolver.add("ZDD-CSP");

    p.add(cbVerbose = new Checkbox("Verbose"));

    add(msg = new TextArea(10, 80), BorderLayout.SOUTH);
    msg.setEditable(false);

    bSolve.addActionListener(this);
    bClear.addActionListener(this);

    add(board = new ChessBoard(), BorderLayout.CENTER);

    JDDConsole.out = new TextAreaTarget(msg);
    addWindowListener(this);
    pack();
  }

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src == bSolve) doSolve();
    else if (src == bClear) doClear();
  }

  // ----------------------------------
  private void doClear() {
    msg.setText("");
  }

  private Queens getSolver(int n) {
    JDDConsole.out.println("Loading solver '" + cSolver.getSelectedItem() + "'...");

    int type = cSolver.getSelectedIndex();
    switch (type) {
      case 0:
        return new BDDQueens(n);
      case 1:
        return new ZDDQueens(n);
      case 2:
        return new ZDDCSPQueens(n);
    }
    return null; // ERROR
  }

  private void doSolve() {
    try {
      final int n = Integer.parseInt(cSize.getSelectedItem());
      Options.verbose = cbVerbose.getState();
      Queens q = getSolver(n);
      boolean[] sol = q.getOneSolution();
      board.set(sol);
      JDDConsole.out.println("" + q.numberOfSolutions() + " solutions /" + q.getTime() + "ms");
    } catch (Exception ex) {
      JDDConsole.out.println("ERROR: " + ex);
      ex.printStackTrace();
    }
  }

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

  public static void main(String[] args) {
    QueensApp app = new QueensApp();
    app.setVisible(true);
  }
}

/* Chess board graphics */
class ChessBoard extends Canvas {
  private int n;
  private boolean[] board;

  public ChessBoard() {
    this.n = 8;
    this.board = new boolean[n * n];
    final int d = Math.min(400, n * 50);
    setSize(new Dimension(d, d));
  }

  void set(boolean[] board) {
    this.board = Array.clone(board);
    this.n = (int) Math.sqrt(board.length);
    repaint();
  }

  public void paint(Graphics g) {
    Dimension dims = getSize();
    int h = 1 + dims.height;
    int w = 1 + dims.width;
    int d = Math.max(20, Math.min(h / n, w / n));

    int x0 = w - d * n;
    int y0 = h - d * n;
    g.translate(x0 / 2, y0 / 2);
    g.setColor(Color.black);
    g.drawRect(0, 0, n * d, n * d);
    for (int x = 0; x < n; x++) {
      for (int y = 0; y < n; y++) {
        if ((x + y) % 2 == 0) g.fillRect(x * d, y * d, d, d);
      }
    }

    g.setColor(Color.red);
    int m = d / 8;
    int e = d - 2 * m;

    for (int x = 0; x < n; x++) {
      for (int y = 0; y < n; y++) {
        if (board[x + n * y]) g.fillOval(m + x * d, m + y * d, e, e);
      }
    }
  }
}

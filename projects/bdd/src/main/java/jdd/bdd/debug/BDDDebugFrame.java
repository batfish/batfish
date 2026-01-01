// XXX: should set the font, since the text sizes are hard-coded to height=12

package jdd.bdd.debug;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import jdd.bdd.BDD;
import jdd.bdd.CacheBase;
import jdd.bdd.NodeTable;
import jdd.util.JDDConsole;
import jdd.util.PrintTarget;
import jdd.util.TextAreaTarget;

/**
 * This class will report BDD statistics using a minimalistic GUI. Currently only cache stats are
 * reported.
 *
 * <p>Note: you should not use this class by itself. use ProfiledBDD or ProfiledBDD2 with the
 * Options.verbose flag set to true.
 *
 * @see BDD
 * @see ProfiledBDD
 * @see ProfiledBDD2
 * @see jdd.util.Options
 */
public class BDDDebugFrame extends Frame implements WindowListener, Runnable, BDDDebuger {

  private static final int SLEEP_TIME = 1000;

  private NodeTable nodetable;
  private Thread thread;
  private boolean stop;
  private LinkedList list;
  private Label status;
  private TextArea statistics;

  public BDDDebugFrame(NodeTable nodetable) {
    super("[BDD Profiler]");

    this.nodetable = nodetable;

    Collection caches = nodetable.addDebugger(this);
    list = new LinkedList();

    Panel p = new Panel(new GridLayout(3, Math.max(1, caches.size() / 3), 5, 5));

    for (Iterator e = caches.iterator(); e.hasNext(); ) {
      CacheBase cb = (CacheBase) e.next();
      CacheFrame cf = new CacheFrame(cb);
      p.add(cf);
      list.add(cf);
    }

    add(p, BorderLayout.CENTER);
    add(status = new Label(""), BorderLayout.SOUTH);
    add(statistics = new TextArea(10, 80), BorderLayout.NORTH);

    statistics.setEditable(false);
    statistics.setVisible(false);

    addWindowListener(this);
    pack();
    pack();
    setVisible(true);

    thread = new Thread(this);
    thread.start();
  }

  public void run() {
    long update = 0;
    while (!stop) {
      try {
        Thread.sleep(SLEEP_TIME);
        status.setText("Update " + ++update);

        for (Iterator it = list.iterator(); it.hasNext(); ) {
          CacheFrame cf = (CacheFrame) it.next();
          cf.repaint();
        }
      } catch (Exception ignored) {
        // also catches NULL pointer expcetion during GC and stuff..
      }
    }

    status.setText("stopped");
  }

  public void stop() {
    if (stop) return; // already stopped
    stop = true;

    // now, show the stats area
    statistics.setVisible(true);
    pack();
    pack();

    // redirect stats to our window
    TextAreaTarget taa = new TextAreaTarget(statistics);
    PrintTarget save = JDDConsole.out;
    JDDConsole.out = taa;
    JDDConsole.out.printf("\nPackage statistics:\n==================\n");
    nodetable.showStats();
    JDDConsole.out = save;
  }

  // ---------------------------------------------

  public void windowActivated(WindowEvent e) {}

  public void windowClosed(WindowEvent e) {}

  public void windowDeactivated(WindowEvent e) {}

  public void windowDeiconified(WindowEvent e) {}

  public void windowIconified(WindowEvent e) {}

  public void windowOpened(WindowEvent e) {}

  public void windowClosing(WindowEvent e) {
    stop = true;
    setVisible(false);
    dispose();
  }

  // ---------------------------------------------------------
  private class CacheFrame extends Canvas {
    private CacheBase cb;
    private MiniGraph g1, g2;

    public CacheFrame(CacheBase cb) {
      this.cb = cb;
      this.g1 = new MiniGraph(95, 0, 100);
      this.g2 = new MiniGraph(95, 0, 100);
    }

    public Dimension getPreferredSize() {
      return new Dimension(200, 90);
    }

    public void paint(Graphics g) {

      int h = getHeight();
      int w = getWidth();
      g.drawRect(1, 1, w - 2, h - 2);

      long accss = cb.getAccessCount();
      if (accss == 0) {
        g.drawString(cb.getName() + " unused.", 20, 30);
      } else {
        g.drawString(cb.getName() + ", SIZE=" + cb.getCacheSize(), 5, 12);

        g.drawString("Load factor and hitrate:", 5, 24);
        g1.add(cb.computeLoadFactor());
        g1.draw(g, 3, 28);

        g2.add(cb.computeHitRate());
        g2.draw(g, 103, 28);

        g.drawString(
            "Acss="
                + accss
                + ", CLRS="
                + cb.getNumberOfClears()
                + "/"
                + cb.getNumberOfPartialClears(),
            5,
            85);
      }
    }
  }

  // ---------------------------------------------------------
  private class MiniGraph {
    private static final int GRAPH_HEIGH = 40;
    private int[] memory;
    private int current, size, last;
    private double min, max;

    public MiniGraph(int size, double min, double max) {
      this.size = size;
      this.current = 0;

      if (min == max) max++; // dont like div by zero

      this.memory = new int[size];
      this.min = min;
      this.max = max;

      for (int i = 0; i < size; i++) memory[i] = -1; // fill with zeros
    }

    public void add(double v) {
      // in precent, rounded
      last = (int) (0.5 + (v - min) * 100.0 / (max - min));

      // in graph height
      v = (v - min) * GRAPH_HEIGH / (max - min);
      current = (current + 1) % size;
      memory[current] = GRAPH_HEIGH - (int) v;
    }

    public void draw(Graphics g, int x0, int y0) {
      g.setColor(Color.lightGray);
      g.fillRect(x0, y0, size, GRAPH_HEIGH);
      g.setColor(Color.blue);

      int n = current;
      x0 += size - 1;
      for (int i = 0; i < size; i++) {
        int p = memory[n];
        if (p >= 0 & p <= GRAPH_HEIGH) {
          g.drawLine(x0, y0 + p, x0, y0 + p + 1);
        }
        x0--;
        n--;
        if (n == -1) n = size - 1;
      }
      g.setColor(Color.black);

      // show the last value
      g.drawString("" + last, x0 + 5, y0 + 25);
    }
  }
}

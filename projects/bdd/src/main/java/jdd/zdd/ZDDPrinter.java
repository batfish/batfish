
package jdd.zdd;

import jdd.bdd.NodeTable;
import jdd.util.Allocator;
import jdd.util.Dot;
import jdd.util.JDDConsole;
import jdd.util.NodeName;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Helper class for printing Z-BDD graphs, as text/sets/DOT files.
 *
 */

public class ZDDPrinter {

	private static NodeTable nt;
	private static PrintStream ps;
	private static final int NODE_MASK = 0x7FFFFFFF, DOT_MARK = 0x80000000; // node-marking stuff
	private static boolean had_0, had_1;
	private static NodeName nn;

	private static void helpGC() { // make thins easier for the garbage collector
		nt = null;
		ps = null;
		nn = null;
	}


	// ----------------------------------------------------------


	/**
	 * Print the Z-BDD as a node-table.
	 * You should call ZDD.print() instead of calling this function directly
	 */
	/* package */static void print(int dd, NodeTable nt, NodeName nn) {
		// if(dd == 0) JDDConsole.out.println("0. empty");
		// else if(dd == 1) JDDConsole.out.println("1. {0}");
		if(dd == 0) JDDConsole.out.printf("0. %s", nn.zero());
		else if(dd == 1) JDDConsole.out.printf("1. %s", nn.one());
		else {
			ZDDPrinter.nt = nt;
			ZDDPrinter.nn = nn;
			print_rec(dd);
			nt.unmark_tree(dd);
			helpGC();
			JDDConsole.out.printf("\n");
		}
	}
	private static void print_rec(int dd) {
		if(dd == 0 || dd == 1) return;
		if(nt.isNodeMarked(dd)) return;
		JDDConsole.out.println("" + dd + ". " + nn.variable(nt.getVar(dd) ) + ": " +  nt.getLow(dd) + ", " + nt.getHigh(dd));
		nt.mark_node(dd);
		print_rec(nt.getLow(dd));
		if(nt.getLow(dd) != nt.getHigh(dd)) print_rec(nt.getHigh(dd) );
	}


	// ------------------------------------------------------------------------------------
	/**
	 * create a DOT file visualizing  a Z-BDD tree.
	 * You should call ZDD.printDot() instead
	 */
	/* package */ static void printDot(String filename, int zdd, NodeTable nt, NodeName nn)  {
		try {
			ps = new PrintStream( new FileOutputStream(filename));

			ps.println("digraph G {");
			// ps.println("\tsize = \"3, 4\";");
			ps.println("\tcenter = true;");
			ps.println("\tnodesep = 0.05;");
			// ps.println("\nratio = fill;");

			ZDDPrinter.had_0 = ZDDPrinter.had_1 = false;
			ZDDPrinter.nt = nt;
			ZDDPrinter.nn = nn;

			ps.println("\tinit__ [label=\"\", style=invis, height=0, width=0];");
			ps.println("\tinit__ -> "  + zdd + ";");

			printDot_rec(zdd);
			if(had_0 && had_1)	ps.println("\t{ rank = same; 0; 1;}");
			if(had_0)	ps.println("\t0 [shape=box, label=\"" + nn.zeroShort() + "\", style=filled, height=0.3, width=0.3];");
			if(had_1)	ps.println("\t1 [shape=box, label=\"" + nn.oneShort() + "\", style=filled, height=0.3, width=0.3];\n");
			ps.println("}\n");
			nt.unmark_tree(zdd);
			ps.close();
			Dot.showDot(filename);
			helpGC();
		} catch(IOException exx) {
			JDDConsole.out.println("ZDDPrinter.printDOT failed: " + exx);
		}
	}
	private static void printDot_rec(int zdd) {
		if(zdd == 0) { had_0 = true; return; }
		if(zdd == 1) { had_1 = true; return; }

		if( nt.isNodeMarked(zdd)) return;

		int low = nt.getLow(zdd);
		int high= nt.getHigh(zdd);
		int var = nt.getVar(zdd);

		nt.mark_node(zdd);

		ps.println("\t" + zdd + "[label=\"" + nn.variable(var) + "\"];");
		ps.println("\t" + zdd + "-> " + low + " [style=dotted];");
		ps.println("\t" + zdd + "-> " + high + " [style=filled];");
		printDot_rec(low);
		if(low != high) printDot_rec(high); // extra check for ZDD:s
	}


	// -----------------------------------------------------------------
	private static char [] set_chars = null; // internal to printSet
	private static int max, count; // internal to printSet


	/* package */ static void printSet(int zdd, NodeTable nt, NodeName nn)  {

		if(zdd < 2) {
			if(nn != null)  JDDConsole.out.println( (zdd == 0) ? nn.zero() : nn.one() );
			else JDDConsole.out.println( (zdd == 0) ? "empty" : "base");
		} else {
			int max_ = 2 + nt.getVar(zdd);

			if(ZDDPrinter.set_chars == null || ZDDPrinter.set_chars.length < max_)
				ZDDPrinter.set_chars = Allocator.allocateCharArray(max_);
			ZDDPrinter.count = 0;
			ZDDPrinter.nn = nn;
			ZDDPrinter.nt = nt;
			JDDConsole.out.print("{ ");
			printSet_rec(zdd, 0, nt.getVar(zdd) );
			JDDConsole.out.println(" }");
			helpGC();
		}
	}

	private static void printSet_rec(int zdd, int level, int top) {
		if(zdd == 0) return;
		if(zdd == 1 && top < 0) {
			if(count != 0) JDDConsole.out.print(", ");
			count++;
			if(nn != null) {	// print as set-covers
				int got = 0;
				for(int i = 0; i < level; i++)
					if(set_chars[i] == '1') { JDDConsole.out.print(nn.variable(level-i-1)); got++; }
				if(got == 0) JDDConsole.out.print(nn.one() );
			} else {	// print as minterms
				for(int i = 0; i < level; i++)
					JDDConsole.out.print(set_chars[i]);
			}
			return;
		}
		top--;
		if(nt.getVar(zdd) <= top) {
			set_chars[level] = '0';
			printSet_rec(zdd, level+1, top);
			return;
		}


		set_chars[level] = '0';
		printSet_rec(nt.getLow(zdd), level+1, top);

		set_chars[level] = '1';
		printSet_rec(nt.getHigh(zdd), level+1, top);

	}
}

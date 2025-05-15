
package jdd.bdd.debug;

import jdd.bdd.BDD;
import jdd.bdd.BDDIO;
import jdd.bdd.BDDNames;
import jdd.bdd.Permutation;
import jdd.util.JDDConsole;
import jdd.util.Options;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.LinkedList;

/**
 * This is a simple BDD trace driver.
 * <p> It reads trace files a subset of Bwolen Yang's "BDD Trace Driver" file format.
 * It is used to verify the integrity and efficiency of a BDD package against another without
 * creating a whole new application for it...
 *
 * <p>Note: we have added the following commands:<br>
 * <b>print_bdd(variable)</b> print the BDD strcutre in the java console.
 * <b>show_bdd(variable)</b> save an image for this BDD in current directory (default is variable.png).
 * <b>save_bdd(variable)</b> save this BDD in the current directory.
 * <p> If you are using an applet, only <tt>print_bdd</tt> will work.
 *<br>
 */

public class BDDTrace {
	class TracedNames extends BDDNames {
		public String variable(int n) {
			if(n < 0) return "(none)";
			TracedVariable t = (TracedVariable)variables.elementAt(n);
			return t.name;
		}
	}

	// --------------------------------------------------------------
	class TracedVariable {
		public String name;
		public int index, last_use, bdd;
		public boolean is_var = false;
		public void show() {
			JDDConsole.out.print(name);
		}
		public void show(BDD bdd) {
			JDDConsole.out.print("\n\t");
			show();
			JDDConsole.out.printf("\n");
			bdd.printSet(this.bdd);
		}
	}

	// --------------------------------------------------------------
	abstract class TracedOperation {
		public int index, size = -1;
		public String op;
		public void show() { }
		public abstract void execute() throws IOException;
		public void show_code() { }
	}

	// --------------------------------------------------------------
	class TracedDebugOperation extends TracedOperation {
		public String text;
		public void execute() {  if(verbose) JDDConsole.out.println(text); }
		public void show_code() { JDDConsole.out.println( "//" + text); }
	}

	class TracedSaveOperation extends TracedOperation {
			public TracedVariable v;
			public void execute() {
				try {
					BDDIO.saveBuDDy(bdd, v.bdd, v.name + ".buddy");
					BDDIO.save(bdd, v.bdd, v.name + ".bdd");
				} catch(IOException exx) {
					// ignore
				}
			}
			public void show_code() {
				JDDConsole.out.println("BDDIO.saveBuDDy(bdd, " + v.bdd + ",\"" + v.name + ".buddy\");");
			}
		}

	class TracedPrintOperation extends TracedOperation {
		public TracedVariable v;
		public boolean graph;
		public void execute() {
			if(graph)  bdd.printDot(v.name, v.bdd);
			else { JDDConsole.out.println(v.name + ":"); bdd.printSet(v.bdd); }
		}
		public void show_code() {
			if(graph)  JDDConsole.out.println(v.name + ".printDot();");
			else  JDDConsole.out.println(v.name + ".printSet();");
		}
	}

	class TracedCheckOperation extends TracedOperation {
		public TracedVariable t1,t2;
		public void execute() throws IOException {
			boolean equal = (t1.bdd == t2.bdd);
			if(size != -1) {
				boolean expected =  (size ==  0 ? false : true);
				if(equal != expected)
					throw new IOException ("are_equal(" + t1.name + ", " + t2.name + ") failed. expected " + expected + ", got " + equal);
			}
		}
	}

	// --------------------------------------------------------------
	class TracedBDDOperation extends TracedOperation {
		public int ops;
		public TracedVariable ret, op1, op2, op3;
		public Vector operands;

		public void show() {
			JDDConsole.out.print(index + "\t");
			ret.show();
			JDDConsole.out.print(" = ");

			if(op.equals("=") ) {
				op1.show();
				JDDConsole.out.print(";");
			} else {
				JDDConsole.out.print(op + "(" );
				boolean first = true;
				for (Enumeration e = operands.elements(); e.hasMoreElements() ;) {
					TracedVariable v = (TracedVariable)e.nextElement();
					if(first) first = false; else JDDConsole.out.print(", ");
					v.show();
				}
				JDDConsole.out.print(");");
			}

			if(size != -1) JDDConsole.out.print("\t% " + size);
			JDDConsole.out.printf("\n");
		}

		public void execute() throws IOException {
			// check_all_variables(); // DEBUG

			bdd.deref( ret.bdd);

			if(op.equals("not")) do_not();
			else if(op.equals("=")) do_assign();
			else if(op.equals("and")) do_and();
			else if(op.equals("or")) do_or();
			else if(op.equals("xor")) do_xor();
			else if(op.equals("xnor")) do_xnor();
			else if(op.equals("nor")) do_nor();
			else if(op.equals("nand")) do_nand();
			else if(op.equals("ite")) do_ite();
			else if(op.equals("vars_curr_to_next")) do_s2sp();
			else if(op.equals("vars_next_to_curr")) do_sp2s();
			else if(op.equals("support_vars")) do_support();
			else if(op.equals("exists")) do_exists();
			else if(op.equals("forall")) do_forall();
			else if(op.equals("restrict")) do_restrict();
			else if(op.equals("rel_prod")) do_relprod();
			else {
				throw new IOException("Unknown operation '" + op + "', #" + op_count );
			}

			bdd.ref( ret.bdd);
			last_assignment = ret;
			checkVar(this);

			if(size != -1) {
				int size2 = node_count(ret);
				if(size != size2) {
					JDDConsole.out.println("\n*************************************************************************");
					JDDConsole.out.println("Size comparison failed after " + op + " ( wanted " + size + ", got " + size2 + ")");
					show();
					JDDConsole.out.println("\n");
					throw new IOException("Size comparison failed");

				}
			}
		}

		// -------------------------------------------------------------------
		private void do_not() throws IOException {
			checkEquality(ops, 1, "do_not");
			ret.bdd = bdd.not( op1.bdd);
		}

		private void do_assign() throws IOException {
			checkEquality(ops, 1, "do_assign");
			ret.bdd = op1.bdd;
		}

		private void do_or() {
			if(ops == 2) ret.bdd = bdd.or(op1.bdd, op2.bdd);
			else {
				for (Enumeration e = operands.elements() ; e.hasMoreElements() ;)
					if(((TracedVariable)e.nextElement()).bdd == 1) { ret.bdd = 1; return; }

				int tmp = 0;
				for (Enumeration e = operands.elements() ; e.hasMoreElements() ;) {
					TracedVariable v = (TracedVariable)e.nextElement();
					int tmp2 = bdd.ref( bdd.or(tmp, v.bdd) );
					bdd.deref(tmp); tmp = tmp2;
				}
				ret.bdd = bdd.deref(tmp);
			}

		}

		private void do_and() {
			if(ops == 2) ret.bdd = bdd.and(op1.bdd, op2.bdd);
			else {
				for (Enumeration e = operands.elements() ; e.hasMoreElements() ;)
					if(((TracedVariable)e.nextElement()).bdd == 0) { ret.bdd = 0; return; }

				int tmp = 1;
				for (Enumeration e = operands.elements() ; e.hasMoreElements() ;) {
					TracedVariable v = (TracedVariable)e.nextElement();
					int tmp2 = bdd.ref( bdd.and(tmp, v.bdd) );
					bdd.deref(tmp); tmp = tmp2;
				}
				ret.bdd = bdd.deref(tmp);
			}
		}

		private void do_nand() {
			if(ops == 2) ret.bdd = bdd.nand(op1.bdd, op2.bdd);
			else {
				do_and();
				int tmp = bdd.ref( ret.bdd) ;
				ret.bdd = bdd.not(tmp);
				bdd.deref( tmp);
			}
		}

		private void do_nor() {
			if(ops == 2) ret.bdd = bdd.nor(op1.bdd, op2.bdd);
			else {
				do_or();
				int tmp = bdd.ref( ret.bdd) ;
				ret.bdd = bdd.not(tmp);
				bdd.deref( tmp);
			}

		}

		private void do_xor() throws IOException { check(ops == 2); ret.bdd = bdd.xor(op1.bdd, op2.bdd); }
		private void do_xnor() throws IOException { check(ops == 2); ret.bdd = bdd.biimp(op1.bdd, op2.bdd); }

		private void do_ite() throws IOException { check(ops == 3); ret.bdd = bdd.ite(op1.bdd, op2.bdd, op3.bdd); }
		private void do_s2sp() throws IOException { check(ops == 1); ret.bdd = bdd.replace(op1.bdd, s2sp); }
		private void do_sp2s() throws IOException { check(ops == 1); ret.bdd = bdd.replace(op1.bdd, sp2s); }

		private void do_support() throws IOException { check(ops == 1); ret.bdd = bdd.support(op1.bdd); }
		private void do_exists() throws IOException { check(ops == 2); ret.bdd = bdd.exists(op2.bdd, op1.bdd); }

		private void do_forall() throws IOException { check(ops == 2); ret.bdd = bdd.forall(op2.bdd, op1.bdd); }
		private void do_restrict() throws IOException { check(ops == 2); ret.bdd =  bdd.restrict(op1.bdd, op2.bdd); }
		private void do_relprod() throws IOException { check(ops == 3); ret.bdd = bdd.relProd(op2.bdd, op3.bdd, op1.bdd); }

		// -------------------------------------------------------------------------

		public void show_code() {
			String code;
			Enumeration e = operands.elements();
			TracedVariable v = (TracedVariable)e.nextElement();
			if(op.equals("=")) JDDConsole.out.println("BDD " + ret.name + " = " + v.name + ";");
			else {
				JDDConsole.out.print("BDD " + ret.name + " = " +v.name + "." +op);
				JDDConsole.out.print("(");
				boolean mode2 = op.equals("ite");
				int i = 0;
				for (i = 0; e.hasMoreElements() ;i++) {
					v = (TracedVariable)e.nextElement();
					if(mode2 && i != 0) JDDConsole.out.print(", ");
					JDDConsole.out.print(v.name);

					if(e.hasMoreElements() && !mode2) JDDConsole.out.print( "." + op + "(");

				}
				if(!mode2) for(int j = 1; j < i; j++) JDDConsole.out.print(")");

				JDDConsole.out.println(");");
			}

			if(op.equals("ite") )
				JDDConsole.out.println("System.out.println(\"" + ret.name + " ==> \"+" +  ret.name + ".nodeCount());" );
		}

	}

	// -----------------------------------------------
	private static final int DEFAULT_NODES = 10000, MAX_NODES = 3000000;
	private BDD bdd;
	private InputStream is;
	private StringBuffer sb;
	private String filename, module;
	private int [] stack;
	private int stack_tos, nodes, cache, vars;
	private HashMap map;
	private Permutation s2sp, sp2s;
	private TracedVariable last_assignment;
	private Vector operations, variables;
	private int op_count, line_count, var_count;
	private long time;

	/** this is our extra VERBOSE flags, to enable trace_verbose_print() output*/
	public static boolean verbose = false;

	public BDDTrace(String file) throws IOException {
		this(file, new FileInputStream(file),  DEFAULT_NODES);
	}

	public BDDTrace(String file, int nodes) throws IOException {
		this(file, new FileInputStream(file), nodes);
	}

	public BDDTrace(String file, InputStream is) throws IOException {
		this(file, is, DEFAULT_NODES);
	}

	public BDDTrace(String file, InputStream is, int nodes) throws IOException {
		this.filename = file;
		this.nodes = nodes;
		this.is = is;
		this.sb = new StringBuffer();
		this.stack = new int[64];
		this.stack_tos = 0;
		this.cache = Math.max( Math.min(nodes / 10, 5000), 50000);
		this.map = new HashMap(1024);

		this.operations = new Vector();
		this.variables  = new Vector();
		this.op_count = 0;
		this.line_count = 1;
		this.var_count = 0;

		// Options.verbose = true; // DEBUG

		TracedVariable vret = new TracedVariable();
		vret.last_use = 0;
		vret.bdd = 0;
		map.put("0", vret);

		vret = new TracedVariable();
		vret.last_use = 0;
		vret.bdd = 1;;
		map.put("1", vret);

		last_assignment = null;

		parse();
		// show_code();

		boolean save = Options.verbose ; Options.verbose  = false;	// to avoid GC messaages
		execute();
		Options.verbose  = save;	// restore it back

		show_results();
		bdd.cleanup();

	}

	// -----------------------------------------------------
	private void show_code() {
		JDDConsole.out.println("import org.sf.javabdd.*;\n"+
			"public class Test {\n"+
    		"public static void main(String[] args) {\n");

		JDDConsole.out.println("\n\n" +
 			"BDDFactory B = BDDFactory.init("+nodes+",100);\n" +
        	"B.setVarNum(" + variables.size() + ");\nBDD ");

		int i = 0;
		for (Enumeration e = variables.elements() ; e.hasMoreElements() ;) {
			TracedVariable v = (TracedVariable)e.nextElement();
			if(v.is_var) {
				if(i != 0) JDDConsole.out.print(",");
				JDDConsole.out.print(v.name + "=B.ithVar(" + i+ ") ");
				i++;
			}
		}
		JDDConsole.out.println(";");

		for (Enumeration e = operations.elements() ; e.hasMoreElements() ;) {
			TracedOperation v = (TracedOperation)e.nextElement();
			v.show_code();
		}

		JDDConsole.out.println("}\n}\n");
	}
	// -----------------------------------------------------
	private void setup_bdd(int vars) {
		this.vars = vars;
		nodes = (int)Math.min( MAX_NODES, nodes * (1 + Math.log(1+vars)) );

		JDDConsole.out.printf("\n");
		JDDConsole.out.println("loading " + module + " from " + filename + " (" + nodes + " nodes, " + vars + " vars)");

		// bdd = new ProfiledBDD(nodes, cache);
		bdd = new ProfiledBDD2(nodes, cache);
		// bdd = new DebugBDD(nodes, cache);
		bdd.setNodeNames(new TracedNames() );
	}

	// -----------------------------------------------------
	private void alloc_var(String name) {
		TracedVariable vret = new TracedVariable();
		vret.last_use = 0;
		vret.bdd = bdd.createVar();
		vret.name = name;
		vret.is_var = true;
		map.put(name, vret);
		variables.add(vret);
		var_count++;
	}

	private void checkVar(TracedBDDOperation tp) {
		checkVar(tp.ret);
		for (Enumeration e = tp.operands.elements() ; e.hasMoreElements() ;) {
			TracedVariable v = (TracedVariable)e.nextElement();
			checkVar(v);
		}
	}

	private void checkVar(TracedVariable v) {
		if(v != null && v.last_use == op_count) {
			// JDDConsole.out.println("Removing " + v.name + " at state " + op_count + ", bdd = " + v.bdd);
			bdd.deref( v.bdd );
			v.last_use = -1; // we dont want to remove this again, in case A = not(A) or B = and(A,A)
			// v.bdd = 0;
		}
	}

	private TracedPrintOperation createPrintOperation(boolean graph, TracedVariable v) {
		TracedPrintOperation tp = new TracedPrintOperation();
		tp.index = op_count;
		tp.graph = graph;
		tp.v = v;
		operations.add( tp );
		return tp;
	}

	private TracedSaveOperation createSaveOperation(TracedVariable v) {
			TracedSaveOperation ts = new TracedSaveOperation();
			ts.index = op_count;
			ts.v = v;
			operations.add( ts );
			return ts;
	}
	private TracedCheckOperation createCheckOperation(TracedVariable v1, TracedVariable v2) {
		TracedCheckOperation tp = new TracedCheckOperation();
		tp.index = op_count;
		tp.t1 = v1;
		tp.t2 = v2;
		operations.add( tp );
		return tp;
	}
	private TracedDebugOperation createDebugOperation(String text) {
		TracedDebugOperation tp = new TracedDebugOperation();
		tp.index = op_count;
		tp.text = text;
		operations.add( tp );
		return tp;
	}
	private TracedBDDOperation createBDDOperation() {
		TracedBDDOperation tp = new TracedBDDOperation();
		tp.index = op_count;
		operations.add( tp );
		tp.operands = new Vector(3);
		return tp;
	}

	// -----------------------------------------------------

	private void show_results() {
		time = System.currentTimeMillis() - time;
		JDDConsole.out.println("" + op_count + " operations performed, total execution time: " + time + " [ms]");

		if(Options.verbose) {
			if(last_assignment != null) {
				int size =  node_count(last_assignment);
				JDDConsole.out.println("Last assginment: " + last_assignment.name + ", " + size + " nodes.");
				// if(size < 20) bdd.printSet(last_assignment.bdd);
				JDDConsole.out.println("\n");
			}
			bdd.showStats();
		}

		// just something to see on the console
		System.err.println("Trace\tFile=" + filename + "\ttime="+ time);
	}

	/** check if the variables to be used are OK */
	private void check_all_variables() {
		for (Enumeration e = variables.elements() ; e.hasMoreElements() ;) {
			TracedVariable v = (TracedVariable)e.nextElement();
			if(v.last_use >= op_count) {
				// v.show();JDDConsole.out.printf("\n");
				// bdd.check_node(v.bdd, v.name); // DEBUG
			}
		}
	}

	// -------------------------------------------------------------------------
	private void execute() throws IOException {
		time = System.currentTimeMillis();
		for (Enumeration e = operations.elements() ; e.hasMoreElements() ;) {
			TracedOperation tp = (TracedOperation)e.nextElement();
			op_count = tp.index;

			if(BDDTrace.verbose) tp.show();	// DEBUG !!
			tp.execute();
		}
	}

	/** BDD trace driver doesn't count nodes the same way as we do ... */
	private int node_count(TracedVariable v) {
		int size = bdd.nodeCount(v.bdd);
		size += (v.bdd < 2 ? 1 : 2); // adjust JDD size to include terminals
		return size;
	}

	private void parse() throws IOException {
		read_module();
		read_input();
		skip_output();
		read_structure();
	}
	private void read_module() throws IOException {
		need("MODULE");
		module  = need();
	}

	private void skip_output() throws IOException {
		need("OUTPUT");
		for(String tmp = need(); !tmp.equals(";"); tmp = need()) ;
	}

	private void read_structure() throws IOException {
		need("STRUCTURE");
		while(true) {
			String ret = need();
			if(ret.equals("ENDMODULE")) return;

			op_count++;

			if(ret.equals("trace_verbose_print")) {
				need("("); String str = getString(); need(")"); need(";");
				createDebugOperation(str);
			} else if(ret.equals("are_equal")) {
				need("("); String str = need();  TracedVariable t1 = needVar(str);
				need(","); str = need();  TracedVariable t2 = needVar(str); need(")"); need(";");
				createCheckOperation(t1,t2);
			} else if(ret.equals("print_bdd") || ret.equals("show_bdd") ) {
				need("("); String str = need();   TracedVariable v = needVar(str);need(")"); need(";");
				createPrintOperation(ret.equals("show_bdd"), v);
			} else if(ret.equals("save_bdd")) {
				need("("); String str = need();   TracedVariable v = needVar(str);need(")"); need(";");
				createSaveOperation(v);
			} else if(ret.equals("check_point_for_force_reordering")) {
				JDDConsole.out.println("NOTE: ignoring variable-reordering request");
				skip_eol();
			} else {

				TracedVariable vret = (TracedVariable) map.get(ret);
				if(vret == null) // just used a new variable
					vret = addTemporaryVariable(ret);

				need("=");
				String op = need();

				updateUsage(vret);

				TracedBDDOperation tp = createBDDOperation();
				TracedVariable var = (TracedVariable) map.get(op);

				if(var != null) {	// asignment!
					need(";"); tp.operands.add(var);
					tp.ret = vret;
					tp.op  = "=";
					updateUsage(var);
				} else {
					tp.op  = op;
					tp.ret = vret;
					if(op.equals("new_int_leaf")) {
						need("("); String c = need(); need(")"); 	need(";");
						tp.operands.add( map.get(c) ); // assuming 0 or 1
						tp.ret = vret;
						tp.op  = "=";
					} else {
						String s1,s2;
						need("(");

						do {
							s1 = need();
							tp.operands.add( needVar(s1) );
							s1 = need();
						} while(s1.equals(",") );
						need(";");
					}
				}

				tp.ops = tp.operands.size();
				if(tp.ops > 0) tp.op1 = (TracedVariable) tp.operands.elementAt(0);
				if(tp.ops > 1) tp.op2 = (TracedVariable) tp.operands.elementAt(1);
				if(tp.ops > 2) tp.op3 = (TracedVariable) tp.operands.elementAt(2);
			}
		}
	}

	// --------------------------------------------------------------------------------------------

	private void read_input() throws IOException {
		boolean interleave = false;
		LinkedList list = new LinkedList();

		need("INPUT");

		for(int i = 0; ; i++) {
			String name = need();
			if(i == 0  && ( name.equals("CURR_NEXT_ASSOCIATE_EVEN_ODD_INPUT_VARS") || name.equals("STATE_VAR_ASSOCIATE_CURR_NEXT_INTERLEAVE"))) {
				if(name.equals("STATE_VAR_ASSOCIATE_CURR_NEXT_INTERLEAVE")) interleave = true;
			} else {
				// alloc_var(name);
				list.add(name);

				name = need();
				if(name.equals(";")) break;
				else if(!name.equals(",")) {
					throw new IOException("expected ',' when reading inputs, but got: " + name+ " at line " + line_count);
				}
			}
		}

		int count =  list.size();
		setup_bdd(count);
		for(Iterator it = list.iterator(); it.hasNext() ;) {
			String name = (String) it.next();
			alloc_var(name);
		}

		// ------------------------ build permutations
		int size = variables.size();
		// Test.checkEquality( size%2, 0, "odd varcount ??");
		int [] v1  = new int[size /2];
		int [] v2  = new int[size /2];

		Enumeration e =variables.elements();
		for (int i = 0; i < (size & ~1) ;i ++) {
			 TracedVariable v = (TracedVariable ) e.nextElement();
			 if(interleave) {
			 	if( (i%2) == 0) 	v1[i/2] = v.bdd;
			 	else				v2[i/2] = v.bdd;
			} else {
				if(i < v1.length) v1[i] = v.bdd;
				else v2[ i - v1.length] = v.bdd;
			}
		 }

		s2sp = bdd.createPermutation(v1, v2);
		sp2s = bdd.createPermutation(v2, v1);

		// s2sp.show();

	}

	private TracedVariable needVar(String str) throws IOException {
		TracedVariable ret = (TracedVariable) map.get(str);
		if(ret == null ) {
			throw new IOException("Unknown variable/operand " + str + " at line " + line_count);
		}
		updateUsage(ret);
		return ret;
	}

	private void updateUsage(TracedVariable v) {
		// if(v.last_use != Integer.MAX_VALUE)
		v.last_use = op_count;
	}

	private TracedVariable addTemporaryVariable(String name) {
		TracedVariable vret = new TracedVariable();
		vret.last_use = op_count;
		vret.name = name;
		vret.bdd = bdd.ref(0); // nothing...
		variables.add( vret);
		map.put(name, vret);

		return vret;
	}
	// -----------------------------------------------------

	private void need(String what) throws IOException {
		String got = need();
		if(!got.equals(what)) {
			check(false, "Syntax error: expected '" + what + "', but read '" + got + "', op=" + op_count);
		}
	}

	private String need() throws IOException {
		String ret = next();
		if(ret == null)  {
			check(false, "pre-mature end of file");
		}
		return ret;
	}
	// -----------------------------------------------------
	private int read() {
		int c = -1;
		if(stack_tos > 0) c = stack[--stack_tos];
		else {
			try { c = is.read(); } catch(IOException exx) {  }
		}
		if(c == '\n') line_count++;
		return c;
	}

	private void push(int c) {
		stack[stack_tos++] = c;
		if(c == '\n') line_count--;
	}
	private boolean isSpace(int c) { return (c == ' ' || c == '\n' || c == '\t' || c == '\r'); }
	private boolean isAlnum(int c) { return ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')  || c == '_'); }

	// -----------------------------------------------------

	private String getString() throws IOException {
		StringBuffer buffer = new StringBuffer();
		int c = 0;
		while( isSpace( c = read()));
		if(c != '"') throw new IOException("Not an string at line " + line_count);

		while( ( c = read()) != '"')
			buffer.append((char)c);

		return buffer.toString();

	}
	private void skip_eol() {
		for(;;) {
			int c = read();
			if(c == -1 || c == '\n') return;
		}
	}
	private String next() {
		sb.setLength(0);
		int c;
		do {
			c = read();
			if(c == -1) return null; // EOF
		} while( isSpace(c));

		if(isAlnum(c)) {
			do {
				sb.append((char)c);
				c = read();
				// XXX: error fixed ??? "return" was missing
				if(c == -1) return sb.toString();
			} while( isAlnum(c));

			if(!isSpace(c)) push(c);
		} else {
			if(c == '%' || c == '#') {
				int old_line = line_count;
				if(c == '%') {
					String count = next();
					TracedOperation tp = (TracedOperation) operations.lastElement();
					if(tp.size  == -1) tp.size = Integer.parseInt(count);
				}

				if(old_line == line_count) skip_eol(); // haven't had a \n yet
				return next();
			}
			return ""+((char)c);
		}

		return sb.toString();
	}

	/* package */ void checkEquality(int a, int b, String txt) throws IOException {
		if(a != b) throw new IOException(txt + ", " + a + " != " + b);
	}

	/* package */ void check(boolean b, String txt) throws IOException {
		if(!b) throw new IOException(txt);
	}

	/* package */ void check(boolean b) throws IOException {
		if(!b) throw new IOException("Check failed");
	}

	// ----------------------------------------------------

	public static void main(String [] args) {
		BDDTrace.verbose = true;
		Options.verbose = true;
		Options.profile_cache = true;

		try {
			if(args.length == 2) {
				new BDDTrace(args[0], Integer.parseInt(args[1]) );
			} else if(args.length == 1) new BDDTrace(args[0]);
			else JDDConsole.out.println("Usage:  java jdd.bdd.BDDTrace file.trace [initial node-base]");
		} catch(IOException exx) {
			JDDConsole.out.println("FAILED: " + exx.getMessage() );
			exx.printStackTrace();
			System.exit(20);
		}
	}
}

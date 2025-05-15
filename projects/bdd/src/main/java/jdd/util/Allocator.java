
package jdd.util;


import jdd.util.math.Digits;

/**
 * handles all allocation of *DD related large arrays.
 * This way, we can keep track of the allocated memory during the lifetime of JDD.
 */

public class Allocator {
	public final static int
		TYPE_INT = 0, TYPE_SHORT = 1, TYPE_BYTE = 2,
		TYPE_DOUBLE = 3, TYPE_CHAR = 4, TYPE_BOOLEAN = 5,
		TYPE_COUNT = 6;

	public final static String [] TYPE_NAMES = {
		"int", "short", "byte", "double", "char", "boolean"
	};
	public final static int [] TYPE_SIZES = {
		// could use Integer.BYTES etc but thats JDK 1.8+
		4, 2, 1, 8, 2, 1
	};


	private static long []stats_count = new long[TYPE_COUNT];
	private static long []stats_total = new long[TYPE_COUNT];
	private static long []stats_max = new long[TYPE_COUNT];
	private static long stats_total_bytes = 0;

	public static long getStatsCount(int type) {
		return stats_count[type];
	}

	public static long getStatsTotal(int type) {
		return stats_total[type];
	}

	public static long getStatsMax(int type) {
		return stats_max[type];
	}

	public static long getStatsTotalBytes() {
		return stats_total_bytes;
	}

	/**
	 * This function is called when memory allocation succeeds .
	 *
	 */
	private static void register(int type, long size) {
		stats_count[type]++;
		stats_total[type] += size;
		stats_max[type] = Math.max( stats_max[type], size);
		stats_total_bytes += size * TYPE_SIZES[type];
	}

	/**
	 * This function is called when memory allocation fails.
	 *
	 * Note that in general OutOfMemoryError cannot be cought, so this function
	 * will probably never be called...
	 */
	private static void fail(long size, int type, OutOfMemoryError ex) {
		long size_total = size * TYPE_SIZES[type];
		String typeName = TYPE_NAMES[type];

		JDDConsole.out.printf("FAILED to allocate %s[%d] (%d bytes)\n",
			typeName, size, size_total);
		showStats();
		throw ex;
	}

	// --------------------------------------------------------------------

	/** allocate an array of integers */
	public static int [] allocateIntArray(int size) {
		try {
			int [] ret = new int[size];
			register(TYPE_INT, size);
			return ret;
		} catch(OutOfMemoryError ex) {
			fail(size, TYPE_INT, ex);
			return null;
		}
	}

	/** allocate an array of double precision floating points */
	public static double [] allocateDoubleArray(int size) {

		try {
			double []ret = new double[size];
			register(TYPE_DOUBLE, size);
			return ret;
		} catch(OutOfMemoryError ex) {
			fail(size,TYPE_DOUBLE,ex);
			return null;
		}
	}


	/** allocate an array of short integers */
	public static short [] allocateShortArray(int size) {
		try {
			short [] ret =  new short[size];
			register(TYPE_SHORT, size);
			return ret;
		} catch(OutOfMemoryError ex) {
			fail(size,TYPE_SHORT,ex);
			return null;
		}
	}

	/** allocate an array of chars */
	public static char [] allocateCharArray(int size) {
		try {
			char [] ret = new char[size];
			register(TYPE_CHAR, size);
			return ret;
		} catch(OutOfMemoryError ex) {
			fail(size, TYPE_CHAR, ex);
			return null;
		}
	}

	/** allocate an array of bytes */
	public static byte [] allocateByteArray(int size) {

		try {
			byte []ret = new byte[size];
			register(TYPE_BYTE, size);
			return ret;
		} catch(OutOfMemoryError ex) {
			fail(size, TYPE_BYTE, ex);
			return null;
		}
	}

	/** allocate an array of boolean */
	public static boolean [] allocateBooleanArray(int size) {

		try {
			boolean []ret = new boolean[size];
			register(TYPE_BOOLEAN, size);
			return ret;
		} catch(OutOfMemoryError ex) {
			fail(size, TYPE_BOOLEAN, ex);
			return null;
		}
	}

	/** show the current memory allocation statistics */
	public static void showStats() {
		JDDConsole.out.printf("Allocator total memory: %d, stats (type,count,max,total):\n",
			stats_total_bytes);

		for(int i = 0; i < TYPE_COUNT; i++) {
			if(stats_count[i] > 0)
				JDDConsole.out.printf( "(%s, %d, %d, %d)\n", TYPE_NAMES[i],
					stats_count[i], stats_max[i], stats_total[i]);
		}
		JDDConsole.out.printf("\n");

		JDDConsole.out.printf("Total=%s max=%s used=%s free=%s\n",
			Digits.prettify1024( jdd.util.jre.JREInfo.totalMemory() ),
			Digits.prettify1024( jdd.util.jre.JREInfo.maxMemory() ),
			Digits.prettify1024( jdd.util.jre.JREInfo.usedMemory() ),
			Digits.prettify1024( jdd.util.jre.JREInfo.freeMemory() )
			);
	}
	public static void resetStats() {
		stats_total_bytes = 0;
		for(int i = 0; i < TYPE_COUNT; i++) {
			stats_count[i]  = stats_max[i] = stats_total[i] = 0;
		}
	}
}

package de.ismll.table;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Ian Schneider
 */
public class MTMatrix {
	static void computeRow(int[][] a,int[][] b,int[][] c,int r) {
		final int cols = b[0].length;
		for (int j = 0; j < cols; ++j) {
			for (int k = 0; k < a[r].length; ++k) {
				c[r][j] += a[r][k] * b[k][j];
			}
		}
	}

	public static int[][] mtmult2(final int[][] a,final int[][] b,int t) throws InterruptedException {
		final int [][] c = new int[a.length][b[0].length];
		final AtomicInteger row = new AtomicInteger();
		final int rows = a.length;
		Thread[] threads = new Thread[t];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					int r;
					while ( (r = row.getAndIncrement()) < rows) {
						computeRow(a,b,c,r);
					}
				}
			});
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
		return c;
	}

	public static void main(String [] args) {
		for (int i = 100; i <= 800; i+= 100) {
			doit(i,i,1);
			doit(i,i,2);
			doit(i,i,3);
			System.out.println("");
		}
	}
	static int[][] bigEffinMatrix(int r, int c) {
		int[][] m = new int[r][c];
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < c; j++) {
				m[i][j] = (int) (Math.random() * 10);
			}
		}
		return m;
	}
	static boolean equals(int[][] a,int[][] b) {
		boolean eq = true;
		for (int i = 0; i < a.length && eq; i++) {
			for (int j = 0; j < b[i].length && eq; j++) {
				eq &= a[i][j] == b[i][j];
			}
		}
		return eq;
	}
	static void doit(int r, int c, int t) {
		System.gc();
		System.gc();
		System.runFinalization();
		int[][] a = bigEffinMatrix(r,c);
		int[][] b = bigEffinMatrix(r,c);
		long time = System.currentTimeMillis();
		int[][] m = mult(a,b);
		System.out.println("single thread : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
		try {
			int[][] n = mtmult(a,b,t);
			System.out.println("mt " + t + " " + (System.currentTimeMillis() - time));
			assert equals(m,n) : "answers not equals";
		} catch (InterruptedException ie) {
			assert false;
		}
		time = System.currentTimeMillis();
		try {
			int[][] n = mtmult2(a,b,t);
			System.out.println("mt2 " + t + " " + (System.currentTimeMillis() - time));
			assert equals(m,n) : "answers not equals";
		} catch (InterruptedException ie) {
			assert false;
		}
	}
	public static int [][] mult(int [][] a, int [][] b) {
		int rows = a.length;
		int cols = b[0].length;
		int [][] c = new int[rows][cols];
		for (int i = 0; i < rows; ++i) {
			for (int j = 0; j < cols; ++j) {
				c[i][j] = 0;
				for (int k = 0; k < a[i].length; ++k) {
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return c;
	}
	public static int [][] mtmult(int [][] a, int [][] b,int t) throws InterruptedException {
		int [][] c = new int[a.length][b[0].length];
		ExecutorService es = Executors.newFixedThreadPool(t);
		ExecutorCompletionService ecs = new ExecutorCompletionService(es);
		for (int i = 0; i < a.length; ++i) {
			ecs.submit(new ComputeRow(a,b,c,i),null);
		}
		for (int i = 0; i < a.length; ++i) {
			ecs.take();
		}
		es.shutdown();
		return c;
	}
	static class ComputeRow implements Runnable {
		final int[][] a;
		final int[][] b;
		final int[][] c;
		final int row;
		public ComputeRow(int[][] a,int[][] b,int[][] c, int row) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.row = row;
		}
		public void run() {
			final int cols = b[0].length;
			for (int j = 0; j < cols; ++j) {
				for (int k = 0; k < a[row].length; ++k) {
					c[row][j] += a[row][k] * b[k][j];
				}
			}
		}
	}
}

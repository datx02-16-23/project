package demo_source;

/**
 * Produce a Heap demo for the presentation.
 * 
 * @author Richard
 *
 */
public class RichardsHeapDemo implements Runnable {
	private static final double[] lookingForDemo = { 1, 3, 3, 7 };
	private static final double[] foundSDemo = new double[lookingForDemo.length];
	private static final double[] initialValuesDemo = { 1, 17, 13, 7 };

	private final double[] lookingFor;
	private final double[] found;
	private final double[] initialValues;

	public RichardsHeapDemo(double[] lookingFor, double[] found, double[] bla) {
		this.lookingFor = lookingFor;
		this.found = found;
		this.initialValues = bla;
	}

	public RichardsHeapDemo() {
		this(lookingForDemo, foundSDemo, initialValuesDemo);
	}

	public void run() {
		// Build the heap.
		StulenHeap heap = new StulenHeap();

		for (double newValue : initialValues) {
			heap.insert(newValue);
		}

		for (int i = 0; i < lookingFor.length; i++) {
			System.out.println();
			double ans = heap.get(lookingFor[i]);

			if (ans == -1337) {
				// Value not in heap - insert it.
				heap.insert(lookingFor[i]);
				System.out.println("Couldn't find the value! Inserting.");
				System.out.println("heap = " + heap.toString());
			} else {				
				System.out.println("Got it!");
			}

			found[i] = lookingFor[i];

		}

	}

	public static void main(String[] args) {
		RichardsHeapDemo rhd = new RichardsHeapDemo();
		System.out.println("Let's Go!");
		rhd.run();
	}
}

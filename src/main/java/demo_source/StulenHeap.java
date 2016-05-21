package demo_source;

import java.util.Arrays;

public class StulenHeap {
	private static final int CAPACITY = 2;

	private int size; // Number of elements in heap
	private double[] heap; // The heap array

	public StulenHeap() {
		size = 0;
		heap = new double[CAPACITY];
	}

	/**
	 * Construct the binary heap given an array of items.
	 */
	public StulenHeap(double[] array) {
		size = array.length;
		heap = new double[array.length + 1];

		System.arraycopy(array, 0, heap, 1, array.length);// we do not use 0
															// index

		buildHeap();
	}

	/**
	 * runs at O(size)
	 */
	private void buildHeap() {
		for (int k = size / 2; k > 0; k--) {
			percolatingDown(k);
		}
	}

	private void percolatingDown(int k) {
		double tmp = heap[k];
		int child;

		for (; 2 * k <= size; k = child) {
			child = 2 * k;

			if (child != size && heap[child] > (heap[child + 1]))
				child++;

			if (tmp > (heap[child]))
				heap[k] = heap[child];
			else
				break;
		}
		heap[k] = tmp;
	}

	/**
	 * Sorts a given array of items.
	 */
	public void heapSort(double[] array) {
		size = array.length;
		heap = new double[size + 1];
		System.arraycopy(array, 0, heap, 1, size);
		buildHeap();

		for (int i = size; i > 0; i--) {
			double tmp = heap[i]; // move top item to the end of the heap array
			heap[i] = heap[1];
			heap[1] = tmp;
			size--;
			percolatingDown(1);
		}
		for (int k = 0; k < heap.length - 1; k++)
			array[k] = heap[heap.length - 1 - k];
	}

	/**
	 * Deletes the top item
	 */
	public double deleteMin() throws RuntimeException {
		if (size == 0)
			throw new RuntimeException();
		double min = heap[1];
		heap[1] = heap[size--];
		percolatingDown(1);
		return min;
	}

	/**
	 * Inserts a new item
	 */
	public void insert(double x) {
		if (size == heap.length - 1)
			doubleSize();

		// Insert a new item to the end of the array
		int pos = ++size;

		// Percolate up
		for (; pos > 1 && x < (heap[pos / 2]); pos = pos / 2){
			heap[pos] = heap[pos / 2];			
		}

		heap[pos] = x;
	}

	/**
	 * Method written for our demo.
	 * 
	 * @return X if found, {@link -1337} otherwise.
	 */
	public double get(double x) {
		System.out.println("Looking for " + x + " in " + Arrays.toString(heap));
		
		int i = 0;
		while (i < heap.length) {
			if (heap[i] < x) {
				i = 2 * i + 1; // Go to left child.
			} else if (heap[i] > x) {
				i = 2 * i + 2; // Go to right child.
			} else {
				return heap[i]; // Yay!
			}
		}

		return -1337;
	}

	private void doubleSize() {
		double[] old = heap;
		heap = new double[heap.length * 2];
		System.arraycopy(old, 1, heap, 1, size);
	}

	public String toString() {
		return Arrays.toString(heap);
	}
}

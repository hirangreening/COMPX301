// ID: 1522172
// Name: Hiran Greening

/**
 * This class implements a heap data structure.
 */
public class Heap {

    // Declare variables
    private String[] heapArray;
    private int size;
    private int capacity;

    /**
     * Constructor for the Heap class.
     * 
     * @param capacity the maximum number of elements the heap can store.
     */
    public Heap(int capacity) {

        // Initialise variables
        this.capacity = capacity;
        this.size = 0;

        // Create a new heap array
        this.heapArray = new String[capacity];
    }

    /**
     * Inserts a value into the heap.
     * 
     * @param value the value to insert.
     */
    public void insert(String value) {

        // Check if the heap is full
        if (size == capacity) {

            // Throw an exception
            throw new RuntimeException("Heap is full");
        }

        // Insert the value into the heap
        heapArray[size] = value;

        // Increment the size of the heap
        size++;

        // Sift up the value
        siftUp(size - 1);
    }

    /**
     * Removes the root element from the heap.
     * 
     * @return the root element.
     */
    public String remove() {

        // Check if the heap is empty
        if (size == 0) {

            // Throw an exception
            throw new RuntimeException("Heap is empty");
        }

        //  set the root element
        String root = heapArray[0];

        // Replace the root element with the last element
        heapArray[0] = heapArray[size - 1];

        // Decrement the size of the heap
        size--;

        // Sift down the root element
        siftDown(0);

        // Return the root element
        return root;
    }

    /**
     * Builds a heap from an array of values.
     * 
     * @param array the array of values.
     */
    public void heapify(String[] array) {

        // Set the heap array
        this.heapArray = array;

        // Set the size of the heap
        this.size = array.length;

        // Build the heap
        for (int i = size / 2 - 1; i >= 0; i--) {

            // Sift down the element
            siftDown(i);
        }
    }

    /**
     * Sorts the heap using the heapsort algorithm.
     */
    public void heapsort() {

        // Build the heap
        heapify(heapArray);

        // Sort the heap
        for (int i = size - 1; i > 0; i--) {

            // Swap the root element with the last element
            swap(0, i);

            // Decrement the size of the heap
            size--;

            // Sift down the root element
            siftDown(0);
        }
    }

    /**
     * Sifts up an element in the heap.
     * 
     * @param index the index of the element to sift up.
     */
    private void siftUp(int index) {

        // while index is greater than 0 and parent is less than index
        while (index > 0 && heapArray[parent(index)].compareTo(heapArray[index]) < 0) {

            // swap parent and index
            swap(parent(index), index);

            // set index to parent
            index = parent(index);
        }
    }

    /**
     * Sifts down an element in the heap.
     * 
     * @param index the index of the element to sift down.
     */
    private void siftDown(int index) {

        // set largest to index
        int largest = index;

        // set left and right to left and right children
        int left = leftChild(index);
        int right = rightChild(index);

        // if left is less than size and left is greater than largest
        if (left < size && heapArray[left].compareTo(heapArray[largest]) > 0) {

            // set largest to left
            largest = left;
        }

        // if right is less than size and right is greater than largest
        if (right < size && heapArray[right].compareTo(heapArray[largest]) > 0) {

            // set largest to right
            largest = right;
        }

        // if largest is not equal to index
        if (largest != index) {

            // swap index and largest
            swap(index, largest);

            // sift down largest
            siftDown(largest);
        }
    }

    /**
     * Returns the heap array.
     * 
     * @return the heap array.
     */
    public String[] getHeapArray() {

        // return the heap array
        return heapArray;
    }

    /**
     * Returns the parent index of a given index.
     * 
     * @param index
     * @return
     */
    private int parent(int index) {

        // return the parent index
        return (index - 1) / 2;
    }

    /**
     * Returns the left child index of a given index.
     * 
     * @param index
     * @return
     */
    private int leftChild(int index) {

        // return the left child index
        return 2 * index + 1;
    }

    /**
     * Returns the right child index of a given index.
     * 
     * @param index
     * @return
     */
    private int rightChild(int index) {

        // return the right child index
        return 2 * index + 2;
    }

    /**
     * Swaps two elements in the heap array.
     * 
     * @param i the index of the first element.
     * @param j the index of the second element.
     */
    private void swap(int i, int j) {

        // set temp to element at i
        String temp = heapArray[i];

        // set element at i to element at j
        heapArray[i] = heapArray[j];

        // set element at j to temp
        heapArray[j] = temp;
    }
}

public class Heap {
    private String[] heapArray;
    private int size;
    private int capacity;

    public Heap(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.heapArray = new String[capacity];
    }

    public void insert(String value) {
        if (size == capacity) {
            throw new RuntimeException("Heap is full");
        }
        heapArray[size] = value;
        size++;
        siftUp(size - 1);
    }

    public String remove() {
        if (size == 0) {
            throw new RuntimeException("Heap is empty");
        }
        String root = heapArray[0];
        heapArray[0] = heapArray[size - 1];
        size--;
        siftDown(0);
        return root;
    }

    public void heapify(String[] array) {
        this.heapArray = array;
        this.size = array.length;
        for (int i = size / 2 - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    public void heapsort() {
        heapify(heapArray);
        for (int i = size - 1; i > 0; i--) {
            swap(0, i);
            size--;
            siftDown(0);
        }
    }

    private void siftUp(int index) {
        while (index > 0 && heapArray[parent(index)].compareTo(heapArray[index]) < 0) {
            swap(parent(index), index);
            index = parent(index);
        }
    }

    private void siftDown(int index) {
        int largest = index;
        int left = leftChild(index);
        int right = rightChild(index);

        if (left < size && heapArray[left].compareTo(heapArray[largest]) > 0) {
            largest = left;
        }
        if (right < size && heapArray[right].compareTo(heapArray[largest]) > 0) {
            largest = right;
        }
        if (largest != index) {
            swap(index, largest);
            siftDown(largest);
        }
    }

    public String[] getHeapArray() {
        return heapArray;
    }
    

    private int parent(int index) {
        return (index - 1) / 2;
    }

    private int leftChild(int index) {
        return 2 * index + 1;
    }

    private int rightChild(int index) {
        return 2 * index + 2;
    }

    private void swap(int i, int j) {
        String temp = heapArray[i];
        heapArray[i] = heapArray[j];
        heapArray[j] = temp;
    }
}

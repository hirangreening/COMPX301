 // ID: 1522172
// Name: Hiran Greening
 
 /**
  * This is a test class to test the Heap class
  */
public class test {

    /**
     * Main method to test the Heap class
     * @param args
     */
    public static void main(String[] args) {

        // array of test data
        String[] testData = {"apple", "orange", "banana", "grape", "strawberry"};

        // Create a new heap
        Heap heap = new Heap(testData.length);

        // Insert test data into the heap
        heap.heapify(testData);

        // Sort the heap
        heap.heapsort();

        // Use the getter to access sorted data
        for (String s : heap.getHeapArray()) {

            // Print the sorted data
            System.out.println(s);
        }
    }
}

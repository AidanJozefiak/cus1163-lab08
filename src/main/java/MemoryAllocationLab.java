import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

    static class MemoryBlock {
        int start;
        int size;
        String processName;  // null if free

        public MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        public boolean isFree() {
            return processName == null;
        }

        public int getEnd() {
            return start + size - 1;
        }
    }

    static int totalMemory;
    static ArrayList<MemoryBlock> memory;
    static int successfulAllocations = 0;
    static int failedAllocations = 0;

    /**
     * TODO 1, 2: Process memory requests from file
     * <p>
     * This method reads the input file and processes each REQUEST and RELEASE.
     * <p>
     * TODO 1: Read and parse the file
     *   - Open the file using BufferedReader
     *   - Read the first line to get total memory size
     *   - Initialize the memory list with one large free block
     *   - Read each subsequent line and parse it
     *   - Call appropriate method based on REQUEST or RELEASE
     * <p>
     * TODO 2: Implement allocation and deallocation
     *   - For REQUEST: implement First-Fit algorithm
     *     * Search memory list for first free block >= requested size
     *     * If found: split the block if necessary and mark as allocated
     *     * If not found: increment failedAllocations
     *   - For RELEASE: find the process's block and mark it as free
     *   - Optionally: merge adjacent free blocks (bonus)
     */
    public static void processRequests(String filename) {

        // TODO 1: Read file and initialize memory
        // Try-catch block to handle file reading
        // Read first line for total memory size
        // Create initial free block: new MemoryBlock(0, totalMemory, null)
        // Read remaining lines in a loop
        // Parse each line and call allocate() or deallocate()
        memory = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
          totalMemory = Integer.parseInt(br.readLine().trim());
          memory.add(new MemoryBlock(0, totalMemory, null));
          String line;
          while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            if (parts[0].equalsIgnoreCase("REQUEST")) {
              String processName = parts[1];
              int size = Integer.parseInt(parts[2]);
              allocate(processName, size);
            } else if (parts[0].equalsIgnoreCase("RELEASE")) {
              String processName = parts[1];
              deallocate(processName);
            } else {
              System.out.println("Unknown command: " + line);
            }
          } 
        } catch (IOException e) {
          System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * TODO 2A: Allocate memory using First-Fit
     */
    private static void allocate(String processName, int size) {
        // Search through memory list
        // Find first free block where size >= requested size
        // If found:
        //   - Mark block as allocated (set processName)
        //   - If block is larger than needed, split it:
        //     * Create new free block for remaining space
        //     * Add it to memory list after current block
        //   - Increment successfulAllocations
        //   - Print success message
        // If not found:
        //   - Increment failedAllocations
        //   - Print failure message
        for (int i = 0;  i < memory.size(); i++) {System.out.println("Allocated " + size + " KB to process " + processName);
          MemoryBlock block = memory.get(i);
          if (block.isFree() && block.size >= size) {
            if (block.size > size) {
              MemoryBlock newBlock = new MemoryBlock(block.start + size, block.size - size, null);
              memory.add(i + 1, newBlock);
            }
            block.size = size;
            block.processName = processName;
            successfulAllocations++;
            System.out.println("Allocated " + size + " KB to process " + processName);
            return;
          }
        }
        failedAllocations++;
        System.out.println("Failed to allocate " + size + " KB to process " + processName);
    }
    
    private static void deallocate(String processName) {
      for (int i = 0; i < memory.size(); i++) {
        MemoryBlock block = memory.get(i);
        if (!block.isFree() && block.processName.equals(processName)) {
          block.processName = null;
          System.out.println("Released memory of process " + processName);
          if (i > 0 && memory.get(i-1).isFree()) {
            MemoryBlock prev = memory.get(i-1);
            prev.size += block.size;
            memory.remove(i);
            i--;
            block = prev;
          }
          if (i < memory.size() - 1 && memory.get(i + 1).isFree()) {
            MemoryBlock next = memory.get(i+1);
            block.size += next.size;
            memory.remove(i+1);
          }
          return;
        }
      }
      System.out.println("No process found with name: " + processName);
    }

    public static void displayStatistics() {
        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        int blockNum = 1;
        for (MemoryBlock block : memory) {
            String status = block.isFree() ? "FREE" : block.processName;
            String allocated = block.isFree() ? "" : " - ALLOCATED";
            System.out.printf("Block %d: [%d-%d]%s%s (%d KB)%s\n",
                    blockNum++,
                    block.start,
                    block.getEnd(),
                    " ".repeat(Math.max(1, 10 - String.valueOf(block.getEnd()).length())),
                    status,
                    block.size,
                    allocated);
        }

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");

        int allocatedMem = 0;
        int freeMem = 0;
        int numProcesses = 0;
        int numFreeBlocks = 0;
        int largestFree = 0;

        for (MemoryBlock block : memory) {
            if (block.isFree()) {
                freeMem += block.size;
                numFreeBlocks++;
                largestFree = Math.max(largestFree, block.size);
            } else {
                allocatedMem += block.size;
                numProcesses++;
            }
        }

        double allocatedPercent = (allocatedMem * 100.0) / totalMemory;
        double freePercent = (freeMem * 100.0) / totalMemory;
        double fragmentation = freeMem > 0 ?
                ((freeMem - largestFree) * 100.0) / freeMem : 0;

        System.out.printf("Total Memory:           %d KB\n", totalMemory);
        System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocatedMem, allocatedPercent);
        System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePercent);
        System.out.printf("Number of Processes:    %d\n", numProcesses);
        System.out.printf("Number of Free Blocks:  %d\n", numFreeBlocks);
        System.out.printf("Largest Free Block:     %d KB\n", largestFree);
        System.out.printf("External Fragmentation: %.2f%%\n", fragmentation);

        System.out.println("\nSuccessful Allocations: " + successfulAllocations);
        System.out.println("Failed Allocations:     " + failedAllocations);
        System.out.println("========================================");
    }

    /**
     * Main method (FULLY PROVIDED)
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MemoryAllocationLab <input_file>");
            System.out.println("Example: java MemoryAllocationLab memory_requests.txt");
            return;
        }

        System.out.println("========================================");
        System.out.println("Memory Allocation Simulator (First-Fit)");
        System.out.println("========================================\n");
        System.out.println("Reading from: " + args[0]);

        processRequests(args[0]);
        displayStatistics();
    }
}

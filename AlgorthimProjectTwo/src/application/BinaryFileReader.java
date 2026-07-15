package application;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;


public class BinaryFileReader {

    private String filePath;
    private MinHeap heap;

    public BinaryFileReader(String filePath, MinHeap heap) {
        this.filePath = filePath;
        this.heap = heap;
    }

    // entry point
    public void readAndBuildHeap() {
        int[] freq = countFrequencies();
        if (freq == null) return;
        loadIntoHeap(freq);
    }
    private int[] countFrequencies() {
        int[] arr = new int[256];

        try (FileInputStream in = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(in)) {

            byte[] buffer = new byte[8192];
            int bytesRead;//store number of byte read

            while ((bytesRead = bis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    arr[buffer[i] & 0xFF]++;//Increase character  frequency buffer[i]this character 
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }

        return arr;
    }

    // insert characters into heap
    private void loadIntoHeap(int[] freq) {
        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                LeafNode leaf = new LeafNode((char) i, freq[i]);
                heap.insert(leaf);
            }
        }
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}
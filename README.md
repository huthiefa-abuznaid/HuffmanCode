# Huffman Coding File Compressor & Decompressor

[cite_start]This project is a complete file compression and decompression application utilizing the **Huffman Coding** algorithm[cite: 52, 63]. [cite_start]It was developed as Project #2 for the *Design and Analysis of Algorithms (COM336)* course at Birzeit University[cite: 53, 54, 55].

[cite_start]The application reads any input file, compresses it into a custom binary format, and is capable of reading it back to reconstruct the original file with 100% losslessness[cite: 57, 63, 64, 65, 66].

## 📖 How It Works

1. [cite_start]**Frequency Counting:** Reads the target file and tallies the occurrence of every byte[cite: 68].
2. [cite_start]**Tree Construction:** Uses a custom priority queue and binary tree to build the Huffman coding tree based on byte frequencies[cite: 62, 69].
3. [cite_start]**Prefix Encoding:** Generates unique, variable-length prefix codes to guarantee ambiguity-free decompression[cite: 57, 60, 61].
4. [cite_start]**Compression & Header Writing:** Outputs the compressed bitstream along with a header containing the encoding table/tree structure[cite: 72, 74].
5. [cite_start]**Decompression:** Reads the compressed file, parses the header, decodes the bitstream, and restores the exact original file[cite: 64, 65, 75].

---

## ✨ Features

- [cite_start]**Custom Data Structures:** Built using a custom-designed Priority Queue and Binary Tree[cite: 62].
- [cite_start]**Graphical User Interface (GUI):** A clean and interactive main interface to display[cite: 80, 88]:
  - [cite_start]The generated encoding table for each byte[cite: 70].
  - [cite_start]Detailed statistics (original size, compressed size, and the exact compression ratio)[cite: 71].
- [cite_start]**OS File Explorer Integration:** A simplified mini-interface that allows you to right-click or quickly compress/decompress files directly through your operating system's file explorer[cite: 82, 83, 89, 90].

---

## 📊 Sample Statistics Output

[cite_start]When compressing a file, the application UI displays the following data[cite: 70, 71, 81, 88]:

| Metric | Value |
| :--- | :--- |
| **Original File Size** | 1,024 KB |
| **Compressed File Size** | 582 KB |
| **Compression Ratio** | 43.16% |

### Huffman Encoding Table Example
| Byte (ASCII) | Frequency | Huffman Code |
| :--- | :--- | :--- |
| `A` (65) | 45 | `0` |
| `B` (66) | 13 | `101` |
| `C` (67) | 12 | `100` |

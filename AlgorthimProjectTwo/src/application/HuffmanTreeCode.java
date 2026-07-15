package application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

import javafx.scene.control.ListView;

public class HuffmanTreeCode {
// tree done and huff code 
// tomoro read from file and make header 
	// buffer
	private MinHeap heap = new MinHeap();
	private Node huffRoot = null;

	private String preOrder = "";
	private int hufTreeCount = 0;
	private int[] charFreq;
	private LeafNode[] list = new LeafNode[256];
	private int listSize = 0;

	private java.io.File inputFile;
	private String outputPath;
	private String[] codes;
	private int totalBitsWritten = 0;
	private int index = 0;
	private int offset = 0;

	public HuffmanTreeCode() {
		super();
	}

	public HuffmanTreeCode(MinHeap heap, Node huffRoot) {
		super();
		this.heap = heap;
		this.huffRoot = huffRoot;
	}

	public void createHuffmanTree() {

		if (heap.getSize() == 1) {
			huffRoot = heap.removeMin();
			codeNumber(huffRoot, "0");
			return;
		}

		while (heap.getSize() > 1) {
			Node x = heap.removeMin();
			Node y = heap.removeMin();
			Node z = new Node(x, y);
			heap.insert(z);
		}
		huffRoot = heap.removeMin();
		codeNumber(huffRoot, "");
	}

// give the character or any thing code 110 0111
	private void codeNumber(Node node, String code) {
		if (node == null)
			return;

		if (node instanceof LeafNode) {
			((LeafNode) node).setCode(code);
			return;
		}
// recusive  to give charater number 0 or 1 
		codeNumber(node.getLeft(), code + "0");
		codeNumber(node.getRight(), code + "1");
	}

	public Node rebuildFromBits(String bitString) {
		int[] index = { 0 }; // wrapper so recursion can modify it
		return rebuildHelper(bitString, index);
	}
// convert numbers on header to charcter if show 0  has leaf if 1 has number 
// if show take 8 bit  why becouse a =96 convert to binary 11000001
// read 1 start 1000001 then comblete index +8

	private Node rebuildHelper(String bitString, int[] index) {
		// stop read if arrived last charcter
		if (index[0] >= bitString.length())
			return null;

		char bit = bitString.charAt(index[0]);
		// put prefix becouse use recursion
		index[0]++;

		if (bit == '1') {
			// Leaf node next 8 bits are the character a= 11000001
			String charBits = bitString.substring(index[0], index[0] + 8);
			char character = (char) Integer.parseInt(charBits, 2);
			index[0] += 8;
			return new LeafNode(character, 0);

		} else {
			// bit == 0 internal node
			// Recursively build left subtree first, then right
			Node left = rebuildHelper(bitString, index);
			// printe when complete recusion
			Node right = rebuildHelper(bitString, index);
			return new Node(left, right);
		}
	}

	// initilaze file and output path
	public void setCompressTarget(java.io.File inputFile, String outputPath) {
		this.inputFile = inputFile;
		this.outputPath = outputPath;
		this.totalBitsWritten = 0;
		this.heap = new MinHeap();
		BinaryFileReader reader = new BinaryFileReader(inputFile.getAbsolutePath(), this.heap);
		reader.readAndBuildHeap();
	}

	public void compress() {

		totalBitsWritten = 0;
		this.codes = buildCodesArray(huffRoot);//Character path
		try (FileOutputStream fos = new FileOutputStream(outputPath);
				BufferedOutputStream bos = new BufferedOutputStream(fos)) {
			// write extension on the header
			String extension = getFileExtension(inputFile);
			bos.write(extension.getBytes());
			bos.write(0);
			totalBitsWritten += (extension.getBytes().length + 1) * 8;
			writeHeader(bos);
			writeOnFile(bos);
			bos.flush();//give data still
		} catch (IOException e) {
			System.out.println("Compress error: " + e.getMessage());
		}

	}

	private void writeHeader(BufferedOutputStream bos) throws IOException {
		// calculate size on byte
		int headerSizeInBytes = getHeaderSizeInBytes();
		// covert size to string
		String sizeStr = String.valueOf(headerSizeInBytes);
		// Length of size for example 12 enghth 2
		String lengthOfSize = String.valueOf(sizeStr.length());

		// write length of header size then size fore example 41234 4 for length and
		// 1234 size
		bos.write(lengthOfSize.getBytes());
		bos.write(sizeStr.getBytes());

		// total bit write 1 byte for length Of size and header size then convert bits
		totalBitsWritten += (1 + sizeStr.length()) * 8;
		// extend huffcode frome tree
		String treeBits = huffRoot.toBits();

		int currBit = 0;
		int count = 0;// count to bit to byte 1 to 8

		for (int i = 0; i < treeBits.length(); i++) {

			char bit = treeBits.charAt(i);

			if (bit == '1')
				// if 1 shift 1 and put 1
				currBit = (currBit << 1) | 1;
			else
				// if 0 shift
				currBit <<= 1;

			count++;
			// if count equal 8 then write one byte
			if (count == 8) {

				bos.write(currBit);
				totalBitsWritten += 8;
				// return currBit and count to 0
				currBit = 0;
				count = 0;
			}
		}

		// if still under 8
		// for example still 101 store 000000101 error why ? the file read right to lift
		// (8 - count) make shift 5 then 10100000
		if (count > 0) {
			currBit <<= (8 - count);
			bos.write(currBit);
			totalBitsWritten += 8;
		}
		// this need when decompress to calculate padding
		bos.write(8 - count);
		totalBitsWritten += 8;
	}

	private void writeOnFile(BufferedOutputStream bos) throws IOException {
		try (FileInputStream in = new FileInputStream(inputFile)) {
			int currB = 0;
			int count = 0;
			byte[] bytes;

			while (in.available() > 0) {
				bytes = in.readNBytes(4096);
				// length from bytes.lenght large length is 4096
				for (int i = 0; i < bytes.length; i++) {
					//charcter ..> byte  byte...> ascii 
					int unsignedByte = bytes[i] & 0xFF;// 0to 255
                    //codes ..> ascii
					if (codes[unsignedByte] != null) {
						// give code huffman for this character
						// then code string "110 divided to "0","1","1"
						char[] bits = codes[unsignedByte].toCharArray();
                          // put on currB
						for (char bit : bits) {
							// if equal 1 make shift and put 1
							if (bit == '1')
								currB = (currB << 1) | 1;
							else
								// make shift 0
								currB <<= 1;

							count++;
							totalBitsWritten++;
// return the currb and count to 0 after count equal 8 becouse byte = 8 bit
							if (count == 8) {
								bos.write(currB);
								currB = 0;
								count = 0;
							}
						}
					}
				}
			}

			// write last partial byte with padding
			if (count > 0) {
				currB <<= (8 - count);
				bos.write(currB);
				totalBitsWritten += 8;
			}

			// write padding number
			bos.write(8 - count);
			totalBitsWritten += 8;

			bos.flush();
		}
	}

	// build array code to easy access on tree
	public static String[] buildCodesArray(Node root) {
		String[] codes = new String[256];
		fillCodes(root, "", codes);
		return codes;
	}

//    Passes on tree and store on array code 
	private static void fillCodes(Node node, String path, String[] codes) {
		if (node == null)
			return;
		if (node instanceof LeafNode) {
			// store the character path on tree
			// convert character to asci code where charcter and path what
			codes[((LeafNode) node).getCharacter() & 0xFF] = path;
			return;
		}
		fillCodes(node.getLeft(), path + "0", codes);
		fillCodes(node.getRight(), path + "1", codes);
	}

	public int getHeaderSizeInBytes() {
		String treeBits = huffRoot.toBits();

		return (treeBits.length() % 8) == 0 ? treeBits.length() / 8 : (treeBits.length() / 8) + 1;
	}

	public String getCompressionRatioFormatted() {
		long beforeBytes = inputFile.length();
		long afterBytes = totalBitsWritten / 8;

		if (beforeBytes == 0)
			return "0.00%";

		double ratio = ((double) afterBytes / beforeBytes) * 100;
		return String.format("%.2f%%", ratio);
	}

	public String getAfterSizeFormatted() {
		long afterBytes = totalBitsWritten / 8;
		return formatSize(afterBytes);
	}

	public String getBeforeSizeFormatted() {
		long beforeBytes = inputFile.length();
		return formatSize(beforeBytes);
	}

	private String formatSize(long bytes) {
		// under 1024 but right number B
		if (bytes < 1024)
			return bytes + " B";
		// under 1MByte write 1024
		if (bytes < 1024 * 1024)
			return String.format("%.2f KB", bytes / 1024.0);
		// large than 1MB
		return String.format("%.2f MB", bytes / (1024.0 * 1024));
	}

	private String getFileExtension(java.io.File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return name.substring(lastIndexOf);
	}

// to show the user character frequency 
	public void readFileAndFillUI(ListView<LeafNode> freqTableList) {
		preOrder = "";
		charFreq = new int[256];
		codes = new String[256];
		listSize = 0;
		list = new LeafNode[256];
		this.heap = new MinHeap();

		try (FileInputStream in = new FileInputStream(inputFile)) {
			byte[] bytesBuffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = in.read(bytesBuffer)) != -1) {
				for (int i = 0; i < bytesRead; i++) {
					charFreq[bytesBuffer[i] & 0xFF]++;
				}
			}

			for (int i = 0; i < charFreq.length; i++) {
				if (charFreq[i] != 0) {
					LeafNode leaf = new LeafNode((char) i, charFreq[i]);
					heap.insert(leaf);
					list[listSize++] = leaf;
				}
			}

			createHuffmanTree();

			sortListByFrequency();

			freqTableList.getItems().clear();
			freqTableList.getItems().addAll(Arrays.copyOfRange(list, 0, listSize));
			freqTableList.setMaxHeight(150);
			freqTableList.setMinWidth(440);

		} catch (IOException e) {
			System.out.println("Error rendering UI table: " + e.getMessage());
		}
	}
//bubble sort
	private void sortListByFrequency() {
		for (int i = 0; i < listSize - 1; i++) {
			for (int j = 0; j < listSize - i - 1; j++) {
				if (list[j].getFreq() < list[j + 1].getFreq()) {
					LeafNode temp = list[j];
					list[j] = list[j + 1];
					list[j + 1] = temp;
				}
			}
		}
	}

	public void readCompressedFile(java.io.File compressedFile, ListView<String> headerList) {
		try {
			if (headerList != null) {
				headerList.getItems().clear();
			}

			FileInputStream fis = new FileInputStream(compressedFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			int c = 0;
			StringBuilder extBuilder = new StringBuilder();
//read extension
			while ((c = bis.read()) != -1 && c != 0) {
				extBuilder.append((char) c);
			}
			String extension = extBuilder.toString();

			if (headerList != null) {
				headerList.getItems().add("Original File Extension : " + extension);
			}
//read first byte for header size 
			String hederSizeLength = new String(bis.readNBytes(1));
			int sizeLength = Integer.valueOf(hederSizeLength);
			int hederSize = Integer.valueOf(new String(bis.readNBytes(sizeLength)));

			if (headerList != null) {
				headerList.getItems().add("Original File Header Size : " + hederSize);
			}
//read header 
			StringBuilder hufTreePreOrder = new StringBuilder();
			int preOrderLength = hederSize; 
			byte[] buffer = new byte[8];
			int totalBytesRead = 0;

			while (totalBytesRead < preOrderLength) {
				//why use mine to reead all datat list 8 bite 
				int bytesRead = bis.read(buffer, 0, Math.min(buffer.length, preOrderLength - totalBytesRead));
				if (bytesRead == -1)
					break;
//sumaion the bits of header 
				for (int b = 0; b < bytesRead; b++) {
					int byte1 = buffer[b] & 0xFF;
					for (int bit = 7; bit >= 0; bit--) {
						int currBit = 1 << bit;
						int result = (byte1 & currBit) == 0 ? 0 : 1;
						hufTreePreOrder.append(result);
					}
				}
				totalBytesRead += bytesRead;
			}

			int treePadding = bis.read(); 
			
			if (treePadding > 0 && treePadding < 8 && hufTreePreOrder.length() >= treePadding) {
				hufTreePreOrder.setLength(hufTreePreOrder.length() - treePadding);
			}

			if (headerList != null) {
				headerList.getItems().add("Original File PreOrder : " + hufTreePreOrder);
				headerList.setMaxHeight(100);
				headerList.setMinWidth(200);
			}

			this.index = 0;
			Node root = constructTree(hufTreePreOrder.toString().toCharArray());

			StringBuilder allData = new StringBuilder();
			byte[] buffer2 = new byte[4096];
			int bytesRead;

			while ((bytesRead = bis.read(buffer2)) != -1) {
				for (int b = 0; b < bytesRead; b++) {
					int byte1 = buffer2[b] & 0xFF;
					for (int bit = 7; bit >= 0; bit--) {
						int currBit = 1 << bit;
						int result = (byte1 & currBit) == 0 ? 0 : 1;
						allData.append(result);
					}
				}

				if (bis.available() == 1) {
					break;
				}
			}

			this.offset = bis.read();

			if (this.offset > 0 && this.offset < 8 && allData.length() >= this.offset) {
				allData.setLength(allData.length() - this.offset);
			}

			this.index = 0;
			//convert string 1001111 to charcter
			decode(compressedFile, allData.toString(), extension, this.offset, root);

			bis.close();
			fis.close();

		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO Error: " + e.getMessage());
		}
	}

	public void decode(java.io.File compressedFile, String data, String extension, int offset, Node root) throws IOException {
		// create a new file and put original extension and extension the original fill
		// name
	    String rawName = compressedFile.getName();
	    int lastDot = rawName.lastIndexOf('.');
	    String originalName = (lastDot > 0) ? rawName.substring(0, lastDot) : rawName;
//extend extension 
	    if (extension.startsWith("..")) {
	        extension = extension.substring(1);
	    } else if (!extension.startsWith(".")) {
	        extension = "." + extension;
	    }
	 // getParent extension path on fill desktop for example on photo on disk ...

	    File unCompressedFile = new File(compressedFile.getParent(), originalName + extension);
	 // make sure this fill create fill process is done if not create a new file

	    if (unCompressedFile.exists()) {
	        unCompressedFile.delete();
	    }
	    unCompressedFile.createNewFile();

	    try (FileOutputStream fos = new FileOutputStream(unCompressedFile);
	         BufferedOutputStream bos = new BufferedOutputStream(fos)) {

	        if (root == null) {
	            System.out.println("Tree root is null. Cannot decode.");
	            return;
	        }
	     // when one character duplicated
	        if (root.getLeft() == null && root.getRight() == null) {
	            if (root instanceof LeafNode) {
	                int limit = data.length();
	                for (int i = 0; i < limit; i++) {
	                    bos.write(((LeafNode) root).getCharacter());
	                }
	            }
	            bos.flush();
	            return;
	        }
	     // copy character on file

	        Node curr = root;
	        byte[] buffer = new byte[4096];
	        int bufferIndex = 0;

	        for (int i = 0; i < data.length(); i++) {
	            char c = data.charAt(i);

	         // got to on data on buffer and covert to for example a=01 and b= 11 code 0111 
	         // AB
	            //uncrepted tree 
	            if (c == '0') {
	                curr = curr.getLeft();
	            } else if (c == '1') {
	                curr = curr.getRight();
	            }

	            if (curr instanceof LeafNode) {
	            	// put character found on buffer
	                buffer[bufferIndex++] = (byte) ((LeafNode) curr).getCharacter();
	                curr = root; 
	              //write buffer on file

	                if (bufferIndex >= buffer.length) {
	                    bos.write(buffer, 0, bufferIndex);
	                    bufferIndex = 0;
	                }
	            }
	        }
	      //write last charcter if fill has 4100 the buffer read and write 4096 still 4 this line write this character

	        if (bufferIndex > 0) {
	            bos.write(buffer, 0, bufferIndex);
	        }

	        bos.flush();
	        System.out.println("Decompression Done! File saved at: " + unCompressedFile.getAbsolutePath());

	    } catch (IOException e) {
	        System.out.println("Error during decompression: " + e.getMessage());
	    }
	}


	private Node constructTree(char[] preOrderArray) {
        if (preOrderArray == null || index >= preOrderArray.length) {
            return null;
        }

        char value = preOrderArray[index++];
        if (value == '1') {
            StringBuilder binary = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                if (index < preOrderArray.length) {
                    binary.append(preOrderArray[index++]);
                }
            }
            char character = (char) Integer.parseInt(binary.toString(), 2);
            return new LeafNode(character, 0);
        } else {
            Node node = new Node();
            node.setLeft(constructTree(preOrderArray));
            node.setRight(constructTree(preOrderArray));
            return node;
        }
    }

// convert series  of string bit  to byte 
	public byte convertBitStringToByte(String bitString) {
		byte result = 0;
		for (int i = 0; i < 8; i++) {
			char bit = bitString.charAt(i);
			// i equal 0 10000000 if i equal 1 make shift 6 and so on
			result |= (bit - '0') << (7 - i);

		}
		// rtern number asci code for example a =97
		return result;
	}
	public Node constructTreeIterative(char[] preOrder) {
	    Stack<Node> stack = new Stack<>();
	    int i = 0;

	    // Read root first
	    Node root = null;

	    while (i < preOrder.length) {
	        char bit = preOrder[i++];

	        Node node;
	        if (bit == '1') {
	            // Leaf: read next 8 bits as character
	            StringBuilder sb = new StringBuilder();
	            for (int j = 0; j < 8; j++) sb.append(preOrder[i++]);
	            char ch = (char) Integer.parseInt(sb.toString(), 2);
	            node = new LeafNode(ch, 0);
	        } else {
	            // Internal node
	            node = new Node();
	        }

	        if (root == null) {
	            root = node;
	        }

	        // Attach to parent waiting on stack
	        if (!stack.isEmpty()) {
	            Node parent = stack.peek();
	            if (parent.getLeft() == null) {
	                parent.setLeft(node);
	            } else {
	                parent.setRight(node);
	                stack.pop(); // both children filled
	            }
	        }

	        // Internal nodes wait for their children
	        if (!(node instanceof LeafNode)) {
	            stack.push(node);
	        }
	    }

	    return root;
	}
	public Node getHuffRoot() {
	    return this.huffRoot;
	}

	public String getPreOrder() {
	    return this.preOrder;
	}
}
package application;

public class Node implements Comparable<Node> {

	private Node left;
	private Node right;
	protected int freq;

	public Node() {

	}

	public Node(Node left, Node right) {
		this.left = left;
		this.right = right;
		this.freq = left.freq + right.freq;
	}

	// use to print on fill
	// use on preOrder if 0 continue not character this has extends or children's
	public String toBits() {
		if (this instanceof LeafNode) {
			char ch = ((LeafNode) this).getCharacter();
			StringBuilder sb = new StringBuilder();
			sb.append('1'); 
			for (int bit = 7; bit >= 0; bit--) {
				sb.append((ch >> bit) & 1);
			}
			return sb.toString();
		} else {
			return "0" + left.toBits() + right.toBits();
		}
	}

	@Override
	public int compareTo(Node o) {
		return Integer.compare(freq, o.freq);
	}

	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		this.right = right;
	}

	@Override
	public String toString() {
		return freq + "";
	}

}

package application;

import java.text.DecimalFormat;

public class LeafNode extends Node {

    private char character;
    private String code;

    public LeafNode(char character, int freq) {
        this.character = character;
        super.freq = freq;
    }

    public LeafNode(char character) {
        this.character = character;
        super.freq = 0;
    }

    public char getCharacter() { return character; }
    public void setCharacter(char character) { this.character = character; }

    public double getSizeBeforeKB() {
        return (8.0 * freq) / 1024.0;
    }
    public double getSizeAfterKB() {
        if (code == null) return 0;
        return (code.length() * freq) / 8.0 / 1024.0;
    }

    @Override
    public String toString() {
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        String formattedBefore = decimalFormat.format(getSizeBeforeKB());
        String formattedAfter  = decimalFormat.format(getSizeAfterKB());

        return "Character : " + this.character +
               ", Freq : " + super.freq +
               ", Size Before : " + formattedBefore + "KB" +
               ", Huf : " + code +
               ", Size After : " + formattedAfter + "KB";
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
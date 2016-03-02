package sady.utilframe.tools;

import java.util.Arrays;

public class StringTools {

	private StringTools() {}
	
	public static String lPad(Integer input, int size, char conpletationChar) {
		return lPad(input.toString(), size, conpletationChar);
	}
	public static String lPad(Long input, int size, char conpletationChar) {
		return lPad(input.toString(), size, conpletationChar);
	}

	public static String lPad(String input, int size, char conpletationChar) {
        String str = (input == null ? "" : input);
        int strLength = str.length();
        if (strLength < size) {
            char[] chars = new char[size];
            Arrays.fill(chars, 0, size - strLength, conpletationChar);
            if (strLength > 0) {
                str.getChars(0, strLength, chars, size - strLength);
            }
            return new String(chars);
        }
        return str;
    }
	
	public static String rPad(String input, int size, char conpletationChar) {
        String str = (input == null ? "" : input);
        int strLength = str.length();
        if (strLength < size) {
            char[] chars = new char[size];
            Arrays.fill(chars, strLength, size, conpletationChar);
            if (strLength > 0) {
                str.getChars(0, strLength, chars, 0);
            }
            return new String(chars);
        }
        return str;
    }
	
	public static String cut(String input, int size) {
		if (input == null) {
			return "";
		}
		if (input.length() < size) {
			return input;
		} else {
			return input.substring(0, size);
		}
	}
}

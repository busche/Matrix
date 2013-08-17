package de.ismll.utilities;

public class Tools {

	public static String[] append(String[] args, String[] strings) {
		String[] ret = new String[args.length+strings.length];
		System.arraycopy(args, 0, ret, 0, args.length);
		System.arraycopy(strings, 0, ret, args.length, strings.length);
		return ret;
	}


	public static String[] split(String input, final char splitChar){
		StringBuilder sb = new StringBuilder();
		int amount=0;

		for (int i = 0; i < input.length(); i++){
			if (input.charAt(i) == splitChar)
				amount++;
		}
		String[] ret=new String[amount+1];
		int idx=0;

		for (int i = 0; i < input.length(); i++){
			char character = input.charAt(i);
			if (character == splitChar){
				ret[idx++]=sb.toString();
				sb.setLength(0);
			} else {
				sb.append(character);
			}
		}

		if (idx <ret.length)
			ret[idx]=sb.toString();


		return ret;


	}

	public static byte[] getBytes(CharSequence s) {
		byte[] ret = new byte[s.length()];
		for (int i = 0; i < s.length(); i++)
			ret[i] = (byte) s.charAt(i);
		return ret;
	}

}

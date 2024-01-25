package com.schlevoigt.JVerein.util;

import java.util.ArrayList;

public class Misc {
	
	public static String getBuchungsZweckKorrektur(String value, boolean withRealLineBreak) {
		String result = "";
		if (value == null)
		{
		return result;
		}
		if (value.length() < 5) 
		{
			return value.replace("SVWZ+", "");
		}
		value = value.replaceAll("\r\n", "|");
		value = value.replaceAll("\r", "|");
		value = value.replaceAll("\n", "|");

		String[] zeilen = value.split("\\|");
		ArrayList<String> zeilenNeu = new ArrayList<String>();

		String currentKey = "";
		String currentLine = "";

		for (int i = 0; i < zeilen.length; i++) {
			String zeile = zeilen[i];
			if (zeile.length() > 4 && zeile.substring(4, 5).equals("+")) {
				if (currentLine.trim().length() > 0) {
					if (currentKey.trim().isEmpty()) {
						zeilenNeu.add(currentLine);
					} else {
						zeilenNeu.add(currentKey + "+" + currentLine);
					}
				}
				currentKey = zeile.substring(0, 4);
				currentLine = zeile.substring(5);
			} else {
				currentLine = currentLine + zeile;
			}
			if (currentKey.equals("SVWZ")) {
				zeilenNeu.add(currentKey + "+" + currentLine);
				currentLine = "";
			}
		}
		if (!currentLine.trim().isEmpty()) {
			if (currentKey.trim().isEmpty()) {
				zeilenNeu.add(currentLine);
			} else {
				zeilenNeu.add(currentKey + "+" + currentLine);
			}
		}

		String lineBreakAfter = null;
		String lineBreakBefore = null;
		for (int i = 0; i < zeilenNeu.size(); i++) {
			String zeile = zeilenNeu.get(i);
			if (zeile.startsWith("KREF+") || zeile.startsWith("EREF+") || zeile.startsWith("MREF+") || zeile.startsWith("CRED+")
					|| zeile.startsWith("PURP+OTHR") || zeile.startsWith("SVWZ+Datum:") || zeile.startsWith("SVWZ+BIC:") || zeile.startsWith("SVWZ+BLZ:")
					|| zeile.startsWith("SVWZ+IBAN:") || (zeile.contains("UFT ") && zeile.contains("TAN "))
					|| (zeile.contains("KD ") && zeile.contains("TAN "))) {
				continue;
			}
			if ((i == zeilenNeu.size() - 1) || (zeile.startsWith("SVWZ+"))) {
				lineBreakAfter = "";
			} else {
				lineBreakAfter = "|";
			}
			zeile = zeile.replaceAll("PURP\\+RINP ", "").replaceAll("PURP\\+ELEC ", "").replaceAll("SVWZ\\+", "");
			if (zeile.startsWith("Dauerauftrag:")) {
				lineBreakBefore = "|";
			} else {
				lineBreakBefore = "";
			}
			result = result + lineBreakBefore + zeile + lineBreakAfter;
		}

		if (result.endsWith("|")) {
			result = result.substring(1, result.length() - 1);
		}
		if (withRealLineBreak) {
			result = result.replaceAll("\\|", "\r\n");
		}

		return result;
	}

}

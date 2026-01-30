package com.secure.prints.util;

public class NameFormatUtil {

    public static String formatName(String enteredName) {
        String name = enteredName.trim().toLowerCase();
        StringBuilder formattedName = new StringBuilder();
        for(int i = 0; i < name.length(); i++) {
            String c = name.substring(i, i + 1);
            if(i == 0) {
                formattedName.append(c.toUpperCase());
            } else if(c.equals(" ") || c.equals("-")) {
                formattedName.append(c);
                i++;
                formattedName.append(name.substring(i, i + 1).toUpperCase());
            } else {
                formattedName.append(c);
            }
        }
        return formattedName.toString();
    }
}

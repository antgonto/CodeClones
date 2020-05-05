/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import enums.RegExpEnum;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Fede
 */
public class RegExpUtils {

    public static String getString(String data, RegExpEnum type) {
        Pattern pattern = Pattern.compile(type.getType());
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}

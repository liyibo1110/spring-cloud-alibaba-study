package com.github.liyibo1110.alibaba.cloud.nacos.utils;

/**
 * NacosConfig相关工具类。
 * @author liyibo
 * @date 2026-05-17 13:07
 */
public final class NacosConfigUtils {

    private NacosConfigUtils() {}

    /**
     * 将中文字符转换成Unicode。
     */
    public static String selectiveConvertUnicode(String configValue) {
        StringBuilder sb = new StringBuilder();
        char[] chars = configValue.toCharArray();
        for (char aChar : chars) {
            if (isBaseLetter(aChar))
                sb.append(aChar);
            else
                sb.append(String.format("\\u%04x", (int) aChar));
        }
        return sb.toString();
    }

    /**
     * 给定char是否为latin或空格。
     */
    public static boolean isBaseLetter(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        return ub == Character.UnicodeBlock.BASIC_LATIN || Character.isWhitespace(ch);
    }

    /**
     * 给定char是否为中文字符。
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }
}

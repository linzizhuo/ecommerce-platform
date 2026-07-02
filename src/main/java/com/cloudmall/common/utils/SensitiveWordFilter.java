package com.cloudmall.common.utils;

import java.util.*;

/**
 * 敏感词过滤器 — DFA算法（Deterministic Finite Automaton）
 *
 * 原理: 将敏感词库构建成树形状态机
 *   敏感词: ["傻子", "傻瓜"]
 *   树: {傻: {子: {isEnd:1}, 瓜: {isEnd:1}}}
 *
 * 匹配时逐字符在树中走，走到isEnd=1的节点即命中
 * 时间复杂度: O(n)，n=文本长度（不随词库大小增长）
 */
public class SensitiveWordFilter {

    // DFA根节点
    private static final Map<Character, Object> DFA_ROOT = new HashMap<>();

    // 默认敏感词库（演示用）
    private static final String[] DEFAULT_WORDS = {
        "傻子", "傻瓜", "笨蛋", "白痴", "垃圾", "骗子", "坑爹",
        "妈的", "卧槽", "操你", "傻逼", "脑残", "废物"
    };

    private static volatile boolean initialized = false;

    /** 初始化DFA树 */
    public static synchronized void init() {
        if (initialized) return;
        for (String word : DEFAULT_WORDS) {
            addWord(word);
        }
        initialized = true;
    }

    /** 添加敏感词到DFA树 */
    @SuppressWarnings("unchecked")
    private static void addWord(String word) {
        Map<Character, Object> current = DFA_ROOT;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Map<Character, Object> next = (Map<Character, Object>) current.get(c);
            if (next == null) {
                next = new HashMap<>();
                current.put(c, next);
            }
            current = next;
        }
        current.put('×', 1); // isEnd标记
    }

    /**
     * 过滤文本中的敏感词，替换为***
     * @return 过滤后的文本
     */
    @SuppressWarnings("unchecked")
    public static String filter(String text) {
        init();
        if (text == null || text.isEmpty()) return text;

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            Map<Character, Object> current = DFA_ROOT;
            int j = i;
            int matchEnd = -1;
            // 尝试匹配
            while (j < text.length()) {
                char c = text.charAt(j);
                Map<Character, Object> next = (Map<Character, Object>) current.get(c);
                if (next == null) break;
                current = next;
                j++;
                if (current.containsKey('×')) matchEnd = j; // 命中敏感词
            }
            if (matchEnd > i) {
                // 替换命中的敏感词
                for (int k = i; k < matchEnd; k++) result.append('*');
                i = matchEnd;
            } else {
                result.append(text.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    /** 检查文本是否包含敏感词 */
    @SuppressWarnings("unchecked")
    public static boolean containsSensitive(String text) {
        init();
        if (text == null) return false;
        for (int i = 0; i < text.length(); i++) {
            Map<Character, Object> current = DFA_ROOT;
            int j = i;
            while (j < text.length()) {
                char c = text.charAt(j);
                Map<Character, Object> next = (Map<Character, Object>) current.get(c);
                if (next == null) break;
                current = next;
                j++;
                if (current.containsKey('×')) return true;
            }
        }
        return false;
    }
}

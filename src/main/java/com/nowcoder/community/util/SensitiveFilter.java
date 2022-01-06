package com.nowcoder.community.util;

import com.mysql.cj.exceptions.CJCommunicationsException;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT_SENSITIVE = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    //注解是在容器实例化bean之后 在调sensitivefilter构造器后这个方法就被自动调用
    @PostConstruct
    public void init() {
        try (
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while ((keyword = reader.readLine()) != null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败" + e.getMessage());
        }


    }

    //将一个敏感词加到前缀树中去
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for(int i = 0; i < keyword.length(); i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            //指向子节点，进行下一层循环
            tempNode = subNode;

            //设置结束标识
            if(i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
    * 过滤敏感词
     * @param text 带过滤的文本
     *
     * @return 过滤后的文本
    * */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3 结尾
        int positon = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while (positon < text.length()){
            char c = text.charAt(positon);

            //跳过符号
            if(isSymbol(c)){
                //若指针1处于根节点,将此符号计入结果，让指针2向下走一步
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                positon++;
                continue;
            }
            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                begin++;
                positon = begin;
                //指针1归位
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){ //结束标识
                //发现敏感词 将begin - positon 字符串替换掉
                sb.append(REPLACEMENT_SENSITIVE);
                //进入下一个位置
                positon++;
                begin = positon;
                tempNode = rootNode;
            }else{
                //检查下一个字符
                positon++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //特殊符号 返回true
    private boolean isSymbol(Character c){
        //ox2E80 - 0x9fff 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀数
    private class TrieNode{
        //结束标志
        private boolean isKeywordEnd = false;

        //当前节点的子节点(k是下级字符，v是下级节点)
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c , TrieNode node){
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.db.DatabaseService;
import com.drowltd.dictionary.core.db.Dictionary;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author Franky
 */
public class Answers {

    private DatabaseService dictDb;
    private List<String> words;
    private List<String> answers;
    private int examWordIndex;
    private Random random = new Random();
    private String translation;

    public  Answers (Dictionary selectedDictionary){
         dictDb = DatabaseService.getInstance();
         words = dictDb.getWordsFromDictionary(selectedDictionary);         
    }
    
    public String getExamWord(Dictionary selectedDic) {
        examWordIndex = random.nextInt(words.size());
        while (dictDb.getTranslation(selectedDic, words.get(examWordIndex)).contains("גז.")) {
            examWordIndex = random.nextInt(words.size());
        }
        translation = dictDb.getTranslation(selectedDic, words.get(examWordIndex));
        return words.get(examWordIndex);
    }

    public String examWord() {
        return words.get(examWordIndex);
    }

    public void possibleAnswers() {

        answers = new ArrayList<String>();
        translation = translation.toLowerCase();

        String t = translation.replaceAll("\\b(n|a|v|(attr)|(adv)|[0-9]+)\\b\\s?", "");
        String[] s = Pattern.compile("\\s*[,|;|.|\\n]\\s*").split(t, 0);
        for (int i = 0; i < s.length; i++) {
            if (s[i].isEmpty()) {
                continue;
            }
            if (s[i].contains("(")) {
                slash(s[i].replaceAll("\\(([^()]*)\\)", ""));
                slash(s[i].replaceAll("\\(([^()]*)\\)", "$1"));
            } else {
                slash(s[i]);
            }
        }
    }

    public void slash(String s) {
        String first = new String();
        String last = new String();
        if (s.contains("/")) {
            String[] slash = s.split("/");
            for (int j = 0; j < slash.length; j++) {
                answers.add(slash[j]);
                System.out.println(slash.length);
                System.out.println(slash[j]);
                if (j != slash.length - 1) {
                    answers.add(slash[j] + " " + slash[slash.length - 1]);
                }
            }
            if ((slash[0].contains(" "))) {
                first = slash[0].substring(0, slash[0].lastIndexOf(" "));
            }
            if ((slash[slash.length - 1].contains(" "))) {
                last = slash[slash.length - 1].substring(slash[slash.length - 1].indexOf(" ") + 1);
            }
            if (!(first.isEmpty())) {
                for (int j = 0; j < slash.length; j++) {
                    answers.add(first + " " + slash[j]);
                }
            }
            if (!(last.isEmpty())) {
                for (int j = 0; j < slash.length; j++) {
                    answers.add(slash[j] + " " + last);
                }
            }
        } else {
            answers.add(s);
        }
         Set<String> set = new LinkedHashSet<String>(answers);
         answers = new ArrayList<String>(set);
    }


    public boolean isCorrect(String guess) {
        for (String i : answers) {
            if (i.equalsIgnoreCase(guess)) {
                System.out.println("Right guess");
                return true;
            }
        }
        System.out.println("Wrong guess");
        return false;
    }
}

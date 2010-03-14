/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.study;

import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.model.Dictionary;
/**
 *
 * @author Sasho
 */
public class StudyService {

    private DictionaryService dictionaryService;

    public StudyService(){
        dictionaryService = DictionaryService.getInstance();
    }

    
}

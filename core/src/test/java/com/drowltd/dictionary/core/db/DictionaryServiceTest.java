package com.drowltd.dictionary.core.db;

import com.drowltd.dictionary.core.exception.NoDictionariesAvailableException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author iivalchev
 */
public class DictionaryServiceTest extends AbstractDictionaryServiceTest {

    
    private DictionaryService dictionaryService;

    public DictionaryServiceTest() {
    }

    @Before
    public void init() throws SQLException, NoDictionariesAvailableException {
        dictionaryService = new DictionaryService(connection);
    }

    @Test
    public void testConstructorAndPopulate() {
        assertTrue("ConfigMaps doesn't match", dictionaryService.getDictConfigMap().equals(dictConfigMap));
    }

    @Test
    public void testGetLoadedDictionaries() {
        assertTrue("Dictionaries doesn't match", dictionaryService.getLoadedDictionaries().equals(new ArrayList<SDictionary>(dictConfigMap.keySet())));
    }
    
}

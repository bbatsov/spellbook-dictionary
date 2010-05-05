package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.exception.AuthenticationException;
import com.drowltd.spellbook.core.exception.UpdateServiceException;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.UpdateService;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.*;

/**
 * @author ikkari
 * Date: May 5, 2010
 * Time: 5:32:22 PM
 */
@Ignore
public class UpdateDialogTest {

    static UpdateService updateService;

    @BeforeClass
    public static void init() throws UpdateServiceException, AuthenticationException {
        DictionaryService.init("/opt/spellbook/db/spellbook.data.db");
        updateService = UpdateService.getInstance("iivalchev", "pass");
    }

    @Test
    public void testUpdate() throws InterruptedException {
        assertTrue("no updates available",updateService.checkForUpdates());
        updateService.setHandler(new UpdateDialog(new JFrame(), false));
        updateService.update();
    }

}

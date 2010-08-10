package bg.drowltd.spellbook.core.service;

import com.google.gdata.util.ServiceException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ikkari
 *         Date: Jun 3, 2010
 *         Time: 4:56:39 PM
 */
@Ignore
public class CodeHostingServiceTest {

    public CodeHostingServiceTest() {
    }

    @Test
    public void testCreateIssue() throws IOException, ServiceException {
        CodeHostingService service = CodeHostingService.getInstance();
        service.createIssue("Generated issue", "This is a test, for the feedback capability", "iivalchev");
    }

    @Test
    public void testCrateIssueFormException() throws IOException, ServiceException {
        CodeHostingService service = CodeHostingService.getInstance();
        try {
            throw new IllegalArgumentException();
        } catch (Exception e) {
            service.createIssue(e, "iivalchev");
        }
    }
}

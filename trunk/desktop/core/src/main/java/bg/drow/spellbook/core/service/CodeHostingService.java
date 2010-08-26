package bg.drow.spellbook.core.service;

import com.google.gdata.client.projecthosting.ProjectHostingService;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.data.Person;
import com.google.gdata.data.projecthosting.IssuesEntry;
import com.google.gdata.data.projecthosting.Label;
import com.google.gdata.data.projecthosting.Owner;
import com.google.gdata.data.projecthosting.Status;
import com.google.gdata.data.projecthosting.Username;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;


import java.io.*;
import java.net.MalformedURLException;

/**
 * @author ikkari
 *         Date: Jun 3, 2010
 *         Time: 11:56:19 AM
 */
public class CodeHostingService {

    private ProjectHostingClient client;
    private ProjectHostingService service;

    private static final String user = "spellbook.feedback";
    private static final String username = user + "@gmail.com";
    private static final String creditentials = "drow.ltd";
    private static final String project = "spellbook-dictionary";
    private static final String defaultOwner = "iivalchev";

    private static final CodeHostingService INSTANCE = new CodeHostingService();

    public static CodeHostingService getInstance() throws MalformedURLException, AuthenticationException {
        return INSTANCE;
    }

    private CodeHostingService() throws MalformedURLException, AuthenticationException {
        service = new ProjectHostingService(project);
        client = new ProjectHostingClient(service, project, username, creditentials);

    }

    public void createIssue(String title, String content, String owner) throws IOException, ServiceException {

        if (title == null || content == null || owner == null) {
            throw new IllegalArgumentException("title == null || content == null || owner == null");
        }

        IssuesEntry issue = new IssuesEntry();
        issue.setTitle(new HtmlTextConstruct(title));
        issue.setContent(new HtmlTextConstruct(content));

        issue.getAuthors().add(new Person(user));

        Owner owner0 = new Owner();
        owner0.setUsername(new Username(owner));
        issue.setOwner(owner0);

        issue.setStatus(new Status("New"));
        issue.addLabel(new Label("Priority-High"));

        client.insertIssue(issue);
    }

    public void createIssue(String title, String content) throws IOException, ServiceException {
        createIssue(title, content, defaultOwner);
    }


    public void createIssue(Throwable t, String owner) throws IOException, ServiceException {
        if (t == null) {
            throw new IllegalArgumentException("t == null");
        }

        if (owner == null) {
            throw new IllegalArgumentException("owner == null");
        }

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(0);
        PrintWriter writer = new PrintWriter(byteStream);
        t.printStackTrace(writer);
        writer.close();

        createIssue(t.toString(), new String(byteStream.toByteArray()), owner);
    }

    public void createIssue(Throwable t) throws IOException, ServiceException {
        createIssue(t, defaultOwner);
    }
}
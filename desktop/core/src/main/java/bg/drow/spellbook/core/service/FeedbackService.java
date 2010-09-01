package bg.drow.spellbook.core.service;

import bg.drow.spellbook.util.ValidationUtil;
import com.google.gdata.client.projecthosting.ProjectHostingService;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.data.Person;
import com.google.gdata.data.projecthosting.*;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;

/**
 * This service allows the users to leave feedback for the project in the form
 * of issues created by the service on the project's issue tracking system.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class FeedbackService {
    private ProjectHostingClient client;
    private ProjectHostingService service;

    private static final String PROJECT = "spellbook-dictionary";
    // all issues are assigned to the project leader by default
    private static final String DEFAULT_OWNER = "lordbad";

    private final String username;
    private final String password;

    public FeedbackService(final String username, final String password) {
        this.username = username;
        this.password = password;

        try {
            service = new ProjectHostingService(PROJECT);
            client = new ProjectHostingClient(service, PROJECT, username, password);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    public void createIssue(String title, String content) throws IOException, ServiceException {
        ValidationUtil.nonNull(title, "Null title");
        ValidationUtil.nonNull(content, "Null content");

        IssuesEntry issue = new IssuesEntry();
        issue.setTitle(new HtmlTextConstruct(title));
        issue.setContent(new HtmlTextConstruct(content));

        issue.getAuthors().add(new Person(username));

        Owner owner = new Owner();
        owner.setUsername(new Username(DEFAULT_OWNER));
        issue.setOwner(owner);

        issue.setStatus(new Status("New"));
        issue.addLabel(new Label("Priority-High"));

        client.insertIssue(issue);
    }

    public void createIssue(Throwable t) throws IOException, ServiceException {
        ValidationUtil.nonNull(t);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(0);
        PrintWriter writer = new PrintWriter(byteStream);
        t.printStackTrace(writer);
        writer.close();

        createIssue(t.toString(), new String(byteStream.toByteArray()));
    }
}

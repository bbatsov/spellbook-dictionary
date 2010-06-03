package com.drowltd.spellbook.core.service;

import com.google.gdata.client.projecthosting.ProjectHostingService;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.data.Person;
import com.google.gdata.data.projecthosting.*;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
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

    private static CodeHostingService INSTANCE;

    public static CodeHostingService getInstance() throws MalformedURLException, AuthenticationException {
        if (INSTANCE == null) INSTANCE = new CodeHostingService();
        return INSTANCE;
    }

    private CodeHostingService() throws MalformedURLException, AuthenticationException {
        service = new ProjectHostingService(project);
        client = new ProjectHostingClient(service, project, username, creditentials);

    }

    public void createIssue(String title, String content, String owner) throws IOException, ServiceException {

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
}

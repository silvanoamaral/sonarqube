/*
 * SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.issue.ws;

import com.google.common.io.Resources;
import java.util.Optional;
import org.sonar.api.server.ws.Change;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.issue.IssueDto;
import org.sonar.db.organization.OrganizationDto;
import org.sonar.server.issue.IssueChangelog;
import org.sonar.server.issue.IssueFinder;
import org.sonar.server.user.UserSession;
import org.sonarqube.ws.Issues.ChangelogWsResponse;

import static com.google.common.base.Preconditions.checkState;
import static org.sonar.core.util.Uuids.UUID_EXAMPLE_01;
import static org.sonar.server.ws.WsUtils.writeProtobuf;
import static org.sonarqube.ws.client.issue.IssuesWsParameters.ACTION_CHANGELOG;
import static org.sonarqube.ws.client.issue.IssuesWsParameters.PARAM_ISSUE;

public class ChangelogAction implements IssuesWsAction {

  private final DbClient dbClient;
  private final IssueFinder issueFinder;
  private final UserSession userSession;
  private final IssueChangelog issueChangelog;

  public ChangelogAction(DbClient dbClient, IssueFinder issueFinder, UserSession userSession, IssueChangelog issueChangelog) {
    this.dbClient = dbClient;
    this.issueFinder = issueFinder;
    this.userSession = userSession;
    this.issueChangelog = issueChangelog;
  }

  @Override
  public void define(WebService.NewController context) {
    WebService.NewAction action = context.createAction(ACTION_CHANGELOG)
      .setDescription("Display changelog of an issue.<br/>" +
        "Requires the 'Browse' permission on the project of the specified issue.")
      .setSince("4.1")
      .setChangelog(
        new Change("6.3", "changes on effort is expressed with the raw value in minutes (instead of the duration previously)"))
      .setHandler(this)
      .setResponseExample(Resources.getResource(IssuesWs.class, "changelog-example.json"));
    action.createParam(PARAM_ISSUE)
      .setDescription("Issue key")
      .setRequired(true)
      .setExampleValue(UUID_EXAMPLE_01);
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    try (DbSession dbSession = dbClient.openSession(false)) {
      IssueDto issue = issueFinder.getByKey(dbSession, request.mandatoryParam(PARAM_ISSUE));

      ChangelogWsResponse build = handle(dbSession, issue);
      writeProtobuf(build, request, response);
    }
  }

  public ChangelogWsResponse handle(DbSession dbSession, IssueDto issue) {
    if (!isMember(dbSession, issue)) {
      return ChangelogWsResponse.newBuilder().build();
    }

    IssueChangelog.ChangelogLoadingContext loadingContext = issueChangelog.newChangelogLoadingContext(dbSession, issue);

    ChangelogWsResponse.Builder builder = ChangelogWsResponse.newBuilder();
    issueChangelog.formatChangelog(dbSession, loadingContext)
      .forEach(builder::addChangelog);
    return builder.build();
  }

  private boolean isMember(DbSession dbSession, IssueDto issue) {
    Optional<ComponentDto> project = dbClient.componentDao().selectByUuid(dbSession, issue.getProjectUuid());
    checkState(project.isPresent(), "Cannot find the project with uuid %s from issue.id %s", issue.getProjectUuid(), issue.getId());
    Optional<OrganizationDto> organization = dbClient.organizationDao().selectByUuid(dbSession, project.get().getOrganizationUuid());
    checkState(organization.isPresent(), "Cannot find the organization with uuid %s from issue.id %s", project.get().getOrganizationUuid(), issue.getId());
    return userSession.hasMembership(organization.get());
  }
}
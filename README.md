# Gitlab Slack Notifications

This project is a small application you can run which processes gitlab webhooks and transforms them into private slack notifications.

## Configuration

### Environment Variables

| Name                  | Default              | Possible Values                  |
|-----------------------|----------------------|----------------------------------|
| `DESTINEES_RESOLVER`  | `EqualEmail`         | `EqualEmail`,`StaticId`          |
| `GITLAB_URL`          | `https://gitlab.com` | Any gitlab instance endpoint     |
| `GITLAB_TOKEN`        | n/a                  | A gitlab auth token              |
| `SLACK_TOKEN`         | n/a                  | A slack app auth token           |
| `STATIC_MAPPING_FILE` | n/a                  | File path to static mapping file |


## Set up

To get this application working it needs to receive gitlab webhooks, this can be configured in your gitlab project or as default for all projects. The webhook API route is: 
```
https://<your-domain>/gitlab/webhook
```

### Slack API

To connect to slack an application authorization token is needed. To get this token you need to create a new application from the provided [manifest](./docs/slack_app/manifest.json). Once the application is installed in your workspace you can navigate to `OAuth & Permissions` and get the token from `Bot User OAuth Token`.

## Gitlab API

To call the gitlab API an authorization token is required. This token requires `read_api` permission and access to all the projects that the webhook is configured for.

### Destinees of Notification

To resolve who will receive the notification there are two implementations:
* `EqualEmail` - Expects the email in Slack and the email in Gitlab to be the same
* `StaticId` - Expects a list of static ids provided as part of the configuration that maps gitlab ids to slack id
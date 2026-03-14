# Play Store Publish (Automated Path)

This project includes automation to upload the release AAB to Google Play.

## Requirements

1. Play Console app already created for package: `com.hardik.access`
2. Service account JSON with Play Console release permissions
3. Android SDK installed (`ANDROID_HOME` set, or `local.properties` present)

## Steps

1) Build and upload in one command:

```bash
PLAY_SERVICE_ACCOUNT_JSON=/absolute/path/to/service-account.json \
PLAY_TRACK=production \
./scripts/publish_playstore.sh
```

`PLAY_TRACK` options can be `internal`, `alpha`, `beta`, or `production`.

2) Check Play Console:
- Open your app
- Verify release in selected track
- Review content declaration and rollout settings

## Notes

- Upload is set to **Draft** release status by default in Gradle config, so you can review before full rollout.
- For first-time publishing, Play Console forms (content rating, data safety, etc.) still need to be completed once in the UI.

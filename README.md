# sdu-cwc-roundtrip-publishone PoC test
Tool for round tripping documents from CWC via PublishOne

## System User
The application uses system user to access to PublishOne.
This user should have Administrator role and should be set as administrator on the folder where documents are imported.
This allows user to edit document content while it is in state Aanmaken

Workflow has to be configured in the next way (for administrator user):
  - for round-trip document type (e.g. commentaar), workflow can go directly from state Aanmaken to Publiceren
  - for auteursbeschrijvingen document type, workflow can go directly from state Aanmaken to Publiceren

## Build & test
sbt clean test

## Sanity Check
sbt scalafmtCheck test:scalafmtCheck

## Release
sbt -DsduTeam=cwc -Dsbt.gigahorse=false release
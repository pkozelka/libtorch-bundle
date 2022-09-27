#!/bin/bash
# Upload script for Maven Central Repository
# Uses OSS Nexus as the interface to expose the artifacts. After successful end, all artifacts are prepared inside
# a newly created staging repository; the user's responsibility is to close and release it manually.

# "5ae9af53a3aeee" for net.kozelka
stagingProfileId=$1

function createStagedRepository() {
  local stagingProfileId=$1
  local description=$2
  local stagedRepositoryId
  echo "Creating staged repository for profile $stagingProfileId" >&2
  stagedRepositoryId=$($CURL -X POST $OSSRH_STAGING/profiles/${stagingProfileId}/start \
    -H Content-Type:application/xml \
    --data "
<promoteRequest>
    <data>
        <description>$description</description>
    </data>
</promoteRequest>" | sed -n '/<stagedRepositoryId>/{s:stagedRepositoryId::g;s:[[:space:]<>/]::g;p;}')
  echo "Created staging repository: $stagedRepositoryId" >&2
  echo "$stagedRepositoryId"
  test -n "$stagedRepositoryId"
}

function finishStagedRepository() {
  local stagingProfileId=${1}
  local stagedRepositoryId=${2}
  echo "Closing staged repository '$stagedRepositoryId' for profile $stagingProfileId" >&2
  $CURL -X POST $OSSRH_STAGING/profiles/${stagingProfileId}/finish \
    -H Content-Type:application/xml \
    --data "
<promoteRequest>
    <data>
        <stagedRepositoryId>${stagedRepositoryId}</stagedRepositoryId>
    </data>
</promoteRequest>"
}

# Upload current directory which must have the structure of maven repo into Nexus
function uploadMavenRepository() {
  local stagedRepositoryId=$1

  for G in $(find * -name '*.pom' -printf '%h\n'); do
    echo "Signing files in $G"
    # sign all files that need to be signed
    rm -f $G/*.asc
    /bin/ls -1 $G/* | grep -v '\.md5$\|\.sha1$' | xargs -L 1 gpg -ab

    # upload everything in the directory
    FILES=$(find $G -type f -printf ',%f')
    FILES="{${FILES:1}}"

    local url
    if [ -z "$stagedRepositoryId" ]; then
      url=$OSSRH_STAGING/deploy/maven2/$G/
    else
      url=$OSSRH_STAGING/deployByRepositoryId/$stagedRepositoryId/$G/
    fi
    echo "Uploading files to $url : $FILES"
    $CURL "$url" --upload-file "$G/$FILES" || return 1
  done
}

function publishRelease() {
  if [ -n "$stagingProfileId" ]; then
    stagedRepositoryId=$(createStagedRepository "$stagingProfileId" "$PWD")
    [ -z "$stagedRepositoryId" ] && return 1
  fi

  uploadMavenRepository "$stagedRepositoryId" || return 1

  if [ -n "$stagedRepositoryId" ]; then
    finishStagedRepository "$stagingProfileId" "$stagedRepositoryId" || return 1
  fi
}

#### MAIN ####

if [ -z "${OSSRH_AUTH}" ]; then
  echo "ERROR: Missing OSSRH_AUTH" >&2
  exit 1
fi

OSSRH_STAGING="https://oss.sonatype.org/service/local/staging"
CURL="curl -u $OSSRH_AUTH --fail"

cd target/release-package/maven-repository || exit 1
publishRelease


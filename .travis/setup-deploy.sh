#!/bin/bash

if [ "$TRAVIS_BRANCH" != 'master' ] || [ "$TRAVIS_PULL_REQUEST" == 'true' ]; then
    echo "Skipping env deployment setup for a non-release build"
    exit 0
fi

echo "Setting up env for deployment"

if [ ! -z "$GPG_SECRETKEY" ]; then
    echo $GPG_SECRETKEY | base64 --decode | openssl aes-256-cbc -K $encrypted_16b59196413c_key -iv $encrypted_16b59196413c_iv -d | $GPG_EXECUTABLE --import
fi

if [ ! -z "$GPG_OWNERTRUST" ]; then
    echo $GPG_OWNERTRUST | base64 --decode | $GPG_EXECUTABLE --import-ownertrust
fi

if [ ! -z "$GPG_OWNERTRUST" ]; then
    cat > ${HOME}/.m2/settings-security.xml << EOM
<settingsSecurity>
    <master>${MAVEN_MASTER}</master>
</settingsSecurity>
EOM
    echo "Maven security settings setup"
fi

cp ./.travis/settings.xml ${HOME}/.m2/settings.xml
echo "Maven settings setup"

echo "Environment setup for signing deployments"
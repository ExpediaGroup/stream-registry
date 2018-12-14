#!/bin/bash

echo "Setting up env for deployment"

if [ "$TRAVIS_BRANCH" != 'master' ] || [ "$TRAVIS_PULL_REQUEST" == 'true' ]; then
    echo "Skipping artifact deployment for a non-release build"
    exit 0
fi

if [ ! -z "$GPG_SECRET_KEYS" ]; then
    echo $GPG_SECRET_KEY | base64 --decode | openssl aes-256-cbc -K $encrypted_499ce72cfe97_key -iv $encrypted_499ce72cfe97_iv -d | $GPG_EXECUTABLE --import
fi

if [ ! -z "$GPG_OWNERTRUST" ]; then
     echo $GPG_OWNERTRUST | base64 --decode | openssl aes-256-cbc -K $encrypted_499ce72cfe97_key -iv $encrypted_499ce72cfe97_iv -d | base64 --decode | $GPG_EXECUTABLE --import-ownertrust
fi
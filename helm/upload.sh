#!/bin/bash
set -e

# release version
if [[ "${GITHUB_BASE_REF}" == "" ]] && [[ "${GITHUB_REF}" == "refs/tags"* ]]; then
  cd helm-charts
  git add .
  git remote rm origin
  git remote add origin https://phertweck:$HELM_PUSH_KEY@github.com/FraunhoferIOSB/helm-charts
  git commit -m "Travis build ${GITHUB_RUN_NUMBER} pushed"
  git push origin master -fq
  cd ../
  rm -rf ./helm-charts
  echo "Helm chart pushed to release repository"
fi

# Only deploy master branch and tagged builds to snapshot repository
if [[ "${GITHUB_BASE_REF}" == "" ]] && ([[ "${GITHUB_REF}" == "refs/heads/master" ]] || [[ "${GITHUB_REF}" == "refs/tags"* ]]); then
    cd helm-charts-snapshot
    git add .
    git remote rm origin
    git remote add origin https://phertweck:$HELM_PUSH_KEY@github.com/FraunhoferIOSB/helm-charts-snapshot
    git commit -m "Travis build ${GITHUB_RUN_NUMBER} pushed"
    git push origin master -fq
    cd ../
    rm -rf ./helm-charts
    echo "Helm chart pushed to snapshot repository"
fi



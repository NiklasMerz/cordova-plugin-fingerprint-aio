workflow "Build version and publish latest on NPM" {
  on = "push"
  resolves = ["npm version"]
}

action "npm version" {
  uses = "actions/npm@6309cd9"
  runs = "npm version prerelease"
}

action "npm publish" {
  needs = "npm version"
  uses = "actions/npm@master"
  args = "publish --tag $(echo $GITHUB_REF| cut -d'/' -f 3)"
  secrets = ["NPM_AUTH_TOKEN"]
}

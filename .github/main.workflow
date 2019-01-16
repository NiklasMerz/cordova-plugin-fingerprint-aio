workflow "Publish version tag on NPM" {
  on = "push"
  resolves = ["npm publish"]
}

action "filter tags" {
  uses = "actions/bin/filter@e96fd9a"
  args = "tag v*"
}

action "npm publish" {
  needs = "filter tags"
  uses = "actions/npm@master"
  args = "publish"
  secrets = ["NPM_AUTH_TOKEN"]
}

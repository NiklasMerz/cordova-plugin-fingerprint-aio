workflow "Build version on NPM" {
  on = "push"
  resolves = ["GitHub Action for npm"]
}

action "GitHub Action for npm" {
  uses = "actions/npm@6309cd9"
  runs = "npm version prerelease"
}

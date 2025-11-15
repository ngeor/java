## [unreleased]

### 🚀 Features

- Add script to test if a directory is a Maven project
- Support filtering pom.xml for some interesting symbols
- Support custom pom search strings
- Support negating matches
- Filter for multi-module projects
- Detect repos without a git remote
- Fetching repos with tags

### 🎨 Styling

- Styling changelog according to default options

### ⚙️ Miscellaneous Tasks

- Updated gitignore
- Updated editorconfig
- Removed .gitattributes
- Removed renovate.json
## [4.10.0] - 2025-11-15

### 🚀 Features

- Added script to migrate my projects from the old Sonatype publishing to the new
- Added script to update secrets in repo
- Add script to kick off the release locally

### 🐛 Bug Fixes

- Reduce output of `release:clean`

### ⚙️ Miscellaneous Tasks

- Update gitignore
- Upgraded dependencies
## [4.9.9] - 2025-09-24

### 🚀 Features

- [**breaking**] Support custom location for `keys.asc` and `pom.xml`
## [4.9.7] - 2025-09-24

### 🐛 Bug Fixes

- Use MAVEN_GPG_PASSPHRASE env variable
## [4.9.6] - 2025-09-24

### 🚀 Features

- Making release output quieter
## [4.9.5] - 2025-09-24

### 🐛 Bug Fixes

- Switch to PIPE
## [4.9.4] - 2025-09-24

### 🚜 Refactor

- Move all release logic into release.py

### ⚙️ Miscellaneous Tasks

- Added Pipfile for release.py
## [4.9.3] - 2025-09-24

### ⚙️ Miscellaneous Tasks

- Updated changelog
- Migrate from OSSRH to Central Maven
## [4.9.2] - 2025-02-24

### 🐛 Bug Fixes

- Adding back dependency management for 3 libraries
## [4.9.1] - 2025-02-17

### ⚙️ Miscellaneous Tasks

- Moved java back to its own repo
## [4.2.0] - 2024-01-26

### 💼 Other

- Updated dependency versions

### ⚙️ Miscellaneous Tasks

- Updated readme
## [4.1.1] - 2022-12-08

### ⚙️ Miscellaneous Tasks

- Fix deployment
## [4.1.0] - 2022-12-08

### 🚀 Features

- Added sortpom
## [4.0.0] - 2022-12-08

### ⚙️ Miscellaneous Tasks

- [**breaking**] Upgraded to Java 17
- [**breaking**] Removed dependency management and most plugins
- Updated CI for Java 17
## [3.3.0] - 2022-09-07

### 🚀 Features

- *(ci)* Use tag based release workflow

### ⚙️ Miscellaneous Tasks

- *(changelog)* Update changelog for 3.2.0
## [3.2.0] - 2022-06-12

### ⚙️ Miscellaneous Tasks

- *(changelog)* Update changelog for 3.1.1
- *(changelog)* Updated changelog
- *(changelog)* Updated changelog
- *(changelog)* Updated changelog
- Group dependencies separately in changelog
- *(changelog)* Updated changelog
- *(changelog)* Updated changelog
## [3.1.1] - 2022-02-05

### 🐛 Bug Fixes

- Generate changelog during release (#18)

### ⚙️ Miscellaneous Tasks

- *(changelog)* Update changelog for 3.1.0
## [3.1.0] - 2022-02-05

### 🚀 Features

- Install and use git-cliff during release GitHub Action

### ⚙️ Miscellaneous Tasks

- *(changelog)* Updated changelog
- *(changelog)* Updated changelog
## [3.0.0] - 2022-02-02

### 🚀 Features

- Using properties to manage dependency and plugin versions
- [**breaking**] Upgrade checkstyle to 9.2.1

### ⚙️ Miscellaneous Tasks

- *(changelog)* Updated changelog
## [2.4.0] - 2022-01-27

### 🚀 Features

- Support finalizing release

### 🐛 Bug Fixes

- Make release script executable upon copying to other repo
## [2.3.0] - 2022-01-26

### 🚀 Features

- Adding script to install workflows and release script in other repos
- Support initialization step in release script

### ⚙️ Miscellaneous Tasks

- Remove obsolete release files
- *(changelog)* Updated changelog
- Excluding changelog commits from changelog
- *(changelog)* Updated changelog
- Removed unused workflow
- Removed duplicate badge
## [2.2.1] - 2022-01-26

### 🚀 Features

- Adding a Python script for releasing
- Performing the release with the Python script

### ⚙️ Miscellaneous Tasks

- Update readme
## [2.2.0] - 2022-01-23

### 🚀 Features

- Testing release branch
- Performing release
## [1.0.0] - 2021-06-26

### 💼 Other

- Registering new module in parent pom
- Added picocli
- Added list command

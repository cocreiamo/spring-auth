# Changelog

All notable changes to this project are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows
[Semantic Versioning](https://semver.org/) from v1.0 onward.

## [Unreleased]

### Added

- Repository predisposition: building Maven skeleton, namespace (`com.cocreiamo.auth` /
  groupId `com.cocreiamo` / JitPack `com.github.cocreiamo.spring-auth`, ADR-0001), build
  harness (CI, CodeQL, Dependabot), the v1 design doc (`docs/DESIGN.md`), and the placeholder
  `SpringAuthAutoConfiguration` that boots the context.
- ADR-0001 (accepted): namespace and distribution.
- ADR-0002 (proposed, open): scaffold generator delivery mechanism.
- ADR-0003 (proposed, open): default template engine for the MVC scaffold.

### Not yet implemented

- The auth flows themselves (Epics A, B, C from `docs/DESIGN.md`). Tracked as GitHub issues.

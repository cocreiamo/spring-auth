# Support

## Where to get help

| You want to | Go to |
|---|---|
| Report a bug or unexpected behaviour | [Issues](https://github.com/cocreiamo/spring-auth/issues/new/choose) |
| Suggest a feature, flow, or config surface | [Issues](https://github.com/cocreiamo/spring-auth/issues/new/choose) |
| Ask whether the library fits your auth scenario | [Discussions](https://github.com/cocreiamo/spring-auth/discussions) |
| Report a vulnerability | [Private security advisory](https://github.com/cocreiamo/spring-auth/security/advisories/new). Do NOT open a public issue. See [SECURITY.md](SECURITY.md). |
| Commercial support, integration help, custom SPI adapters | francesco@iambilotta.com |

## Response times

This is an open-source project maintained by one engineer in Europe. SLAs are best-effort:

- **Security advisories**: triaged within 5 working days.
- **Bugs with a clean reproducer**: triaged within 7 days.
- **Feature requests**: triaged when the maintainer has bandwidth, no fixed window.
- **Discussions**: best-effort, usually within a week.

## Before opening an issue

- Verify the behaviour against the latest released version (note: the repo is currently
  predisposed; many flows are not implemented yet, see the [Roadmap](README.md#roadmap)).
- Check [existing issues](https://github.com/cocreiamo/spring-auth/issues?q=) for duplicates.
- Read the [README](README.md), the [design doc](docs/DESIGN.md), and the [ADRs](docs/adr/).
- Strip secrets from your reproducer.

## Versioning and breaking changes

The project follows [Semantic Versioning](https://semver.org/) from v1.0 onward. The `0.x`
series predates the API freeze; do not assume API stability if you are pinned to a 0.x.

## Out of scope

The maintainer will not:

- Build adapters for non-Spring frameworks as a free request.
- Backport fixes to discontinued versions or to Spring Boot 3.x / Spring Security 6.x.

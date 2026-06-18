---
name: Bug report
about: A reproducible defect in the library
labels: bug
---

## What happened

(One paragraph. Concrete, observable behavior.)

## What you expected to happen

(One paragraph. The contract you thought you had.)

## Reproduction

```java
// minimal code that triggers the bug (config class, SPI impl, controller)
```

```yaml
# minimal application.yml: the spring-auth.* and spring.security.* config involved
```

## Environment

- spring-auth version: x.y.z
- Spring Boot: x.y.z
- Spring Security: x.y.z
- JDK: vendor + version
- OS: linux/macos/windows + version

## Logs / stack trace

(WARN/ERROR lines. Redact any credentials, tokens, or personal data.)

## Have you tried

- [ ] Searched existing issues
- [ ] Reproduced on the latest released version
- [ ] Confirmed it is not a misconfiguration documented in the README

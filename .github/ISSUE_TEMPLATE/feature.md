---
name: Feature request
about: A new capability, flow, or config surface
labels: enhancement
---

## Use case

(What you are trying to do. Real, not hypothetical. What is the Laravel cognate, if any?)

## Proposed shape

(Property? SPI interface? Auto-config bean? Scaffold template? Sketch the API surface, even if rough.)

## Considered alternatives

(What you have already tried with raw Spring Security and why it did not fit.)

## Open questions

- ?
- ?

## Principle check

- [ ] This configures Spring Security, it does NOT reimplement it.
- [ ] This does NOT assume an ORM (it goes through an SPI with a default impl).
- [ ] This is in v1 scope (not a deferred feature: API tokens, OAuth2 social, 2FA, passkeys, multi-tenant).

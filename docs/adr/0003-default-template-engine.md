# ADR-0003: Default template engine for the MVC scaffold

- **Status:** accepted
- **Date:** 2026-06-18
- **Deciders:** Francesco Bilotta

## Context

The scaffold generator (Epic C) emits the auth + account-settings screens as plain Spring MVC
plus a server-side template engine, so spring-auth is usable with zero extra deps beyond a
view layer. The default engine is a soft one-way door: it sets the syntax of the generated
templates, so switching it later means re-emitting (or hand-porting) every generated view.
It is reversible per-project via the planned `--ui` generator flag, but the DEFAULT is a
project-level commitment that shapes the docs, the example app, and the first impression.

Two candidates are on the table (DESIGN doc section 3.5 decision 3).

## Decision

**The engine is agnostic at the scaffold layer; the DEFAULT for now is `--ui=lievit` (lievit
over JTE).** Three points:

1. **The backend is 100% template-agnostic.** The auto-config starter (Epics A + B) ships no
   views at all, exactly like Fortify. The template choice touches ONLY the scaffold output,
   so an adopter who wants just the configured Spring Security backend pulls zero view deps.
2. **The scaffold goal (`-Dui=` / `--ui`) ships one template SET per engine**, the adopter
   picks: `lievit` (reactive, lievit components rendered on JTE, the Livewire-feel tier),
   `jte` (plain JTE forms, dependency-free, the Blade-POST analog), `thymeleaf` (widest Spring
   reach). Agnosticism is achieved by shipping multiple sets, not by forcing one into the core.
3. **Default = `lievit` (+ JTE substrate)** to give the Livewire-equivalent DX out of the box
   and to keep the cocreiamo stack on one engine (lievit + housetree `gest` render in JTE).
   Thymeleaf stays the broad-reach alternative set; `jte` plain is the zero-dependency set.
   The `lievit` set carries zero compile dependency from the starter (DESIGN section 3.2(b)).

This supersedes the earlier Thymeleaf-vs-JTE framing: the answer is "both, plus lievit, behind
`--ui`", with lievit+JTE as the shipped default.

## Options

### Thymeleaf
- **For:** by far the widest reach in the Spring MVC world; the default most Spring tutorials
  and Spring Initializr assume; natural-templating (views open in a browser as static HTML);
  the lowest-surprise choice for an external adopter who is the target user.
- **Against:** not the engine the cocreiamo / housetree family standardised on; one more
  templating dialect for the maintainer to keep current.

### JTE
- **For:** family coherence: lievit and the housetree `gest` app render in JTE; type-safe,
  compiled templates; aligns the cocreiamo stack on one engine, which matters when the
  optional lievit-backed scaffold (`--ui=lievit`) is the reactive tier.
- **Against:** a thinner training set / smaller community than Thymeleaf; a less familiar
  on-ramp for the broad Spring audience spring-auth wants to attract; needs the JTE build
  plugin wired in the generated project.

## Consequences

Whichever is the default, the other stays available behind `--ui`, and the lievit-backed set
(`--ui=lievit`, zero compile dependency from the starter, DESIGN section 3.2(b)) is orthogonal
to this choice. The default mainly decides what the README quickstart and the sample app show.
Tension to weigh: Thymeleaf maximises external reach (the adopter-first lens), JTE maximises
family coherence (one engine across cocreiamo). The right answer depends on whether the
primary audience for v1 is external Spring developers or the internal family stack.

## Alternatives considered

- **Mustache / Freemarker / Groovy templates.** Not considered seriously: neither maximises
  reach (Thymeleaf wins) nor family coherence (JTE wins), so they lose on both axes.
- **No bundled default (force the adopter to choose at generate time).** Rejected for the
  default-experience story: `laravel new --livewire` ships a working default; an opinionated
  default with an escape hatch beats a mandatory upfront choice.

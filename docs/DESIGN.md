# spring-auth: bringing Laravel's auth DX to Spring (design proposal v1)

Status: study + design proposal. No repo created, no code written, no namespace chosen.
Org reserved: `cocreiamo` (repo `git@github.com:cocreiamo/spring-auth.git`, currently empty).

## 0. What we studied

Scaffolded the official `laravel/livewire-starter-kit` with:

```
laravel new authstudy --livewire --no-interaction
```

(ran inside `/tmp/laravel-auth-study`; the installer emitted JSON `{"success":true,...}` and
produced `/tmp/laravel-auth-study/authstudy`). `php artisan migrate` was NOT run (the machine
lacks `pdo_sqlite`); irrelevant, the scaffolded auth code is DB-independent.

The kit shipped is newer than the "Volt component with `login()` method" generation. It is:
Laravel 13 + Livewire 4 + Flux 2 (UI) + **Laravel Fortify** (the headless auth backend) + passkeys + 2FA.
This matters for the mapping: the auth backend is now a **separate package (Fortify)** with a
**pipeline-based login** and **pluggable action classes**, and the starter kit is mostly
**views + Fortify wiring + action stubs**. That is a clean two-layer split that maps almost 1:1
onto the spring-auth design (auto-config layer + scaffold layer).

## 1. The Laravel auth surface the kit generates (inventory)

### Two-layer architecture
- **Fortify** = the headless engine (routes, controllers, login pipeline, password broker,
  email verification, password confirmation, 2FA, passkeys). Ships no views.
- **Starter kit** = the scaffold: Blade/Livewire views + `FortifyServiceProvider` wiring +
  overridable **action classes** (`CreateNewUser`, `ResetUserPassword`) + reusable validation
  traits (`PasswordValidationRules`, `ProfileValidationRules`) copied INTO the app.

### Auth UI (views in `resources/views/pages/auth/`, folder-routed)
login, register, forgot-password, reset-password, verify-email, confirm-password,
two-factor-challenge. They are **plain server-rendered Blade forms** that POST to Fortify's
named routes (`login.store`, `register.store`, `password.email`, `password.update`,
`verification.send`, `password.confirm.store`). The **settings** pages
(`profile`, `security`, `appearance`, 2FA setup, recovery codes, delete-account) ARE Livewire 4
single-file components (`⚡`-prefixed) with reactive fields + validation. So: auth flows are
classic POST forms, account-management is reactive Livewire. Login form excerpt:

```blade
<form method="POST" action="{{ route('login.store') }}">
    @csrf
    <flux:input name="email" type="email" required autofocus autocomplete="email" />
    <flux:input name="password" type="password" required autocomplete="current-password" viewable />
    <flux:checkbox name="remember" :label="__('Remember me')" />
    <flux:button variant="primary" type="submit">{{ __('Log in') }}</flux:button>
</form>
```

Register POSTs `name/email/password/password_confirmation` to `register.store`; passwords carry
`Password::defaults()->toPasswordRulesString()` for the browser password manager.

### Routes + middleware (Fortify-registered, all under `web`)
- `guest` on: `login`, `login.store` (+`throttle:login`), `register(.store)`, `password.request`,
  `password.email`, `password.reset`, `password.update`, `two-factor.login(.store)` (+`throttle:two-factor`).
- `auth` on: `logout`, `verification.notice/send/verify` (verify also `signed` + `throttle`),
  `user-profile-information.update`, `user-password.update`, `password.confirm(.store)`,
  the 2FA management + passkey management routes.
- App routes guard with `['auth','verified']` (dashboard) and `password.confirm` on the
  sensitive `settings/security` page.

### Login pipeline (Fortify `AuthenticatedSessionController::store`)
`EnsureLoginIsNotThrottled` (skipped if a named limiter is set) -> `CanonicalizeUsername`
(lowercase) -> `RedirectsIfTwoFactorAuthenticatable` -> **`AttemptToAuthenticate`** (calls
`StatefulGuard::attempt(credentials, remember)`; on success the guard logs the user in) ->
**`PrepareAuthenticatedSession`** (`$request->session()->regenerate()` = session-fixation defence).

### Logout (`AuthenticatedSessionController::destroy` + the kit's `Logout` action)
`guard->logout()` -> `session()->invalidate()` -> `session()->regenerateToken()` -> redirect `/`.

### config/auth.php = the guards & providers surface
- `guards.web = { driver: session, provider: users }`
- `providers.users = { driver: eloquent, model: User }` (commented `database` driver alternative)
- `passwords.users = { table: password_reset_tokens, expire: 60, throttle: 60 }`
- `password_timeout: 10800` (3h, the password.confirm window)

### User model
Extends `Authenticatable`; `password => 'hashed'` cast (auto-bcrypt via the configured hasher);
`remember_token` column; implements `PasskeyUser`; uses `TwoFactorAuthenticatable` +
`PasskeyAuthenticatable`. `MustVerifyEmail` is the commented opt-in for verification gating.

### Throttling / rate limiting
Named limiters in `FortifyServiceProvider`: `login` keyed by `lower(email)|ip` at 5/min;
`two-factor` keyed by session login id at 5/min; `passkeys` keyed by credential/session + ip at 10/min.

### Password reset flow
forgot-password form -> `password.email` -> broker emails a tokenized signed link ->
`password.reset/{token}` form -> `password.update` -> Fortify runs the app's `ResetUserPassword`
action (validates `Password::default()` + `confirmed`, force-fills the hashed password). Tokens
live in `password_reset_tokens`, expire 60 min, throttle 60s.

### Email verification flow
`MustVerifyEmail` user -> `verified` middleware blocks unverified -> `verification.notice` page ->
`verification.send` re-sends -> `verification.verify/{id}/{hash}` is a `signed` + throttled link
that marks the email verified.

### Password confirmation ("sudo mode")
`password.confirm` middleware guards sensitive routes; if the last confirmation is older than
`password_timeout` (3h) it forces the confirm-password screen before proceeding.

### Account management scaffolding (settings/*)
profile update (`user-profile-information.update`), password update (`user-password.update`,
validates `current_password`), 2FA enable/confirm/disable + QR + recovery codes, passkey
register/delete, appearance, delete-account (password-confirmed). 2FA + passkeys are Fortify
`Features`, toggled in `config/fortify.php`.

## 2. Laravel -> Spring Security mapping (API names verified vs Spring Security 7.0 reference)

| Laravel concept | Spring Security mechanism (verified) |
|---|---|
| `guard` (`web`, session driver) | a `SecurityFilterChain` bean built from `HttpSecurity`, stateful (`HttpSessionSecurityContextRepository`) |
| `provider` (eloquent/database) | `UserDetailsService` (+ `DaoAuthenticationProvider`); the eloquent/database split = a JDBC vs JPA vs custom `UserDetailsService` impl |
| `Auth::attempt($creds, $remember)` | `AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)` (the `UsernamePasswordAuthenticationFilter` drives it) |
| `Hash` / `password => 'hashed'` cast | `PasswordEncoder`, specifically `DelegatingPasswordEncoder` (`PasswordEncoderFactories.createDelegatingPasswordEncoder()`, bcrypt default) |
| `AttemptToAuthenticate` pipe | `UsernamePasswordAuthenticationFilter` -> provider -> `UserDetailsService.loadUserByUsername` + `PasswordEncoder.matches` |
| `remember` checkbox + `remember_token` | `formLogin` + `rememberMe(...)`; persistent variant `PersistentTokenBasedRememberMeServices(key, uds, PersistentTokenRepository)` with `JdbcTokenRepositoryImpl` (table `persistent_logins`) |
| `throttle:login` (5/min by email\|ip) | NOT built into Spring Security as a login throttle; needs a custom filter / Bucket4j / Resilience4j, or an `AuthenticationFailureHandler` counting failures. Honest gap, see v1 scope. |
| `session()->regenerate()` on login | `sessionManagement().sessionFixation().changeSessionId()` (the Spring Security DEFAULT) |
| `session()->invalidate()` + token on logout | `logout().invalidateHttpSession(true).deleteCookies("JSESSIONID")` (+ CSRF token rotates with the new session) |
| `@csrf` / CSRF middleware | Spring Security CSRF on by default; `CsrfToken` via `HttpSessionCsrfTokenRepository` (or `CookieCsrfTokenRepository` for JS) |
| password reset (token table, signed link, broker) | a token-based flow we ship: a `PasswordResetToken` store (JDBC), a one-time signed token, a mail sender; Spring Security has NO built-in broker, spring-auth provides it |
| email verification (`signed` + `throttle` link) | same: a signed-link flow spring-auth ships; not in core Spring Security |
| `password.confirm` middleware (3h sudo window) | a custom filter / `AuthorizationManager` checking a "recently re-authenticated at" timestamp in the session; spring-auth ships it (no core equivalent) |
| `@middleware('auth')` / `verified` | `authorizeHttpRequests(a -> a.requestMatchers(...).authenticated())`; `verified` = a custom `AuthorizationManager` / filter on an `email_verified_at` claim |
| `@middleware('guest')` | redirect-if-authenticated handler on the login/register chain (no single core annotation; an entry-point/handler) |
| Fortify action classes (`CreateNewUser`, ...) | strategy beans behind interfaces (an `@FunctionalInterface` SPI the dev overrides), spring-auth's analog |
| Fortify `Features` toggles | Spring Boot `@ConditionalOnProperty` auto-config switches (`spring-auth.features.*`) |
| 2FA / passkeys (Fortify features) | Spring Security has WebAuthn (`webAuthn(...)`) + one-time-token; 2FA TOTP is custom. Deferred in v1. |

Key correctness notes: `changeSessionId()` is the documented default session-fixation strategy
(so spring-auth gets Laravel's regenerate-on-login for free, just don't disable it); persistent
remember-me needs the `persistent_logins` table; login throttling is genuinely NOT in core
Spring Security and is the one place Laravel's Fortify ships more than the framework.

## 3. spring-auth v1 design

### 3.1 DX target
A Spring Boot dev adds one starter, runs one generator, and gets a working
login / register / forgot / reset / verify / logout flow with sane Spring Security defaults,
the same "it just works, now customize the views and the user provider" feeling as
`laravel new --livewire`. Crucially spring-auth sits ON TOP of Spring Security and never
reimplements it: it auto-configures a `SecurityFilterChain` and ships flows Spring Security
lacks (reset, verify, sudo-mode, login throttle), all overridable; the dev can always drop to
raw `HttpSecurity`.

### 3.2 Two halves, exactly like Fortify + starter kit
**(a) Auto-configuring starter (`spring-auth-starter`)** = the Fortify analog (headless, no views):
- An `@AutoConfiguration` that registers a `SecurityFilterChain` with: form login, session
  (stateful), CSRF on, `DelegatingPasswordEncoder`, session-fixation `changeSessionId()`,
  logout that invalidates the session, optional `rememberMe`.
- All beans `@ConditionalOnMissingBean` so the dev overrides any single piece.
- Feature flags via `@ConditionalOnProperty` (`spring-auth.features.registration`,
  `.password-reset`, `.email-verification`, `.remember-me`, `.password-confirm`).
- The flows Spring Security lacks, shipped as services + controllers behind interfaces:
  password reset (token store + mailer + signed token), email verification (signed link),
  password-confirm sudo window, and a pluggable login rate limiter (default in-memory bucket,
  the explicit "this is the part core Spring Security doesn't give you").
- A **user provider SPI**: the dev implements a small `UserDetailsService`-shaped interface (or
  points at a table) and registration/reset write through an overridable
  `UserRegistrar` / `PasswordResetter` strategy bean (the `CreateNewUser` / `ResetUserPassword` analog).

**(b) Scaffold generator** = the starter-kit analog (writes code INTO the user's app):
- Generates the controllers (or thin route config) + templates for the seven auth screens +
  the account-settings screens (profile, password, delete) + the wiring class that overrides
  the SPI beans, dropped into the user's `src/main`. The dev owns and edits them afterwards,
  exactly like the copied Blade views and action classes.
- Default render target: **plain Spring MVC + a template engine** (Thymeleaf or JTE; pick one
  default, support both) so spring-auth is usable with zero extra deps beyond a view layer.
- **lievit composition (optional, the Flux/Livewire-feel half)**: there is a sibling OSS
  **lievit** ("Livewire for Spring": reactive server-rendered components with `l:model`,
  `l:error`, server-side validation). When lievit is on the classpath, the generator can emit
  the login/register/reset forms as **lievit components** so the scaffold gets the same reactive
  live-validation feel the Livewire kit has (inline field errors, no full reload), matching
  Laravel's two-tier split (classic forms for auth, reactive components for settings). Kept
  strictly optional: a `--ui=mvc|lievit` generator flag (default `mvc`); spring-auth-starter has
  NO compile dependency on lievit. This is the only place the two projects touch, and it is a
  template-selection seam, not a code dependency.

### 3.3 The "guards & providers" analog for Spring (config surface without hiding Spring Security)
A small typed config (`spring-auth.*`) that maps onto Spring Security building blocks 1:1, never
replacing them:
- `spring-auth.guard` -> which `SecurityFilterChain` / `securityMatcher` the defaults apply to
  (default: the catch-all chain). Multiple guards = multiple chains, the documented multi-chain pattern.
- `spring-auth.provider` -> selects the `UserDetailsService` strategy: `jdbc` (a table + columns),
  `jpa` (an entity), or `custom` (the dev's bean wins via `@ConditionalOnMissingBean`).
- `spring-auth.password-reset.token-table`, `.expiry`, `.throttle` -> the reset flow (Laravel's
  `passwords.users`).
- `spring-auth.password-confirm.timeout` -> the sudo window (`password_timeout`).
- `spring-auth.remember-me.persistent` -> toggles `PersistentTokenBasedRememberMeServices`.
Escape hatch is first-class and documented: define your own `SecurityFilterChain` bean and the
auto-config backs off entirely (`@ConditionalOnMissingBean(SecurityFilterChain.class)`). The
config is a convenience over `HttpSecurity`, never a wall in front of it.

### 3.4 v1 scope vs deferred
**Ships in v1**
- Form login (username/email + password), `DelegatingPasswordEncoder`, session + CSRF defaults.
- User provider SPI (jdbc + custom; jpa optional).
- Registration (overridable `UserRegistrar`).
- Password reset (token store + mailer + signed token).
- Email verification (signed link, `verified`-style gate).
- Remember-me (persistent token, `persistent_logins`).
- Password-confirm sudo window.
- Login rate limiting (the explicit value-add over core Spring Security; pluggable, in-memory default).
- Logout (session invalidate + cookie clear).
- The scaffold generator (MVC default, lievit optional).

**Deferred (named so, with the Laravel cognate)**
- API tokens à la Sanctum/Passport (stateless bearer / personal access tokens, OAuth2 auth server).
- OAuth2 / OIDC login + social login (Spring already has `oauth2Login`; spring-auth would only add scaffold).
- 2FA / TOTP + recovery codes (Fortify `twoFactorAuthentication`).
- Passkeys / WebAuthn (Spring Security has `webAuthn`; scaffold-only later).
- Multi-tenant / multiple user models.

### 3.5 One-way-door decisions to FLAG to the human (NOT decided here)
1. **Maven groupId + Java package namespace.** Org is `cocreiamo`; sibling lievit uses package
   `io.lievit.*` + groupId `io.github.lievit`. Options on the table (do not pick): `io.cocreiamo.*`,
   `io.github.cocreiamo.*`, or a shared family root. This is a one-way door (rename = breaking),
   reserved for the human; spring-auth + lievit family coherence is the open question.
2. **Scaffold delivery mechanism.** Three candidates, each a one-way door for users' muscle memory:
   (a) a **Maven/Gradle plugin goal** (`mvn spring-auth:scaffold`, stays in-build, no extra install);
   (b) a **standalone CLI** (`spring-auth init`, best DX, an extra binary to ship/maintain);
   (c) an **initializr-style web/template generator** (start.spring.io feel, most infra).
   Recommendation to consider (not a decision): start with the build-plugin goal (lowest friction,
   no new distribution channel), keep the CLI as a thin wrapper later.
3. **Default template engine** (Thymeleaf vs JTE) for the MVC scaffold: soft one-way door for the
   generated templates' syntax; flag it but it is reversible per-project via the `--ui` flag.

### 3.6 Proposed initial issue backlog (epics + first-wave stories) — LISTED ONLY, not created

**Epic A — Auto-config starter (the headless engine)**
- A1 `SecurityFilterChain` auto-config: form login + session + CSRF + `changeSessionId` + logout, all `@ConditionalOnMissingBean`.
- A2 `DelegatingPasswordEncoder` default bean + override point.
- A3 User provider SPI: jdbc-backed `UserDetailsService` from configured table/columns; custom bean backs it off.
- A4 Feature-flag config model (`spring-auth.features.*`) + `@ConfigurationProperties` + docs.
- A5 Escape-hatch test: a user-defined `SecurityFilterChain` fully disables the auto-config (integration test proving back-off).

**Epic B — Flows Spring Security lacks**
- B1 Password reset: token entity + JDBC store + signed token + mailer port + reset controller; expiry + throttle config.
- B2 Email verification: signed-link generator + `verified` authorization gate + resend (throttled).
- B3 Password-confirm sudo window: session timestamp + filter/`AuthorizationManager` + configurable timeout.
- B4 Remember-me: opt-in `PersistentTokenBasedRememberMeServices` + `JdbcTokenRepositoryImpl` schema (`persistent_logins`).
- B5 Login rate limiter: pluggable SPI, in-memory default keyed by `username|ip`, documented as the Fortify-throttle analog.

**Epic C — Scaffold generator**
- C1 Generator skeleton + delivery mechanism (decision #2) producing the 7 auth screens + wiring class.
- C2 MVC/Thymeleaf (or JTE) template set (decision #3) for all auth + settings screens.
- C3 lievit-backed template set behind `--ui=lievit`, zero compile dep from the starter.
- C4 Account settings scaffold: profile update, password update (current-password check), delete-account (password-confirmed).
- C5 Idempotent re-run / no-clobber behavior + generated-code ownership docs.

**Epic D — Project hygiene / docs**
- D1 README + quickstart ("the `laravel new --livewire` of Spring") + the escape-hatch doc.
- D2 Sample app (the walking skeleton: register -> verify -> login -> sudo -> reset -> logout).
- D3 CI: build + the back-off integration test + template-render smoke.
- D4 Versioning + the deferred-features roadmap (Sanctum/OAuth2/2FA/passkeys) stated up front.

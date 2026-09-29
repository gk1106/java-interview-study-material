# Spring Security — Filter Chain, JWT, OAuth2/OIDC, CSRF, Password Encoding

Security questions test whether you can reason about *where in the request lifecycle* auth happens
and *why* a given configuration choice is safe or dangerous — not just recite filter names. This
file covers the Spring Security filter chain order, the modern `SecurityFilterChain` lambda DSL,
authentication vs authorization, the JWT flow for stateless APIs, OAuth2/OIDC basics, CSRF, and
password encoding.

## Filter Chain & SecurityFilterChain Configuration

- [Basic] What is the Spring Security filter chain, and where does it sit relative to your
  controllers? → Spring Security is implemented as a chain of **Servlet Filters** registered
  (via `DelegatingFilterProxy` / `FilterChainProxy`) to run **before** the `DispatcherServlet`
  routes a request to any controller — every request passes through this filter chain first
  (authentication, authorization, CSRF checks, etc.), and only requests that pass every relevant
  filter's checks ever reach your `@RestController` methods at all. → Follow-up: *Why is it
  implemented as servlet filters rather than, say, a Spring MVC interceptor?* Filters run at the
  raw servlet-container level, **before** Spring MVC's `DispatcherServlet` even resolves which
  controller would handle the request — this lets Security reject/redirect unauthorized requests
  as early as possible (before any Spring MVC machinery, argument binding, or controller code runs
  at all), which is both a performance win and a security-boundary win (nothing controller-specific
  needs to be trusted to enforce auth).
- [Intermediate] Name the key filters in the default Spring Security chain and their rough order,
  and explain why the order matters. → Roughly (varies by config):
  `SecurityContextPersistenceFilter`/`SecurityContextHolderFilter` (restores the security context
  for this request, e.g. from a session) → `CsrfFilter` (validates the CSRF token if CSRF
  protection is enabled) → `UsernamePasswordAuthenticationFilter` (handles form-login POST
  submissions) or a custom JWT filter you add → `BasicAuthenticationFilter` (handles HTTP Basic
  auth headers) → `ExceptionTranslationFilter` (catches `AuthenticationException`/
  `AccessDeniedException` thrown further down the chain and converts them into the right HTTP
  response — a redirect to login, or a 401/403) → `FilterSecurityInterceptor`/
  `AuthorizationFilter` (the final authorization decision — is this authenticated principal allowed
  to access this specific resource). Order matters because **authentication must happen before
  authorization can make a decision** — you can't check "is this user allowed" before you've
  established "who is this user," and `ExceptionTranslationFilter` needs to sit *after* the filters
  that can throw those exceptions so it can actually catch them. → Follow-up: *Where would you
  insert a custom JWT validation filter, and what method do you use?* Typically positioned relative
  to `UsernamePasswordAuthenticationFilter` via `addFilterBefore(jwtFilter,
  UsernamePasswordAuthenticationFilter.class)` — since JWT auth replaces session/form-login-based
  auth for a stateless API, the custom filter needs to run early enough to populate the
  `SecurityContext` before any authorization decision filters run later in the chain.
- [Intermediate] What does the modern lambda-based `SecurityFilterChain` bean configuration look
  like, and why did Spring Security move away from extending `WebSecurityConfigurerAdapter`? →
  Modern style (Spring Security 5.7+/6):
  ```java
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
      return http.build();
  }
  ```
  The deprecated `WebSecurityConfigurerAdapter` (extending a base class and overriding
  `configure(HttpSecurity)`) was removed because inheritance-based configuration made it easy to
  accidentally lose default security settings if you overrode a method incorrectly, encouraged a
  single monolithic config class, and didn't compose well — the component-based
  `SecurityFilterChain` `@Bean` approach lets you define **multiple** filter chains (with
  `@Order`) for different URL patterns (e.g. a stateless JWT chain for `/api/**` and a
  session-based chain for an admin UI), which was awkward with the old inheritance model. →
  Follow-up: *How would you configure two separate filter chains — one for `/api/**` using JWT,
  one for everything else using session-based form login?* Define two `@Bean` methods each
  returning a `SecurityFilterChain`, each scoped with `.securityMatcher("/api/**")` (or the
  complementary pattern) and each with `@Order` to control evaluation precedence — Spring Security
  evaluates chains in order and uses the first one whose matcher matches the request.

## Authentication vs Authorization

- [Basic] What's the difference between authentication and authorization, and which comes first? →
  **Authentication** answers "who are you" — verifying an identity claim (a password, a JWT
  signature, an OAuth token) and, on success, populating the `SecurityContext` with an
  `Authentication` object representing the principal. **Authorization** answers "are you allowed to
  do this" — checking the now-established principal's roles/authorities/permissions against what a
  specific resource/action requires. Authentication always happens first; authorization decisions
  are meaningless without first knowing who's asking. → Follow-up: *What HTTP status codes
  typically correspond to an authentication failure versus an authorization failure, and why does
  the distinction matter for API design?* 401 Unauthorized = authentication failed/missing (the
  client should log in / supply valid credentials); 403 Forbidden = authentication succeeded but
  the authenticated principal lacks permission for this specific resource (re-authenticating won't
  help — a different, more privileged account would be needed). Conflating them misleads API
  clients about what corrective action to take.
- [Intermediate] What's the difference between `hasRole("ADMIN")` and `hasAuthority("ROLE_ADMIN")`
  in Spring Security's authorization expressions? → They end up checking the **same thing**, but
  `hasRole("ADMIN")` automatically prepends the `"ROLE_"` prefix internally before comparing —
  `hasRole("ADMIN")` is exactly equivalent to `hasAuthority("ROLE_ADMIN")`. This is a common source
  of confusion: if your authorities are actually granted *without* the `ROLE_` prefix (e.g. a JWT
  claim mapper that creates a `GrantedAuthority("ADMIN")` directly), `hasRole("ADMIN")` silently
  fails to match anything because it's really checking for `"ROLE_ADMIN"`, which was never granted.
  → Follow-up: *When would you use `hasAuthority` over `hasRole` deliberately?* When your
  authorization model distinguishes fine-grained **permissions** from coarse **roles** (e.g.
  `hasAuthority("orders:write")` for a specific capability, versus `hasRole("ADMIN")` for a broad
  role bucket) — permissions typically don't use the `ROLE_` convention at all, since they're not
  roles, so `hasAuthority` is the correct, unprefixed check for them.
- [Intermediate] What is method-level security (`@PreAuthorize`), and how does it differ from URL-
  pattern-based authorization in `SecurityFilterChain`? → URL-pattern authorization
  (`.requestMatchers(...).hasRole(...)`) is coarse-grained — it decides access purely from the
  request's URL/method, before the request even reaches the controller/service layer.
  `@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")` (requires
  `@EnableMethodSecurity`) is fine-grained, evaluated at the **method level** (typically on a
  service method), with access to method parameters via SpEL — enabling checks that genuinely
  depend on the actual data/arguments involved (e.g. "an admin can edit any user, but a regular
  user can only edit their own profile," which URL pattern matching alone can't express since the
  URL `/users/{id}` looks identical regardless of whose `id` it is). → Follow-up: *Why might you
  use both URL-pattern and method-level security together rather than relying on just one?*
  Defense in depth and separation of concerns — URL patterns give a fast, coarse first filter
  (reject clearly-unauthenticated/wrong-role requests before they reach any business logic at all,
  minimizing wasted work), while method-level checks enforce the finer, data-dependent rules that
  URL patterns structurally can't express; relying on method-level checks alone also means a
  forgotten `@PreAuthorize` on one new endpoint leaves it completely unprotected, whereas a
  default-deny URL pattern (`anyRequest().authenticated()`) provides a safety net.

## JWT Flow for Stateless APIs

- [Basic] Describe the JWT authentication flow for a stateless REST API end to end. → (1) Client
  sends credentials to a login endpoint. (2) Server verifies credentials, then issues a **JWT** —
  a signed (and optionally encrypted) token encoding claims (user ID, roles, expiry) in its payload
  — back to the client. (3) Client stores the token (memory, secure storage — see the CSRF section
  for why *not* a plain cookie readable by JS) and sends it on every subsequent request, typically
  in the `Authorization: Bearer <token>` header. (4) A custom filter on the server intercepts each
  request, extracts the token, **verifies its signature** (using the server's secret/public key —
  no database lookup needed) and expiry, and if valid, populates the `SecurityContext` with the
  claims from the token directly. (5) No server-side session state is stored anywhere — the token
  itself is fully self-describing, which is what makes this "stateless." → Follow-up: *Why is this
  called "stateless," and what's the practical benefit at scale?* The server holds **no** per-user
  session data between requests — every request carries everything needed to authenticate it. This
  means any server instance behind a load balancer can validate any request independently, with no
  shared session store/sticky sessions required — critical for horizontally scaling stateless
  microservices.
- [Intermediate] Where exactly does JWT validation happen in the Spring Security filter chain, and
  what does the filter actually check? → A custom filter (extending `OncePerRequestFilter`,
  positioned via `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)`) runs early in
  the chain: it extracts the token from the `Authorization` header, verifies the
  **cryptographic signature** (using `HMAC` with a shared secret for symmetric signing, or
  `RSA`/`ECDSA` with a public key for asymmetric — asymmetric is preferred when the *validator*
  shouldn't be trusted with the ability to *issue* tokens, e.g. multiple microservices validating
  tokens issued by one central auth service), checks the **expiry** (`exp` claim) and, often,
  **issuer**/**audience** claims, and if all checks pass, builds an `Authentication` object from the
  token's claims (user ID, roles) and sets it on the `SecurityContextHolder` for the remainder of
  this request's processing. → Follow-up: *What happens if the filter finds an expired or
  invalid-signature token?* It should **not** throw an uncaught exception that crashes the filter
  chain — properly, it leaves the `SecurityContext` empty/unauthenticated and lets the request
  continue down the chain, where the later authorization filter then rejects it with 401/403 as an
  unauthenticated request, giving a clean, consistent error response rather than a raw stack trace.
- [Advanced] JWTs are self-contained and stateless by design — how do you handle "logout" or
  "revoke this user's access immediately" (e.g. after a password change or a stolen-token
  incident) when the server holds no session to invalidate? → This is JWT's fundamental trade-off:
  pure stateless validation means the server can't "forget" a token before its natural expiry.
  Practical mitigations: (1) **short-lived access tokens** (minutes, not hours/days) paired with a
  longer-lived **refresh token** that *is* checked against a server-side store (DB/Redis) on
  every refresh — revoking access then just means deleting the refresh token record, and the
  compromised access token naturally expires soon after regardless; (2) a **denylist/blocklist**
  of revoked token IDs (`jti` claim) kept in a fast store (Redis, with a TTL matching the token's
  own expiry so the blocklist entry auto-expires and doesn't grow unbounded) — the validation
  filter does one extra fast lookup per request, trading some of the "no state at all" purity for
  actual revocability; (3) embedding a **token version/generation number** in the JWT that's
  compared against a per-user version stored in the DB, bumped on password change/logout-all-
  devices — any older token instantly fails validation without needing a growing denylist. In
  practice, most real systems use short-lived access tokens + a stateful refresh-token store as the
  primary mechanism, since it bounds the "can't immediately revoke" exposure window to just the
  access token's short lifetime without needing per-request blocklist lookups on the hot path. →
  Follow-up: *Why is refreshing tokens against a server-side store not a contradiction of
  "stateless" JWT auth?* The **access token** validation path (the hot path, hit on every API
  request) stays fully stateless; only the **refresh** path (hit rarely, once per token lifetime,
  not per request) touches server state — the stateless benefit is preserved exactly where it
  matters for scaling (high-frequency request validation), while accepting a small amount of state
  on the low-frequency refresh path in exchange for real revocability.

## OAuth2 / OIDC Basics

- [Basic] What problem does OAuth2 solve, and how does it differ from just checking a username and
  password directly? → OAuth2 lets a user grant a **third-party application limited access** to
  their resources on another service, **without sharing their actual password** with that
  third-party app at all — e.g. letting a scheduling app read your Google Calendar without ever
  handing that app your Google password. It's fundamentally a **delegated authorization** protocol,
  not itself an authentication protocol (that's what OIDC, layered on top, adds — see below). →
  Follow-up: *What are the four main roles/parties in an OAuth2 flow?* Resource Owner (the user),
  Client (the third-party app requesting access), Authorization Server (issues tokens after the
  user grants consent — e.g. Google's auth server), and Resource Server (hosts the protected
  resource, validates the access token on each request — e.g. the Google Calendar API).
- [Intermediate] Walk through the OAuth2 Authorization Code flow, and explain why it's considered
  the most secure grant type for a server-side web app. → (1) The client redirects the user's
  browser to the Authorization Server's `/authorize` endpoint with the client ID, requested scopes,
  and a `redirect_uri`. (2) The user authenticates (if not already) and consents to the requested
  scopes at the Authorization Server — the client app never sees the user's credentials at all.
  (3) The Authorization Server redirects back to the client's `redirect_uri` with a short-lived,
  single-use **authorization code**. (4) The client's **backend** (not the browser) exchanges this
  code for an **access token** (and often a refresh token) by calling the Authorization Server's
  `/token` endpoint directly, authenticating itself with a `client_secret` — this token exchange
  happens server-to-server, never exposing the access token to the browser/front-channel at all.
  (5) The client backend then uses the access token to call the Resource Server on the user's
  behalf. It's considered the most secure grant because the actual access token never transits
  through the browser/URL bar/redirect chain (only the short-lived, single-use code does) — a code
  intercepted somewhere is far less dangerous since it's useless without the `client_secret` held
  only by the trusted backend. → Follow-up: *Why is Authorization Code + PKCE now recommended even
  for confidential (server-side) clients, not just public clients like mobile/SPA apps?* PKCE (a
  dynamically generated code verifier/challenge pair per auth request) protects against
  authorization-code-interception attacks even if the code somehow leaks (e.g. via a misconfigured
  redirect, browser history, or a referrer leak) — it's a defense-in-depth addition that's become
  the universal recommendation for all client types, not just the originally-intended public-client
  case (public clients can't safely hold a `client_secret` at all, which is what PKCE was
  originally designed to compensate for).
- [Intermediate] What does OIDC (OpenID Connect) add on top of plain OAuth2, and why is this
  distinction commonly confused? → OAuth2 alone only proves the client was granted **authorization**
  to access some resource with some scope — it says nothing formally about the user's *identity*.
  OIDC is a thin identity layer built on top of OAuth2 that adds a standardized **ID Token** (a JWT,
  signed by the Authorization Server, containing identity claims like `sub` (subject/user ID),
  `email`, `name`, issued alongside the OAuth2 access token) plus a standard `/userinfo` endpoint
  and a `openid` scope that triggers this identity behavior. The confusion arises because many
  systems use OAuth2's access token informally *as if* it proved identity (e.g. "logging in with
  Google" flows before OIDC was standardized often misused OAuth2's access token this way,
  sometimes insecurely) — OIDC exists specifically to standardize "login with X" (authentication)
  correctly and distinctly from "grant access to Y" (authorization), using the ID token for the
  former and the access token strictly for the latter. → Follow-up: *If you only need "login with
  Google" (identity) and don't need to call any Google API afterward, which token do you actually
  care about — the access token or the ID token?* The ID token — that's the one carrying the
  verified identity claims; the access token would only matter if you also needed to call a Google
  API (like Calendar) on the user's behalf afterward.

## CSRF

- [Basic] What is CSRF (Cross-Site Request Forgery), and why is it specifically a risk for
  **cookie-based** session authentication but largely irrelevant for a stateless JWT-in-header
  API? → CSRF exploits the fact that a browser **automatically attaches cookies** (including a
  session cookie) to any request to a site, even one triggered by a malicious third-party page the
  user has open in another tab — an attacker's page can silently submit a form/fetch to
  `bank.com/transfer` and the browser helpfully attaches the user's valid session cookie, making the
  request look legitimate to the server. It's a risk specifically because cookies are sent
  **automatically and implicitly** by the browser with no action from the calling page's JavaScript.
  A JWT sent in a custom `Authorization: Bearer` **header**, by contrast, is **not** automatically
  attached by the browser to cross-site requests — the attacker's malicious page has no way to make
  the victim's browser send that header without the victim's own JavaScript explicitly doing it
  (which same-origin policy/CORS would block for a cross-site attacker page) — so CSRF isn't a
  meaningful threat for that authentication style. → Follow-up: *Why does this reasoning break down
  if you store a JWT in a cookie instead of sending it via an explicit header?* If the JWT itself is
  stored in a cookie and the server reads auth from that cookie automatically, you've re-introduced
  the exact same automatic-attachment problem CSRF exploits — the "JWT avoids CSRF" property is
  really "sending auth via an explicit header the browser doesn't auto-attach avoids CSRF," which
  stops being true the moment you put the token back in a cookie.
- [Intermediate] When should you disable CSRF protection (`.csrf(csrf -> csrf.disable())`) in
  Spring Security, and when is that dangerous? → Safe/appropriate to disable for a **purely
  stateless API** authenticated via a bearer token in a header (no cookies involved in
  authentication at all) — since CSRF fundamentally exploits automatic cookie attachment, and
  there's no session cookie here for an attacker to ride on, the protection has nothing to defend
  and only adds friction (clients would otherwise need to fetch and echo back a CSRF token on every
  mutating request, which makes no sense for a stateless token-auth API). Dangerous to disable for
  any endpoint that **is** authenticated via a session cookie (traditional server-rendered forms, or
  any hybrid app using cookie-based session auth for some routes) — disabling CSRF there removes a
  real protection against a real attack vector, leaving state-changing endpoints forgeable from any
  malicious third-party page. → Follow-up: *What's the risk of a team copy-pasting
  `csrf.disable()` into a mixed app that uses both cookie-session auth for an admin UI and JWT for
  an API, without scoping it correctly?* Disabling CSRF globally would leave the cookie-authenticated
  admin UI routes vulnerable to CSRF while only the JWT API routes actually needed it disabled — the
  fix is scoping CSRF protection per `SecurityFilterChain` (e.g. disable only on the chain matching
  `/api/**`, leave it enabled on the chain matching the session-based UI routes).

## Password Encoding

- [Basic] Why must passwords be hashed with `BCrypt` (or similar) rather than a plain hash like
  SHA-256, and what does `PasswordEncoder.matches()` actually do? → A plain fast hash (SHA-256, even
  salted) is **designed to be fast** — exactly the wrong property for password storage, since it
  makes brute-force/dictionary attacks against a leaked hash database cheap at massive scale on
  modern hardware (GPUs compute billions of SHA-256 hashes per second). `BCrypt` (and `Argon2`,
  `scrypt`, `PBKDF2`) are **deliberately slow, tunable-cost** hashing algorithms — they incorporate
  a work factor (BCrypt's "rounds," e.g. 10-12) that can be dialed up over time as hardware gets
  faster, keeping brute-force attacks impractically slow even against a leaked hash. `BCrypt` also
  auto-generates and embeds a unique random **salt** per password in its output string, so two
  users with the same password get completely different stored hashes (defeating rainbow-table
  attacks). `PasswordEncoder.matches(rawPassword, storedHash)` re-hashes the raw input using the
  salt/parameters embedded in the stored hash string itself and compares the result — you never
  decrypt a stored hash (hashing is one-way by design), only re-verify by re-hashing. →
  Follow-up: *If you increase BCrypt's cost factor (rounds) in production, do existing users'
  stored password hashes need to be rehashed immediately?* No — `BCrypt`'s cost factor is embedded
  in the hash string itself (`$2a$12$...` — the `12` is the rounds used for that specific hash), so
  `matches()` still correctly verifies old hashes at their original cost factor; a common pattern is
  to opportunistically **rehash at the new higher cost the next time the user successfully logs in**
  (since you have their plaintext password at that moment), gradually migrating the whole user base
  without a disruptive bulk rehash/forced-reset event.
- [Intermediate] What's `DelegatingPasswordEncoder`, and why does Spring Boot's default
  `PasswordEncoder` bean use it instead of a plain `BCryptPasswordEncoder`? → It's a `PasswordEncoder`
  that prefixes the stored hash with an identifier (`{bcrypt}$2a$10$...`, or `{noop}plaintext`,
  `{scrypt}...`, etc.) indicating **which** algorithm produced it, and on `matches()`, reads that
  prefix to delegate verification to the correct underlying encoder — this lets an application
  **migrate encoding algorithms over time** (e.g. from an older algorithm to `Argon2`) without
  breaking verification of passwords hashed under the old scheme, since each stored hash
  self-identifies which algorithm to use for checking it; new passwords are encoded with the
  currently-configured default algorithm going forward. Spring Security's default
  `PasswordEncoderFactories.createDelegatingPasswordEncoder()` defaults new encodings to `BCrypt`.
  → Follow-up: *What's the security risk of the `{noop}` prefix, and why does it exist at all in
  the framework?* `{noop}` stores/compares the password in **plain text** with no hashing — it
  exists purely for quick local testing/demos, never for anything resembling production; leaving
  or accidentally configuring `{noop}` in a real deployment means a DB leak exposes every user's
  actual plaintext password directly, a critical, entirely avoidable security failure.

---

**File question count: 15** ([Basic] 5, [Intermediate] 7, [Advanced] 3)

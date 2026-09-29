# Core Spring — IoC, DI, Bean Scopes & Lifecycle

Core Spring questions probe whether you actually understand *why* the container works the way it
does, not just the annotation names. Interviewers with 3+ YOE candidates in front of them almost
always push past "what is DI" into "what happens if two beans depend on each other" or "which
`BeanPostProcessor` runs first and why does that matter." This file covers the IoC container,
dependency injection styles, bean scopes, the full bean lifecycle, `@Configuration` vs
`@Component` vs `@Bean`, circular dependency resolution, and `@Autowired` resolution rules.

## IoC Container & Dependency Injection

- [Basic] What is Inversion of Control, and how does the Spring container implement it? →
  Normally your code creates and wires its own dependencies (`new UserService(new UserRepo())`);
  IoC inverts that — an external container (the `ApplicationContext`) creates objects, resolves
  their dependencies, and hands you a fully wired object. Spring implements this via the
  `BeanFactory`/`ApplicationContext`, which reads bean definitions (from annotations, XML, or
  Java config), builds a dependency graph, and instantiates/wires beans in the correct order. →
  Follow-up: *What's the difference between `BeanFactory` and `ApplicationContext`?*
  `BeanFactory` is the root interface with lazy, on-demand bean instantiation; `ApplicationContext`
  extends it and adds eager singleton instantiation by default, event publishing, internationalization,
  AOP integration, and environment/property abstraction — it's what almost every real app uses.
- [Basic] What is Dependency Injection, and what are the three ways to do it in Spring? →
  DI is the mechanism the IoC container uses to supply a bean's dependencies rather than the bean
  looking them up itself. Three styles: **constructor injection** (dependencies passed via
  constructor params — Spring auto-detects this since 4.3 without `@Autowired` if there's only
  one constructor), **setter injection** (via setter methods, optional dependencies), and **field
  injection** (`@Autowired` directly on a field via reflection). → Follow-up: *Which does Spring
  itself recommend, and why?* Constructor injection — see next question.
- [Intermediate] Why is constructor injection preferred over field injection in production code? →
  Several concrete reasons: (1) it lets the field be `final`, so the object is immutable and can
  never be in a partially-constructed state; (2) it makes required dependencies explicit and
  impossible to forget — you *cannot* instantiate the class without them, whereas field injection
  lets you `new UserService()` in a test and get a silent `NullPointerException` later; (3) it
  makes circular dependencies **fail fast at startup** (`BeanCurrentlyInCreationException`) instead
  of Spring quietly resolving them via early-reference proxies with field injection; (4) it's
  trivial to unit test with plain `new UserService(mockRepo)` — no need to spin up a Spring context
  or use reflection-based mocking to inject a private field. → Follow-up: *Is there ever a
  legitimate reason to use field injection?* Rarely — maybe in test classes for `@MockBean`/`@Autowired`
  convenience, since test classes aren't instantiated by your own code anyway; in production code it's
  generally considered a code smell.
- [Intermediate] What happens if a bean has multiple constructors and none is annotated with
  `@Autowired`? → Spring throws `NoSuchBeanDefinitionException`/`BeanCreationException` at startup
  because it can't decide which constructor to use for autowiring — it only auto-selects a single
  constructor automatically when there's exactly one. With multiple constructors you must mark
  the one Spring should use with `@Autowired`. → Follow-up: *Can you have `@Autowired` on more than
  one constructor?* No — at most one constructor per bean can be marked `@Autowired(required=true)`;
  you can mark several as `@Autowired(required=false)` for optional alternative wiring, but that's
  unusual and rarely worth the complexity.
- [Advanced] What's the practical difference between injecting a `List<MyInterface>` versus a
  single `MyInterface` when multiple implementations exist? → Spring can inject **all** matching
  beans into a `List<MyInterface>` (or `Map<String, MyInterface>` keyed by bean name), ordered by
  `@Order`/`Ordered` if present — this is the idiomatic strategy-pattern wiring (e.g. a list of
  `PaymentValidator` beans all run in sequence). Injecting a bare `MyInterface` with more than one
  candidate and no `@Primary`/`@Qualifier` throws `NoUniqueBeanDefinitionException` at startup. →
  Follow-up: *How would you build a plugin-style validation chain using this?* Define an interface,
  implement it per validator, inject `List<Validator>` into an orchestrator bean, and iterate —
  Spring wires every discovered implementation automatically with zero orchestrator changes needed
  when a new validator is added.

## Bean Scopes

- [Basic] Name Spring's built-in bean scopes and what each means. → `singleton` (default — one
  instance per container, shared everywhere), `prototype` (a new instance every time the bean is
  requested/injected), `request` (one instance per HTTP request, web-aware contexts only),
  `session` (one instance per HTTP session), and `application` (one instance per `ServletContext`).
  → Follow-up: *Is `singleton` scope the same as the Singleton GoF pattern?* No — it's singleton
  **per Spring container**, not per JVM; if you run two `ApplicationContext`s in the same JVM (rare,
  but happens in some test setups), each has its own "singleton" instance.
- [Intermediate] What's the classic problem with injecting a `prototype`-scoped bean into a
  `singleton`-scoped bean, and how do you fix it? → The singleton is only created once, so its
  prototype dependency is also only injected once at construction time — you get the *same*
  prototype instance forever, defeating the purpose of prototype scope. Fixes: (1) inject an
  `ObjectFactory<T>`/`ObjectProvider<T>` and call `.getObject()` each time you need a fresh
  instance, (2) use a lookup-method (`@Lookup`) that Spring overrides at runtime to fetch a new
  prototype bean each call, or (3) inject the `ApplicationContext` itself and call
  `getBean(MyPrototype.class)` (least preferred — couples the bean to the container). →
  Follow-up: *Why not just mark the singleton itself `prototype`?* That changes its own lifecycle
  semantics entirely (a new instance per injection point/request) which usually isn't what you
  want for a stateless service bean, and it doesn't solve the underlying "stale captured reference"
  problem if something else still holds a long-lived reference to it.
- [Intermediate] How do `request` and `session` scoped beans get injected into a `singleton`
  bean (e.g. a `@Service`)? → Directly injecting a `request`-scoped bean into a singleton would hit
  the same staleness problem as prototype — worse, there may be *no* active HTTP request when the
  singleton is constructed at startup. Spring solves this with a **scoped proxy**
  (`proxyMode = ScopedProxyMode.TARGET_CLASS` on the scope annotation): the singleton actually holds
  a CGLIB proxy that, on every method call, looks up the real request/session-scoped bean from the
  current thread-bound request context and delegates to it. → Follow-up: *What breaks if you forget
  `proxyMode` on a request-scoped bean injected into a singleton?* Startup fails, typically with
  `ScopeNotActiveException` when a method is invoked outside a request, because Spring tries to
  inject the raw bean directly rather than a proxy that can defer the lookup.

## Bean Lifecycle

- [Basic] What are the main phases of a Spring bean's lifecycle, in order? → (1) Instantiation
  (constructor called), (2) populate properties (dependency injection via setters/fields),
  (3) `BeanNameAware`/`BeanFactoryAware`/`ApplicationContextAware` callbacks if implemented,
  (4) `BeanPostProcessor.postProcessBeforeInitialization`, (5) `@PostConstruct` then
  `InitializingBean.afterPropertiesSet()` then a custom `init-method` if configured, (6)
  `BeanPostProcessor.postProcessAfterInitialization` (this is where AOP proxies typically get
  created), (7) bean is ready and in use, (8) on container shutdown: `@PreDestroy`, then
  `DisposableBean.destroy()`, then a custom `destroy-method`. → Follow-up: *Which of these steps
  only apply to singleton beans?* The destruction callbacks — Spring only manages the full
  lifecycle including destruction for singleton-scoped beans; prototype beans are handed off after
  creation and Spring does **not** call their destroy callbacks (you're responsible for
  cleanup yourself).
- [Intermediate] What's the difference between `@PostConstruct` and implementing
  `InitializingBean.afterPropertiesSet()`? Which order do they run in if both are present? →
  Both run after dependency injection is complete, before the bean is available for use.
  `@PostConstruct` (JSR-250, annotation-based, no Spring coupling) runs **first**, then
  `InitializingBean.afterPropertiesSet()` (Spring-specific interface), then any custom
  `init-method` specified in XML/`@Bean(initMethod=...)` last. In practice almost nobody implements
  `InitializingBean` anymore since `@PostConstruct` is framework-agnostic and equally capable. →
  Follow-up: *Why would you ever prefer `InitializingBean` over `@PostConstruct` in a library you
  publish?* If you want compile-time enforcement (an unimplemented interface method is a compile
  error, a missing annotation on a renamed method silently does nothing) and you're already coupled
  to Spring's API, though this is a rare, mostly historical preference.
- [Intermediate] What is a `BeanPostProcessor`, and give a concrete real-world example of one Spring
  itself uses. → It's an extension hook that lets you intercept **every** bean right before and
  after its initialization callbacks (`postProcessBeforeInitialization`/`AfterInitialization`),
  letting you wrap, modify, or replace the bean instance for *all* beans in the context, not just
  one. Spring uses this mechanism internally for: `AutowiredAnnotationBeanPostProcessor` (processes
  `@Autowired`/`@Value`), `CommonAnnotationBeanPostProcessor` (`@PostConstruct`/`@PreDestroy`), and
  critically, **AOP proxy creation** — `AnnotationAwareAspectJAutoProxyCreator` wraps a bean in a
  CGLIB/JDK dynamic proxy during `postProcessAfterInitialization` if it matches an `@Aspect`
  pointcut, which is exactly why `@Transactional`/`@Async`/custom `@Aspect` advice work — the bean
  you get back from `getBean()` is a proxy, not the raw object. → Follow-up: *Why does that matter
  for self-invocation of `@Transactional` methods?* Calling an `@Transactional` method from another
  method **inside the same class** bypasses the proxy entirely (it's a direct `this.method()` call
  on the raw object) so no transaction is started — a very commonly hit production bug.
- [Advanced] Why can circular dependency resolution via field/setter injection succeed while
  constructor injection of the same cycle fails at startup? → Spring resolves singleton circular
  dependencies using a **three-level cache** (singletonObjects, earlySingletonObjects,
  singletonFactories) it populates during bean creation: when creating bean A, before its
  properties are populated, Spring exposes an "early reference" (a factory that can produce a proxy
  or raw instance) into `singletonFactories`. If bean B (created while resolving A's dependencies)
  needs A back, Spring hands it this early reference instead of waiting for A to fully finish
  construction. This works for **setter/field injection** because the object already exists (just
  not fully populated yet) at the point B needs it — B gets a reference to an object that will be
  populated by the time anyone actually calls a method on it. It **cannot** work for constructor
  injection because A's constructor hasn't even returned yet — there is no object instance at all
  to hand out as an early reference — so Spring can't resolve the cycle and throws
  `BeanCurrentlyInCreationException`. → Follow-up: *What's the recommended fix for a genuine
  circular dependency between two services?* Refactor — it's almost always a design smell (extract
  the shared behavior into a third bean both depend on, or use `@Lazy` on one injection point as a
  last-resort workaround that defers resolution via a proxy, though that just hides the design
  issue rather than fixing it).

## @Configuration vs @Component vs @Bean

- [Basic] What's the difference between `@Component` and `@Bean`? → `@Component` (and its
  specializations `@Service`, `@Repository`, `@Controller`) is a class-level annotation picked up
  by **component scanning** — Spring instantiates the class itself as a bean. `@Bean` is a
  method-level annotation inside a `@Configuration` (or any `@Component`) class — you write the
  object-creation logic yourself and return it; it's used when you don't own the class (third-party
  library types) or need conditional/parameterized construction logic. → Follow-up: *Can you use
  `@Bean` methods inside a plain `@Component`, not just `@Configuration`?* Yes, but the "full" CGLIB
  proxying behavior described below only applies inside `@Configuration` classes — inside a plain
  `@Component`, `@Bean` methods are processed in "lite" mode.
- [Intermediate] Why does `@Configuration` classes get CGLIB-proxied, and what does that enable
  that plain `@Component` with `@Bean` methods doesn't? → When you mark a class `@Configuration`
  (the default `proxyBeanMethods = true`), Spring generates a CGLIB subclass of it at startup. Every
  time you **call another `@Bean` method directly from within the same class** (e.g.
  `serviceA()` calls `serviceB()` inside the same config class), the CGLIB proxy intercepts that
  call and returns the **already-created singleton** from the container instead of invoking the raw
  method again — guaranteeing singleton semantics even when beans reference each other by plain
  Java method calls. Without proxying (`proxyBeanMethods = false`, or a plain `@Component`), calling
  `serviceB()` directly just runs the method body again, creating a **second, unmanaged** instance
  that isn't the one registered in the container — a subtle correctness bug. → Follow-up: *When
  would you deliberately set `proxyBeanMethods = false`?* When you don't inter-call `@Bean` methods
  within the class (each method is independent) and want to skip the CGLIB subclassing cost —
  relevant for startup-time-sensitive apps (Spring Native/GraalVM, serverless cold starts) where
  every proxy generated adds reflection/class-loading overhead.
- [Intermediate] How does component scanning decide what to register as a bean? → `@ComponentScan`
  (implicitly included by `@SpringBootApplication`) scans the specified base package(s) for classes
  annotated with `@Component` or any meta-annotation composed from it (`@Service`, `@Repository`,
  `@Controller`, `@RestController`, `@Configuration`) and registers each as a bean definition,
  using the class name (decapitalized) as the default bean name unless overridden
  (`@Component("customName")`). `@SpringBootApplication`'s scan base package defaults to the
  package of the main application class and everything under it — which is exactly why the
  convention is to put your main class in the root package above all feature packages. →
  Follow-up: *What happens to a `@Service` class sitting in a package outside the scan base
  package?* It's silently never picked up — no bean is registered, and any `@Autowired` dependency
  on it fails at startup with `NoSuchBeanDefinitionException`; a classic "why isn't my bean found"
  bug caused by package layout, fixed either by moving the class or adding an explicit
  `@ComponentScan(basePackages = ...)`.

## Autowiring & Circular Dependencies

- [Basic] How does Spring resolve `@Autowired` when multiple beans implement the same interface? →
  Resolution order: (1) by **type** — if exactly one bean of that type exists, inject it;
  (2) if multiple exist, look for `@Primary` on one of them and prefer it; (3) if a `@Qualifier`
  is specified at the injection point, match the bean whose name/qualifier value matches; (4) if
  none of that disambiguates, fall back to matching the **field/parameter name** against a bean
  name; (5) if still ambiguous, throw `NoUniqueBeanDefinitionException`. → Follow-up: *What's the
  difference in intent between `@Primary` and `@Qualifier`?* `@Primary` sets a default/fallback
  choice when the caller doesn't care which implementation ("use this one unless told otherwise");
  `@Qualifier` is an explicit, caller-specified choice ("I want *this specific* implementation") —
  they can coexist, with an explicit `@Qualifier` at the call site overriding `@Primary`.
- [Intermediate] What's the difference between `@Resource` and `@Autowired`? → `@Autowired`
  (Spring's own annotation) resolves **by type first**, then disambiguates by qualifier/name;
  `@Resource` (JSR-250, `jakarta.annotation`) resolves **by name first**, falling back to type only
  if no name match is found — the opposite priority order. `@Resource` is framework-agnostic (works
  outside Spring, e.g. in Java EE/Jakarta EE containers), which occasionally matters for code meant
  to be portable, but in a Spring Boot codebase `@Autowired` (or plain constructor injection with no
  annotation at all, since Spring 4.3+ auto-detects a single constructor) is the idiomatic choice. →
  Follow-up: *Do you need `@Autowired` at all on a constructor if there's only one constructor?*
  No — since Spring 4.3, a class with exactly one constructor has it implicitly used for autowiring
  without any annotation; `@Autowired` is only required when there are multiple constructors and you
  need to tell Spring which one to use.
- [Advanced] You have two beans implementing `PaymentGateway`: `StripeGateway` and `PaypalGateway`.
  How would you cleanly let a controller choose which one to use at request time, without `if/else`
  chains scattered through the codebase? → Inject `Map<String, PaymentGateway>` — Spring populates
  it with every bean of that type keyed by bean name (`"stripeGateway"`, `"paypalGateway"`), letting
  you do `gateways.get(requestedProvider + "Gateway")` in one place (a factory/strategy lookup).
  Alternatively, define a custom qualifier annotation per provider (`@Stripe`, `@Paypal`) for
  compile-time-safe injection at specific call sites, or implement a small
  `PaymentGatewayResolver` bean that itself receives `List<PaymentGateway>` and exposes a
  `resolve(String provider)` method, keeping the strategy-selection logic testable and centralized
  rather than duplicated wherever a gateway is needed. → Follow-up: *Why is the `Map<String, T>`
  injection pattern preferable to a giant `switch` statement in a service class?* Open/closed
  principle — adding a new gateway means adding a new `@Component` implementation, with zero
  changes to the resolver/switch logic; a `switch` requires editing existing, already-tested code
  every time a new provider is added.

---

**File question count: 17** ([Basic] 5, [Intermediate] 8, [Advanced] 4)

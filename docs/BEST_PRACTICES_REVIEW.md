# AgentOS best-practices review

Audit of the local control-plane app (agents, human-in-the-loop approvals, cost, emergency kill switch) after the Play release-readiness change. Severities are blocker, high, medium, and low.

This document records the pre-fix findings. The resolution column says what this branch changed. Product and backend gaps are recommendations only; they are not counted as findings.

## Counts

| Severity | Before | Fixed here | Still open |
| --- | ---: | ---: | ---: |
| Blocker | 2 | 2 | 0 |
| High | 9 | 9 | 0 |
| Medium | 14 | 10 | 4 |
| Low | 5 | 0 | 5 |
| Total | 30 | 21 | 9 |

## Architecture and layering

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| H4 | High | Screens collected `StateFlow` with `collectAsState()`, so collection was not lifecycle-aware. `lifecycle-runtime-compose` was already a dependency. | Screens and `MainActivity` use `collectAsStateWithLifecycle()`. |
| H8 | High | Composables wrote public `MutableStateFlow`s (`agentStatusFilter.value = …`, onboarding and delegation fields, analytics range). | Flows are private. Screens call setters. |
| M11 | Medium | There is no dependency-injection graph. `MainActivity` builds the repository through `AgentOSDatabase.getRepository()`. | Open. A manual factory is enough for this app. Hilt would be a new framework, not a fix. |
| M12 | Medium | Tabs are an `Int`, not a Navigation-Compose graph. The library is on the classpath and unused for navigation. | Open. A graph would be a navigation rewrite without a new user-facing destination. |
| M14 | Medium | Screens take `AgentOSViewModel` directly. Analytics hero numbers (1.24M tokens, 1,840ms, 99.2%, 0.8%) are static copy, not computed from Room. | Open. Computing those figures would change the product numbers. |

Business logic for approve, reject, and kill lives in `AgentOSRepository`. Composables render state and forward events.

## Room

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| H1 | High | Database version 1, `exportSchema = false`, and `fallbackToDestructiveMigration()`. A schema change would wipe agents, tasks, and the kill-switch flag. | Version 2. Schema export is on (`app/schemas/.../2.json`). `MIGRATION_1_2` adds indices. Destructive fallback is gone. |
| H2 | High | Approve, reject, kill, and task create wrote several tables without a transaction. | Those writes use `withTransaction`. |
| M1 | Medium | No indices on `agents.status`, `task_executions.agentId`, `task_executions.status`, or analytics `timestamp` / `agentId`. | Indices added in the entity declarations and in migration 1→2. |

`allowMainThreadQueries` is still off for the production database. Queries stay on Room’s executor.

## Concurrency and error handling

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| B2 | Blocker | `approveHitlTask` copied the task, then after 2.5s wrote that stale copy as `COMPLETED`. It did not re-read status or the kill-switch flag. An in-flight approval could resurrect a killed task. `createAndRunTask` only checked `KILLED`. | Completion re-reads the task and settings inside a transaction and does not overwrite `KILLED` or an engaged kill switch. New tasks are inserted as `KILLED` when the switch is already engaged. |
| H3 | High | `MainActivity` created a `CoroutineScope(Dispatchers.IO)` and passed it into the repository. Nothing cancelled it. Failures in seed and delayed completion were uncaught. | One process-scoped repository from `AgentOSDatabase.getRepository()`. Background work uses a `SupervisorJob` and logs `Background work failed` without the exception message. ViewModel actions catch failures, rethrow `CancellationException`, and toast a generic string. |
| H9 | High | The Settings circuit-breaker `Switch` used `onCheckedChange = { }`. The daily cap slider did persist. | The switch calls `updateCircuitBreaker`. |

## Security

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| B1 | Blocker | Room `maskedApiKey` stored key-shaped values: seed strings such as `sk-ant-api03-••••••••••••39a1`, and for a real key the first four and last four characters. The full key was discarded, not encrypted. | `KeystoreAgentSecrets` encrypts the key with Android Keystore AES-GCM into app-private files under `filesDir/agent-secrets/`. Room stores only `••••` plus the last four characters. Deleting an agent deletes the file. Backup rules exclude that directory. The in-memory key is cleared after deploy. |
| M5 | Medium | No network security config. Cleartext was not explicitly denied. | `network_security_config.xml` sets `cleartextTrafficPermitted=false`. The manifest sets `usesCleartextTraffic="false"`. |

The launcher activity is the only exported component, which it must be. `allowBackup` stays false. Logs for secret read/write failures do not include key material.

## Compose

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| H5 | High | System back left the activity while onboarding, detail, delegation, inspector, or the HITL sheet was open. `enableOnBackInvokedCallback` was unset, so predictive back was off. | `BackHandler` dismisses the topmost overlay. The application sets `android:enableOnBackInvokedCallback="true"`. |
| M10 | Medium | No `@Preview`s. | Previews for `EmptyState` and `StatusBadge`. |

State for filters and forms is held in the ViewModel. `LazyColumn` already keyed by id.

## Material 3, theme, and layout

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| M6 | Medium | Dark theme only. `isSystemInDarkTheme` was unused. A `SideEffect` painted the status and navigation bars and forced a dark appearance, fighting `enableEdgeToEdge()`. | Light and dark schemes follow the system. System bars stay transparent; icon appearance follows the theme. Ink on bright status fills uses a stable `OnStatusFill` so light mode does not turn that text white. |
| M7 | Medium | No max width. On a tablet the lists stretched edge to edge. | Tab content is centered with `widthIn(max = 840.dp)`. |
| M9 | Medium | `String.format` without a `Locale`. `Divider` and `Icons.Filled.ArrowForward` are deprecated. | `formatUsd` uses `Locale.getDefault()`. Horizontal and vertical dividers replace `Divider`. The inspector uses the AutoMirrored arrow. |

## Accessibility

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| M3 | Medium | Filter chips, the kill-switch buttons (44.dp), the preflight button (28.dp), review buttons (32.dp), and the header kill control (36.dp) were under 48.dp. | Those controls use `heightIn(min = 48.dp)`, `defaultMinSize(minHeight = 48.dp)`, or `minimumInteractiveComponentSize()`. |
| M4 | Medium | Navigation icons repeated the visible label. The HITL badge count had no description. | Nav icons are decorative when the label is visible. The badge sets a content description of the awaiting-approval count. |

## Localization

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| H6 | High | User-visible chrome lived in composables. `strings.xml` only had `app_name`. | Chrome, toasts, and content descriptions are in `res/values/strings.xml`. |
| L1 | Low | Seed task logs and the clipboard CSV/JSON export stay English data. | Open. They are sample records, not chrome. |

## Performance

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| M2 | Medium | `LazyRow` filter chips had no keys. | `items(..., key = { it })`. |
| M13 | Medium | No baseline profile. | Open. A profile needs a Macrobenchmark module and device traces. Startup is not otherwise changed. |
| L5 | Low | No startup tracing. | Open. Same reason as M13. |

`LazyColumn` rows were already keyed.

## Testing

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| H7 | High | Tests were `2 + 2`, the app name, and the package name. Nothing covered approve, deny, or kill. | `AgentOSRepositoryTest` covers hint-only storage, approve, reject, kill, and the in-flight approval race. `AgentOSViewModelTest` checks that approve closes the sheet. `HitlDecisionUiTest` clicks `reject_hitl_action` and `approve_hitl_action`. |

## Play policy risks

| ID | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| L3 | Low | Firebase `ComponentDiscoveryService` can still be merged from the unused Firebase libraries. `FirebaseInitProvider` is already removed because there is no `google-services.json`. | Open. Removing `firebase-ai` and App Check is a dependency cleanup, not required for this client fix. |
| L4 | Low | Some 9–11.sp labels are low contrast on dark surfaces (`TextMuted` on `BackgroundDark`). | Open. Raising them changes the visual system. |
| L2 | Low | The Settings role chips are labels. Nothing enforces Viewer, Operator, or Admin. | Open. Enforcement without accounts would be fake security. |

Other Play notes already handled on the release branch: `targetSdk` 36, `allowBackup=false`, no advertising id, INTERNET only as a requested permission, 16 KB ELF alignment of the packaged native libraries. A privacy policy and Data safety form are Play Console steps, not an in-app feature.

## What this branch did not add

No backend, account system, push, or new screens. The UI layout is the same control plane with a system light theme, larger touch targets, and string resources.

Dead `AgentViewModel` was removed. It was unused.

## Recommendations that cannot be fixed in the client

These are not findings to implement here.

1. **Server-enforced kill switch.** This app can only stop work it simulated locally. A real agent will keep running until the server refuses new steps and aborts in-flight calls when the switch is engaged.
2. **Server-enforced spending limits.** The daily cap and circuit breaker are local settings. The provider bill is unchanged until the backend rejects calls over the cap.
3. **Real agent connectivity.** Endpoints are stored and a ping is a delay. There is no authenticated call to an agent runtime.
4. **Authentication.** Role chips do not identify an operator. Approvals need a signed-in user on the server.
5. **Push for approvals.** `pushNotificationsEnabled` is a stored flag. FCM (or equivalent) has to deliver HITL requests when the app is backgrounded.
6. **Play Console.** Publish a privacy policy URL and complete Data safety before production. Internal testing still needs the release key from the release-readiness workflow.

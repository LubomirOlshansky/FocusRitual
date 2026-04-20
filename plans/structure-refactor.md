# composeApp Structure Refactor

Pure file-move + import-update refactor of `composeApp/src/{commonMain,iosMain,androidMain}/kotlin/com/focusritual/app/`. No behavior change, no API change, no new dependencies.

---

## 1. Goal & Non-Goals

**Goal.** Enforce a consistent feature layout, eliminate `core → feature` layer violations, and create an explicit home for multi-feature integration glue.

**Non-goals (D8).**
- No Koin DI (Phase 6 of mixer-refactor remains deferred).
- No module split (single `composeApp` module).
- No `iosApp/` Swift file changes.
- No new dependencies.
- No Compose API or contract changes.
- No `AtmosphericField` hex-literal cleanup.
- No `commonTest` directory restructure (tests stay flat — D7).

---

## 2. Target Directory Tree (post-refactor)

### `commonMain/kotlin/com/focusritual/app/`

```
app/
├── App.kt
├── Platform.kt
└── integration/                                           (NEW)
    └── liveactivity/                                      (NEW)
        └── LiveActivityEffect.kt                          (was: core/liveactivity/LiveActivityEffect.kt)
core/
├── audio/
│   ├── AudioCommand.kt
│   ├── AudioPlayer.kt
│   ├── AudioPlayerFactory.kt
│   ├── AudioPlayerHandle.kt
│   ├── OrganicMotionEngine.kt
│   └── SoundMixer.kt
├── designsystem/
│   ├── component/
│   │   ├── AirPlayButton.kt
│   │   ├── CloseButton.kt
│   │   ├── PlayButton.kt
│   │   ├── ProtectFocusCard.kt
│   │   ├── ProtectFocusSetupSheet.kt
│   │   ├── StartSessionButton.kt
│   │   ├── StepperRow.kt
│   │   └── VolumeSlider.kt
│   └── theme/  (unchanged)
└── protectfocus/  (unchanged)
feature/
├── about/                                                 (unchanged — flat, 2 files)
│   ├── AboutSheet.kt
│   └── SoundCredit.kt
├── mixer/
│   ├── CurrentMixModal.kt                                 (screen-level; stays at root)
│   ├── MixerContract.kt
│   ├── MixerScreen.kt
│   ├── MixerViewModel.kt
│   ├── domain/
│   │   ├── MixAudioOrchestrator.kt                        (was: domain/audio/MixAudioOrchestrator.kt)
│   │   ├── MixRepository.kt
│   │   ├── MixerDtos.kt
│   │   ├── MixerMappers.kt
│   │   ├── SoundCatalog.kt
│   │   ├── SoundCatalogImpl.kt
│   │   ├── SoundState.kt                                  (was: model/SoundState.kt)
│   │   └── usecase/  (unchanged — 7 use-case files)
│   └── ui/
│       ├── CategoryPillRow.kt
│       ├── CurrentMixPanel.kt
│       ├── HeroSessionButton.kt
│       ├── ImmersiveBackground.kt
│       ├── SectionHeader.kt
│       ├── SoundTile.kt                                   (was: core/designsystem/component/SoundTile.kt)
│       └── modal/                                         (NEW path; was feature/mixer/modal/)
│           ├── ActiveSoundRow.kt
│           ├── DoneButton.kt
│           ├── GlobalOrganicMotionRow.kt
│           └── ModalHeader.kt
├── session/
│   ├── FocusSessionContract.kt
│   ├── FocusSessionScreen.kt
│   ├── FocusSessionViewModel.kt
│   ├── SessionPreferences.kt
│   └── ui/                                                (NEW)
│       └── SessionModeToggle.kt                           (was: core/designsystem/component/SessionModeToggle.kt)
└── timer/
    ├── ActiveSessionContract.kt
    ├── ActiveSessionScreen.kt
    ├── ActiveSessionViewModel.kt
    ├── SessionCompleteScreen.kt
    └── ui/                                                (NEW)
        ├── AtmosphericField.kt                            (was: feature/timer/AtmosphericField.kt)
        ├── SessionBackground.kt                           (was: feature/timer/SessionBackground.kt)
        └── SessionControls.kt                             (was: feature/timer/SessionControls.kt)
```

### `iosMain/kotlin/com/focusritual/app/`

```
MainViewController.kt
Platform.ios.kt
app/                                                       (NEW)
└── integration/                                           (NEW)
    └── liveactivity/                                      (NEW)
        └── LiveActivityEffect.ios.kt                      (was: core/liveactivity/LiveActivityEffect.ios.kt)
core/
├── audio/AudioPlayer.ios.kt
├── designsystem/component/
│   ├── AirPlayButton.ios.kt
│   ├── ProtectFocusCard.ios.kt
│   └── ProtectFocusSetupSheet.ios.kt
├── liveactivity/                                          (retained — pure platform infra)
│   ├── LiveActivityBridge.kt
│   ├── LiveActivityController.kt
│   └── LiveActivityState.kt
└── protectfocus/  (unchanged)
```

### `androidMain/kotlin/com/focusritual/app/`

```
MainActivity.kt
Platform.android.kt
app/                                                       (NEW)
└── integration/                                           (NEW)
    └── liveactivity/                                      (NEW)
        └── LiveActivityEffect.android.kt                  (was: core/liveactivity/LiveActivityEffect.kt — D5 rename)
core/
├── audio/  (unchanged)
├── designsystem/component/  (unchanged — 3 stubs)
└── protectfocus/  (unchanged)
```

---

## 3. Architectural Principles Enforced

- **Strict layer direction:** `app/` → `feature/` → `core/`. `core/` may never import from `feature/`. `feature/` may not import from another `feature/`.
- **`app/integration/`** is the explicit home for code that legitimately spans multiple features (e.g. LiveActivity driver consuming `MixerUiState` + `ActiveSessionUiState` + `SessionMode`).
- **Canonical feature shape (D1):** `<Name>Screen.kt` + `<Name>ViewModel.kt` + `<Name>Contract.kt` + `ui/` (sub-composables, optional `ui/modal/`) + `domain/` (models, repos, mappers, orchestrators, contracts; `domain/usecase/` for single-responsibility use cases) + `data/` (only when persistence/network exists — none today).
- **Platform actuals** use `<Name>.<platform>.kt` suffix (e.g. `LiveActivityEffect.android.kt`, mirroring existing `.ios.kt` convention — D5).
- **File-private composables** stay where they are; this refactor does not extract them.

---

## 4. Phased Plan

Each phase is independently buildable and testable. Run [Verification Commands](#8-verification-commands) at end of every phase.

### Phase A — Move feature-specific UI out of `core/designsystem`

| | |
|---|---|
| **Scope** | Relocate 2 components mis-filed under design system. |
| **Moves** | `core/designsystem/component/SoundTile.kt` → `feature/mixer/ui/SoundTile.kt`; `core/designsystem/component/SessionModeToggle.kt` → `feature/session/ui/SessionModeToggle.kt` (creates `feature/session/ui/`). |
| **Package decl edits** | `package com.focusritual.app.core.designsystem.component` → `feature.mixer.ui` / `feature.session.ui` respectively. |
| **Import updates** | `MixerScreen.kt`, `feature/mixer/ui/CurrentMixPanel.kt` (if uses `SoundTile`), `feature/mixer/ui/modal/ActiveSoundRow.kt` (if uses `SoundTile`), `FocusSessionScreen.kt`. Verify with grep over `commonMain` + `commonTest`. |
| **Acceptance** | All targets compile; all tests pass; grep for old import strings returns zero. |
| **Risk** | Low. Both files are file-internal in name and have a small import surface. |
| **Rollback** | `git revert` of phase commit. |

### Phase B — Create `app/integration/liveactivity/`; relocate effect files

| | |
|---|---|
| **Scope** | Move the 3 `LiveActivityEffect*` files (commonMain expect + iosMain actual + androidMain actual); rename Android actual per D5. iOS bridge plumbing (`LiveActivityBridge.kt`, `LiveActivityController.kt`, `LiveActivityState.kt`) **stays** in `iosMain core/liveactivity/` (no feature imports). |
| **Moves** | `commonMain core/liveactivity/LiveActivityEffect.kt` → `app/integration/liveactivity/LiveActivityEffect.kt`; `iosMain core/liveactivity/LiveActivityEffect.ios.kt` → `app/integration/liveactivity/LiveActivityEffect.ios.kt`; `androidMain core/liveactivity/LiveActivityEffect.kt` → `app/integration/liveactivity/LiveActivityEffect.android.kt` (rename + move). |
| **Package decl edits** | All three files: `package com.focusritual.app.core.liveactivity` → `package com.focusritual.app.app.integration.liveactivity`. |
| **Import updates** | `App.kt` (only known caller); any remaining call sites found via grep. The iosMain effect file's existing imports of `core.liveactivity.LiveActivityBridge/Controller/State` remain valid (those files did not move). |
| **Acceptance** | All targets compile; expect/actual still resolve (same FQN across source sets); `grep -rn "import com.focusritual.app.feature" composeApp/src/*/kotlin/com/focusritual/app/core/` returns **zero**. |
| **Risk** | Medium — expect/actual matching depends on identical FQN across source sets. Mitigation: rename package in all three files in the same commit. |
| **Rollback** | `git revert`. |

### Phase C — Mixer internal reshuffling

| | |
|---|---|
| **Scope** | Collapse `feature/mixer/modal/` into `feature/mixer/ui/modal/`; move `feature/mixer/model/SoundState.kt` into `feature/mixer/domain/`; move `feature/mixer/domain/audio/MixAudioOrchestrator.kt` up into `feature/mixer/domain/`. Delete now-empty `model/` and `domain/audio/` folders. |
| **Moves** | `feature/mixer/modal/ActiveSoundRow.kt` → `feature/mixer/ui/modal/ActiveSoundRow.kt`; same for `DoneButton.kt`, `GlobalOrganicMotionRow.kt`, `ModalHeader.kt`; `feature/mixer/model/SoundState.kt` → `feature/mixer/domain/SoundState.kt`; `feature/mixer/domain/audio/MixAudioOrchestrator.kt` → `feature/mixer/domain/MixAudioOrchestrator.kt`. |
| **Package decl edits** | `feature.mixer.modal` → `feature.mixer.ui.modal`; `feature.mixer.model` → `feature.mixer.domain`; `feature.mixer.domain.audio` → `feature.mixer.domain`. |
| **Import updates** | Across `MixerScreen.kt`, `MixerViewModel.kt`, `MixerContract.kt`, `CurrentMixModal.kt`, `feature/mixer/ui/*`, `feature/mixer/domain/*`, all `feature/mixer/domain/usecase/*` files; plus tests in `commonTest/.../feature/mixer/domain/**` (production-symbol imports only — test files stay where they are per D7). |
| **Acceptance** | All targets compile; `:composeApp:allTests` green; `model/` and `domain/audio/` directories absent; no stale imports. |
| **Risk** | Medium — largest fan-out. Mitigation: do mass find-replace via the [Import-Update Guide](#6-import-update-guide). |
| **Rollback** | `git revert`. |

### Phase D — Timer `ui/` split

| | |
|---|---|
| **Scope** | Move 3 sub-composables off the timer feature root into `feature/timer/ui/`. Screen + VM + Contract + `SessionCompleteScreen` stay at root (D4). |
| **Moves** | `feature/timer/AtmosphericField.kt` → `feature/timer/ui/AtmosphericField.kt`; `feature/timer/SessionBackground.kt` → `feature/timer/ui/SessionBackground.kt`; `feature/timer/SessionControls.kt` → `feature/timer/ui/SessionControls.kt`. |
| **Package decl edits** | `feature.timer` → `feature.timer.ui` for the three moved files. |
| **Import updates** | `ActiveSessionScreen.kt`, `SessionCompleteScreen.kt`, and any cross-references between the three moved files. |
| **Acceptance** | All targets compile; visual smoke = unchanged (no behavior touched). |
| **Risk** | Low. Same package, same module, only sub-composable composables. |
| **Rollback** | `git revert`. |

### Phase E — Verification + memory updates

| | |
|---|---|
| **Scope** | Final cross-target build matrix; grep verification; update Serena `project_structure` memory to reflect the new tree, the `app/integration/` package, and the new feature-canonical layout. |
| **Acceptance** | (1) `:composeApp:assembleDebug` green; (2) `:composeApp:linkDebugFrameworkIosSimulatorArm64` green; (3) `:composeApp:allTests` green; (4) `grep -rn "import com.focusritual.app.feature" composeApp/src/*/kotlin/com/focusritual/app/core/` → **zero results**; (5) `grep -rn "package com.focusritual.app.\(core.liveactivity.LiveActivityEffect\|feature.mixer.modal\|feature.mixer.model\|feature.mixer.domain.audio\|core.designsystem.component.SoundTile\|core.designsystem.component.SessionModeToggle\)" composeApp/src` → **zero results**; (6) `project_structure` memory updated. |
| **Risk** | None — verification only. |
| **Rollback** | N/A. |

---

## 5. File-Move Map

| Old path | New path | Phase |
|---|---|---|
| `composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/SoundTile.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/SoundTile.kt` | A |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/SessionModeToggle.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/session/ui/SessionModeToggle.kt` | A |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/core/liveactivity/LiveActivityEffect.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/app/integration/liveactivity/LiveActivityEffect.kt` | B |
| `composeApp/src/iosMain/kotlin/com/focusritual/app/core/liveactivity/LiveActivityEffect.ios.kt` | `composeApp/src/iosMain/kotlin/com/focusritual/app/app/integration/liveactivity/LiveActivityEffect.ios.kt` | B |
| `composeApp/src/androidMain/kotlin/com/focusritual/app/core/liveactivity/LiveActivityEffect.kt` | `composeApp/src/androidMain/kotlin/com/focusritual/app/app/integration/liveactivity/LiveActivityEffect.android.kt` | B |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/modal/ActiveSoundRow.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/ActiveSoundRow.kt` | C |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/modal/DoneButton.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/DoneButton.kt` | C |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/modal/GlobalOrganicMotionRow.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/GlobalOrganicMotionRow.kt` | C |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/modal/ModalHeader.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/ModalHeader.kt` | C |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/model/SoundState.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/SoundState.kt` | C |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/audio/MixAudioOrchestrator.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixAudioOrchestrator.kt` | C |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/timer/AtmosphericField.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/timer/ui/AtmosphericField.kt` | D |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/timer/SessionBackground.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/timer/ui/SessionBackground.kt` | D |
| `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/timer/SessionControls.kt` | `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/timer/ui/SessionControls.kt` | D |

**Total:** 14 files moved (1 also renamed: `LiveActivityEffect.kt` → `LiveActivityEffect.android.kt`).

**Empty directories to delete after Phase C:** `feature/mixer/model/`, `feature/mixer/domain/audio/`, `feature/mixer/modal/`. **After Phase B:** `commonMain/.../core/liveactivity/`, `androidMain/.../core/liveactivity/`. (`iosMain core/liveactivity/` is retained.)

---

## 6. Import-Update Guide

Find-and-replace these import strings across `composeApp/src/{commonMain,iosMain,androidMain,commonTest}`. Test files only need the production-symbol import lines updated (D7 — no test moves).

| Old import | New import | Phase |
|---|---|---|
| `com.focusritual.app.core.designsystem.component.SoundTile` | `com.focusritual.app.feature.mixer.ui.SoundTile` | A |
| `com.focusritual.app.core.designsystem.component.SessionModeToggle` | `com.focusritual.app.feature.session.ui.SessionModeToggle` | A |
| `com.focusritual.app.core.liveactivity.LiveActivityEffect` | `com.focusritual.app.app.integration.liveactivity.LiveActivityEffect` | B |
| `com.focusritual.app.core.liveactivity.rememberLiveActivityEffect` *(if such a top-level helper exists)* | `com.focusritual.app.app.integration.liveactivity.rememberLiveActivityEffect` | B |
| `com.focusritual.app.feature.mixer.modal.ActiveSoundRow` | `com.focusritual.app.feature.mixer.ui.modal.ActiveSoundRow` | C |
| `com.focusritual.app.feature.mixer.modal.DoneButton` | `com.focusritual.app.feature.mixer.ui.modal.DoneButton` | C |
| `com.focusritual.app.feature.mixer.modal.GlobalOrganicMotionRow` | `com.focusritual.app.feature.mixer.ui.modal.GlobalOrganicMotionRow` | C |
| `com.focusritual.app.feature.mixer.modal.ModalHeader` | `com.focusritual.app.feature.mixer.ui.modal.ModalHeader` | C |
| `com.focusritual.app.feature.mixer.model.SoundState` | `com.focusritual.app.feature.mixer.domain.SoundState` | C |
| `com.focusritual.app.feature.mixer.model.SoundIcon` | `com.focusritual.app.feature.mixer.domain.SoundIcon` | C |
| `com.focusritual.app.feature.mixer.model.defaultSounds` | `com.focusritual.app.feature.mixer.domain.defaultSounds` | C |
| `com.focusritual.app.feature.mixer.model.*` | `com.focusritual.app.feature.mixer.domain.*` | C |
| `com.focusritual.app.feature.mixer.domain.audio.MixAudioOrchestrator` | `com.focusritual.app.feature.mixer.domain.MixAudioOrchestrator` | C |
| `com.focusritual.app.feature.mixer.domain.audio.*` | `com.focusritual.app.feature.mixer.domain.*` | C |
| `com.focusritual.app.feature.timer.AtmosphericField` | `com.focusritual.app.feature.timer.ui.AtmosphericField` | D |
| `com.focusritual.app.feature.timer.SessionBackground` | `com.focusritual.app.feature.timer.ui.SessionBackground` | D |
| `com.focusritual.app.feature.timer.SessionControls` | `com.focusritual.app.feature.timer.ui.SessionControls` | D |

Each moved file's own `package` declaration must also be updated to match the new directory (Kotlin/IntelliJ does not enforce, but our convention requires it).

---

## 7. Risk Register

| # | Risk | Mitigation |
|---|---|---|
| 1 | **Missed import** in a transitive caller (e.g. test file or sibling composable in same package that previously imported via star-import). | After each phase: full target-matrix build + `:composeApp:allTests` + grep for old FQNs (see [Verification Commands](#8-verification-commands)). |
| 2 | **IDE / Gradle caches** stale after package renames cause spurious "unresolved reference" errors. | Run `./gradlew --stop && ./gradlew clean` once after Phase B and once after Phase C; in Android Studio, *File → Invalidate Caches*. |
| 3 | **Live Activity expect/actual mismatch** if commonMain, iosMain, androidMain effect files end up in different packages mid-phase. | Move all 3 LiveActivity files in the **same commit** (Phase B). Verify expect/actual link by running `:composeApp:linkDebugFrameworkIosSimulatorArm64` immediately after the commit. |
| 4 | **`.android.kt` rename** breaks build if any other code references the old simple class name through file-name-based mechanism (none expected — Kotlin uses FQN). | Confirm zero references to the file by name; rely on `actual` keyword resolution. The rename is cosmetic; Kotlin does not bind to file names. |
| 5 | **Accidental behavior change** if a copy-paste during package edit drops a line. | Use `git mv` (preserves history) and edit only the `package` line + the import lines. Diff each commit: `git diff --stat` should show ≈ 1–3 line delta per moved file beyond the rename itself. |

---

## 8. Verification Commands

Run after **every phase**:

```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
./gradlew :composeApp:allTests
```

Layer-violation grep (must return **zero** lines after Phase B and forever after):

```bash
grep -rn "import com.focusritual.app.feature" \
    composeApp/src/commonMain/kotlin/com/focusritual/app/core/ \
    composeApp/src/iosMain/kotlin/com/focusritual/app/core/ \
    composeApp/src/androidMain/kotlin/com/focusritual/app/core/
```

Stale-FQN grep (must return **zero** lines after Phase E):

```bash
grep -rnE "com\.focusritual\.app\.(core\.liveactivity\.LiveActivityEffect|feature\.mixer\.(modal|model|domain\.audio)|core\.designsystem\.component\.(SoundTile|SessionModeToggle)|feature\.timer\.(AtmosphericField|SessionBackground|SessionControls))" \
    composeApp/src
```

Empty-folder check (must return **zero** lines after Phase E):

```bash
find composeApp/src -type d -empty
```

---

## 9. Decisions Log

- **D1.** Canonical feature layout: `<Name>Screen.kt` + `<Name>ViewModel.kt` + `<Name>Contract.kt` + `ui/` (sub-composables; `ui/modal/` permitted) + `domain/` (models, repos, mappers, orchestrators, contracts; `domain/usecase/` for use cases) + `data/` (only when persistence/network present — currently none).
- **D2.** Fix `core → feature` violations two ways: (a) move feature-specific UI back into the feature (`SoundTile` → `feature/mixer/ui/`, `SessionModeToggle` → `feature/session/ui/`); (b) introduce `app/integration/` package for multi-feature glue, and move the 3 `LiveActivityEffect*` files there. iOS bridge plumbing (`LiveActivityBridge.kt`, `LiveActivityController.kt`, `LiveActivityState.kt`) **stays** in `iosMain core/liveactivity/` because it imports no feature.
- **D3.** Mixer-specific moves: collapse `feature/mixer/modal/*` → `feature/mixer/ui/modal/*`; move `feature/mixer/model/SoundState.kt` → `feature/mixer/domain/SoundState.kt` (delete `model/`); move `feature/mixer/domain/audio/MixAudioOrchestrator.kt` → `feature/mixer/domain/MixAudioOrchestrator.kt` (delete `domain/audio/`). `MixerScreen.kt` and `CurrentMixModal.kt` stay at the feature root; `CurrentMixModal.kt` is treated as screen-level.
- **D4.** Timer split: keep at root — `ActiveSessionContract.kt`, `ActiveSessionViewModel.kt`, `ActiveSessionScreen.kt`, `SessionCompleteScreen.kt`. Move into `feature/timer/ui/` — `AtmosphericField.kt`, `SessionBackground.kt`, `SessionControls.kt`.
- **D5.** Android actuals naming: rename `androidMain/.../core/liveactivity/LiveActivityEffect.kt` → `LiveActivityEffect.android.kt` (matches existing `.ios.kt` convention). Performed as part of the D2 move.
- **D6.** Session + About cleanup: `feature/session/` requires no production moves beyond the `SessionModeToggle` arrival from Phase A (already clean: Contract / VM / Screen + `SessionPreferences`). `FocusSessionScreen.kt` (432 LOC) is large but its sub-composables are file-private — extraction is **out of scope** here (future task). `feature/about/` stays flat (2 files); acknowledged in the `project_structure` memory update in Phase E.
- **D7.** Test mirroring: tests stay flat under `commonTest/.../feature/mixer/domain/**`. Only their **production-symbol imports** are updated to the new packages. No test files are moved.
- **D8.** Out-of-scope: no Koin DI; no module split; no `iosApp/` Swift changes; no new dependencies; no Compose API changes; no `AtmosphericField` hex cleanup. Pure file-move + import-update only.

---

## 10. Out-of-Scope Follow-ups

- Koin DI rollout (mixer-refactor Phase 6).
- `AtmosphericField.kt` hex-literal → design-token cleanup.
- `FocusSessionScreen.kt` (432 LOC) extraction of file-private sub-composables into `feature/session/ui/`.
- `multiplatform-settings` integration for `SessionPreferences` persistence.
- Possible module split (`:core`, `:feature-mixer`, `:feature-session`, `:feature-timer`) once feature surface stabilises.
- Test-tree mirroring of the new feature `ui/` and `domain/` packages (currently flat per D7).

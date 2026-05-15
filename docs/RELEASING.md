# Releasing frnk

frnk ships as **source** — there are no Maven artifacts, no AARs, and no published XCFrameworks. Downstream apps consume it as a Git submodule + Gradle composite build (`includeBuild("../frnk")`), and pin to a specific version by checking out a release tag on the submodule.

A release therefore consists of three things:

1. A signed Git tag `vMAJOR.MINOR.PATCH` on `main`.
2. A `CHANGELOG.md` section describing what changed.
3. A GitHub Release with those notes — auto-created by `.github/workflows/release.yml` on tag push.

## Versioning policy

We follow [Semantic Versioning 2.0](https://semver.org/spec/v2.0.0.html).

While the API is in `0.x`:

| Bump   | What it means                                                  |
| ------ | -------------------------------------------------------------- |
| `0.x.0` | May include breaking API changes. Read the release notes before bumping. |
| `0.x.y` | Additive features and bug fixes only. Safe to take without inspection. |

Once `1.0.0` ships, standard SemVer applies: breaking changes go in `MAJOR`.

Pre-release tags follow the `vX.Y.Z-rcN` / `-betaN` / `-alphaN` shape (e.g. `v0.2.0-rc1`). The release workflow detects the hyphen and marks them as GitHub pre-releases.

## How to cut a release

You need: a clean `main` with all wanted changes merged and CI green.

1. **Decide the next version.** Look at the `## [Unreleased]` section of `CHANGELOG.md`. If it contains anything under `Changed` or `Removed` that is API-affecting, bump `MINOR` (pre-1.0) or `MAJOR` (post-1.0). Otherwise bump `PATCH`.

2. **Update `Frnk.VERSION`** in `shared-utils/src/commonMain/kotlin/dev/jdgarita/frnk/utils/Frnk.kt`. The release workflow refuses to publish if this doesn't match the tag.

3. **Update `CHANGELOG.md`:**
   - Rename `## [Unreleased]` to `## [X.Y.Z] - YYYY-MM-DD`.
   - Drop any empty subsections (`### Added` / `### Changed` / `### Fixed` / `### Removed`) for that version.
   - Add a fresh empty `## [Unreleased]` block at the top.
   - Update the link references at the bottom (`[Unreleased]: ...compare/vX.Y.Z...HEAD` and add a `[X.Y.Z]: ...releases/tag/vX.Y.Z`).

4. **Commit and tag:**

   ```bash
   git add CHANGELOG.md shared-utils/src/commonMain/kotlin/dev/jdgarita/frnk/utils/Frnk.kt
   git commit -m "Release vX.Y.Z"
   git tag -a vX.Y.Z -m "vX.Y.Z"
   ```

5. **Push:**

   ```bash
   git push origin main
   git push origin vX.Y.Z
   ```

The `release` workflow fires on the tag push, verifies `Frnk.VERSION` matches, extracts the matching CHANGELOG section, and creates the GitHub Release. Watch it at <https://github.com/jdgarita/frnk/actions>.

## If the release workflow fails

- **`Frnk.VERSION` mismatch:** delete the tag (`git tag -d vX.Y.Z && git push origin :refs/tags/vX.Y.Z`), fix `Frnk.kt`, commit, re-tag, re-push.
- **No CHANGELOG section found:** the workflow falls back to GitHub's auto-generated notes (PR list since the previous tag). You can edit the release body manually afterwards on the GitHub UI.
- **Anything else:** delete the tag as above and re-run after fixing.

## Hotfixes

We currently do **not** maintain release branches. If a critical fix is needed:

1. Cherry-pick or land the fix on `main`.
2. Cut a `PATCH` release as described above.

If at some point we need to support multiple majors in parallel (e.g. patch `0.4.x` while `main` is on `0.5.x`), we'll introduce `release/0.4` branches at that point — not before.

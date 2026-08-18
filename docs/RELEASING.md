# Releasing frnk

frnk ships as **source** — there are no Maven artifacts, no AARs, and no published XCFrameworks. Downstream apps consume it as a Git submodule + Gradle composite build (`includeBuild("../frnk")`), and pin to a specific version by checking out a release tag on the submodule.

A release therefore consists of three things:

1. An annotated Git tag (`vMAJOR.MINOR.PATCH` or a prerelease tag like `vX.Y.Z-alphaN`) on a commit reachable from `main`.
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

Prerelease tags follow the `vX.Y.Z-alphaN` / `-betaN` / `-rcN` shape (e.g. `v0.2.0-alpha1`). The release workflow detects the hyphen and marks them as GitHub prereleases.

## How to cut a release

You need: a clean `main` with all wanted changes landed and **validated locally** — build/test CI is paused while the repo is private (only `release.yml` runs, on tag push), so run `./gradlew compileAndroidMain :demo-android:compileDebugKotlin` and `./gradlew testAndroidHostTest :demo-android:testDebugUnitTest` yourself before tagging.

`main` is protected: direct pushes are rejected, and a PR needs one **code-owner approval** (plus re-approval after any further push to it). The release bookkeeping commit therefore goes through a PR like every other change, and the tag is pushed **only once that PR has merged** — see the callout in step 5.

1. **Decide the next version.** Look at the `## [Unreleased]` section of `CHANGELOG.md`. If it contains anything under `Changed` or `Removed` that is API-affecting, bump `MINOR` (pre-1.0) or `MAJOR` (post-1.0). Otherwise bump `PATCH`.

   Check what is actually tagged rather than trusting the CHANGELOG — a version can be fully written up, and even have `Frnk.VERSION` bumped, without a tag ever being pushed (`0.3.0` sat like that from 2026-08-11 until it was backfilled a week later):

   ```bash
   git fetch origin --tags
   git tag --sort=-v:refname | head -5
   gh release list --limit 5
   ```

2. **Update `Frnk.VERSION`** in `frnk/core/util/src/commonMain/kotlin/dev/jdgarita/frnk/utils/Frnk.kt`. The release workflow refuses to publish if this doesn't match the tag.

3. **Update `CHANGELOG.md`:**
   - Rename `## [Unreleased]` to `## [X.Y.Z] - YYYY-MM-DD` or `## [X.Y.Z-alphaN] - YYYY-MM-DD`.
   - Drop any empty subsections (`### Added` / `### Changed` / `### Fixed` / `### Removed`) for that version.
   - Add a fresh empty `## [Unreleased]` block at the top.
   - Update the link references at the bottom (`[Unreleased]: ...compare/vX.Y.Z-alphaN...HEAD` and add a `[X.Y.Z-alphaN]: ...releases/tag/vX.Y.Z-alphaN`).
   - Cross-check the section against `git log <last-tag>..HEAD`: a merged PR that shipped no CHANGELOG note of its own leaves a gap that only shows up here.

4. **Commit on a branch and open a PR:**

   ```bash
   git switch -c release/v0.3.1
   git add CHANGELOG.md frnk/core/util/src/commonMain/kotlin/dev/jdgarita/frnk/utils/Frnk.kt
   git commit -m "Release v0.3.1"
   git push -u origin release/v0.3.1
   gh pr create --base main --title "Release v0.3.1" --body "Cuts v0.3.1 — see CHANGELOG.md."
   ```

5. **Merge, then tag the commit that landed on `main`:**

   ```bash
   gh pr merge --squash --delete-branch      # --merge / --rebase are allowed too
   git checkout main && git pull --ff-only origin main
   git log --oneline -1                      # this is the commit to tag
   git tag -a v0.3.1 -m "v0.3.1"
   git push origin v0.3.1
   git branch --contains v0.3.1              # must print main
   ```

   > **Tag last, on its own push, after the merge.** Two traps, both hit while cutting `v0.3.1`:
   >
   > - `git push origin main v0.3.1` is **not atomic**. The ruleset rejects `main`, the tag lands anyway, `release.yml` fires, and you get a published GitHub Release for a commit that is on no branch.
   > - Tagging the pre-merge commit does not survive a **squash** merge (the default here), which rewrites the SHA. The tree is identical, so nothing is broken for a consumer who already pinned it, but the tag points at an orphan and `git branch --contains` comes back empty.
   >
   > Both are repaired the same way — see "Undoing a bad tag".

The `release` workflow fires on the tag push, verifies `Frnk.VERSION` matches, extracts the matching CHANGELOG section, and creates the GitHub Release. Watch it at <https://github.com/jdgarita/frnk/actions>, or:

```bash
gh run watch "$(gh run list --workflow release.yml --limit 1 --json databaseId --jq '.[0].databaseId')"
gh release view v0.3.1 --web
```

## Undoing a bad tag

A tag on the wrong commit, a `Frnk.VERSION` mismatch that failed the workflow, a Release published against an orphaned SHA — all the same repair. Tags sit outside the branch ruleset, so none of this needs a PR:

```bash
gh release delete v0.3.1 --yes            # only if the workflow got far enough to publish one
git push origin :refs/tags/v0.3.1
git tag -d v0.3.1
# fix whatever was wrong, then re-tag the right commit:
git tag -a v0.3.1 <sha> -m "v0.3.1"
git push origin v0.3.1                    # release.yml recreates the Release from the CHANGELOG
```

Deleting a tag is only cheap while the release is fresh — anyone who already pinned it keeps the old commit and will not see the move. Once a release has been out for more than a moment, cut a new `PATCH` instead.

## If the release workflow fails

- **`Frnk.VERSION` mismatch:** the workflow tells you both values. Delete the tag as above, fix `Frnk.kt`, land it through a PR, re-tag, re-push.
- **No CHANGELOG section found:** the workflow falls back to GitHub's auto-generated notes (PR list since the previous tag) and logs a warning rather than failing. Either edit the release body on the GitHub UI, or fix the `## [X.Y.Z]` heading and recut.
- **Anything else:** delete the tag as above and re-run after fixing.

## Hotfixes

We currently do **not** maintain release branches. If a critical fix is needed:

1. Cherry-pick or land the fix on `main`.
2. Cut a `PATCH` release as described above.

If at some point we need to support multiple majors in parallel (e.g. patch `0.4.x` while `main` is on `0.5.x`), we'll introduce `release/0.4` branches at that point — not before.

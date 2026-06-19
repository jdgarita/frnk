package dev.jdgarita.frnk.ui.scaffolds.onboarding.ext

import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingPageModel
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingPageState
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize

internal fun OnboardingPageModel.toUiState(): OnboardingPageState =
    OnboardingPageState(
        title = FrnkTextState.Title(content = this.title),
        description =
            FrnkTextState.Body(
                content = this.description
            ),
        icon =
            FrnkIconState.Content(
                icon = this.icon,
                contentDescription = null, // todo: revisit this later
                size = FrnkIconSize.xxl,
                tint = colorPrimary
            )
    )
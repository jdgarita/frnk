package dev.jdgarita.frnk.demo.ui.onboarding

import androidx.compose.runtime.Composable
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import dev.jdgarita.frnk.ui.mvi.FrnkScreen
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.scaffolds.onboarding.FrnkOnboardingScreen
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingArguments
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingPageModel
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingViewModel
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(onEffect: (uiEffect: UiEffect) -> Unit) {
    val viewModel: OnboardingViewModel = koinViewModel()

    FrnkScreen(
        arguments =
            OnboardingArguments(
                pages =
                    listOf(
                        OnboardingPageModel(
                            title = FrnkStringSource.Raw("Welcome to Frnk"),
                            description =
                                FrnkStringSource.Raw(
                                    "A Kotlin Multiplatform toolkit to ship polished apps in days, not weeks."
                                ),
                            icon = FrnkIconSource.Vector(Lucide.Check)
                        ),
                        OnboardingPageModel(
                            title = FrnkStringSource.Raw("Search everything"),
                            description = FrnkStringSource.Raw("Typed, paginated, offline-ready data access across every source."),
                            icon = FrnkIconSource.Vector(Lucide.Search)
                        ),
                        OnboardingPageModel(
                            title = FrnkStringSource.Raw("Ready when you are"),
                            description = FrnkStringSource.Raw("Tap Get Started to begin your first session."),
                            icon = FrnkIconSource.Vector(Lucide.Settings)
                        )
                    )
            ),
        viewModel = viewModel,
        onEffect = onEffect
    ) { state ->
        FrnkOnboardingScreen(
            state = state,
            onIntent = viewModel::send
        )
    }
}
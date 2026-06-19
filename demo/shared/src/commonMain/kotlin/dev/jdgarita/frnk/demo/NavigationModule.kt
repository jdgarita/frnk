package dev.jdgarita.frnk.demo

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import dev.jdgarita.frnk.ui.mvi.FrnkScreen
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingArguments
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingPageModel
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingScreen
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingViewModel
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
fun navigationModule(backStack: NavBackStack<NavKey>) =
    module {
        navigation<FrnkRoute.Onboarding> {
            val onboardingViewModel: OnboardingViewModel = koinViewModel()

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
                viewModel = onboardingViewModel,
                onEffect = { backStack.back() }
            ) { state ->
                OnboardingScreen(
                    state = state,
                    onIntent = onboardingViewModel::send
                )
            }
        }
    }
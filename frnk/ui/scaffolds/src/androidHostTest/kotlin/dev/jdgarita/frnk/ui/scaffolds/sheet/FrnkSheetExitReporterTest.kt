package dev.jdgarita.frnk.ui.scaffolds.sheet

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the exit reporter's state machine: one report per presentation, from whichever signal
 * arrives first, and nothing for a hide the host performed itself.
 */
class FrnkSheetExitReporterTest {
    private var reports = 0
    private val reporter = FrnkSheetExitReporter { reports += 1 }

    @Test
    fun hiddenSheetThatWasNeverPresentedReportsNothing() {
        reporter.observe(visible = false, settledHidden = true)
        reporter.presented()
        reporter.observe(visible = true, settledHidden = true)

        assertEquals(0, reports)
    }

    @Test
    fun sheetThatWentUpAndSettledHiddenWhileVisibleReportsOnce() {
        reporter.presented()
        reporter.observe(visible = true, settledHidden = true)
        reporter.observe(visible = true, settledHidden = false)
        reporter.observe(visible = true, settledHidden = true)
        // The host drops its flag in response; nothing more is owed.
        reporter.observe(visible = false, settledHidden = true)

        assertEquals(1, reports)
    }

    @Test
    fun libraryCallbackArrivingFirstLeavesTheObserverSilent() {
        reporter.presented()
        reporter.observe(visible = true, settledHidden = false)
        reporter.report()
        reporter.observe(visible = true, settledHidden = true)

        assertEquals(1, reports)
    }

    @Test
    fun observerArrivingFirstDropsTheLateLibraryCallback() {
        reporter.presented()
        reporter.observe(visible = true, settledHidden = false)
        reporter.observe(visible = true, settledHidden = true)
        reporter.report()

        assertEquals(1, reports)
    }

    @Test
    fun programmaticHideIsNotAnExitTheObserverReports() {
        reporter.presented()
        reporter.observe(visible = true, settledHidden = false)
        reporter.observe(visible = false, settledHidden = false)
        reporter.observe(visible = false, settledHidden = true)

        assertEquals(0, reports)
    }

    @Test
    fun swipeBeforeTheRiseSettlesStillReports() {
        reporter.presented()
        // Rising: not idle, never settled anywhere but Hidden.
        reporter.observe(visible = true, settledHidden = false)
        reporter.observe(visible = true, settledHidden = true)

        assertEquals(1, reports)
    }

    @Test
    fun eachPresentationReportsItsOwnExit() {
        reporter.presented()
        reporter.observe(visible = true, settledHidden = false)
        reporter.observe(visible = true, settledHidden = true)
        reporter.observe(visible = false, settledHidden = true)
        reporter.presented()
        reporter.observe(visible = true, settledHidden = false)
        reporter.observe(visible = true, settledHidden = true)

        assertEquals(2, reports)
    }
}
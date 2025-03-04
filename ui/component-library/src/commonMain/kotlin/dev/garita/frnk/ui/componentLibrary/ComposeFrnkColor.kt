@file:Suppress("ktlint:standard:max-line-length")

package dev.garita.frnk.ui.componentLibrary

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.jdgarita.frnk.presentation.resources.color.SemanticColor

interface ComposeFrnkColor {

    // System
    val systemDim65: Color
    val systemDim35: Color
    val systemBlue: Color
    val systemRed: Color
    val systemBlack: Color
    val systemGrayDark: Color
    val systemGrayLight: Color

    // Accents
    val accentsPrimary: Color
    val accentsOnPrimary: Color
    val accentsPrimarySurface: Color
    val accentsOnPrimarySurface: Color
    val accentsAccent: Color
    val accentsOnAccent: Color
    val accentsAccentSurface: Color
    val accentsOnAccentSurface: Color
    val accentsNeutral: Color
    val accentsOnNeutral: Color
    val accentsNeutralSurface: Color
    val accentsOnNeutralSurface: Color
    val accentsInactiveSurface: Color
    val accentsInactive: Color
    val accentsClip: Color
    val accentsOnClip: Color
    val accentsClipSurface: Color
    val accentsOnClipSurface: Color

    // Layout
    val layoutSurfaceLow: Color
    val layoutSurfaceMid: Color
    val layoutSurfaceHigh: Color
    val layoutOnSurface: Color
    val layoutOnSurfaceSecondary: Color
    val layoutStrokes: Color
    val layoutLightbox: Color

    // Additional
    val additionalSuccess: Color
    val additionalOnSuccess: Color
    val additionalSuccessSurface: Color
    val additionalOnSuccessSurface: Color
    val additionalFail: Color
    val additionalOnFail: Color
    val additionalFailSurface: Color
    val additionalOnFailSurface: Color

    // Key Tile
    val keyTileKeyTileSurface: Color
    val keyTileKeyTileIcon: Color
    val keyTileKeyTileTitle: Color

    // Prices
    val pricesCouponValue: Color
    val pricesDiscountValue: Color

    // Rewards Card
    val rewardsCardRewardsCardStackedLow: Color
    val rewardsCardRewardsCardStackedHigh: Color
    val rewardsCardRewardsCardProgressBarFill: Color

    // Tab Bar
    val tabBarTabBarSurface: Color
    val tabBarTabBarActive: Color
    val tabBarTabBarInactive: Color

    // Top Nav Bar
    val topNavBarTopNavBarSurface: Color
    val topNavBarOnTopNavBar: Color
    val topNavBarActionOnTopNavBar: Color

    // Flag
    val flagProductSale: Color
    val flagOnProductSale: Color
    val flagProductLoyalty: Color
    val flagOnProductLoyalty: Color
    val flagProductCurrency: Color
    val flagOnProductCurrency: Color
    val flagProductAttribute: Color
    val flagOnProductAttribute: Color
    val flagCouponExpires: Color
    val flagOnCouponExpires: Color
    val flagCouponNew: Color
    val flagOnCouponNew: Color
    val flagCouponFeatured: Color
    val flagOnCouponFeatured: Color

    // Text Input
    val textInputTextColor: Color
    val textInputFocused: Color
    val textInputError: Color
    val disabledSupportingTextColor: Color
    val disabledLeadingIconColor: Color
    val disabledPlaceholderColor: Color

    // Row
    val rowToggleOnColor: Color

    // TODO: Alphabetize
    val theVoid: Color
    val clear: Color

    companion object {

        val default: ComposeFrnkColor by lazy(mode = LazyThreadSafetyMode.NONE) { default() }

        fun default(
            systemDim65: Color = Color.Unspecified,
            systemDim35: Color = Color.Unspecified,
            systemBlue: Color = Color.Unspecified,
            systemRed: Color = Color.Unspecified,
            systemBlack: Color = Color.Unspecified,
            systemGrayDark: Color = Color.Unspecified,
            systemGrayLight: Color = Color.Unspecified,
            accentsPrimary: Color = Color.Unspecified,
            accentsOnPrimary: Color = Color.Unspecified,
            accentsPrimarySurface: Color = Color.Unspecified,
            accentsOnPrimarySurface: Color = Color.Unspecified,
            accentsAccent: Color = Color.Unspecified,
            accentsOnAccent: Color = Color.Unspecified,
            accentsAccentSurface: Color = Color.Unspecified,
            accentsOnAccentSurface: Color = Color.Unspecified,
            accentsNeutral: Color = Color.Unspecified,
            accentsOnNeutral: Color = Color.Unspecified,
            accentsNeutralSurface: Color = Color.Unspecified,
            accentsOnNeutralSurface: Color = Color.Unspecified,
            accentsInactiveSurface: Color = Color.Unspecified,
            accentsInactive: Color = Color.Unspecified,
            accentsClip: Color = Color.Unspecified,
            accentsOnClip: Color = Color.Unspecified,
            accentsClipSurface: Color = Color.Unspecified,
            accentsOnClipSurface: Color = Color.Unspecified,
            layoutSurfaceLow: Color = Color.Unspecified,
            layoutSurfaceMid: Color = Color.Unspecified,
            layoutSurfaceHigh: Color = Color.Unspecified,
            layoutOnSurface: Color = Color.Unspecified,
            layoutOnSurfaceSecondary: Color = Color.Unspecified,
            layoutStrokes: Color = Color.Unspecified,
            layoutLightbox: Color = Color.Unspecified,
            additionalSuccess: Color = Color.Unspecified,
            additionalOnSuccess: Color = Color.Unspecified,
            additionalSuccessSurface: Color = Color.Unspecified,
            additionalOnSuccessSurface: Color = Color.Unspecified,
            additionalFail: Color = Color.Unspecified,
            additionalOnFail: Color = Color.Unspecified,
            additionalFailSurface: Color = Color.Unspecified,
            additionalOnFailSurface: Color = Color.Unspecified,
            keyTileKeyTileSurface: Color = Color.Unspecified,
            keyTileKeyTileIcon: Color = Color.Unspecified,
            keyTileKeyTileTitle: Color = Color.Unspecified,
            pricesCouponValue: Color = Color.Unspecified,
            pricesDiscountValue: Color = Color.Unspecified,
            rewardsCardRewardsCardStackedLow: Color = Color.Unspecified,
            rewardsCardRewardsCardStackedHigh: Color = Color.Unspecified,
            rewardsCardRewardsCardProgressBarFill: Color = Color.Unspecified,
            tabBarTabBarSurface: Color = Color.Unspecified,
            tabBarTabBarActive: Color = Color.Unspecified,
            tabBarTabBarInactive: Color = Color.Unspecified,
            topNavBarTopNavBarSurface: Color = Color.Unspecified,
            topNavBarOnTopNavBar: Color = Color.Unspecified,
            topNavBarActionOnTopNavBar: Color = Color.Unspecified,
            flagProductSale: Color = Color.Unspecified,
            flagOnProductSale: Color = Color.Unspecified,
            flagProductLoyalty: Color = Color.Unspecified,
            flagOnProductLoyalty: Color = Color.Unspecified,
            flagProductCurrency: Color = Color.Unspecified,
            flagOnProductCurrency: Color = Color.Unspecified,
            flagProductAttribute: Color = Color.Unspecified,
            flagOnProductAttribute: Color = Color.Unspecified,
            flagCouponExpires: Color = Color.Unspecified,
            flagOnCouponExpires: Color = Color.Unspecified,
            flagCouponNew: Color = Color.Unspecified,
            flagOnCouponNew: Color = Color.Unspecified,
            flagCouponFeatured: Color = Color.Unspecified,
            flagOnCouponFeatured: Color = Color.Unspecified,
            textInputTextColor: Color = Color.Unspecified,
            textInputFocused: Color = Color.Unspecified,
            textInputError: Color = Color.Unspecified,
            disabledSupportingTextColor: Color = Color.Unspecified,
            disabledLeadingIconColor: Color = Color.Unspecified,
            disabledPlaceholderColor: Color = Color.Unspecified,
            rowToggleOnColor: Color = Color.Unspecified,
            theVoid: Color = Color.Unspecified,
            clear: Color = Color.Unspecified
        ): ComposeFrnkColor = object : ComposeFrnkColor {
            override val systemDim65: Color
                get() = systemDim65
            override val systemDim35: Color
                get() = systemDim35
            override val systemBlue: Color
                get() = systemBlue
            override val systemRed: Color
                get() = systemRed
            override val systemBlack: Color
                get() = systemBlack
            override val systemGrayDark: Color
                get() = systemGrayDark
            override val systemGrayLight: Color
                get() = systemGrayLight
            override val accentsPrimary: Color
                get() = accentsPrimary
            override val accentsOnPrimary: Color
                get() = accentsOnPrimary
            override val accentsPrimarySurface: Color
                get() = accentsPrimarySurface
            override val accentsOnPrimarySurface: Color
                get() = accentsOnPrimarySurface
            override val accentsAccent: Color
                get() = accentsAccent
            override val accentsOnAccent: Color
                get() = accentsOnAccent
            override val accentsAccentSurface: Color
                get() = accentsAccentSurface
            override val accentsOnAccentSurface: Color
                get() = accentsOnAccentSurface
            override val accentsNeutral: Color
                get() = accentsNeutral
            override val accentsOnNeutral: Color
                get() = accentsOnNeutral
            override val accentsNeutralSurface: Color
                get() = accentsNeutralSurface
            override val accentsOnNeutralSurface: Color
                get() = accentsOnNeutralSurface
            override val accentsInactiveSurface: Color
                get() = accentsInactiveSurface
            override val accentsInactive: Color
                get() = accentsInactive
            override val accentsClip: Color
                get() = accentsClip
            override val accentsOnClip: Color
                get() = accentsOnClip
            override val accentsClipSurface: Color
                get() = accentsClipSurface
            override val accentsOnClipSurface: Color
                get() = accentsOnClipSurface
            override val layoutSurfaceLow: Color
                get() = layoutSurfaceLow
            override val layoutSurfaceMid: Color
                get() = layoutSurfaceMid
            override val layoutSurfaceHigh: Color
                get() = layoutSurfaceHigh
            override val layoutOnSurface: Color
                get() = layoutOnSurface
            override val layoutOnSurfaceSecondary: Color
                get() = layoutOnSurfaceSecondary
            override val layoutStrokes: Color
                get() = layoutStrokes
            override val layoutLightbox: Color
                get() = layoutLightbox
            override val additionalSuccess: Color
                get() = additionalSuccess
            override val additionalOnSuccess: Color
                get() = additionalOnSuccess
            override val additionalSuccessSurface: Color
                get() = additionalSuccessSurface
            override val additionalOnSuccessSurface: Color
                get() = additionalOnSuccessSurface
            override val additionalFail: Color
                get() = additionalFail
            override val additionalOnFail: Color
                get() = additionalOnFail
            override val additionalFailSurface: Color
                get() = additionalFailSurface
            override val additionalOnFailSurface: Color
                get() = additionalOnFailSurface
            override val keyTileKeyTileSurface: Color
                get() = keyTileKeyTileSurface
            override val keyTileKeyTileIcon: Color
                get() = keyTileKeyTileIcon
            override val keyTileKeyTileTitle: Color
                get() = keyTileKeyTileTitle
            override val pricesCouponValue: Color
                get() = pricesCouponValue
            override val pricesDiscountValue: Color
                get() = pricesDiscountValue
            override val rewardsCardRewardsCardStackedLow: Color
                get() = rewardsCardRewardsCardStackedLow
            override val rewardsCardRewardsCardStackedHigh: Color
                get() = rewardsCardRewardsCardStackedHigh
            override val rewardsCardRewardsCardProgressBarFill: Color
                get() = rewardsCardRewardsCardProgressBarFill
            override val tabBarTabBarSurface: Color
                get() = tabBarTabBarSurface
            override val tabBarTabBarActive: Color
                get() = tabBarTabBarActive
            override val tabBarTabBarInactive: Color
                get() = tabBarTabBarInactive
            override val topNavBarTopNavBarSurface: Color
                get() = topNavBarTopNavBarSurface
            override val topNavBarOnTopNavBar: Color
                get() = topNavBarOnTopNavBar
            override val topNavBarActionOnTopNavBar: Color
                get() = topNavBarActionOnTopNavBar
            override val flagProductSale: Color
                get() = flagProductSale
            override val flagOnProductSale: Color
                get() = flagOnProductSale
            override val flagProductLoyalty: Color
                get() = flagProductLoyalty
            override val flagOnProductLoyalty: Color
                get() = flagOnProductLoyalty
            override val flagProductCurrency: Color
                get() = flagProductCurrency
            override val flagOnProductCurrency: Color
                get() = flagOnProductCurrency
            override val flagProductAttribute: Color
                get() = flagProductAttribute
            override val flagOnProductAttribute: Color
                get() = flagOnProductAttribute
            override val flagCouponExpires: Color
                get() = flagCouponExpires
            override val flagOnCouponExpires: Color
                get() = flagOnCouponExpires
            override val flagCouponNew: Color
                get() = flagCouponNew
            override val flagOnCouponNew: Color
                get() = flagOnCouponNew
            override val flagCouponFeatured: Color
                get() = flagCouponFeatured
            override val flagOnCouponFeatured: Color
                get() = flagOnCouponFeatured
            override val textInputTextColor: Color
                get() = textInputTextColor
            override val textInputFocused: Color
                get() = textInputFocused
            override val textInputError: Color
                get() = textInputError
            override val disabledSupportingTextColor: Color
                get() = disabledSupportingTextColor
            override val disabledLeadingIconColor: Color
                get() = disabledLeadingIconColor
            override val disabledPlaceholderColor: Color
                get() = disabledPlaceholderColor
            override val rowToggleOnColor: Color
                get() = rowToggleOnColor
            override val theVoid: Color
                get() = theVoid
            override val clear: Color
                get() = clear
        }

        fun dark(
            systemDim65: Color = SemanticColor.SystemDim65.darkColor,
            systemDim35: Color = SemanticColor.SystemDim35.darkColor,
            systemBlue: Color = SemanticColor.SystemBlue.darkColor,
            systemRed: Color = SemanticColor.SystemRed.darkColor,
            systemBlack: Color = SemanticColor.SystemBlack.darkColor,
            systemGrayDark: Color = SemanticColor.SystemGrayDark.darkColor,
            systemGrayLight: Color = SemanticColor.SystemGrayLight.darkColor,
            accentsPrimary: Color = SemanticColor.AccentsPrimary.darkColor,
            accentsOnPrimary: Color = SemanticColor.AccentsOnPrimary.darkColor,
            accentsPrimarySurface: Color = SemanticColor.AccentsPrimarySurface.darkColor,
            accentsOnPrimarySurface: Color = SemanticColor.AccentsOnPrimarySurface.darkColor,
            accentsAccent: Color = SemanticColor.AccentsAccent.darkColor,
            accentsOnAccent: Color = SemanticColor.AccentsOnAccent.darkColor,
            accentsAccentSurface: Color = SemanticColor.AccentsAccentSurface.darkColor,
            accentsOnAccentSurface: Color = SemanticColor.AccentsOnAccentSurface.darkColor,
            accentsNeutral: Color = SemanticColor.AccentsNeutral.darkColor,
            accentsOnNeutral: Color = SemanticColor.AccentsOnNeutral.darkColor,
            accentsNeutralSurface: Color = SemanticColor.AccentsNeutralSurface.darkColor,
            accentsOnNeutralSurface: Color = SemanticColor.AccentsOnNeutralSurface.darkColor,
            accentsInactiveSurface: Color = SemanticColor.AccentsInactiveSurface.darkColor,
            accentsInactive: Color = SemanticColor.AccentsInactive.darkColor,
            accentsClip: Color = SemanticColor.AccentsClip.darkColor,
            accentsOnClip: Color = SemanticColor.AccentsOnClip.darkColor,
            accentsClipSurface: Color = SemanticColor.AccentsClipSurface.darkColor,
            accentsOnClipSurface: Color = SemanticColor.AccentsOnClipSurface.darkColor,
            layoutSurfaceLow: Color = SemanticColor.LayoutSurfaceLow.darkColor,
            layoutSurfaceMid: Color = SemanticColor.LayoutSurfaceMid.darkColor,
            layoutSurfaceHigh: Color = SemanticColor.LayoutSurfaceHigh.darkColor,
            layoutOnSurface: Color = SemanticColor.LayoutOnSurface.darkColor,
            layoutOnSurfaceSecondary: Color = SemanticColor.LayoutOnSurfaceSecondary.darkColor,
            layoutStrokes: Color = SemanticColor.LayoutStrokes.darkColor,
            layoutLightbox: Color = SemanticColor.LayoutLightbox.darkColor,
            additionalSuccess: Color = SemanticColor.AdditionalSuccess.darkColor,
            additionalOnSuccess: Color = SemanticColor.AdditionalOnSuccess.darkColor,
            additionalSuccessSurface: Color = SemanticColor.AdditionalSuccessSurface.darkColor,
            additionalOnSuccessSurface: Color = SemanticColor.AdditionalOnSuccessSurface.darkColor,
            additionalFail: Color = SemanticColor.AdditionalFail.darkColor,
            additionalOnFail: Color = SemanticColor.AdditionalOnFail.darkColor,
            additionalFailSurface: Color = SemanticColor.AdditionalFailSurface.darkColor,
            additionalOnFailSurface: Color = SemanticColor.AdditionalOnFailSurface.darkColor,
            keyTileKeyTileSurface: Color = SemanticColor.KeyTileKeyTileSurface.darkColor,
            keyTileKeyTileIcon: Color = SemanticColor.KeyTileKeyTileIcon.darkColor,
            keyTileKeyTileTitle: Color = SemanticColor.KeyTileKeyTileTitle.darkColor,
            pricesCouponValue: Color = SemanticColor.PricesCouponValue.darkColor,
            pricesDiscountValue: Color = SemanticColor.PricesDiscountValue.darkColor,
            rewardsCardRewardsCardStackedLow: Color = SemanticColor.RewardsCardRewardsCardStackedLow.darkColor,
            rewardsCardRewardsCardStackedHigh: Color = SemanticColor.RewardsCardRewardsCardStackedHigh.darkColor,
            rewardsCardRewardsCardProgressBarFill: Color = SemanticColor.RewardsCardRewardsCardProgressBarFill.darkColor,
            tabBarTabBarSurface: Color = SemanticColor.TabBarTabBarSurface.darkColor,
            tabBarTabBarActive: Color = SemanticColor.TabBarTabBarActive.darkColor,
            tabBarTabBarInactive: Color = SemanticColor.TabBarTabBarInactive.darkColor,
            topNavBarTopNavBarSurface: Color = SemanticColor.TopNavBarTopNavBarSurface.darkColor,
            topNavBarOnTopNavBar: Color = SemanticColor.TopNavBarOnTopNavBar.darkColor,
            topNavBarActionOnTopNavBar: Color = SemanticColor.TopNavBarActionOnTopNavBar.darkColor,
            flagProductSale: Color = SemanticColor.FlagProductSale.darkColor,
            flagOnProductSale: Color = SemanticColor.FlagOnProductSale.darkColor,
            flagProductLoyalty: Color = SemanticColor.FlagProductLoyalty.darkColor,
            flagOnProductLoyalty: Color = SemanticColor.FlagOnProductLoyalty.darkColor,
            flagProductCurrency: Color = SemanticColor.FlagProductCurrency.darkColor,
            flagOnProductCurrency: Color = SemanticColor.FlagOnProductCurrency.darkColor,
            flagProductAttribute: Color = SemanticColor.FlagProductAttribute.darkColor,
            flagOnProductAttribute: Color = SemanticColor.FlagOnProductAttribute.darkColor,
            flagCouponExpires: Color = SemanticColor.FlagCouponExpires.darkColor,
            flagOnCouponExpires: Color = SemanticColor.FlagOnCouponExpires.darkColor,
            flagCouponNew: Color = SemanticColor.FlagCouponNew.darkColor,
            flagOnCouponNew: Color = SemanticColor.FlagOnCouponNew.darkColor,
            flagCouponFeatured: Color = SemanticColor.FlagCouponFeatured.darkColor,
            flagOnCouponFeatured: Color = SemanticColor.FlagOnCouponFeatured.darkColor,
            textInputTextColor: Color = SemanticColor.TextInputTextColor.darkColor,
            textInputFocused: Color = SemanticColor.TextInputFocused.darkColor,
            textInputError: Color = SemanticColor.TextInputError.darkColor,
            disabledSupportingTextColor: Color = SemanticColor.DisabledSupportingTextColor.darkColor,
            disabledLeadingIconColor: Color = SemanticColor.DisabledLeadingIconColor.darkColor,
            disabledPlaceholderColor: Color = SemanticColor.DisabledPlaceholderColor.darkColor,
            rowToggleOnColor: Color = SemanticColor.RowToggleOnColor.darkColor,
            theVoid: Color = SemanticColor.TheVoid.darkColor,
            clear: Color = SemanticColor.Clear.darkColor
        ): ComposeFrnkColor = object : ComposeFrnkColor {
            override val systemDim65: Color
                get() = systemDim65
            override val systemDim35: Color
                get() = systemDim35
            override val systemBlue: Color
                get() = systemBlue
            override val systemRed: Color
                get() = systemRed
            override val systemBlack: Color
                get() = systemBlack
            override val systemGrayDark: Color
                get() = systemGrayDark
            override val systemGrayLight: Color
                get() = systemGrayLight
            override val accentsPrimary: Color
                get() = accentsPrimary
            override val accentsOnPrimary: Color
                get() = accentsOnPrimary
            override val accentsPrimarySurface: Color
                get() = accentsPrimarySurface
            override val accentsOnPrimarySurface: Color
                get() = accentsOnPrimarySurface
            override val accentsAccent: Color
                get() = accentsAccent
            override val accentsOnAccent: Color
                get() = accentsOnAccent
            override val accentsAccentSurface: Color
                get() = accentsAccentSurface
            override val accentsOnAccentSurface: Color
                get() = accentsOnAccentSurface
            override val accentsNeutral: Color
                get() = accentsNeutral
            override val accentsOnNeutral: Color
                get() = accentsOnNeutral
            override val accentsNeutralSurface: Color
                get() = accentsNeutralSurface
            override val accentsOnNeutralSurface: Color
                get() = accentsOnNeutralSurface
            override val accentsInactiveSurface: Color
                get() = accentsInactiveSurface
            override val accentsInactive: Color
                get() = accentsInactive
            override val accentsClip: Color
                get() = accentsClip
            override val accentsOnClip: Color
                get() = accentsOnClip
            override val accentsClipSurface: Color
                get() = accentsClipSurface
            override val accentsOnClipSurface: Color
                get() = accentsOnClipSurface
            override val layoutSurfaceLow: Color
                get() = layoutSurfaceLow
            override val layoutSurfaceMid: Color
                get() = layoutSurfaceMid
            override val layoutSurfaceHigh: Color
                get() = layoutSurfaceHigh
            override val layoutOnSurface: Color
                get() = layoutOnSurface
            override val layoutOnSurfaceSecondary: Color
                get() = layoutOnSurfaceSecondary
            override val layoutStrokes: Color
                get() = layoutStrokes
            override val layoutLightbox: Color
                get() = layoutLightbox
            override val additionalSuccess: Color
                get() = additionalSuccess
            override val additionalOnSuccess: Color
                get() = additionalOnSuccess
            override val additionalSuccessSurface: Color
                get() = additionalSuccessSurface
            override val additionalOnSuccessSurface: Color
                get() = additionalOnSuccessSurface
            override val additionalFail: Color
                get() = additionalFail
            override val additionalOnFail: Color
                get() = additionalOnFail
            override val additionalFailSurface: Color
                get() = additionalFailSurface
            override val additionalOnFailSurface: Color
                get() = additionalOnFailSurface
            override val keyTileKeyTileSurface: Color
                get() = keyTileKeyTileSurface
            override val keyTileKeyTileIcon: Color
                get() = keyTileKeyTileIcon
            override val keyTileKeyTileTitle: Color
                get() = keyTileKeyTileTitle
            override val pricesCouponValue: Color
                get() = pricesCouponValue
            override val pricesDiscountValue: Color
                get() = pricesDiscountValue
            override val rewardsCardRewardsCardStackedLow: Color
                get() = rewardsCardRewardsCardStackedLow
            override val rewardsCardRewardsCardStackedHigh: Color
                get() = rewardsCardRewardsCardStackedHigh
            override val rewardsCardRewardsCardProgressBarFill: Color
                get() = rewardsCardRewardsCardProgressBarFill
            override val tabBarTabBarSurface: Color
                get() = tabBarTabBarSurface
            override val tabBarTabBarActive: Color
                get() = tabBarTabBarActive
            override val tabBarTabBarInactive: Color
                get() = tabBarTabBarInactive
            override val topNavBarTopNavBarSurface: Color
                get() = topNavBarTopNavBarSurface
            override val topNavBarOnTopNavBar: Color
                get() = topNavBarOnTopNavBar
            override val topNavBarActionOnTopNavBar: Color
                get() = topNavBarActionOnTopNavBar
            override val flagProductSale: Color
                get() = flagProductSale
            override val flagOnProductSale: Color
                get() = flagOnProductSale
            override val flagProductLoyalty: Color
                get() = flagProductLoyalty
            override val flagOnProductLoyalty: Color
                get() = flagOnProductLoyalty
            override val flagProductCurrency: Color
                get() = flagProductCurrency
            override val flagOnProductCurrency: Color
                get() = flagOnProductCurrency
            override val flagProductAttribute: Color
                get() = flagProductAttribute
            override val flagOnProductAttribute: Color
                get() = flagOnProductAttribute
            override val flagCouponExpires: Color
                get() = flagCouponExpires
            override val flagOnCouponExpires: Color
                get() = flagOnCouponExpires
            override val flagCouponNew: Color
                get() = flagCouponNew
            override val flagOnCouponNew: Color
                get() = flagOnCouponNew
            override val flagCouponFeatured: Color
                get() = flagCouponFeatured
            override val flagOnCouponFeatured: Color
                get() = flagOnCouponFeatured
            override val textInputTextColor: Color
                get() = textInputTextColor
            override val textInputFocused: Color
                get() = textInputFocused
            override val textInputError: Color
                get() = textInputError
            override val disabledSupportingTextColor: Color
                get() = disabledSupportingTextColor
            override val disabledLeadingIconColor: Color
                get() = disabledLeadingIconColor
            override val disabledPlaceholderColor: Color
                get() = disabledPlaceholderColor
            override val rowToggleOnColor: Color
                get() = rowToggleOnColor
            override val theVoid: Color
                get() = theVoid
            override val clear: Color
                get() = clear
        }

        fun light(
            systemDim65: Color = SemanticColor.SystemDim65.lightColor,
            systemDim35: Color = SemanticColor.SystemDim35.lightColor,
            systemBlue: Color = SemanticColor.SystemBlue.lightColor,
            systemRed: Color = SemanticColor.SystemRed.lightColor,
            systemBlack: Color = SemanticColor.SystemBlack.lightColor,
            systemGrayDark: Color = SemanticColor.SystemGrayDark.lightColor,
            systemGrayLight: Color = SemanticColor.SystemGrayLight.lightColor,
            accentsPrimary: Color = SemanticColor.AccentsPrimary.lightColor,
            accentsOnPrimary: Color = SemanticColor.AccentsOnPrimary.lightColor,
            accentsPrimarySurface: Color = SemanticColor.AccentsPrimarySurface.lightColor,
            accentsOnPrimarySurface: Color = SemanticColor.AccentsOnPrimarySurface.lightColor,
            accentsAccent: Color = SemanticColor.AccentsAccent.lightColor,
            accentsOnAccent: Color = SemanticColor.AccentsOnAccent.lightColor,
            accentsAccentSurface: Color = SemanticColor.AccentsAccentSurface.lightColor,
            accentsOnAccentSurface: Color = SemanticColor.AccentsOnAccentSurface.lightColor,
            accentsNeutral: Color = SemanticColor.AccentsNeutral.lightColor,
            accentsOnNeutral: Color = SemanticColor.AccentsOnNeutral.lightColor,
            accentsNeutralSurface: Color = SemanticColor.AccentsNeutralSurface.lightColor,
            accentsOnNeutralSurface: Color = SemanticColor.AccentsOnNeutralSurface.lightColor,
            accentsInactiveSurface: Color = SemanticColor.AccentsInactiveSurface.lightColor,
            accentsInactive: Color = SemanticColor.AccentsInactive.lightColor,
            accentsClip: Color = SemanticColor.AccentsClip.lightColor,
            accentsOnClip: Color = SemanticColor.AccentsOnClip.lightColor,
            accentsClipSurface: Color = SemanticColor.AccentsClipSurface.lightColor,
            accentsOnClipSurface: Color = SemanticColor.AccentsOnClipSurface.lightColor,
            layoutSurfaceLow: Color = SemanticColor.LayoutSurfaceLow.lightColor,
            layoutSurfaceMid: Color = SemanticColor.LayoutSurfaceMid.lightColor,
            layoutSurfaceHigh: Color = SemanticColor.LayoutSurfaceHigh.lightColor,
            layoutOnSurface: Color = SemanticColor.LayoutOnSurface.lightColor,
            layoutOnSurfaceSecondary: Color = SemanticColor.LayoutOnSurfaceSecondary.lightColor,
            layoutStrokes: Color = SemanticColor.LayoutStrokes.lightColor,
            layoutLightbox: Color = SemanticColor.LayoutLightbox.lightColor,
            additionalSuccess: Color = SemanticColor.AdditionalSuccess.lightColor,
            additionalOnSuccess: Color = SemanticColor.AdditionalOnSuccess.lightColor,
            additionalSuccessSurface: Color = SemanticColor.AdditionalSuccessSurface.lightColor,
            additionalOnSuccessSurface: Color = SemanticColor.AdditionalOnSuccessSurface.lightColor,
            additionalFail: Color = SemanticColor.AdditionalFail.lightColor,
            additionalOnFail: Color = SemanticColor.AdditionalOnFail.lightColor,
            additionalFailSurface: Color = SemanticColor.AdditionalFailSurface.lightColor,
            additionalOnFailSurface: Color = SemanticColor.AdditionalOnFailSurface.lightColor,
            keyTileKeyTileSurface: Color = SemanticColor.KeyTileKeyTileSurface.lightColor,
            keyTileKeyTileIcon: Color = SemanticColor.KeyTileKeyTileIcon.lightColor,
            keyTileKeyTileTitle: Color = SemanticColor.KeyTileKeyTileTitle.lightColor,
            pricesCouponValue: Color = SemanticColor.PricesCouponValue.lightColor,
            pricesDiscountValue: Color = SemanticColor.PricesDiscountValue.lightColor,
            rewardsCardRewardsCardStackedLow: Color = SemanticColor.RewardsCardRewardsCardStackedLow.lightColor,
            rewardsCardRewardsCardStackedHigh: Color = SemanticColor.RewardsCardRewardsCardStackedHigh.lightColor,
            rewardsCardRewardsCardProgressBarFill: Color = SemanticColor.RewardsCardRewardsCardProgressBarFill.lightColor,
            tabBarTabBarSurface: Color = SemanticColor.TabBarTabBarSurface.lightColor,
            tabBarTabBarActive: Color = SemanticColor.TabBarTabBarActive.lightColor,
            tabBarTabBarInactive: Color = SemanticColor.TabBarTabBarInactive.lightColor,
            topNavBarTopNavBarSurface: Color = SemanticColor.TopNavBarTopNavBarSurface.lightColor,
            topNavBarOnTopNavBar: Color = SemanticColor.TopNavBarOnTopNavBar.lightColor,
            topNavBarActionOnTopNavBar: Color = SemanticColor.TopNavBarActionOnTopNavBar.lightColor,
            flagProductSale: Color = SemanticColor.FlagProductSale.lightColor,
            flagOnProductSale: Color = SemanticColor.FlagOnProductSale.lightColor,
            flagProductLoyalty: Color = SemanticColor.FlagProductLoyalty.lightColor,
            flagOnProductLoyalty: Color = SemanticColor.FlagOnProductLoyalty.lightColor,
            flagProductCurrency: Color = SemanticColor.FlagProductCurrency.lightColor,
            flagOnProductCurrency: Color = SemanticColor.FlagOnProductCurrency.lightColor,
            flagProductAttribute: Color = SemanticColor.FlagProductAttribute.lightColor,
            flagOnProductAttribute: Color = SemanticColor.FlagOnProductAttribute.lightColor,
            flagCouponExpires: Color = SemanticColor.FlagCouponExpires.lightColor,
            flagOnCouponExpires: Color = SemanticColor.FlagOnCouponExpires.lightColor,
            flagCouponNew: Color = SemanticColor.FlagCouponNew.lightColor,
            flagOnCouponNew: Color = SemanticColor.FlagOnCouponNew.lightColor,
            flagCouponFeatured: Color = SemanticColor.FlagCouponFeatured.lightColor,
            flagOnCouponFeatured: Color = SemanticColor.FlagOnCouponFeatured.lightColor,
            textInputTextColor: Color = SemanticColor.TextInputTextColor.lightColor,
            textInputFocused: Color = SemanticColor.TextInputFocused.lightColor,
            textInputError: Color = SemanticColor.TextInputError.lightColor,
            disabledSupportingTextColor: Color = SemanticColor.DisabledSupportingTextColor.lightColor,
            disabledLeadingIconColor: Color = SemanticColor.DisabledLeadingIconColor.lightColor,
            disabledPlaceholderColor: Color = SemanticColor.DisabledPlaceholderColor.lightColor,
            rowToggleOnColor: Color = SemanticColor.RowToggleOnColor.lightColor,
            theVoid: Color = SemanticColor.TheVoid.lightColor,
            clear: Color = SemanticColor.Clear.lightColor
        ): ComposeFrnkColor = object : ComposeFrnkColor {
            override val systemDim65: Color
                get() = systemDim65
            override val systemDim35: Color
                get() = systemDim35
            override val systemBlue: Color
                get() = systemBlue
            override val systemRed: Color
                get() = systemRed
            override val systemBlack: Color
                get() = systemBlack
            override val systemGrayDark: Color
                get() = systemGrayDark
            override val systemGrayLight: Color
                get() = systemGrayLight
            override val accentsPrimary: Color
                get() = accentsPrimary
            override val accentsOnPrimary: Color
                get() = accentsOnPrimary
            override val accentsPrimarySurface: Color
                get() = accentsPrimarySurface
            override val accentsOnPrimarySurface: Color
                get() = accentsOnPrimarySurface
            override val accentsAccent: Color
                get() = accentsAccent
            override val accentsOnAccent: Color
                get() = accentsOnAccent
            override val accentsAccentSurface: Color
                get() = accentsAccentSurface
            override val accentsOnAccentSurface: Color
                get() = accentsOnAccentSurface
            override val accentsNeutral: Color
                get() = accentsNeutral
            override val accentsOnNeutral: Color
                get() = accentsOnNeutral
            override val accentsNeutralSurface: Color
                get() = accentsNeutralSurface
            override val accentsOnNeutralSurface: Color
                get() = accentsOnNeutralSurface
            override val accentsInactiveSurface: Color
                get() = accentsInactiveSurface
            override val accentsInactive: Color
                get() = accentsInactive
            override val accentsClip: Color
                get() = accentsClip
            override val accentsOnClip: Color
                get() = accentsOnClip
            override val accentsClipSurface: Color
                get() = accentsClipSurface
            override val accentsOnClipSurface: Color
                get() = accentsOnClipSurface
            override val layoutSurfaceLow: Color
                get() = layoutSurfaceLow
            override val layoutSurfaceMid: Color
                get() = layoutSurfaceMid
            override val layoutSurfaceHigh: Color
                get() = layoutSurfaceHigh
            override val layoutOnSurface: Color
                get() = layoutOnSurface
            override val layoutOnSurfaceSecondary: Color
                get() = layoutOnSurfaceSecondary
            override val layoutStrokes: Color
                get() = layoutStrokes
            override val layoutLightbox: Color
                get() = layoutLightbox
            override val additionalSuccess: Color
                get() = additionalSuccess
            override val additionalOnSuccess: Color
                get() = additionalOnSuccess
            override val additionalSuccessSurface: Color
                get() = additionalSuccessSurface
            override val additionalOnSuccessSurface: Color
                get() = additionalOnSuccessSurface
            override val additionalFail: Color
                get() = additionalFail
            override val additionalOnFail: Color
                get() = additionalOnFail
            override val additionalFailSurface: Color
                get() = additionalFailSurface
            override val additionalOnFailSurface: Color
                get() = additionalOnFailSurface
            override val keyTileKeyTileSurface: Color
                get() = keyTileKeyTileSurface
            override val keyTileKeyTileIcon: Color
                get() = keyTileKeyTileIcon
            override val keyTileKeyTileTitle: Color
                get() = keyTileKeyTileTitle
            override val pricesCouponValue: Color
                get() = pricesCouponValue
            override val pricesDiscountValue: Color
                get() = pricesDiscountValue
            override val rewardsCardRewardsCardStackedLow: Color
                get() = rewardsCardRewardsCardStackedLow
            override val rewardsCardRewardsCardStackedHigh: Color
                get() = rewardsCardRewardsCardStackedHigh
            override val rewardsCardRewardsCardProgressBarFill: Color
                get() = rewardsCardRewardsCardProgressBarFill
            override val tabBarTabBarSurface: Color
                get() = tabBarTabBarSurface
            override val tabBarTabBarActive: Color
                get() = tabBarTabBarActive
            override val tabBarTabBarInactive: Color
                get() = tabBarTabBarInactive
            override val topNavBarTopNavBarSurface: Color
                get() = topNavBarTopNavBarSurface
            override val topNavBarOnTopNavBar: Color
                get() = topNavBarOnTopNavBar
            override val topNavBarActionOnTopNavBar: Color
                get() = topNavBarActionOnTopNavBar
            override val flagProductSale: Color
                get() = flagProductSale
            override val flagOnProductSale: Color
                get() = flagOnProductSale
            override val flagProductLoyalty: Color
                get() = flagProductLoyalty
            override val flagOnProductLoyalty: Color
                get() = flagOnProductLoyalty
            override val flagProductCurrency: Color
                get() = flagProductCurrency
            override val flagOnProductCurrency: Color
                get() = flagOnProductCurrency
            override val flagProductAttribute: Color
                get() = flagProductAttribute
            override val flagOnProductAttribute: Color
                get() = flagOnProductAttribute
            override val flagCouponExpires: Color
                get() = flagCouponExpires
            override val flagOnCouponExpires: Color
                get() = flagOnCouponExpires
            override val flagCouponNew: Color
                get() = flagCouponNew
            override val flagOnCouponNew: Color
                get() = flagOnCouponNew
            override val flagCouponFeatured: Color
                get() = flagCouponFeatured
            override val flagOnCouponFeatured: Color
                get() = flagOnCouponFeatured
            override val textInputTextColor: Color
                get() = textInputTextColor
            override val textInputFocused: Color
                get() = textInputFocused
            override val textInputError: Color
                get() = textInputError
            override val disabledSupportingTextColor: Color
                get() = disabledSupportingTextColor
            override val disabledLeadingIconColor: Color
                get() = disabledLeadingIconColor
            override val disabledPlaceholderColor: Color
                get() = disabledPlaceholderColor
            override val rowToggleOnColor: Color
                get() = rowToggleOnColor
            override val theVoid: Color
                get() = theVoid
            override val clear: Color
                get() = clear
        }
    }
}

val SemanticColor.lightColor: Color
    get() = with(SwiftlyColorResources.shared.get(this).light) {
        Color(alpha = floatAlpha, red = floatR, green = floatG, blue = floatB)
    }

val SemanticColor.darkColor: Color
    get() = with(FrnkColorResources.shared.get(this).dark) {
        Color(alpha = floatAlpha, red = floatR, green = floatG, blue = floatB)
    }

@Composable
fun SemanticColor.toThemeColor(): Color {
    return if (isSystemInDarkTheme() && FrnkTheme.isDarkModeSupported) {
        darkColor
    } else {
        lightColor
    }
}
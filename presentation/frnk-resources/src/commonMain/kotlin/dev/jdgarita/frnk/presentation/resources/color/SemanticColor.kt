package dev.jdgarita.frnk.presentation.resources.color

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalObjCName::class)
@Serializable
enum class SemanticColor {
    // System
    @ObjCName("systemDim65")
    SystemDim65,

    @ObjCName("systemDim35")
    SystemDim35,

    @ObjCName("systemBlue")
    SystemBlue,

    @ObjCName("systemRed")
    SystemRed,

    @ObjCName("systemBlack")
    SystemBlack,

    @ObjCName("systemGrayDark")
    SystemGrayDark,

    @ObjCName("systemGrayLight")
    SystemGrayLight,

    // Accents
    @ObjCName("accentsPrimary")
    AccentsPrimary,

    @ObjCName("accentsOnPrimary")
    AccentsOnPrimary,

    @ObjCName("accentsPrimarySurface")
    AccentsPrimarySurface,

    @ObjCName("accentsOnPrimarySurface")
    AccentsOnPrimarySurface,

    @ObjCName("accentsAccent")
    AccentsAccent,

    @ObjCName("accentsOnAccent")
    AccentsOnAccent,

    @ObjCName("accentsAccentSurface")
    AccentsAccentSurface,

    @ObjCName("accentsOnAccentSurface")
    AccentsOnAccentSurface,

    @ObjCName("accentsNeutral")
    AccentsNeutral,

    @ObjCName("accentsOnNeutral")
    AccentsOnNeutral,

    @ObjCName("accentsNeutralSurface")
    AccentsNeutralSurface,

    @ObjCName("accentsOnNeutralSurface")
    AccentsOnNeutralSurface,

    @ObjCName("accentsInactiveSurface")
    AccentsInactiveSurface,

    @ObjCName("accentsInactive")
    AccentsInactive,

    @ObjCName("accentsClip")
    AccentsClip,

    @ObjCName("accentsOnClip")
    AccentsOnClip,

    @ObjCName("accentsClipSurface")
    AccentsClipSurface,

    @ObjCName("accentsOnClipSurface")
    AccentsOnClipSurface,

    // Layout
    @ObjCName("layoutSurfaceLow")
    LayoutSurfaceLow,

    @ObjCName("layoutSurfaceMid")
    LayoutSurfaceMid,

    @ObjCName("layoutSurfaceHigh")
    LayoutSurfaceHigh,

    @ObjCName("layoutOnSurface")
    LayoutOnSurface,

    @ObjCName("layoutOnSurfaceSecondary")
    LayoutOnSurfaceSecondary,

    @ObjCName("layoutStrokes")
    LayoutStrokes,

    @ObjCName("layoutLightbox")
    LayoutLightbox,

    // Additional
    @ObjCName("additionalSuccess")
    AdditionalSuccess,

    @ObjCName("additionalOnSuccess")
    AdditionalOnSuccess,

    @ObjCName("additionalSuccessSurface")
    AdditionalSuccessSurface,

    @ObjCName("additionalOnSuccessSurface")
    AdditionalOnSuccessSurface,

    @ObjCName("additionalFail")
    AdditionalFail,

    @ObjCName("additionalOnFail")
    AdditionalOnFail,

    @ObjCName("additionalFailSurface")
    AdditionalFailSurface,

    @ObjCName("additionalOnFailSurface")
    AdditionalOnFailSurface,

    // Key Tile
    @ObjCName("keyTileKeyTileSurface")
    KeyTileKeyTileSurface,

    @ObjCName("keyTileKeyTileIcon")
    KeyTileKeyTileIcon,

    @ObjCName("keyTileKeyTileTitle")
    KeyTileKeyTileTitle,

    // Prices
    @ObjCName("pricesCouponValue")
    PricesCouponValue,

    @ObjCName("pricesDiscountValue")
    PricesDiscountValue,

    // Rewards Card
    @ObjCName("rewardsCardRewardsCardStackedLow")
    RewardsCardRewardsCardStackedLow,

    @ObjCName("rewardsCardRewardsCardStackedHigh")
    RewardsCardRewardsCardStackedHigh,

    @ObjCName("rewardsCardRewardsCardProgressBarFill")
    RewardsCardRewardsCardProgressBarFill,

    // Tab Bar
    @ObjCName("tabBarTabBarSurface")
    TabBarTabBarSurface,

    @ObjCName("tabBarTabBarActive")
    TabBarTabBarActive,

    @ObjCName("tabBarTabBarInactive")
    TabBarTabBarInactive,

    // Top Nav Bar
    @ObjCName("topNavBarTopNavBarSurface")
    TopNavBarTopNavBarSurface,

    @ObjCName("topNavBarOnTopNavBar")
    TopNavBarOnTopNavBar,

    @ObjCName("topNavBarActionOnTopNavBar")
    TopNavBarActionOnTopNavBar,

    // Flag
    @ObjCName("flagProductSale")
    FlagProductSale,

    @ObjCName("flagOnProductSale")
    FlagOnProductSale,

    @ObjCName("flagProductLoyalty")
    FlagProductLoyalty,

    @ObjCName("flagOnProductLoyalty")
    FlagOnProductLoyalty,

    @ObjCName("flagProductCurrency")
    FlagProductCurrency,

    @ObjCName("flagOnProductCurrency")
    FlagOnProductCurrency,

    @ObjCName("flagProductAttribute")
    FlagProductAttribute,

    @ObjCName("flagOnProductAttribute")
    FlagOnProductAttribute,

    @ObjCName("flagCouponExpires")
    FlagCouponExpires,

    @ObjCName("flagOnCouponExpires")
    FlagOnCouponExpires,

    @ObjCName("flagCouponNew")
    FlagCouponNew,

    @ObjCName("flagOnCouponNew")
    FlagOnCouponNew,

    @ObjCName("flagCouponFeatured")
    FlagCouponFeatured,

    @ObjCName("flagOnCouponFeatured")
    FlagOnCouponFeatured,

    // Text Input
    @ObjCName("textInputTextColor")
    TextInputTextColor,

    @ObjCName("textInputFocused")
    TextInputFocused,

    @ObjCName("textInputError")
    TextInputError,

    @ObjCName("disabledSupportingTextColor")
    DisabledSupportingTextColor,

    @ObjCName("disabledPlaceholderColor")
    DisabledPlaceholderColor,

    @ObjCName("disabledLeadingIconColor")
    DisabledLeadingIconColor,

    // Row
    @ObjCName("rowToggleOnColor")
    RowToggleOnColor,

    // TODO: Alphabetize
    @ObjCName("clear")
    Clear,

    @ObjCName("theVoid")
    TheVoid
}
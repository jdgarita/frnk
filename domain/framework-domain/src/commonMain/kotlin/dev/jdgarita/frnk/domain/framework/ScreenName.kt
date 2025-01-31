@file:Suppress(
    "ktlint:standard:enum-entry-name-case"
)

package dev.jdgarita.frnk.domain.framework

enum class ScreenName {
    // Generated from
    // https://dev.azure.com/prestoq/Retailer%20Platform/_wiki/wikis/Retail%20Platform/12718/Platform-3.0-Screen-Names
    // These screen names are important and are used for analytics and ads tracking, etc.  Do not invent a screen name
    // and add it to this file.  The wiki needs to be updated with the screen and tracked properly.
    // =======================================================================
    // == DO NOT ADD TO THIS FILE UNLESS THE SCREEN NAME EXISTS IN THE WIKI ==
    // =======================================================================
    Splash,
    PushNotificationPrompt,
    Welcome,
    OnboardingTutorial,
    LinkLoyaltyId,
    NoLoyaltyCard,
    AuthenticationInput,
    AuthenticationVerification,
    AuthenticationTenantLogin,
    PasswordResetViaEmail,
    ProfilePrompt,
    Account,
    AccountAppSettings,
    AccountNotificationSettings,
    AccountProfile,
    AccountCashBack,
    AccountDeletePrompt,
    AccountDeleteFeedback,
    BrandLandingPage,
    BrandLandingCouponSubpage,
    BrandLandingProductSubpage,
    PushNotificationInbox,
    PushNotificationInboxDetails,
    LocationPrompt,
    StoreLocatorMap,
    StoreLocatorList,
    StoreDetail,
    StoreDetailsV2,
    FlyerList,
    FlyerDetail,
    FlyerItem,
    FlyerFullScreen,
    FlyerTapProductList,
    FlyerTapCouponList,
    ForcedUpdate,

    // =======================================================================
    // == DO NOT ADD TO THIS FILE UNLESS THE SCREEN NAME EXISTS IN THE WIKI ==
    // =======================================================================
    DealsHome,
    LoyaltyAuthenticationPrompt,
    LoyaltyHome,
    LoyaltyCardScan,
    LoyaltyTutorial,
    LoyaltyRewardCenter,
    LoyaltyRewardDetail,
    LoyaltyAddedRewards,
    LoyaltyRedeemedRewards,
    CouponsClipped,
    CouponsClippedRefine,
    CouponsRedeemed,
    CouponCategoryMenu,
    CouponCategoryDetails,
    CouponCategoryRefine,
    CouponDetail,
    ChallengeDetailPage,
    ChallengeList,

    // =======================================================================
    // == DO NOT ADD TO THIS FILE UNLESS THE SCREEN NAME EXISTS IN THE WIKI ==
    // =======================================================================
    CashBackDetail,
    CashBackCashOut,
    CashOutMethodsBalanceCashOut,
    CashOutConfirmation,
    SearchInput,
    SearchResults,
    SearchResultRefine,
    ProductCategoryMenu,
    ProductCategory,
    ProductCategoryDetails,
    ProductCategoryRefine,
    ProductDetail,
    ShoppingList,
    ShoppingListSearchInput,
    ExternalUrlEvent, // external url
    SystemScreen, // system screen
    Unknown,
    MadeToOrder, // TODO: Check with product for a name
    Cart,
    Checkout,
    SavedAddresses,
    PaymentMethods,
    Orders,
    FulfillmentSelector,
    FulfillmentSelectorMap,
    FulfillmentStoreSearch,
    DeliverySavedAddresses,
    AddNewAddress,
    EditAddress
}
// =======================================================================
// == DO NOT ADD TO THIS FILE UNLESS THE SCREEN NAME EXISTS IN THE WIKI ==
// =======================================================================
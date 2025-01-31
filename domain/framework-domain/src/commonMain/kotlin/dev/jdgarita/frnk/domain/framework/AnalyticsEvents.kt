// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
package dev.jdgarita.frnk.domain.framework

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object FirstOpen : AnalyticsEvent(
    "first_open",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AppUpdate : AnalyticsEvent(
    "app_update",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthStart : AnalyticsEvent(
    "auth_start",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthComplete : AnalyticsEvent(
    "auth_complete",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class AuthFail(
    val errorReason: String,
    val authType: String? = null
) : AnalyticsEvent(
    "auth_fail",
    mutableMapOf<String, String>().apply {
        put("errorReason", errorReason)
        authType?.let { put("authType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthSkip : AnalyticsEvent(
    "auth_skip",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AccountComplete : AnalyticsEvent(
    "account_complete",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class AccountFail(
    val errorReason: String
) : AnalyticsEvent(
    "account_fail",
    mutableMapOf<String, String>().apply {
        put("errorReason", errorReason)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AccountSkip : AnalyticsEvent(
    "account_skip",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ScreenView(
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "screen_view",
    mutableMapOf<String, String>().apply {
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class SearchAttempt(
    val searchTerm: String,
    val ecomEnabled: String? = null
) : AnalyticsEvent(
    "search_attempt",
    mutableMapOf<String, String>().apply {
        put("searchTerm", searchTerm)
        ecomEnabled?.let { put("ecomEnabled", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class SearchResults(
    val numberResultsReturned: Int,
    val resultItemId: String,
    val resultItemType: String,
    val resultsReturned: String,
    val searchTerm: String,
    val relevancyScore: String,
    val productId: String? = null,
    val ecomEnabled: String? = null
) : AnalyticsEvent(
    "search_results",
    mutableMapOf<String, String>().apply {
        put("numberResultsReturned", numberResultsReturned.toString())
        put("resultItemId", resultItemId)
        put("resultItemType", resultItemType)
        put("resultsReturned", resultsReturned)
        put("searchTerm", searchTerm)
        put("relevancyScore", relevancyScore)
        productId?.let { put("productId", it) }
        ecomEnabled?.let { put("ecomEnabled", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class SearchFail(
    val numberResultsReturned: Int,
    val resultItemId: String,
    val resultItemType: String,
    val resultsReturned: String,
    val searchTerm: String,
    val ecomEnabled: String? = null
) : AnalyticsEvent(
    "search_fail",
    mutableMapOf<String, String>().apply {
        put("numberResultsReturned", numberResultsReturned.toString())
        put("resultItemId", resultItemId)
        put("resultItemType", resultItemType)
        put("resultsReturned", resultsReturned)
        put("searchTerm", searchTerm)
        ecomEnabled?.let { put("ecomEnabled", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object HomeScrollDepth : AnalyticsEvent(
    "home_scroll_depth",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CouponCategoryView(
    val categoryId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "coupon_category_view",
    mutableMapOf<String, String>().apply {
        put("categoryId", categoryId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CouponView(
    val couponId: String
) : AnalyticsEvent(
    "coupon_view",
    mutableMapOf<String, String>().apply {
        put("couponId", couponId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CouponTap(
    val couponId: String,
    val brandLandingPageId: String? = null,
    val flyerType: String? = null
) : AnalyticsEvent(
    "coupon_tap",
    mutableMapOf<String, String>().apply {
        put("couponId", couponId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CouponDetailsView(
    val couponId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "coupon_details_view",
    mutableMapOf<String, String>().apply {
        put("couponId", couponId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CouponClipAttempt(
    val couponId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "coupon_clip_attempt",
    mutableMapOf<String, String>().apply {
        put("couponId", couponId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CouponClipSuccess(
    val couponId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "coupon_clip_success",
    mutableMapOf<String, String>().apply {
        put("couponId", couponId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CouponClipFailure(
    val couponId: String,
    val errorReason: String,
    val brandLandingPageId: String? = null,
    val errorSource: String? = null,
    val errorMessage: String? = null
) : AnalyticsEvent(
    "coupon_clip_failure",
    mutableMapOf<String, String>().apply {
        put("couponId", couponId)
        put("errorReason", errorReason)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
        errorSource?.let { put("errorSource", it) }
        errorMessage?.let { put("errorMessage", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CouponDetailsShare(
    val couponId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "coupon_details_share",
    mutableMapOf<String, String>().apply {
        put("couponId", couponId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ChallengeView(
    val challengeId: String,
    val flagType: String? = null
) : AnalyticsEvent(
    "challenge_view",
    mutableMapOf<String, String>().apply {
        put("challengeId", challengeId)
        flagType?.let { put("flagType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ChallengeTap(
    val challengeId: String,
    val alternateTenantLoyaltyId: String? = null,
    val flagType: String? = null
) : AnalyticsEvent(
    "challenge_tap",
    mutableMapOf<String, String>().apply {
        put("challengeId", challengeId)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
        flagType?.let { put("flagType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ChallengeDetailsView(
    val challengeId: String,
    val flagType: String? = null
) : AnalyticsEvent(
    "challenge_details_view",
    mutableMapOf<String, String>().apply {
        put("challengeId", challengeId)
        flagType?.let { put("flagType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ChallengeActionAttempt(
    val challengeId: String,
    val alternateTenantLoyaltyId: String? = null,
    val flagType: String? = null
) : AnalyticsEvent(
    "challenge_action_attempt",
    mutableMapOf<String, String>().apply {
        put("challengeId", challengeId)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
        flagType?.let { put("flagType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ChallengeActionFailure(
    val challengeId: String,
    val errorReason: String,
    val alternateTenantLoyaltyId: String? = null,
    val flagType: String? = null
) : AnalyticsEvent(
    "challenge_action_failure",
    mutableMapOf<String, String>().apply {
        put("challengeId", challengeId)
        put("errorReason", errorReason)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
        flagType?.let { put("flagType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ChallengeDetailsShare(
    val challengeId: String,
    val alternateTenantLoyaltyId: String? = null,
    val flagType: String? = null
) : AnalyticsEvent(
    "challenge_details_share",
    mutableMapOf<String, String>().apply {
        put("challengeId", challengeId)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
        flagType?.let { put("flagType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class RewardView(
    val rewardId: String
) : AnalyticsEvent(
    "reward_view",
    mutableMapOf<String, String>().apply {
        put("rewardId", rewardId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class RewardTap(
    val rewardId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "reward_tap",
    mutableMapOf<String, String>().apply {
        put("rewardId", rewardId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class RewardDetailsView(
    val rewardId: String
) : AnalyticsEvent(
    "reward_details_view",
    mutableMapOf<String, String>().apply {
        put("rewardId", rewardId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class RewardClipAttempt(
    val rewardId: String
) : AnalyticsEvent(
    "reward_clip_attempt",
    mutableMapOf<String, String>().apply {
        put("rewardId", rewardId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class RewardClipFailure(
    val rewardId: String
) : AnalyticsEvent(
    "reward_clip_failure",
    mutableMapOf<String, String>().apply {
        put("rewardId", rewardId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ProductCategoryView(
    val categoryId: String
) : AnalyticsEvent(
    "product_category_view",
    mutableMapOf<String, String>().apply {
        put("categoryId", categoryId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ProductListAttempt(
    val productId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "product_list_attempt",
    mutableMapOf<String, String>().apply {
        put("productId", productId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ProductListFailure(
    val productId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "product_list_failure",
    mutableMapOf<String, String>().apply {
        put("productId", productId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ProductDetailsView(
    val productId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "product_details_view",
    mutableMapOf<String, String>().apply {
        put("productId", productId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ProductDetailsShare(
    val productId: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "product_details_share",
    mutableMapOf<String, String>().apply {
        put("productId", productId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ProductScan(
    val productId: String,
    val barcodeValue: String
) : AnalyticsEvent(
    "product_scan",
    mutableMapOf<String, String>().apply {
        put("productId", productId)
        put("barcodeValue", barcodeValue)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerListView(
    val flyerId: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_list_view",
    mutableMapOf<String, String>().apply {
        put("flyerId", flyerId)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerListItemView(
    val flyerId: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_list_item_view",
    mutableMapOf<String, String>().apply {
        put("flyerId", flyerId)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerDetailsView(
    val flyerId: String,
    val flyerPage: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_details_view",
    mutableMapOf<String, String>().apply {
        put("flyerId", flyerId)
        put("flyerPage", flyerPage)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerDetailsPagination(
    val flyerId: String,
    val flyerPage: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_details_pagination",
    mutableMapOf<String, String>().apply {
        put("flyerId", flyerId)
        put("flyerPage", flyerPage)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerDetailsShare(
    val flyerId: String,
    val flyerPage: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_details_share",
    mutableMapOf<String, String>().apply {
        put("flyerId", flyerId)
        put("flyerPage", flyerPage)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerFullscreenView(
    val flyerId: String,
    val flyerPage: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_fullscreen_view",
    mutableMapOf<String, String>().apply {
        put("flyerId", flyerId)
        put("flyerPage", flyerPage)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerFullscreenClose(
    val flyerId: String,
    val flyerPage: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_fullscreen_close",
    mutableMapOf<String, String>().apply {
        put("flyerId", flyerId)
        put("flyerPage", flyerPage)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerFullscreenTapAttempt(
    val flyerId: String,
    val flyerPage: String,
    val couponId: String? = null,
    val productId: String? = null,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_fullscreen_tap_attempt",
    mutableMapOf<String, String>().apply {
        put("flyerId", flyerId)
        put("flyerPage", flyerPage)
        couponId?.let { put("couponId", it) }
        productId?.let { put("productId", it) }
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerFullscreenTapFailure(
    val flyerId: String,
    val errorReason: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_fullscreen_tap_failure",
    mutableMapOf<String, String>().apply {
        put("flyerId", flyerId)
        put("errorReason", errorReason)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerCouponShare(
    val couponId: String,
    val flyerId: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_coupon_share",
    mutableMapOf<String, String>().apply {
        put("couponId", couponId)
        put("flyerId", flyerId)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FlyerProductShare(
    val productId: String,
    val flyerId: String,
    val flyerType: String? = null
) : AnalyticsEvent(
    "flyer_product_share",
    mutableMapOf<String, String>().apply {
        put("productId", productId)
        put("flyerId", flyerId)
        flyerType?.let { put("flyerType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ListItemAction(
    val itemId: String,
    val listActionType: String,
    val listItemType: String,
    val listItemName: String
) : AnalyticsEvent(
    "list_item_action",
    mutableMapOf<String, String>().apply {
        put("itemId", itemId)
        put("listActionType", listActionType)
        put("listItemType", listItemType)
        put("listItemName", listItemName)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class DeepLinkInvocation(
    val deepLinkURI: String
) : AnalyticsEvent(
    "deep_link_invocation",
    mutableMapOf<String, String>().apply {
        put("deepLinkURI", deepLinkURI)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class StoreDirectorySetDefault(
    val fulfillmentMethod: String? = null
) : AnalyticsEvent(
    "store_directory_set_default",
    mutableMapOf<String, String>().apply {
        fulfillmentMethod?.let { put("fulfillmentMethod", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class StoreDirectorySearchAttempt(
    val searchTerm: String,
    val searchType: String,
    val fulfillmentMethod: String? = null
) : AnalyticsEvent(
    "store_directory_search_attempt",
    mutableMapOf<String, String>().apply {
        put("searchTerm", searchTerm)
        put("searchType", searchType)
        fulfillmentMethod?.let { put("fulfillmentMethod", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class StoreDirectorySearchResult(
    val numberResultsReturned: Int,
    val searchTerm: String? = null,
    val searchType: String? = null,
    val fulfillmentMethod: String? = null
) : AnalyticsEvent(
    "store_directory_search_result",
    mutableMapOf<String, String>().apply {
        put("numberResultsReturned", numberResultsReturned.toString())
        searchTerm?.let { put("searchTerm", it) }
        searchType?.let { put("searchType", it) }
        fulfillmentMethod?.let { put("fulfillmentMethod", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class AdRequest(
    val categoryId: String,
    val placementId: String,
    val environment: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "ad_request",
    mutableMapOf<String, String>().apply {
        put("categoryId", categoryId)
        put("placementId", placementId)
        put("environment", environment)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    },
    AnalyticsEventType.Advertisement
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class AdsReceived(
    val categoryId: String,
    val placementId: String,
    val adsCount: Int,
    val adImpressionId: String,
    val adId: String,
    val environment: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "ads_received",
    mutableMapOf<String, String>().apply {
        put("categoryId", categoryId)
        put("placementId", placementId)
        put("adsCount", adsCount.toString())
        put("adImpressionId", adImpressionId)
        put("adId", adId)
        put("environment", environment)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    },
    AnalyticsEventType.Advertisement
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class Impression(
    val categoryId: String,
    val placementId: String,
    val adImpressionId: String,
    val adId: String,
    val platform: String,
    val adSource: String,
    val environment: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "impression",
    mutableMapOf<String, String>().apply {
        put("categoryId", categoryId)
        put("placementId", placementId)
        put("adImpressionId", adImpressionId)
        put("adId", adId)
        put("platform", platform)
        put("adSource", adSource)
        put("environment", environment)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    },
    AnalyticsEventType.Advertisement
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class AdsView(
    val categoryId: String,
    val placementId: String,
    val adImpressionId: String,
    val adId: String,
    val platform: String,
    val adSource: String,
    val environment: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "ads_view",
    mutableMapOf<String, String>().apply {
        put("categoryId", categoryId)
        put("placementId", placementId)
        put("adImpressionId", adImpressionId)
        put("adId", adId)
        put("platform", platform)
        put("adSource", adSource)
        put("environment", environment)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    },
    AnalyticsEventType.Advertisement
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class Click(
    val categoryId: String,
    val placementId: String,
    val adImpressionId: String,
    val adId: String,
    val platform: String,
    val adSource: String,
    val brandLandingPageId: String? = null
) : AnalyticsEvent(
    "click",
    mutableMapOf<String, String>().apply {
        put("categoryId", categoryId)
        put("placementId", placementId)
        put("adImpressionId", adImpressionId)
        put("adId", adId)
        put("platform", platform)
        put("adSource", adSource)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
    },
    AnalyticsEventType.Advertisement
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ClickedItemPosition(
    val itemId: String,
    val numberResultsReturned: Int,
    val resultItemId: String,
    val resultItemType: String,
    val resultsReturned: String,
    val searchTerm: String,
    val searchType: String,
    val position: Int,
    val priceDiscounted: String
) : AnalyticsEvent(
    "clicked_item_position",
    mutableMapOf<String, String>().apply {
        put("itemId", itemId)
        put("numberResultsReturned", numberResultsReturned.toString())
        put("resultItemId", resultItemId)
        put("resultItemType", resultItemType)
        put("resultsReturned", resultsReturned)
        put("searchTerm", searchTerm)
        put("searchType", searchType)
        put("position", position.toString())
        put("priceDiscounted", priceDiscounted)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AppRatingRequested : AnalyticsEvent(
    "app_rating_requested",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ProductTap(
    val productId: String,
    val categoryId: String,
    val position: Int,
    val priceDiscounted: String,
    val flagType: String,
    val sponsoredProduct: String,
    val tapType: String,
    val price: String,
    val brandLandingPageId: String? = null,
    val ecomEnabled: String? = null,
    val flyerType: String? = null,
    val fulfillmentMethod: String? = null
) : AnalyticsEvent(
    "product_tap",
    mutableMapOf<String, String>().apply {
        put("productId", productId)
        put("categoryId", categoryId)
        put("position", position.toString())
        put("priceDiscounted", priceDiscounted)
        put("flagType", flagType)
        put("sponsoredProduct", sponsoredProduct)
        put("tapType", tapType)
        put("price", price)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
        ecomEnabled?.let { put("ecomEnabled", it) }
        flyerType?.let { put("flyerType", it) }
        fulfillmentMethod?.let { put("fulfillmentMethod", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object SessionEnd : AnalyticsEvent(
    "session_end",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class DealView(
    val dealId: String,
    val alternateTenantLoyaltyId: String? = null,
    val flagType: String? = null
) : AnalyticsEvent(
    "deal_view",
    mutableMapOf<String, String>().apply {
        put("dealId", dealId)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
        flagType?.let { put("flagType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class DealDetailsView(
    val dealId: String,
    val alternateTenantLoyaltyId: String? = null,
    val flagType: String? = null
) : AnalyticsEvent(
    "deal_details_view",
    mutableMapOf<String, String>().apply {
        put("dealId", dealId)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
        flagType?.let { put("flagType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class DealTap(
    val dealId: String,
    val alternateTenantLoyaltyId: String? = null,
    val flagType: String? = null
) : AnalyticsEvent(
    "deal_tap",
    mutableMapOf<String, String>().apply {
        put("dealId", dealId)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
        flagType?.let { put("flagType", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackView(
    val cashbackId: String,
    val brandLandingPageId: String? = null,
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_view",
    mutableMapOf<String, String>().apply {
        put("cashbackId", cashbackId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackDetailsView(
    val cashbackId: String,
    val brandLandingPageId: String? = null,
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_details_view",
    mutableMapOf<String, String>().apply {
        put("cashbackId", cashbackId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackClipAttempt(
    val cashbackId: String,
    val brandLandingPageId: String? = null,
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_clip_attempt",
    mutableMapOf<String, String>().apply {
        put("cashbackId", cashbackId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackClipFailure(
    val cashbackId: String,
    val errorReason: String,
    val brandLandingPageId: String? = null,
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_clip_failure",
    mutableMapOf<String, String>().apply {
        put("cashbackId", cashbackId)
        put("errorReason", errorReason)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackBalanceTap(
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_balance_tap",
    mutableMapOf<String, String>().apply {
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackBalanceCashoutAttempt(
    val cashoutMethod: String,
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_balance_cashout_attempt",
    mutableMapOf<String, String>().apply {
        put("cashoutMethod", cashoutMethod)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackBalanceCashoutFailure(
    val errorReason: String,
    val cashoutMethod: String,
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_balance_cashout_failure",
    mutableMapOf<String, String>().apply {
        put("errorReason", errorReason)
        put("cashoutMethod", cashoutMethod)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackEmailTap(
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_email_tap",
    mutableMapOf<String, String>().apply {
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackEmailSaveAttempt(
    val cashoutMethod: String,
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_email_save_attempt",
    mutableMapOf<String, String>().apply {
        put("cashoutMethod", cashoutMethod)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackEmailSaveFailure(
    val errorReason: String,
    val cashoutMethod: String,
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_email_save_failure",
    mutableMapOf<String, String>().apply {
        put("errorReason", errorReason)
        put("cashoutMethod", cashoutMethod)
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CashbackDetailsShare(
    val cashbackId: String,
    val brandLandingPageId: String? = null,
    val alternateTenantLoyaltyId: String? = null
) : AnalyticsEvent(
    "cashback_details_share",
    mutableMapOf<String, String>().apply {
        put("cashbackId", cashbackId)
        brandLandingPageId?.let { put("brandLandingPageId", it) }
        alternateTenantLoyaltyId?.let { put("alternateTenantLoyaltyId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class NearbyStoreLinkTap(
    val challengeId: String? = null,
    val dealId: String? = null
) : AnalyticsEvent(
    "nearby_store_link_tap",
    mutableMapOf<String, String>().apply {
        challengeId?.let { put("challengeId", it) }
        dealId?.let { put("dealId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class LoyaltyIdScan(
    val loyaltyScan: String
) : AnalyticsEvent(
    "loyalty_id_scan",
    mutableMapOf<String, String>().apply {
        put("loyaltyScan", loyaltyScan)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object LoyaltyIdManual : AnalyticsEvent(
    "loyalty_id_manual",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class LoyaltyIdError(
    val errorReason: String
) : AnalyticsEvent(
    "loyalty_id_error",
    mutableMapOf<String, String>().apply {
        put("errorReason", errorReason)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ShoppingListOneItem : AnalyticsEvent(
    "shopping_list_one_item",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ShoppingListAvgItems : AnalyticsEvent(
    "shopping_list_avg_items",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ShoppingListComplete(
    val itemId: String
) : AnalyticsEvent(
    "shopping_list_complete",
    mutableMapOf<String, String>().apply {
        put("itemId", itemId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ShoppingListWriteIn(
    val searchTerm: String,
    val searchType: String
) : AnalyticsEvent(
    "shopping_list_write_in",
    mutableMapOf<String, String>().apply {
        put("searchTerm", searchTerm)
        put("searchType", searchType)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ShoppingListAvgComplete : AnalyticsEvent(
    "shopping_list_avg_complete",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ShoppingListAvgWriteIn : AnalyticsEvent(
    "shopping_list_avg_write_in",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ShoppingListProductAddedVsWriteIn : AnalyticsEvent(
    "shopping_list_product_added_vs_write_in",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ShoppingListCompletedVsActive : AnalyticsEvent(
    "shopping_list_completed_vs_active",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class TopCategoryTap(
    val categoryId: String,
    val position: Int
) : AnalyticsEvent(
    "top_category_tap",
    mutableMapOf<String, String>().apply {
        put("categoryId", categoryId)
        put("position", position.toString())
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class QuantityStepperTap(
    val productId: String,
    val productCount: String,
    val productWeight: String,
    val fulfillmentMethod: String? = null
) : AnalyticsEvent(
    "quantity_stepper_tap",
    mutableMapOf<String, String>().apply {
        put("productId", productId)
        put("productCount", productCount)
        put("productWeight", productWeight)
        fulfillmentMethod?.let { put("fulfillmentMethod", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class SignupPromptView(
    val originatingTapType: String
) : AnalyticsEvent(
    "signup_prompt_view",
    mutableMapOf<String, String>().apply {
        put("originatingTapType", originatingTapType)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class SignupPromptTap(
    val tapType: String
) : AnalyticsEvent(
    "signup_prompt_tap",
    mutableMapOf<String, String>().apply {
        put("tapType", tapType)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CheckAddressTap(
    val ecomEnabled: String? = null
) : AnalyticsEvent(
    "check_address_tap",
    mutableMapOf<String, String>().apply {
        ecomEnabled?.let { put("ecomEnabled", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CartView(
    val bannerStatus: String,
    val addressSaved: String,
    val cartCount: String,
    val fulfillmentMethod: String,
    val ecomEnabled: String? = null
) : AnalyticsEvent(
    "cart_view",
    mutableMapOf<String, String>().apply {
        put("bannerStatus", bannerStatus)
        put("addressSaved", addressSaved)
        put("cartCount", cartCount)
        put("fulfillmentMethod", fulfillmentMethod)
        ecomEnabled?.let { put("ecomEnabled", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object CartStoreFinderView : AnalyticsEvent(
    "cart_store_finder_view",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CartStoreFinderTap(
    val position: Int,
    val tapType: String
) : AnalyticsEvent(
    "cart_store_finder_tap",
    mutableMapOf<String, String>().apply {
        put("position", position.toString())
        put("tapType", tapType)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class SwipeDelete(
    val productId: String,
    val productCount: String,
    val productWeight: String,
    val swipeDirection: String
) : AnalyticsEvent(
    "swipe_delete",
    mutableMapOf<String, String>().apply {
        put("productId", productId)
        put("productCount", productCount)
        put("productWeight", productWeight)
        put("swipeDirection", swipeDirection)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class CheckoutTap(
    val cartCount: String,
    val cartSubtotal: String,
    val cartSavings: String,
    val fulfillmentMethod: String
) : AnalyticsEvent(
    "checkout_tap",
    mutableMapOf<String, String>().apply {
        put("cartCount", cartCount)
        put("cartSubtotal", cartSubtotal)
        put("cartSavings", cartSavings)
        put("fulfillmentMethod", fulfillmentMethod)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class AccountPageTap(
    val destination: String
) : AnalyticsEvent(
    "account_page_tap",
    mutableMapOf<String, String>().apply {
        put("destination", destination)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class EcomActionFailure(
    val actionAttempt: String,
    val errorMessage: String,
    val productId: String? = null
) : AnalyticsEvent(
    "ecom_action_failure",
    mutableMapOf<String, String>().apply {
        put("actionAttempt", actionAttempt)
        put("errorMessage", errorMessage)
        productId?.let { put("productId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class EcomWebviewFailure(
    val actionAttempt: String,
    val errorSource: String,
    val errorMessage: String
) : AnalyticsEvent(
    "ecom_webview_failure",
    mutableMapOf<String, String>().apply {
        put("actionAttempt", actionAttempt)
        put("errorSource", errorSource)
        put("errorMessage", errorMessage)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ReceiptScanTap : AnalyticsEvent(
    "receipt_scan_tap",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ReceiptScanPermission(
    val permission_status: String
) : AnalyticsEvent(
    "receipt_scan_permission",
    mutableMapOf<String, String>().apply {
        put("permission_status", permission_status)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ReceiptScanPhotoCaptured : AnalyticsEvent(
    "receipt_scan_photo_captured",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ReceiptScanPhotoSubmitted(
    val photo_count: String
) : AnalyticsEvent(
    "receipt_scan_photo_submitted",
    mutableMapOf<String, String>().apply {
        put("photo_count", photo_count)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ReceiptScanSuccess : AnalyticsEvent(
    "receipt_scan_success",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ReceiptScanFailure(
    val errorReason: String
) : AnalyticsEvent(
    "receipt_scan_failure",
    mutableMapOf<String, String>().apply {
        put("errorReason", errorReason)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ReceiptScanPhotoDiscard : AnalyticsEvent(
    "receipt_scan_photo_discard",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ReceiptScanPhotoDiscardConfirmation : AnalyticsEvent(
    "receipt_scan_photo_discard_confirmation",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ReceiptScanPhotoDiscardCancelation : AnalyticsEvent(
    "receipt_scan_photo_discard_cancelation",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object ReceiptScanPhotoOpen : AnalyticsEvent(
    "receipt_scan_photo_open",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object PointHistoryAccessTap : AnalyticsEvent(
    "point_history_access_tap",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object PointHistoryView : AnalyticsEvent(
    "point_history_view",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object PointExpirationView : AnalyticsEvent(
    "point_expiration_view",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class PointHistoryDetailsTap(
    val itemId: String,
    val tapType: String,
    val rewardId: String? = null,
    val challengeId: String? = null
) : AnalyticsEvent(
    "point_history_details_tap",
    mutableMapOf<String, String>().apply {
        put("itemId", itemId)
        put("tapType", tapType)
        rewardId?.let { put("rewardId", it) }
        challengeId?.let { put("challengeId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthInvalidEmailFormat : AnalyticsEvent(
    "auth_invalid_email_format",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthInvalidPhoneNumber : AnalyticsEvent(
    "auth_invalid_phone_number",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthEmailPasswordCreationFailed : AnalyticsEvent(
    "auth_email_password_creation_failed",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthEmailPasswordCreationSuccess : AnalyticsEvent(
    "auth_email_password_creation_success",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthEmailLinkSent : AnalyticsEvent(
    "auth_email_link_sent",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthEmailLinkSendFailure : AnalyticsEvent(
    "auth_email_link_send_failure",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthSmsCodeSuccess : AnalyticsEvent(
    "auth_sms_code_success",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthSmsCodeFailed : AnalyticsEvent(
    "auth_sms_code_failed",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthSmsNewCodeRequest : AnalyticsEvent(
    "auth_sms_new_code_request",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthConnectionLost : AnalyticsEvent(
    "auth_connection_lost",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthConnectionRestored : AnalyticsEvent(
    "auth_connection_restored",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthLoyaltyIdCreated : AnalyticsEvent(
    "auth_loyalty_id_created",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthLoyaltyIdCreationFail : AnalyticsEvent(
    "auth_loyalty_id_creation_fail",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class WebviewError(
    val errorReason: String
) : AnalyticsEvent(
    "webview_error",
    mutableMapOf<String, String>().apply {
        put("errorReason", errorReason)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class SearchResultTap(
    val itemId: String,
    val listItemName: String,
    val numberResultsReturned: Int,
    val resultItemId: String,
    val resultItemType: String,
    val resultsReturned: String,
    val searchTerm: String,
    val searchType: String,
    val relevancyScore: String,
    val productId: String? = null,
    val categoryId: String? = null
) : AnalyticsEvent(
    "search_result_tap",
    mutableMapOf<String, String>().apply {
        put("itemId", itemId)
        put("listItemName", listItemName)
        put("numberResultsReturned", numberResultsReturned.toString())
        put("resultItemId", resultItemId)
        put("resultItemType", resultItemType)
        put("resultsReturned", resultsReturned)
        put("searchTerm", searchTerm)
        put("searchType", searchType)
        put("relevancyScore", relevancyScore)
        productId?.let { put("productId", it) }
        categoryId?.let { put("categoryId", it) }
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class OnSaleForYouView(
    val numberResultsReturned: Int
) : AnalyticsEvent(
    "on_sale_for_you_view",
    mutableMapOf<String, String>().apply {
        put("numberResultsReturned", numberResultsReturned.toString())
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ShoppingListCouponAdded(
    val itemId: String
) : AnalyticsEvent(
    "shopping_list_coupon_added",
    mutableMapOf<String, String>().apply {
        put("itemId", itemId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ShoppingListItemDelete(
    val itemId: String
) : AnalyticsEvent(
    "shopping_list_item_delete",
    mutableMapOf<String, String>().apply {
        put("itemId", itemId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ShoppingListQuantityActive(
    val quantity: String
) : AnalyticsEvent(
    "shopping_list_quantity_active",
    mutableMapOf<String, String>().apply {
        put("quantity", quantity)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class ShoppingListQuantityCompleted(
    val quantity: String
) : AnalyticsEvent(
    "shopping_list_quantity_completed",
    mutableMapOf<String, String>().apply {
        put("quantity", quantity)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class OrderConfirmationToastView(
    val fulfillmentMethod: String,
    val orderId: String
) : AnalyticsEvent(
    "order_confirmation_toast_view",
    mutableMapOf<String, String>().apply {
        put("fulfillmentMethod", fulfillmentMethod)
        put("orderId", orderId)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class OidcSigninStart(
    val signInNotSignUp: Int
) : AnalyticsEvent(
    "oidc_signin_start",
    mutableMapOf<String, String>().apply {
        put("signInNotSignUp", signInNotSignUp.toString())
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object OidcSigninSuccess : AnalyticsEvent(
    "oidc_signin_success",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object OidcSigninExit : AnalyticsEvent(
    "oidc_signin_exit",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object OidcSigninError : AnalyticsEvent(
    "oidc_signin_error",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object OidcTokenExchangeSuccess : AnalyticsEvent(
    "oidc_token_exchange_success",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object OidcTokenExchangeFailure : AnalyticsEvent(
    "oidc_token_exchange_failure",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object AuthSignout : AnalyticsEvent(
    "auth_signout",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object LocationPermissionsPopupView : AnalyticsEvent(
    "location_permissions_popup_view",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class LocationPermissionsPopupTap(
    val tapType: String
) : AnalyticsEvent(
    "location_permissions_popup_tap",
    mutableMapOf<String, String>().apply {
        put("tapType", tapType)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object LocationSettingsPopupView : AnalyticsEvent(
    "location_settings_popup_view",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class LocationSettingsPopupTap(
    val tapType: String
) : AnalyticsEvent(
    "location_settings_popup_tap",
    mutableMapOf<String, String>().apply {
        put("tapType", tapType)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object StoreDetailsFlyerTap : AnalyticsEvent(
    "store_details_flyer_tap",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class FulfillmentChange(
    val fulfillmentMethod: String,
    val changeType: String
) : AnalyticsEvent(
    "fulfillment_change",
    mutableMapOf<String, String>().apply {
        put("fulfillmentMethod", fulfillmentMethod)
        put("changeType", changeType)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data object StoreDirectionsTap : AnalyticsEvent(
    "store_directions_tap",
    emptyMap()
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class StoreDetailsPhoneTap(
    val department: String
) : AnalyticsEvent(
    "store_details_phone_tap",
    mutableMapOf<String, String>().apply {
        put("department", department)
    }
)

// DO NOT EDIT - This file is auto-generated - see bin/generate_analytics.sh
data class StoreDetailsHoursTap(
    val tapType: String,
    val department: String
) : AnalyticsEvent(
    "store_details_hours_tap",
    mutableMapOf<String, String>().apply {
        put("tapType", tapType)
        put("department", department)
    }
)
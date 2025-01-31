package dev.jdgarita.frnk.domain.framework

import dev.jdgarita.frnk.domain.framework.cache.Clearable

interface FrnkLogger : TelemetryLogger, Clearable {

    companion object {

        /**
         * Attribute describing the InstallId. This attribute should be set in the
         * logging context at application start so that it exists on all events.
         */
        const val PROP_INSTALL_ID = "InstallId"

        /**
         * Attribute describing the previous application version.
         */
        const val PROP_APP_VERSION: String = "AppVersion"

        /**
         * Attribute describing the name of the application.
         */
        const val PROP_APP_NAME: String = "AppName"

        /**
         * Attribute describing the id of the application.
         */
        const val PROP_APP_ID: String = "AppId"

        const val PROP_AD_SITE_ID = "SiteIdentifier"

        /**
         * Attribute describing the current mode of the screen (dark/light)
         */
        const val PROP_SCREEN_MODE: String = "ScreenMode"

        /**
         * Attribute providing a unique identifier to the user's app session. This is
         * intended to be added once at app startup and logged with each event.
         */
        const val PROP_SESSION_CONTEXT_ID: String = "SessionContextId"

        const val PROP_SESSION_ELAPSED_TIME_MS: String = "SessionElapsedTimeMS"

        /**
         * The screen name associated with a log event.
         *
         * Why is this in client-framework-data? Often, we need to attach screen name
         * data to analytics that are delivered at the data source layer.
         */
        const val PROP_SCREEN_NAME: String = "ScreenName"

        /**
         * Previous screen name associated with a log event.
         */
        const val PROP_PREVIOUS_SCREEN_NAME: String = "PreviousScreenName"

        /**
         * Attribute providing the user agent
         */
        const val PROP_USER_AGENT: String = "UserAgent"

        const val PROP_SITE_ID: String = "SiteId"

        /**
         * Attribute providing the id of the current retailer
         */
        const val PROP_TENANT_ID: String = "TenantId"

        const val PROP_TENANT_LOYALTY_ID: String = "TenantLoyaltyId"

        const val PROP_STORE_ID = "StoreId"

        const val PROP_SWIFTLY_USER_ID = "SwiftlyUserId"

        const val PROP_ALTERNATE_TENANT_LOYALTY_ID = "alternateTenantLoyaltyId"

        /**
         * Attribute providing the id of the current client platform version
         */
        const val PROP_CLIENT_PLATFORM = "ClientPlatform"

        /**
         * Attribute providing if the build is internal or not.
         */
        const val PROP_IS_INTERNAL = "IsInternal"

        /**
         * Attribute providing the platform build number.
         */
        const val PROP_BUILD_NUMBER = "BuildNumber"

        /*
         * EXTENSION PROPERTIES THAT DEFINE THE ENUMERATED ATTRIBUTE VALUES
         */

        const val PROP_VAL_UI_MODE_LIGHT: String = "light"
        const val PROP_VAL_UI_MODE_NIGHT: String = "dark"
    }
}
//
//  AppDelegate.swift
//  iosApp
//
//  Created by Vivien Mahé on 26/07/2024.
//  Copyright © 2024 Tweener Labs. All rights reserved.
//

import SwiftUI
import shared
import FirebaseCore
import FirebaseMessaging
import GoogleSignIn
import OSLog

@MainActor
class AppDelegate : NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        
        LibrariesConfigurationHelper().doInitConfigurations()

        return true
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any]) async -> UIBackgroundFetchResult {
        AlarmeeHelper().onNotificationReceived(userInfo: userInfo)
        return UIBackgroundFetchResult.newData
    }
    
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        AlarmeeHelper().onNotificationReceived(userInfo: userInfo)
        
        completionHandler([.banner, .list, .badge, .sound])
    }
    
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        
        print("userInfo: \(userInfo)")

        if let value = userInfo["deepLinkUri"] as? String {
            // Check for the deep link URI here, which is only present if it was provided in the Alarmee(deepLinkUri: ...) creation.
        }

        completionHandler()
    }

    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        if userActivity.activityType == NSUserActivityTypeBrowsingWeb,
           let url = userActivity.webpageURL {
            handleIncomingURL(url)
            return true
        }
        
        print("No valid URL in user activity.")
        return false
    }
    
    func handleIncomingURL(_ url: URL) {
        print("handleIncomingURL", url)
        
        // First check if the URL is handled by Google Sign In
        if (GIDSignIn.sharedInstance.handle(url)) {
            print("Handled by GIDSignIn")
        }
        
        // Then check if the URL is handled by Passage (Firebase Authentication)
        else if (PassageHelper().handle(url: url.absoluteString)) {
            print("Handled by Passage")
        }
    }
}

package com.tweener.kmpship.domain.error

/**
 * @author Vivien Mahe
 * @since 15/01/2024
 */
class UserNotAuthenticatedException : NoSuchElementException("There is no user currently logged in!")

package dev.jdgarita.frnk.presentation.componentCore

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalObjCName::class)
@Serializable
@Suppress("unused")
enum class SemanticTypography {
    //region Body
    @ObjCName("bodyRegular")
    BodyRegular,

    @ObjCName("bodyRegularStrikethrough")
    BodyRegularStrikethrough,

    @ObjCName("bodyMedium")
    BodyMedium,

    @ObjCName("bodySemibold")
    BodySemibold,

    @ObjCName("bodyBold")
    BodyBold,

    @ObjCName("bodyBoldSubscript")
    BodyBoldSubscript,
    //endregion

    //region Caption
    @ObjCName("captionRegular")
    CaptionRegular,

    @ObjCName("captionRegularStrikethrough")
    CaptionRegularStrikethrough,

    @ObjCName("captionRegularCapitalized")
    CaptionRegularCapitalized,

    @ObjCName("captionMedium")
    CaptionMedium,

    @ObjCName("captionBold")
    CaptionBold,

    @ObjCName("captionHeavy")
    CaptionHeavy,
    //endregion

    //region Footnote
    @ObjCName("footnoteRegular")
    FootnoteRegular,

    @ObjCName("footnoteMedium")
    FootnoteMedium,

    @ObjCName("footnoteBold")
    FootnoteBold,
    //endregion

    //region Subtitle
    @ObjCName("subtitleRegular")
    SubtitleRegular,

    @ObjCName("subtitleRegularStrikethrough")
    SubtitleRegularStrikethrough,

    @ObjCName("subtitleMedium")
    SubtitleMedium,

    @ObjCName("subtitleBold")
    SubtitleBold,
    //endregion

    //region Title
    @ObjCName("titleMedium")
    TitleMedium,

    @ObjCName("titleSemibold")
    TitleSemibold,

    @ObjCName("titleBold")
    TitleBold,

    @ObjCName("titleHeavy")
    TitleHeavy,
    //endregion

    //region Title Large
    @ObjCName("titleLargeRegular")
    TitleLargeRegular,

    @ObjCName("titleLargeMedium")
    TitleLargeMedium,

    @ObjCName("titleLargeSemibold")
    TitleLargeSemibold,

    @ObjCName("titleLargeHeavy")
    TitleLargeHeavy,
    //endregion

    //region Display
    @ObjCName("displayRegular")
    DisplayRegular,

    @ObjCName("displayRegularStrikethrough")
    DisplayRegularStrikethrough,

    @ObjCName("displaySemibold")
    DisplaySemibold,

    @ObjCName("displayBold")
    DisplayBold,
    //endregion

    //region Display Large
    @ObjCName("displayLargeRegular")
    DisplayLargeRegular,

    @ObjCName("displayLargeSemibold")
    DisplayLargeSemibold
    //endregion
}
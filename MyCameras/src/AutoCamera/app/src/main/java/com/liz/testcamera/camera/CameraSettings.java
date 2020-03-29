/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liz.testcamera.camera;

import android.media.CamcorderProfile;

/**
 *  Provides utilities and keys for Camera settings.
 */
public class CameraSettings {
    private static final int NOT_FOUND = -1;

    public static final String KEY_VERSION = "pref_version_key";
    public static final String KEY_LOCAL_VERSION = "pref_local_version_key";
    public static final String KEY_RECORD_LOCATION = "pref_camera_recordlocation_key";
    public static final String KEY_VIDEO_QUALITY = "pref_video_quality_key";
    public static final String KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL = "pref_video_time_lapse_frame_interval_key";
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
    public static final String KEY_FLASH_MODE = "pref_camera_flashmode_key";
    public static final String KEY_VIDEOCAMERA_FLASH_MODE = "pref_camera_video_flashmode_key";
    public static final String KEY_WHITE_BALANCE = "pref_camera_whitebalance_key";
    public static final String KEY_SCENE_MODE = "pref_camera_scenemode_key";
    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
    public static final String KEY_TIMER = "pref_camera_timer_key";
    public static final String KEY_TIMER_SOUND_EFFECTS = "pref_camera_timer_sound_key";
    public static final String KEY_VIDEO_EFFECT = "pref_video_effect_key";
    public static final String KEY_CAMERA_ID = "pref_camera_id_key";
    public static final String KEY_CAMERA_HDR = "pref_camera_hdr_key";
    public static final String KEY_CAMERA_HQ = "pref_camera_hq_key";
    public static final String KEY_CAMERA_HDR_PLUS = "pref_camera_hdr_plus_key";
    public static final String KEY_CAMERA_FIRST_USE_HINT_SHOWN = "pref_camera_first_use_hint_shown_key";
    public static final String KEY_VIDEO_FIRST_USE_HINT_SHOWN = "pref_video_first_use_hint_shown_key";
    public static final String KEY_PHOTOSPHERE_PICTURESIZE = "pref_photosphere_picturesize_key";
    public static final String KEY_STARTUP_MODULE_INDEX = "camera.startup_module";

    public static final String KEY_VIDEO_ENCODER = "pref_camera_videoencoder_key";
    public static final String KEY_AUDIO_ENCODER = "pref_camera_audioencoder_key";
    public static final String KEY_VIDEO_DURATION = "pref_camera_video_duration_key";
    public static final String KEY_POWER_MODE = "pref_camera_powermode_key";
    public static final String KEY_PICTURE_FORMAT = "pref_camera_pictureformat_key";
    public static final String KEY_ZSL = "pref_camera_zsl_key";
    public static final String KEY_CAMERA_SAVEPATH = "pref_camera_savepath_key";
    public static final String KEY_FILTER_MODE = "pref_camera_filter_mode_key";
    public static final String KEY_COLOR_EFFECT = "pref_camera_coloreffect_key";
    public static final String KEY_FACE_DETECTION = "pref_camera_facedetection_key";
    public static final String KEY_TOUCH_AF_AEC = "pref_camera_touchafaec_key";
    public static final String KEY_SELECTABLE_ZONE_AF = "pref_camera_selectablezoneaf_key";
    public static final String KEY_SATURATION = "pref_camera_saturation_key";
    public static final String KEY_CONTRAST = "pref_camera_contrast_key";
    public static final String KEY_SHARPNESS = "pref_camera_sharpness_key";
    public static final String KEY_AUTOEXPOSURE = "pref_camera_autoexposure_key";
    public static final String KEY_ANTIBANDING = "pref_camera_antibanding_key";
    public static final String KEY_ISO = "pref_camera_iso_key";
    public static final String KEY_LENSSHADING = "pref_camera_lensshading_key";
    public static final String KEY_HISTOGRAM = "pref_camera_histogram_key";
    public static final String KEY_DENOISE = "pref_camera_denoise_key";
    public static final String KEY_BRIGHTNESS = "pref_camera_brightness_key";
    public static final String KEY_REDEYE_REDUCTION = "pref_camera_redeyereduction_key";
    public static final String KEY_SELFIE_MIRROR = "pref_camera_selfiemirror_key";
    public static final String KEY_SHUTTER_SOUND = "pref_camera_shuttersound_key";
    public static final String KEY_CDS_MODE = "pref_camera_cds_mode_key";
    public static final String KEY_VIDEO_CDS_MODE = "pref_camera_video_cds_mode_key";
    public static final String KEY_TNR_MODE = "pref_camera_tnr_mode_key";
    public static final String KEY_VIDEO_TNR_MODE = "pref_camera_video_tnr_mode_key";
    public static final String KEY_AE_BRACKET_HDR = "pref_camera_ae_bracket_hdr_key";
    public static final String KEY_ADVANCED_FEATURES = "pref_camera_advanced_features_key";
    public static final String KEY_HDR_MODE = "pref_camera_hdr_mode_key";
    public static final String KEY_HDR_NEED_1X = "pref_camera_hdr_need_1x_key";
    public static final String KEY_DEVELOPER_MENU = "pref_developer_menu_key";

    public static final String KEY_VIDEO_SNAPSHOT_SIZE = "pref_camera_videosnapsize_key";
    public static final String KEY_VIDEO_HIGH_FRAME_RATE = "pref_camera_hfr_key";
    public static final String KEY_SEE_MORE = "pref_camera_see_more_key";
    public static final String KEY_NOISE_REDUCTION = "pref_camera_noise_reduction_key";
    public static final String KEY_VIDEO_HDR = "pref_camera_video_hdr_key";
    public static final String DEFAULT_VIDEO_QUALITY_VALUE = "custom";
    public static final String KEY_SKIN_TONE_ENHANCEMENT = "pref_camera_skinToneEnhancement_key";
    public static final String KEY_SKIN_TONE_ENHANCEMENT_FACTOR = "pref_camera_skinToneEnhancement_factor_key";

    public static final String KEY_FACE_RECOGNITION = "pref_camera_facerc_key";
    public static final String KEY_DIS = "pref_camera_dis_key";

    public static final String KEY_LONGSHOT = "pref_camera_longshot_key";
    public static final String KEY_INSTANT_CAPTURE = "pref_camera_instant_capture_key";
    public static final String KEY_ZOOM = "pref_camera_zoom_key";

    public static final String KEY_BOKEH_MODE = "pref_camera_bokeh_mode_key";
    public static final String KEY_BOKEH_MPO = "pref_camera_bokeh_mpo_key";
    public static final String KEY_BOKEH_BLUR_VALUE = "pref_camera_bokeh_blur_degree_key";

    private static final String KEY_QC_SUPPORTED_AE_BRACKETING_MODES = "ae-bracket-hdr-values";
    private static final String KEY_QC_SUPPORTED_AF_BRACKETING_MODES = "af-bracket-values";
    private static final String KEY_QC_SUPPORTED_RE_FOCUS_MODES = "re-focus-values";
    private static final String KEY_QC_SUPPORTED_CF_MODES = "chroma-flash-values";
    private static final String KEY_QC_SUPPORTED_OZ_MODES = "opti-zoom-values";
    private static final String KEY_QC_SUPPORTED_FSSR_MODES = "FSSR-values";
    private static final String KEY_QC_SUPPORTED_TP_MODES = "true-portrait-values";
    private static final String KEY_QC_SUPPORTED_MTF_MODES = "multi-touch-focus-values";
    private static final String KEY_QC_SUPPORTED_FACE_RECOGNITION_MODES = "face-recognition-values";
    private static final String KEY_QC_SUPPORTED_DIS_MODES = "dis-values";
    private static final String KEY_QC_SUPPORTED_SEE_MORE_MODES = "see-more-values";
    private static final String KEY_QC_SUPPORTED_NOISE_REDUCTION_MODES = "noise-reduction-mode-values";
    private static final String KEY_QC_SUPPORTED_STILL_MORE_MODES = "still-more-values";
    private static final String KEY_QC_SUPPORTED_CDS_MODES = "cds-mode-values";
    private static final String KEY_QC_SUPPORTED_VIDEO_CDS_MODES = "video-cds-mode-values";
    private static final String KEY_QC_SUPPORTED_TNR_MODES = "tnr-mode-values";
    private static final String KEY_QC_SUPPORTED_VIDEO_TNR_MODES = "video-tnr-mode-values";
    private static final String KEY_SNAPCAM_SUPPORTED_HDR_MODES = "hdr-mode-values";
    private static final String KEY_SNAPCAM_SUPPORTED_HDR_NEED_1X = "hdr-need-1x-values";
    public static final String KEY_QC_AE_BRACKETING = "ae-bracket-hdr";
    public static final String KEY_QC_AF_BRACKETING = "af-bracket";
    public static final String KEY_QC_RE_FOCUS = "re-focus";
    public static final int KEY_QC_RE_FOCUS_COUNT = 7;
    public static final String KEY_QC_LEGACY_BURST = "snapshot-burst-num";
    public static final String KEY_QC_CHROMA_FLASH = "chroma-flash";
    public static final String KEY_QC_OPTI_ZOOM = "opti-zoom";
    public static final String KEY_QC_FSSR = "FSSR";
    public static final String KEY_QC_TP = "true-portrait";
    public static final String KEY_QC_MULTI_TOUCH_FOCUS = "multi-touch-focus";
    public static final String KEY_QC_STILL_MORE = "still-more";
    public static final String KEY_QC_FACE_RECOGNITION = "face-recognition";
    public static final String KEY_QC_DIS_MODE = "dis";
    public static final String KEY_QC_CDS_MODE = "cds-mode";
    public static final String KEY_QC_VIDEO_CDS_MODE = "video-cds-mode";
    public static final String KEY_QC_TNR_MODE = "tnr-mode";
    public static final String KEY_QC_VIDEO_TNR_MODE = "video-tnr-mode";
    public static final String KEY_SNAPCAM_HDR_MODE = "hdr-mode";
    public static final String KEY_SNAPCAM_HDR_NEED_1X = "hdr-need-1x";
    public static final String KEY_VIDEO_HSR = "video-hsr";
    public static final String KEY_QC_SEE_MORE_MODE = "see-more";
    public static final String KEY_QC_NOISE_REDUCTION_MODE = "noise-reduction-mode";
    public static final String KEY_QC_INSTANT_CAPTURE = "instant-capture";
    public static final String KEY_QC_INSTANT_CAPTURE_VALUES = "instant-capture-values";

    public static final String KEY_INTERNAL_PREVIEW_RESTART = "internal-restart";
    public static final String KEY_QC_ZSL_HDR_SUPPORTED = "zsl-hdr-supported";
    public static final String KEY_QC_LONGSHOT_SUPPORTED = "longshot-supported";
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    public static final String KEY_AUTO_HDR = "pref_camera_auto_hdr_key";

    //for flip
    public static final String KEY_QC_PREVIEW_FLIP = "preview-flip";
    public static final String KEY_QC_VIDEO_FLIP = "video-flip";
    public static final String KEY_QC_SNAPSHOT_PICTURE_FLIP = "snapshot-picture-flip";
    public static final String KEY_QC_SUPPORTED_FLIP_MODES = "flip-mode-values";

    public static final String FLIP_MODE_OFF = "off";
    public static final String FLIP_MODE_V = "flip-v";
    public static final String FLIP_MODE_H = "flip-h";
    public static final String FLIP_MODE_VH = "flip-vh";

    private static final String KEY_QC_PICTURE_FORMAT = "picture-format-values";
    public static final String KEY_VIDEO_ROTATION = "pref_camera_video_rotation_key";
    private static final String VIDEO_QUALITY_HIGH = "high";
    private static final String VIDEO_QUALITY_MMS = "mms";
    private static final String VIDEO_QUALITY_YOUTUBE = "youtube";


    //manual 3A keys and parameter strings
    public static final String KEY_MANUAL_EXPOSURE = "pref_camera_manual_exp_key";
    public static final String KEY_MANUAL_WB = "pref_camera_manual_wb_key";
    public static final String KEY_MANUAL_FOCUS = "pref_camera_manual_focus_key";

    public static final String KEY_MANUAL_EXPOSURE_MODES = "manual-exp-modes";
    public static final String KEY_MANUAL_WB_MODES = "manual-wb-modes";
    public static final String KEY_MANUAL_FOCUS_MODES = "manual-focus-modes";
    //manual exposure
    public static final String KEY_MIN_EXPOSURE_TIME = "min-exposure-time";
    public static final String KEY_MAX_EXPOSURE_TIME = "max-exposure-time";
    public static final String KEY_EXPOSURE_TIME = "exposure-time";
    public static final String KEY_MIN_ISO = "min-iso";
    public static final String KEY_MAX_ISO = "max-iso";
    public static final String KEY_CONTINUOUS_ISO = "continuous-iso";
    public static final String KEY_MANUAL_ISO = "manual";
    public static final String KEY_AUTO_ISO = "auto";
    public static final String KEY_CURRENT_ISO = "cur-iso";
    public static final String KEY_CURRENT_EXPOSURE_TIME = "cur-exposure-time";

    //manual WB
    public static final String KEY_MIN_WB_GAIN = "min-wb-gain";
    public static final String KEY_MAX_WB_GAIN = "max-wb-gain";
    public static final String KEY_MANUAL_WB_GAINS = "manual-wb-gains";
    public static final String KEY_MIN_WB_CCT = "min-wb-cct";
    public static final String KEY_MAX_WB_CCT = "max-wb-cct";
    public static final String KEY_MANUAL_WB_CCT = "wb-manual-cct";
    public static final String KEY_MANUAL_WHITE_BALANCE = "manual";
    public static final String KEY_MANUAL_WB_TYPE = "manual-wb-type";
    public static final String KEY_MANUAL_WB_VALUE = "manual-wb-value";

    //manual focus
    public static final String KEY_MIN_FOCUS_SCALE = "min-focus-pos-ratio";
    public static final String KEY_MAX_FOCUS_SCALE = "max-focus-pos-ratio";
    public static final String KEY_MIN_FOCUS_DIOPTER = "min-focus-pos-diopter";
    public static final String KEY_MAX_FOCUS_DIOPTER = "max-focus-pos-diopter";
    public static final String KEY_MANUAL_FOCUS_TYPE = "manual-focus-pos-type";
    public static final String KEY_MANUAL_FOCUS_POSITION = "manual-focus-position";
    public static final String KEY_MANUAL_FOCUS_SCALE = "cur-focus-scale";
    public static final String KEY_MANUAL_FOCUS_DIOPTER = "cur-focus-diopter";

    public static final String KEY_QC_SUPPORTED_MANUAL_FOCUS_MODES = "manual-focus-modes";
    public static final String KEY_QC_SUPPORTED_MANUAL_EXPOSURE_MODES = "manual-exposure-modes";
    public static final String KEY_QC_SUPPORTED_MANUAL_WB_MODES = "manual-wb-modes";

    //Bokeh
    public static final String KEY_QC_IS_BOKEH_MODE_SUPPORTED = "is-bokeh-supported";
    public static final String KEY_QC_IS_BOKEH_MPO_SUPPORTED = "is-bokeh-mpo-supported";
    public static final String KEY_QC_BOKEH_MODE = "bokeh-mode";
    public static final String KEY_QC_BOKEH_MPO_MODE = "bokeh-mpo-mode";
    public static final String KEY_QC_SUPPORTED_DEGREES_OF_BLUR = "supported-blur-degrees";
    public static final String KEY_QC_BOKEH_BLUR_VALUE = "bokeh-blur-value";

    public static final String KEY_TS_MAKEUP_UILABLE = "pref_camera_tsmakeup_key";
    public static final String KEY_TS_MAKEUP_PARAM = "tsmakeup"; // on/of
    public static final String KEY_TS_MAKEUP_PARAM_WHITEN = "tsmakeup_whiten"; // 0~100
    public static final String KEY_TS_MAKEUP_PARAM_CLEAN = "tsmakeup_clean";  // 0~100
    public static final String KEY_TS_MAKEUP_LEVEL = "pref_camera_tsmakeup_level_key";
    public static final String KEY_TS_MAKEUP_LEVEL_WHITEN = "pref_camera_tsmakeup_whiten";
    public static final String KEY_TS_MAKEUP_LEVEL_CLEAN = "pref_camera_tsmakeup_clean";

    public static final String KEY_REFOCUS_PROMPT = "refocus-prompt";

    public static final String KEY_SHOW_MENU_HELP = "help_menu";

    public static final String KEY_REQUEST_PERMISSION = "request_permission";

    public static final String KEY_SELFIE_FLASH = "pref_selfie_flash_key";

    public static final String EXPOSURE_DEFAULT_VALUE = "0";

    public static final int CURRENT_VERSION = 5;
    public static final int CURRENT_LOCAL_VERSION = 2;

    public static final int DEFAULT_VIDEO_DURATION = 0; // no limit
    private static final int MMS_VIDEO_DURATION = (CamcorderProfile.get(CamcorderProfile.QUALITY_LOW) != null) ?
            CamcorderProfile.get(CamcorderProfile.QUALITY_LOW).duration : 30;
    private static final int YOUTUBE_VIDEO_DURATION = 15 * 60; // 15 mins
}

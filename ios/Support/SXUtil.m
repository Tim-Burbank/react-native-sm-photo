
#import "SXUtil.h"
#import "UIImage+Operation.h"
#import "PhotoPlugin.h"
#import "PhotoModel.h"
#import "UIColor+StringColor.h"

@implementation SXUtil

+ (NSString *)SXLocalizedString:(NSString *)key comment:(NSString *)comment {
    NSString *l = [[NSUserDefaults standardUserDefaults] objectForKey:@"language"];
    if (l) {
        NSDictionary *dic = [PhotoModel shared].langDic[l];
        if (dic) {
            NSString *labelString = dic[key];
            if (labelString) {
                return labelString;
            }
        }
        NSString *path = @"";
        if([l containsString:@"zh"]) {
            path = [PhotoPlugin.currentBundle pathForResource:@"zh-Hans" ofType:@"lproj"];
        } else if ([l containsString:@"en"]) {
            path = [PhotoPlugin.currentBundle pathForResource:@"en" ofType:@"lproj"];
        }
        NSString *labelString = [[NSBundle bundleWithPath:path] localizedStringForKey:key value:nil table:@"DXPhotoPicker"];
        if (labelString) {
            return labelString;
        }
    }
    return NSLocalizedStringWithDefaultValue(key, @"DXPhotoPicker", PhotoPlugin.currentBundle, @"", comment);
}

+ (NSData *)compressImage:(UIImage *)image shortEdge:(CGFloat)sg level:(CGFloat)l {
    CGFloat width = image.size.width;
    CGFloat height = image.size.height;
    CGFloat ratio = 0;
    if (width >= height) {
        if (height <= sg) {
            return [self getUIImageJPEDGRepresentation:image level:l];
        }
        ratio = height / sg;
        width = width / ratio;
        height = sg;
    } else {
        if (width <= sg) {
            return [self getUIImageJPEDGRepresentation:image level:l];
        }
        ratio = width / sg;
        height = height / ratio;
        width = sg;
    }
    
    UIImage *result = [image imageByScalingAndCroppingForSize:CGSizeMake(width, height) shortEdge:YES];
    return UIImageJPEGRepresentation(result, l);
}

+ (NSData *)getUIImageJPEDGRepresentation:(UIImage *)image level:(CGFloat)level {
    NSData *data = UIImageJPEGRepresentation(image, level);
    if (data) {
        return data;
    } else {
        UIGraphicsBeginImageContext(image.size);
        [image drawInRect:CGRectMake(0, 0, image.size.width, image.size.height)];
        UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        return UIImageJPEGRepresentation(newImage, level);
    }
}

+ (UIColor *)colorWithKey:(NSString *)key {
    NSString *lang = [[NSUserDefaults standardUserDefaults] objectForKey:@"language"];
    NSDictionary *dict = [PhotoModel shared].colorDic[lang];
    if (dict) {
        NSString *css = dict[key];
        return [UIColor pp_colorWithHexString:css];
    }
    return [self defaultColor:key];
}

+ (UIColor *)defaultColor:(NSString *)key {
    if ([key isEqualToString:DownloadProgressTitleColor]) {
        return [UIColor pp_colorWithHexString:@"D8B434"];
    } else if ([key isEqualToString:DownloadProgressStartColor]) {
        return [UIColor pp_colorWithHexString:@"B18B3E"];
    } else if ([key isEqualToString:DownloadProgressEndColor]) {
        return [UIColor pp_colorWithHexString:@"FFCA22"];
    } else if ([key isEqualToString:CameraRecordVideoVCCodeLabelTextColor]) {
        return [UIColor pp_colorWithHexString:@"D6B435"];
    } else if ([key isEqualToString:CameraRecordVideoVCRecordButtonColor]) {
        return [UIColor pp_colorWithHexString:@"D6B435"];
    } else if ([key isEqualToString:LineTimerBarColor]) {
        return [UIColor pp_colorWithHexString:@"D6B435"];
    } else if ([key isEqualToString:RecordVideoProgressColor]) {
        return [UIColor pp_colorWithHexString:@"D8D8D8"];
    } else if ([key isEqualToString:TimeLabelColor]) {
        return [UIColor pp_colorWithHexString:@"FFD703"];
    } else if ([key isEqualToString:SendButtonTintNormalColor]) {
        return [UIColor pp_colorWithHexString:@"D8B434"];
    } else if ([key isEqualToString:SendButtonTintHighlightedColor]) {
        return [UIColor pp_colorWithHexString:@"FFFFFF"];
    } else if ([key isEqualToString:SendButtonTintDisabledColor]) {
        return [UIColor pp_colorWithHexString:@"FFFFFF"];
    } else {
        return [UIColor clearColor];
    }
}

+ (UIImage *)bunldeName:(NSString *)name {
    NSString *lang = [[NSUserDefaults standardUserDefaults] objectForKey:@"language"];
    if (lang) {
        NSDictionary *dic = [PhotoModel shared].imgDic[lang];
        if (dic) {
            NSString *subPath = dic[name];
            NSString *imagePath = [[NSBundle mainBundle] pathForResource:subPath ofType:nil];
            if (imagePath) {
                return [UIImage imageWithContentsOfFile:imagePath];
            }
        }
    }
    return [UIImage imageNamed:name inBundle:PhotoPlugin.currentBundle compatibleWithTraitCollection:nil];
}

@end

NSString *const BackButton = @"BackButton";
NSString *const NaviSelected = @"NaviSelected";
NSString *const NaviDeselected = @"NaviDeselected";
NSString *const photo_check_selected = @"photo_check_selected";
NSString *const photo_check_selected_check = @"photo_check_selected_check";
NSString *const photo_check_default = @"photo_check_default";
NSString *const assets_placeholder_picture = @"assets_placeholder_picture";
NSString *const videoicon = @"videoicon";
NSString *const photo_full_image_selected = @"photo_full_image_selected";
NSString *const photo_full_image_unselected = @"photo_full_image_unselected";
NSString *const image_unAuthorized = @"image_unAuthorized";
NSString *const back_white_arrow = @"back_white_arrow";
NSString *const record_button_normal = @"record_button_normal";
NSString *const record_button_pause = @"record_button_pause";
NSString *const record_button_active = @"record_button_active";
NSString *const camera_flip_button = @"camera_flip_button";
NSString *const layer = @"layer";
NSString *const video_preview_play_button = @"video_preview_play_button";
NSString *const video_preview_pause_button = @"video_preview_pause_button";
NSString *const point = @"point";
// color key
NSString *const DownloadProgressTitleColor = @"DownloadProgressTitleColor";
NSString *const DownloadProgressStartColor = @"DownloadProgressStartColor";
NSString *const DownloadProgressEndColor = @"DownloadProgressEndColor";
NSString *const CameraRecordVideoVCCodeLabelTextColor = @"CameraRecordVideoVCCodeLabelTextColor";
NSString *const CameraRecordVideoVCRecordButtonColor= @"CameraRecordVideoVCRecordButtonColor";
NSString *const LineTimerBarColor = @"LineTimerBarColor";
NSString *const RecordVideoProgressColor = @"RecordVideoProgressColor";
NSString *const TimeLabelColor = @"TimeLabelColor";
NSString *const SendButtonTintNormalColor = @"SendButtonTintNormalColor";
NSString *const SendButtonTintHighlightedColor = @"SendButtonTintHighlightedColor";
NSString *const SendButtonTintDisabledColor = @"SendButtonTintDisabledColor";

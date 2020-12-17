

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface SXUtil : NSObject

+ (NSString *)SXLocalizedString:(NSString *)key comment:(NSString *)comment;

+ (NSData *)compressImage:(UIImage *)image shortEdge:(CGFloat)sg level:(CGFloat)l;

+ (NSData *)getUIImageJPEDGRepresentation:(UIImage *)image level:(CGFloat)level;

+ (UIImage *)bunldeName:(NSString *)name;

+ (UIColor *)colorWithKey:(NSString *)key;

@end

extern NSString *const BackButton;
extern NSString *const NaviSelected;
extern NSString *const NaviDeselected;
extern NSString *const photo_check_selected;
extern NSString *const photo_check_selected_check;
extern NSString *const photo_check_default;
extern NSString *const assets_placeholder_picture;
extern NSString *const videoicon;
extern NSString *const photo_full_image_selected;
extern NSString *const photo_full_image_unselected;
extern NSString *const image_unAuthorized;
extern NSString *const back_white_arrow;
extern NSString *const record_button_normal;
extern NSString *const record_button_pause;
extern NSString *const record_button_active;
extern NSString *const camera_flip_button;
extern NSString *const layer;
extern NSString *const video_preview_play_button;
extern NSString *const video_preview_pause_button;
extern NSString *const point;

// color key
extern NSString *const DownloadProgressTitleColor;
extern NSString *const DownloadProgressStartColor;
extern NSString *const DownloadProgressEndColor;
extern NSString *const CameraRecordVideoVCCodeLabelTextColor;
extern NSString *const CameraRecordVideoVCRecordButtonColor;
extern NSString *const LineTimerBarColor;
extern NSString *const RecordVideoProgressColor;
extern NSString *const TimeLabelColor;
extern NSString *const SendButtonTintNormalColor;
extern NSString *const SendButtonTintHighlightedColor;
extern NSString *const SendButtonTintDisabledColor;

NS_ASSUME_NONNULL_END

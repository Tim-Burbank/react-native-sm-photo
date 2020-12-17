
#import <UIKit/UIKit.h>
#import <Photos/Photos.h>

NS_ASSUME_NONNULL_BEGIN

@protocol SXPhotoPickerControllerDelegate;

typedef NS_ENUM(NSInteger, MediaType) {
    MediaTypeAll,
    MediaTypePhoto,
    MediaTypeVideo
};

@interface SXPhotoPickerController : UINavigationController

@property (nonatomic, assign, class) NSInteger maxSelectedNumber;

@property (nonatomic, assign, class) NSInteger minSelectedNumber;

@property (nonatomic, assign, class) BOOL isAvatar;

@property (nonatomic, assign, class) MediaType  mediaType;

@property (nonatomic, weak) id<SXPhotoPickerControllerDelegate> photoPickerDelegate;

@end

@protocol SXPhotoPickerControllerDelegate <NSObject>

@optional

/**
 seletced call back
 
 - parameter photosPicker: the photoPicker
 - parameter sendImages:   selected images
 - parameter isFullImage:  if the selected image is high quality
 */

- (void)photoPickerController:(SXPhotoPickerController *)photoPicker sendImages:(NSArray<PHAsset *> *)sendImages isFullImage:(BOOL)isFull;

- (void)photoPickerDidCancel:(SXPhotoPickerController *)photoPicker;

// 已经选择了iCloud视频或者本地视频后，因为已经处理过，为了避免重复处理，不再走处理的代理photoPickerController(_ photoPicker: DXPhotoPickerController?, sendImages: [PHAsset]?, isFullImage: Bool)
//
// - Parameters:
//   - photoPicker: DXPhotoPickerController
//   - localPath: 视频的本地路径
//   - thumbnailPath: 视频缩略图的路径
//   - duration: 视频的时长

- (void)didSendLocalVideo:(SXPhotoPickerController *)photoPicker localPath:(NSString *)path thumbnailPath:(NSString *)tbpath duration:(NSInteger)time;

@end



NS_ASSUME_NONNULL_END

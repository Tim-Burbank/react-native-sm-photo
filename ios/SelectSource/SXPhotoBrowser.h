
#import <UIKit/UIKit.h>
#import <Photos/Photos.h>

NS_ASSUME_NONNULL_BEGIN

@protocol SXPhotoBrowserDelegate;

@interface SXPhotoBrowser : UIViewController

@property (nonatomic, weak) id<SXPhotoBrowserDelegate> delegate;

- (instancetype)initWithPhotos:(NSArray *)photos currentIndex:(NSInteger)index isFullImage:(BOOL)isFull;

@end

@protocol SXPhotoBrowserDelegate <NSObject>

- (void)sendImagesFromPhotoBrowser:(SXPhotoBrowser *)photoBrowser currentAsset:(PHAsset *)ca;   

- (NSInteger)selectedPhotosNumberInPhotoBrowser:(SXPhotoBrowser *)photoBrowser;

- (BOOL)photoBrowser:(SXPhotoBrowser *)photoBrowser currentPhotoAssetIsSelected:(PHAsset *)asset;

- (BOOL)photobrowser:(SXPhotoBrowser *)photoBrowser selectedAssset:(PHAsset *)asset;

- (void)photoBrowser:(SXPhotoBrowser *)photoBrowser deselectedAsset:(PHAsset *)asset;

- (void)photoBrowser:(SXPhotoBrowser *)photoBrowser selectedFullImage:(BOOL)isfull;

@end

NS_ASSUME_NONNULL_END

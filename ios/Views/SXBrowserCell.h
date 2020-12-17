
#import <UIKit/UIKit.h>
#import <Photos/Photos.h>
#import <AVKit/AVKit.h>
#import "SXPhotoBrowser.h"

NS_ASSUME_NONNULL_BEGIN

@interface SXBrowserCell : UICollectionViewCell

@property (nonatomic, weak) SXPhotoBrowser *photoBrowser;

@property (nonatomic, strong) PHAsset *assset;

@end

NS_ASSUME_NONNULL_END


#import <Foundation/Foundation.h>
#import "SXAlbum.h"

NS_ASSUME_NONNULL_BEGIN

@interface DXPickerHelper : NSObject

+ (void)saveIndentifier:(NSString *)identifier;

+ (NSString *)fetchAlbumIdentifier;

+ (SXAlbum *)fetchAlbum;

+ (NSArray *)fetchAlbumList;

+ (PHImageRequestID)fetchImageWithAsset:(PHAsset *)asset targetSize:(CGSize)size imageResultHanlder:(void(^)(UIImage *image))handler;

+ (PHImageRequestID)fetchImageWithAsset:(PHAsset *)asset targetSize:(CGSize)size needHighQuality:(BOOL)isNeed imageResultHandler:(void(^)(UIImage *image, NSArray<UIImage *> *images, NSTimeInterval duration))handler;

+ (void)fetchImageSize:(PHAsset *)asset imageResizeResultHandler:(void(^)(CGFloat imageSize, NSString *sizeString))handler;

+ (NSString *)limitSizeString;

+ (void)gifCanuse:(PHAsset *)asset resultHandler:(void(^)(BOOL canUse))handler;

+ (NSArray *)fetchImageAssetsViaCollectionResults:(PHFetchResult *)results;

@end

@interface PHAsset (ImageType)

@property (nonatomic, assign, readonly) BOOL isGIF;

@end

NS_ASSUME_NONNULL_END


#import "DXPickerHelper.h"
#import "SXPhotoPickerController.h"
#import "PhotoModel.h"
#import <MobileCoreServices/MobileCoreServices.h>

static NSString *const kSXPickerManagerDefaultAlbumIdentifier = @"com.dennis.kDXPhotoPickerStoredGroup";
static NSString *const kSXPickerManagerDefaultVideoAlbumIdentifier = @"com.dennis.kDXVideoPickerStoredGroup";

@interface DXPickerHelper ()

@end

@implementation DXPickerHelper

+ (void)saveIndentifier:(NSString *)identifier {
    if (identifier) {
        if (SXPhotoPickerController.mediaType == MediaTypeVideo) {
            [[NSUserDefaults standardUserDefaults] setObject:identifier forKey:kSXPickerManagerDefaultVideoAlbumIdentifier];
        } else {
            [[NSUserDefaults standardUserDefaults] setObject:identifier forKey:kSXPickerManagerDefaultAlbumIdentifier];
        }
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
}

+ (NSString *)fetchAlbumIdentifier {
    if (SXPhotoPickerController.mediaType == MediaTypeVideo) {
        return [[NSUserDefaults standardUserDefaults] objectForKey:kSXPickerManagerDefaultVideoAlbumIdentifier];
    }
//    else {
//        return [[NSUserDefaults standardUserDefaults] objectForKey:kSXPickerManagerDefaultAlbumIdentifier];
//    }
    return nil;
}

+ (SXAlbum *)fetchAlbum {
    SXAlbum *album = [[SXAlbum alloc] init];
    NSString *identifier = [self fetchAlbumIdentifier];
    if (!identifier) {
        return album;
    }
    PHFetchOptions *options = [[PHFetchOptions alloc] init];
    if (SXPhotoPickerController.mediaType == MediaTypeVideo) {
        options.predicate = [NSPredicate predicateWithFormat:@"mediaType = %d", PHAssetMediaTypeVideo];
    } else if (SXPhotoPickerController.mediaType == MediaTypePhoto) {
        options.predicate = [NSPredicate predicateWithFormat:@"mediaType = %d", PHAssetMediaTypeImage];
    }
    
    PHFetchResult<PHAssetCollection *> *result = [PHAssetCollection fetchAssetCollectionsWithLocalIdentifiers:@[identifier] options:nil];
    if (result.count <= 0) {
        return album;
    }
    PHAssetCollection *collection = [result firstObject];
    PHFetchResult<PHAsset *> *requestResult = [PHAsset fetchAssetsInAssetCollection:collection options:options];
    album.name = collection.localizedTitle;
    album.results = requestResult;
    album.count = requestResult.count;
    album.startDate = collection.startDate;
    album.identifier = collection.localIdentifier;
    return album;
}

+ (NSArray *)fetchAlbums {
    PHFetchOptions *userAlbumsOptions = [[PHFetchOptions alloc] init];
    userAlbumsOptions.predicate = [NSPredicate predicateWithFormat:@"estimatedAssetCount > 0"];
    NSSortDescriptor *sortd = [NSSortDescriptor sortDescriptorWithKey:@"startDate" ascending:NO];
    userAlbumsOptions.sortDescriptors = @[sortd];
    NSMutableArray *albums = [[NSMutableArray alloc] init];
    PHFetchResult *result1 = [PHAssetCollection fetchAssetCollectionsWithType:PHAssetCollectionTypeSmartAlbum subtype:PHAssetCollectionSubtypeAlbumRegular options:nil];
    [albums addObject:result1];
    PHFetchResult *result2 = [PHAssetCollection fetchAssetCollectionsWithType:PHAssetCollectionTypeAlbum subtype:PHAssetCollectionSubtypeAny options:userAlbumsOptions];
    [albums addObject:result2];
    return albums;
}

+ (NSArray *)fetchAlbumList {
    NSArray *results = [self fetchAlbums];
    NSMutableArray *list = [[NSMutableArray alloc] init];
    PHFetchOptions *options = [[PHFetchOptions alloc] init];
    if (SXPhotoPickerController.mediaType == MediaTypeVideo) {
        options.predicate = [NSPredicate predicateWithFormat:@"mediaType = %d", PHAssetMediaTypeVideo];
    } else if (SXPhotoPickerController.mediaType == MediaTypePhoto) {
        options.predicate = [NSPredicate predicateWithFormat:@"mediaType = %d", PHAssetMediaTypeImage];
    }
    for (NSInteger index = 0; index < results.count; index++) {
        PHFetchResult *result = results[index];
        [result enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            PHAssetCollection *album = (PHAssetCollection *)obj;
            PHFetchResult *assetResults = [PHAsset fetchAssetsInAssetCollection:album options:options];
            NSInteger count = 0;
            switch (album.assetCollectionType) {
                case PHAssetCollectionTypeAlbum:
                    count = assetResults.count;
                    break;
                case PHAssetCollectionTypeSmartAlbum:
                    count = assetResults.count;
                    break;
                case PHAssetCollectionTypeMoment:
                    count = 0;
                    break;
                default:
                    break;
            }
            if (count > 0) {
                @autoreleasepool {
                    SXAlbum *ab = [[SXAlbum alloc] init];
                    ab.count = count;
                    ab.results = assetResults;
                    ab.name  = album.localizedTitle;
                    ab.startDate = album.startDate;
                    ab.identifier = album.localIdentifier;
                    if ([PhotoModel shared].SelectGIF == GIFTYPEUse) {
                        if ([ab.name isEqualToString:@"Animated"] || [ab.name isEqualToString:@"动图"]) {
                            [list addObject:ab];
                        }
                    } else if ([PhotoModel shared].SelectGIF == GIFTYPEUnuse) {
                        if (![ab.name isEqualToString:@"Animated"] && ![ab.name isEqualToString:@"动图"]) {
                            [list addObject:ab];
                        }
                    } else {
                        [list addObject:ab];
                    }
                }
            }
        }];
    }
    return list;
}

+ (PHImageRequestID)fetchImageWithAsset:(PHAsset *)asset targetSize:(CGSize)tsize imageResultHanlder:(void (^)(UIImage * _Nonnull))handler {
    if (!asset) {
        return nil;
    }
    PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
    options.resizeMode = PHImageRequestOptionsResizeModeExact;
    CGFloat scale = [UIScreen mainScreen].scale;
    CGSize size = CGSizeMake(tsize.width*scale, tsize.height*scale);
    return [[PHImageManager defaultManager] requestImageForAsset:asset targetSize:size contentMode:PHImageContentModeAspectFill options:options resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
        handler(result);
    }];
}

+ (PHImageRequestID)fetchImageWithAsset:(PHAsset *)asset targetSize:(CGSize)size needHighQuality:(BOOL)isNeed imageResultHandler:(void (^)(UIImage * _Nonnull, NSArray<UIImage *> * _Nonnull, NSTimeInterval))handler {
    if (!asset) {
        return nil;
    }
    
    PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
    if (isNeed) {
        options.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
    } else {
        options.resizeMode = PHImageRequestOptionsResizeModeExact;
    }
    [options setNetworkAccessAllowed:YES];
    
    return [[PHImageManager defaultManager] requestImageDataForAsset:asset options:options resultHandler:^(NSData * _Nullable imageData, NSString * _Nullable dataUTI, UIImageOrientation orientation, NSDictionary * _Nullable info) {
        UIImage *image = nil;
        if (imageData) {
            image = [UIImage imageWithData:imageData];
        }
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            CGFloat totalDuration = 0.0;
            NSMutableArray *images = [[NSMutableArray alloc] init];
            CGImageSourceRef source = CGImageSourceCreateWithData((CFDataRef)imageData, nil);
            if (source) {
                size_t count = CGImageSourceGetCount(source);
                for (size_t i = 0; i < count; i++) {
                    CGImageRef img =  CGImageSourceCreateImageAtIndex(source, i, nil);
                    if (img) {
                        [images addObject:[UIImage imageWithCGImage:img]];
                    }
                    
                    CFDictionaryRef cfinfo = CGImageSourceCopyPropertiesAtIndex(source, i, nil);
                    NSDictionary *info = (__bridge NSDictionary *)cfinfo;
                    NSDictionary *timeDict = info[(NSString *)kCGImagePropertyGIFDictionary];
                    NSNumber *dalyTime = timeDict[(NSString *)kCGImagePropertyGIFDelayTime];
                    totalDuration += [dalyTime floatValue];
                    CFRelease(cfinfo);
                }
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                handler(image, images, totalDuration);
            });
        });
    }];
}

+ (void)fetchImageSize:(PHAsset *)asset imageResizeResultHandler:(void (^)(CGFloat, NSString * _Nonnull))handler {
    [[PHImageManager defaultManager] requestImageDataForAsset:asset options:nil resultHandler:^(NSData * _Nullable imageData, NSString * _Nullable dataUTI, UIImageOrientation orientation, NSDictionary * _Nullable info) {
        NSString *string = @"0M";
        CGFloat imageSize = 0.0;
        if (!imageData) {
            handler(imageSize, string);
            return ;
        }
        imageSize = imageData.length;
        if (imageSize > 1024 * 1024) {
            CGFloat size = imageSize/(1024*1024);
            string = [NSString stringWithFormat:@"%0.1fM", size];
            handler(imageSize, string);
        } else {
            CGFloat size = imageSize/1024;
            string = [NSString stringWithFormat:@"%0.1fK", size];
            handler(imageSize, string);
        }
    }];
}

+ (NSString *)limitSizeString {
    NSString *string = @"0M";
    CGFloat imageSize = [PhotoModel shared].GifSizeLimit;
    if (imageSize > 1024.0*1024.0) {
        CGFloat size = imageSize/(1024*1024);
        string = [NSString stringWithFormat:@"%0.1fM", size];
    } else {
        CGFloat size = imageSize/1024;
        string = [NSString stringWithFormat:@"%0.1fK", size];
    }
    return string;
}

+ (void)gifCanuse:(PHAsset *)asset resultHandler:(void (^)(BOOL))handler {
    [[PHImageManager defaultManager] requestImageDataForAsset:asset options:nil resultHandler:^(NSData * _Nullable imageData, NSString * _Nullable dataUTI, UIImageOrientation orientation, NSDictionary * _Nullable info) {
        if ([dataUTI isEqualToString:(NSString *)kUTTypeGIF]) {
            if (imageData) {
                handler(imageData.length < [PhotoModel shared].GifSizeLimit);
            } else {
                handler(YES);
            }
        } else {
            handler(YES);
        }
    }];
}

+ (NSArray *)fetchImageAssetsViaCollectionResults:(PHFetchResult *)results {
    NSMutableArray *resultsArr = [[NSMutableArray alloc] init];
    if (!results) {
        return resultsArr;
    }
    [results enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        PHAsset *asset = (PHAsset *)obj;
        if ([PhotoModel shared].SelectGIF == GIFTYPEAll) {
            [resultsArr addObject:asset];
        } else if ([PhotoModel shared].SelectGIF == GIFTYPEUse) {
            if (asset.isGIF) {
                [resultsArr addObject:asset];
            }
        } else {
            if (!asset.isGIF) {
                [resultsArr addObject:asset];
            }
        }
    }];
    return resultsArr;
}

@end

@implementation PHAsset (ImageType)

- (BOOL)isGIF {
    NSString *fileName =  [self valueForKey:@"filename"];
    if (fileName) {
        if ([fileName hasSuffix:@"GIF"]) {
            return YES;
        }
    }
    return NO;
}

@end



#import <Foundation/Foundation.h>
#import <Photos/Photos.h>

NS_ASSUME_NONNULL_BEGIN

@interface SXAlbum : NSObject

@property (nonatomic, strong) PHFetchResult *results;

@property (nonatomic, assign) NSInteger count;

@property (nonatomic, copy) NSString *name;

@property (nonatomic, strong) NSDate *startDate;

@property (nonatomic, copy) NSString *identifier;

@end

NS_ASSUME_NONNULL_END

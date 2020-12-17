
#import <UIKit/UIKit.h>
#import "SXUtil.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, DownloadProgressStyle) {
    DownloadProgressBlue,
    DownloadProgressYello
};

@interface DownloadProgress : UIView

@property (nonatomic, assign) CGFloat progress;

- (instancetype)initWithFrame:(CGRect)frame style:(DownloadProgressStyle)s;

@end


@interface DownloadAlert : UIView

@property (nonatomic, assign) CGFloat progress;

@property (nonatomic, copy) NSString *title;

@property (nonatomic, copy) NSString *subTitle;

@end

@interface CircleProgressAlert : UIView

@property (nonatomic, copy) NSString *title;

@end

@interface CirClrProgressView : UIView

@end

NS_ASSUME_NONNULL_END

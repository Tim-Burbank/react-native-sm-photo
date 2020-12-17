

#import <UIKit/UIKit.h>
#import "SXAlbum.h"

NS_ASSUME_NONNULL_BEGIN

@interface SXImageFlowViewController : UIViewController

- (instancetype)initWithAlbum:(SXAlbum *)album;

- (instancetype)initWithIdentifier:(NSString *)identifier;

+ (void)showAlert:(UIViewController *)viewController defaultHandler:(void (^)(void))hanlder;

@end

NS_ASSUME_NONNULL_END

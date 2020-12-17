
#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, PPNavigationBarPosition) {
    PPNavigationBarPositionLeft,
    PPNavigationBarPositionRight
};

NS_ASSUME_NONNULL_BEGIN

@interface UIViewController (PPNavigationBarPosition)

- (void)createBarButtonItemAtPosition:(PPNavigationBarPosition)position normalImage:(UIImage *)normalImage highlightImage:(UIImage *)highlightImage action:(SEL)action;

- (void)createBarButtonItemAtPosition:(PPNavigationBarPosition)position text:(NSString *)title action:(SEL)action;

- (void)createBackBarButtonItemStatusNormalImage:(UIImage *)normalImage highlightImage:(UIImage *)highlightImage backTitle:(NSString *)backTitle action:(SEL)action;


@end

NS_ASSUME_NONNULL_END

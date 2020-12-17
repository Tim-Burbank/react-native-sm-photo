
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol LineTimerDelegate <NSObject>

- (void)timeout;

@end

@interface LineTimer : UIView

@property (nonatomic, weak) id<LineTimerDelegate> delegate;

- (void)start:(NSTimeInterval)duration;

- (NSTimeInterval)stop;

@end

NS_ASSUME_NONNULL_END



#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface SXSendButton : UIView

@property (nonatomic, copy) NSString *badgeValue;

- (void)addTarget:(id)target action:(SEL)selector;

@end

NS_ASSUME_NONNULL_END

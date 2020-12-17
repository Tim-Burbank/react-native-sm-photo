
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol RecordButtonDelegate <NSObject>

- (void)start;

- (void)stop:(BOOL)cancel;

@end

@interface RecordButton : UIButton

@property (nonatomic, weak) id<RecordButtonDelegate> delegate;

@end

NS_ASSUME_NONNULL_END


#import "LineTimer.h"
#import "SXUtil.h"

@interface LineTimer ()

@property (nonatomic, strong) NSTimer *timer;

@property (nonatomic, assign) NSTimeInterval startTime;

@property (nonatomic, assign) NSTimeInterval duration;

@property (nonatomic, strong) UIView *bar;

@end

@implementation LineTimer

- (void)start:(NSTimeInterval)duration {
    _duration = duration;
    _startTime = [[NSDate alloc] init].timeIntervalSince1970;
    _timer = [NSTimer scheduledTimerWithTimeInterval:0.1 target:self selector:@selector(trick) userInfo:nil repeats:YES];
}

- (void)trick {
    NSTimeInterval now = [[NSDate alloc] init].timeIntervalSince1970;
    NSTimeInterval diff = now - _startTime;
    if (diff > _duration) {
        self.bar.hidden = YES;
        [_timer invalidate];
        _timer = nil;
        [self.delegate timeout];
    } else {
        self.bar.hidden = NO;
        NSTimeInterval per = diff / _duration;
        self.bar.frame = CGRectMake(0, 0, self.frame.size.width-self.frame.size.width*per, self.frame.size.height);
        self.bar.center = CGPointMake(self.frame.size.width/2, 0);
    }
}

- (NSTimeInterval)stop {
    self.bar.hidden = YES;
    [_timer invalidate];
    _timer = nil;
    NSTimeInterval now = [[NSDate alloc] init].timeIntervalSince1970;
    return now - _startTime;
}

- (UIView *)bar {
    if (!_bar) {
        _bar = [[UIView alloc] init];
        _bar.backgroundColor = [SXUtil colorWithKey:LineTimerBarColor];
        [self addSubview:_bar];
    }
    return _bar;
}

@end


#import "RecordButton.h"

@interface RecordButton ()

@property (nonatomic, assign) BOOL recording;

@end

@implementation RecordButton


- (void)setRecording:(BOOL)recording {
    _recording = recording;
    if (_recording) {
        self.alpha = 0.5;
        [self.delegate start];
    } else {
        self.alpha = 1;
    }
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    self.recording = true;
}

- (void)touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    if (_recording) {
        self.recording = NO;
        [self.delegate stop:NO];
    }
}

- (void)touchesMoved:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    if (_recording) {
        UITouch *t = [touches.allObjects firstObject];
        if (t) {
            CGPoint p = [t locationInView:self];
            CGRect f = self.frame;
            f.origin = CGPointZero;
            if (!CGRectContainsPoint(f, p)) {
                self.recording = NO;
                [self.delegate stop:YES];
            }
        }
    }
}

- (void)touchesCancelled:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    if (_recording) {
        self.recording = NO;
        [self.delegate stop:YES];
    }
}

@end

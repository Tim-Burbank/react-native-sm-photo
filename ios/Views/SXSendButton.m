
#import "SXSendButton.h"
#import "UIColor+StringColor.h"
#import "SXUtil.h"
#import "UIView+Frame.h"

@interface SXSendButton () {
    CGSize commonSize;
    UIFont *sendButtonFont;
}

@property (nonatomic, strong) UILabel *badgeValueLable;

@property (nonatomic, strong) UIView *backgroundView;

@property (nonatomic, strong) UIButton *sendButton;

@end

@implementation SXSendButton

CGFloat sendButtonTextWitdh = 38.0;

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        sendButtonFont = [UIFont systemFontOfSize:16.0];
        self.frame = CGRectMake(0, 0, 56, 26);
        [self setupViews];
    }
    return self;
}

- (void)setupViews {
    [self addSubview:self.backgroundView];
    [self addSubview:self.badgeValueLable];
    [self addSubview:self.sendButton];
    self.badgeValue = @"0";
}

- (void)addTarget:(id)target action:(SEL)selector {
    [self.sendButton addTarget:target action:selector forControlEvents:UIControlEventTouchUpInside];
}

- (void)setBadgeValue:(NSString *)badgeValue {
    // _badgeValue = badgeValue;
    // CGRect rect = [_badgeValue boundingRectWithSize:CGSizeMake(100, 20) options:NSStringDrawingTruncatesLastVisibleLine attributes:@{NSFontAttributeName: sendButtonFont} context:nil];
    // CGFloat labelWidth = (rect.size.width+9) > 20 ? (rect.size.width+9) : 20;
    // self.badgeValueLable.width = labelWidth;
    // self.badgeValueLable.height = 20;
    // self.sendButton.width = self.badgeValueLable.width + sendButtonTextWitdh;
    // self.width = self.sendButton.width;
    // self.badgeValueLable.text = badgeValue;
    _badgeValue = badgeValue;
    BOOL isDoubleGigit = [badgeValue integerValue] >= 10;
    CGFloat height = 20;
    UIFont *textFont = isDoubleGigit ? [UIFont systemFontOfSize:14.0] : sendButtonFont;
    CGRect rect = [_badgeValue boundingRectWithSize:CGSizeMake(100, height) options:NSStringDrawingTruncatesLastVisibleLine attributes:@{NSFontAttributeName: textFont} context:nil];
    CGFloat labelWidth = (rect.size.width+9) > height ? (rect.size.width+9) : height;
    self.badgeValueLable.font = textFont;
    self.badgeValueLable.width = labelWidth;
    self.badgeValueLable.height = height;
    self.backgroundView.width = labelWidth;
    self.backgroundView.height = height;
    self.sendButton.width = self.badgeValueLable.width + sendButtonTextWitdh;
    self.width = self.sendButton.width;
    self.badgeValueLable.text = badgeValue;
    _backgroundView.layer.masksToBounds = YES;
    _backgroundView.layer.cornerRadius = height/2;
    
    if ([badgeValue integerValue] > 0) {
        [self showBadageValue];
        self.backgroundView.transform = CGAffineTransformMakeScale(0, 0);
        __weak typeof(self) weakSelf = self;
        [UIView animateWithDuration:0.2 animations:^{
            weakSelf.backgroundView.transform = CGAffineTransformMakeScale(1.1, 1.1);
        } completion:^(BOOL finished) {
            weakSelf.backgroundView.transform = CGAffineTransformMakeScale(1.0, 1.0);
        }];
    } else {
        [self hideBadgeValue];
    }
    
}

- (void)showBadageValue {
    self.badgeValueLable.hidden = NO;
    self.backgroundView.hidden = NO;
    self.sendButton.enabled = YES;
}

- (void)hideBadgeValue {
    self.badgeValueLable.hidden = YES;
    self.backgroundView.hidden = YES;
    self.sendButton.enabled = NO;
}

#pragma mark - lazy
- (UILabel *)badgeValueLable {
    if (!_badgeValueLable) {
        _badgeValueLable = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 20, 20)];
        _badgeValueLable.center = CGPointMake(_badgeValueLable.center.x, self.center.y);
        _badgeValueLable.backgroundColor = [UIColor clearColor];
        _badgeValueLable.textColor = [UIColor blackColor];
        _badgeValueLable.font = sendButtonFont;
        _badgeValueLable.textAlignment = NSTextAlignmentCenter;
    }
    return _badgeValueLable;
}

- (UIView *)backgroundView {
    if (!_backgroundView) {
        _backgroundView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 20, 20)];
        _backgroundView.center = CGPointMake(_backgroundView.center.x, self.center.y);
        _backgroundView.backgroundColor = [SXUtil colorWithKey:SendButtonTintNormalColor];
        _backgroundView.layer.masksToBounds = YES;
        _backgroundView.layer.cornerRadius = _backgroundView.bounds.size.width/2;
        
    }
    return _backgroundView;
}

- (UIButton *)sendButton {
    
    if (!_sendButton) {
        _sendButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _sendButton.frame = self.bounds;
        [_sendButton setTitle:[SXUtil SXLocalizedString:@"send" comment:@"发送"] forState:UIControlStateNormal];
        [_sendButton setTitleColor:[SXUtil colorWithKey:SendButtonTintNormalColor] forState:UIControlStateNormal];
        [_sendButton setTitleColor:[SXUtil colorWithKey:SendButtonTintHighlightedColor] forState:UIControlStateHighlighted];
        [_sendButton setTitleColor:[SXUtil colorWithKey:SendButtonTintDisabledColor] forState:UIControlStateDisabled];
        _sendButton.titleLabel.font = sendButtonFont;
        _sendButton.contentEdgeInsets = UIEdgeInsetsMake(0, 20, 0, 0);
        _sendButton.backgroundColor = [UIColor clearColor];
    }
    return _sendButton;
}

@end



#import "DownloadProgress.h"
#import "SXUtil.h"

@interface DownloadProgress () {
    DownloadProgressStyle style;
    
    CGFloat KProgressBorderWidth;
    
    CGFloat KProgressPadding;
    
    CGFloat margin;
    
    CGFloat maxWidth;
    
    CGFloat maxHeight;
}

@property (nonatomic, strong) UIColor *backColor;

@property (nonatomic, strong) UIView *pView;

@property (nonatomic, strong) CAShapeLayer *maskLayer;

@end

@implementation DownloadProgress

- (void)setProgress:(CGFloat)progress  {
    _progress = progress;
    if (style == DownloadProgressBlue) {
        _pView.frame = CGRectMake(margin, margin, maxWidth*progress, maxHeight);
    } else {
        _maskLayer.frame = CGRectMake(0, 0, self.bounds.size.width*progress, self.bounds.size.height);
    }
}


- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame style:(DownloadProgressStyle)s {
    self = [super initWithFrame:frame];
    if (self) {
        style = s;
        _backColor = [UIColor colorWithRed:0 green:191.0/255.0 blue:1 alpha:1];
        _pView = [[UIView alloc] init];
        KProgressPadding = 1.0;
        KProgressBorderWidth = 2.0;
        margin = 3.0;
        maxWidth = 0.0;
        maxHeight = 0.0;
        if (style == DownloadProgressBlue) {
            margin = KProgressBorderWidth + KProgressPadding;
            maxWidth = self.bounds.size.width - margin * 2.0;
            maxHeight = self.bounds.size.height -margin * 2.0;
            UIView *border = [[UIView alloc] initWithFrame:self.bounds];
            border.layer.cornerRadius = self.bounds.size.height * 0.5;
            border.layer.masksToBounds = YES;
            border.backgroundColor = [UIColor whiteColor];
            border.layer.backgroundColor = _backColor.CGColor;
            border.layer.borderWidth = 2;
            [self addSubview:border];
            _pView.backgroundColor = _backColor;
            _pView.layer.cornerRadius = (self.bounds.size.height - (KProgressBorderWidth + KProgressPadding) * 2.0) * 0.5;
            _pView.layer.masksToBounds = YES;
            [self addSubview:_pView];
        } else {
            _pView.frame = self.bounds;
            _pView.backgroundColor = [UIColor clearColor];
            CGFloat cornerRadius = _pView.bounds.size.height * 0.5;
            CAGradientLayer *gradientLayer = [[CAGradientLayer alloc] init];
            gradientLayer.frame = self.bounds;
            gradientLayer.colors = @[(__bridge id)[SXUtil colorWithKey:DownloadProgressStartColor].CGColor, (__bridge id)[SXUtil colorWithKey:DownloadProgressEndColor].CGColor];
            gradientLayer.startPoint = CGPointMake(0, 0);
            gradientLayer.endPoint = CGPointMake(1, 0);
            gradientLayer.cornerRadius = cornerRadius;
            gradientLayer.masksToBounds = YES;
            [_pView.layer addSublayer:gradientLayer];
            
            _maskLayer = [[CAShapeLayer alloc] init];
            _maskLayer.frame = CGRectMake(0, 0, 0, _pView.bounds.size.height);
            _maskLayer.borderWidth = cornerRadius;
            _maskLayer.cornerRadius = cornerRadius;
            _maskLayer.masksToBounds = YES;
            gradientLayer.mask = _maskLayer;
            [self addSubview:_pView];
        }
    }
    return self;
}

@end


@interface DownloadAlert () {
    CGFloat scale;
}

@property (nonatomic, strong) DownloadProgress *progressView;

@property (nonatomic, strong) UILabel *subLabel;

@property (nonatomic, strong) UILabel *titleLabel;


@end

@implementation DownloadAlert

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        scale = [UIScreen mainScreen].bounds.size.width / 375.0;
        UIBlurEffect *effect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
        UIVisualEffectView *effectView = [[UIVisualEffectView alloc] initWithEffect:effect];
        effectView.frame = self.bounds;
        [self addSubview:effectView];
        
        UIView *backView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width-48*scale, 140*scale)];
        backView.center = self.center;
        backView.backgroundColor = [UIColor blackColor];
        backView.layer.cornerRadius = 4;
        backView.clipsToBounds = YES;
        [self addSubview:backView];
        
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 25*scale, backView.bounds.size.width, [UIFont boldSystemFontOfSize:20.0].lineHeight)];
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        _titleLabel.text = [SXUtil SXLocalizedString:@"iCloudPhoto" comment:@""];
        _titleLabel.textColor = [SXUtil colorWithKey:DownloadProgressTitleColor];
        _titleLabel.font = [UIFont boldSystemFontOfSize:20];
        [backView addSubview:_titleLabel];
        
        _progressView = [[DownloadProgress alloc] initWithFrame: CGRectMake(40*scale, 25*scale+CGRectGetMaxY(_titleLabel.frame), backView.bounds.size.width-80*scale, 6) style:DownloadProgressYello];
        [backView addSubview:_progressView];
        
        _subLabel = [[UILabel alloc] initWithFrame: CGRectMake(CGRectGetMinX(_progressView.frame), 12*scale+CGRectGetMaxY(_progressView.frame), _progressView.bounds.size.width, [UIFont systemFontOfSize:12].lineHeight)];
        _subLabel.font = [UIFont systemFontOfSize:12];
        _subLabel.textColor = [SXUtil colorWithKey:DownloadProgressTitleColor];
        _subLabel.textAlignment = NSTextAlignmentCenter;
        [backView addSubview:_subLabel];
        
    }
    return self;
}

- (void)setProgress:(CGFloat)progress {
    _progress = progress;
    dispatch_async(dispatch_get_main_queue(), ^{
        self.progressView.progress = self->_progress;
    });
}

- (void)setTitle:(NSString *)title {
    _title = [title copy];
    dispatch_async(dispatch_get_main_queue(), ^{
        self.titleLabel.text = self->_title;
    });
}

- (void)setSubTitle:(NSString *)subTitle {
    if (!subTitle) {
        return ;
    }
    _subTitle = subTitle;
    dispatch_async(dispatch_get_main_queue(), ^{
        self.subLabel.text = subTitle;
    });
}

@end

@interface CircleProgressAlert ()

@property (nonatomic, strong) UILabel *titleLabel;

@end

@implementation CircleProgressAlert

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    
    if (self) {
        CGFloat scale = [UIScreen mainScreen].bounds.size.width/375.0;
        UIBlurEffect *effect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
        UIVisualEffectView *effectView = [[UIVisualEffectView alloc] initWithEffect:effect];
        effectView.frame = self.bounds;
        [self addSubview:effectView];
        
        CGFloat bw = 165.0 * scale;
        UIView *backView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, bw, bw)];
        backView.center = self.center;
        backView.backgroundColor = [UIColor blackColor];
        backView.layer.cornerRadius = 4;
        backView.clipsToBounds = YES;
        [self addSubview:backView];
        
        CGFloat w = 50 * scale;
        CirClrProgressView *progressView = [[CirClrProgressView alloc] initWithFrame: CGRectMake((bw-w)*0.5, 35*scale, w, w)];
        [backView addSubview:progressView];
        
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, CGRectGetMaxY(progressView.frame)+25*scale, backView.bounds.size.width, [UIFont boldSystemFontOfSize: 20].lineHeight)];
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        _titleLabel.text = [SXUtil SXLocalizedString:@"Video processing" comment:@""];
        _titleLabel.textColor = [SXUtil colorWithKey:DownloadProgressTitleColor];
        _titleLabel.font = [UIFont boldSystemFontOfSize:20];
        [backView addSubview:_titleLabel];
    }
    return self;
}

- (void)setTitle:(NSString *)title {
    _title = title;
    dispatch_async(dispatch_get_main_queue(), ^{
        self.titleLabel.text = self->_title;
    });
}

@end


@implementation CirClrProgressView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        self.backgroundColor = [UIColor blackColor];
        CGFloat width = self.bounds.size.width;
        CGFloat height = self.bounds.size.height;
        
        CAShapeLayer *trackLayer = [[CAShapeLayer alloc] init];
        trackLayer.frame = self.bounds;
        trackLayer.fillColor = nil;
        trackLayer.strokeColor = [SXUtil colorWithKey:DownloadProgressTitleColor].CGColor;
        trackLayer.lineWidth = 5;
        trackLayer.lineCap = kCALineCapRound;
        
        UIBezierPath *path = [UIBezierPath bezierPathWithArcCenter:CGPointMake(width*0.5, height*0.5) radius:self.bounds.size.width*0.5-2.5 startAngle:-M_PI*0.5+0.1 endAngle:M_PI*0.5 clockwise:YES];
        trackLayer.path = path.CGPath;
        [self.layer addSublayer:trackLayer];
        
        CALayer *gradientlayer = [[CALayer alloc] init];
        gradientlayer.frame = self.bounds;
        [self.layer addSublayer:gradientlayer];
        
        CAGradientLayer *gl1 = [[CAGradientLayer alloc] init];
        gl1.frame = CGRectMake(width*0.5, 0, width*0.5, height);
        gl1.colors = @[(__bridge id)[SXUtil colorWithKey:DownloadProgressTitleColor].CGColor, (__bridge id)[UIColor blackColor].CGColor];
        gl1.startPoint = CGPointMake(0, 0);
        gl1.endPoint = CGPointMake(0, 1);
        [gradientlayer addSublayer:gl1];
        
        CAGradientLayer *gl2 = [[CAGradientLayer alloc] init];
        gl2.frame = CGRectMake(0, 0, width*0.5, height);
        gl2.colors = @[(__bridge id)[UIColor blackColor].CGColor];
        gl2.startPoint = CGPointMake(0, 0);
        gl2.endPoint = CGPointMake(0, 0);
        [gradientlayer addSublayer:gl2];
        gradientlayer.mask = trackLayer;
        
        CABasicAnimation *animation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
        animation.duration = 1;
        animation.repeatCount = 10000.0;
        animation.fromValue = @(2.0 * M_PI);
        animation.toValue = @0.0;
        [self.layer addAnimation:animation forKey:@"Double"];
        
    }
    return self;
}

@end

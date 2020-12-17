
#import "TakePhotoPreviewViewController.h"
#import "SXUtil.h"
#import "RecordButton.h"
#import "UIImage+Operation.h"

@interface TakePhotoPreviewViewController ()

@property (nonatomic, strong) UIButton *backButton;

@property (nonatomic, strong) UIButton *doneButton;

@property (nonatomic, strong) RecordButton *recordButton;

@end

@implementation TakePhotoPreviewViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    UIImageView *imageView = [[UIImageView alloc] initWithImage:_image];
    [self.view addSubview:imageView];
    imageView.contentMode = UIViewContentModeScaleAspectFit;
    imageView.frame = self.view.bounds;
    
    [self.view addSubview:self.backButton];
    [self.backButton addTarget:self action:@selector(cancelTapped) forControlEvents:UIControlEventTouchUpInside];
    
    self.view.backgroundColor = [UIColor blackColor];
    
    [self.view addSubview:self.doneButton];
    [self.doneButton addTarget:self action:@selector(done) forControlEvents:UIControlEventTouchUpInside];
    UILabel *label = [[UILabel alloc] init];
    label.text = [SXUtil SXLocalizedString:@"preview" comment:@"预览"];
    label.textColor = [UIColor whiteColor];
    label.font = [UIFont systemFontOfSize:18.0];
    [self.view addSubview:label];
    label.frame = CGRectMake(100, UIApplication.sharedApplication.statusBarFrame.size.height, self.view.frame.size.width-200, 44);
    label.textAlignment = NSTextAlignmentCenter;
}

- (void)done {
    [self.delegate confirmPhoto:_image];
}

- (void)cancelTapped {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:[SXUtil SXLocalizedString:@"discard" comment:@""] message:[SXUtil SXLocalizedString:@"sureReturn" comment:@""] preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *backAction = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"back" comment:@"返回"] style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self.navigationController popViewControllerAnimated:YES];
    }];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:[SXUtil SXLocalizedString:@"cancel" comment:@"取消"] style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
    }];
    [alert addAction:backAction];
    [alert addAction:cancelAction];
    [self presentViewController:alert animated:YES completion:nil];
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

#pragma mark - lazy

- (UIButton *)backButton {
    if (!_backButton) {
        _backButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_backButton setImage:[UIImage imageBundleNamed:back_white_arrow] forState:UIControlStateNormal];
        _backButton.frame = CGRectMake(0, UIApplication.sharedApplication.statusBarFrame.size.height, 44, 44);
        
    }
    return _backButton;
}

- (UIButton *)doneButton {
    if (!_doneButton) {
        _doneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_doneButton setTitle:[SXUtil SXLocalizedString:@"send" comment:@"发送"] forState:UIControlStateNormal];
        [_doneButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _doneButton.frame = CGRectMake(self.view.frame.size.width-60, UIApplication.sharedApplication.statusBarFrame.size.height, 44, 44);
    }
    return _doneButton;
}

- (RecordButton *)recordButton {
    if (!_recordButton) {
        _recordButton = [RecordButton buttonWithType:UIButtonTypeCustom];
        _recordButton.frame = CGRectMake(0, 0, 70, 70);
        _recordButton.layer.borderWidth = 2;
        _recordButton.layer.borderColor = [UIColor colorWithRed:214/255.0 green:180/255.0 blue:53/255.0 alpha:1].CGColor;
        _recordButton.layer.cornerRadius = 35;
    }
    return _recordButton;
}

@end

//
//  svphelper.h
//  PhotoPlugin
//
//  Created by stoprain on 2018/4/18.
//  Copyright © 2018 yunio. All rights reserved.
//

@import UIKit;

@interface Hyphenate: NSObject
+ (void)showsvp:(UIView *)target status:(NSString *)status;
+ (void)hidesvp;
+ (UIViewController *)getRootViewController;
@end

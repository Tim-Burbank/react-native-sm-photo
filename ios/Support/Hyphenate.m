//
//  Hyphenate.m
//  PhotoPlugin
//
//  Created by stoprain on 2018/4/18.
//  Copyright © 2018 yunio. All rights reserved.
//

#import "Hyphenate.h"
#include <objc/message.h>

@implementation Hyphenate

+ (void)showsvp:(UIView *)target status:(NSString *)status {
    Class clazz = NSClassFromString(@"SVProgressHUD");
    SEL selector1 = NSSelectorFromString(@"setContainerView:");
    SEL selector2 = NSSelectorFromString(@"showWithStatus:");
    Class clazz1 = NSClassFromString(@"AppTool");
    SEL selector3 = NSSelectorFromString(@"setSVPWithBlackType");
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
    [clazz performSelector:selector1 withObject:target];
    [clazz performSelector:selector2 withObject:status];
    [clazz1 performSelector:selector3];
#pragma clang diagnostic pop
}
+ (void)hidesvp {
    Class clazz = NSClassFromString(@"SVProgressHUD");
    SEL selector1 = NSSelectorFromString(@"dismiss");
    SEL selector2 = NSSelectorFromString(@"setContainerView:");
    Class clazz1 = NSClassFromString(@"AppTool");
    SEL selector3 = NSSelectorFromString(@"setSVPWithDefaultType");
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
    [clazz performSelector:selector1];
    [clazz performSelector:selector2 withObject:nil];
    [clazz1 performSelector:selector3];
#pragma clang diagnostic pop
}
+ (UIViewController *)getRootViewController {
    UIViewController * result = [[UIViewController alloc] init];
    
    UIWindow * window = [UIApplication sharedApplication].keyWindow;
    if (window.windowLevel != UIWindowLevelNormal) {
        NSArray * windows = [UIApplication sharedApplication].windows;
        for (UIWindow * temp in windows) {
            if (temp.windowLevel == UIWindowLevelNormal) {
                window = temp;
                break;
            }
        }
    }
    
    UIViewController * appRootVC = window.rootViewController;
    if (appRootVC) {
        UIView * frontView = window.subviews.firstObject;
        if (frontView) {
            id nextResponder = frontView.nextResponder;
            if ([appRootVC presentedViewController]) {
                nextResponder = appRootVC.presentedViewController;
            }
            if ([nextResponder isKindOfClass:[UITabBarController class]]) {
                UITabBarController * tabbar = (UITabBarController *)nextResponder;
                UINavigationController * nav = (UINavigationController *)tabbar.viewControllers[tabbar.selectedIndex];
                result = nav.childViewControllers.lastObject;
            }else if ([nextResponder isKindOfClass:[UINavigationController class]]) {
                UINavigationController * nav = (UINavigationController *)nextResponder;
                result = nav.childViewControllers.lastObject;
            }else {
                if (![nextResponder isKindOfClass:[UIView class]]) {
                    result = nextResponder;
                } else {
                    result = appRootVC;
                }
            }
        }
    }
    return result;
}
@end

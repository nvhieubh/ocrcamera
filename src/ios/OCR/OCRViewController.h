//

#import <UIKit/UIKit.h>
#import "ItemScan.h"

@protocol OCRViewControllerDelegate;
@interface OCRViewController : UIViewController
@property (nonatomic, strong) UIImage *image;
@property (weak, nonatomic) IBOutlet ItemScan *merchantItemScan;
@property (weak, nonatomic) IBOutlet ItemScan *amountItemScan;
@property (weak, nonatomic) IBOutlet ItemScan *dateItemScan;
@property (weak, nonatomic) IBOutlet UIButton *doneButton;
@property (weak, nonatomic) NSObject<OCRViewControllerDelegate> *delegate;
@end
@protocol OCRViewControllerDelegate
- (void)completeWithImage:(UIImage *)img dictionary:(NSDictionary *)dic;
@end

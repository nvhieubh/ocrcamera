
#import "OCRViewController.h"
#import <TOCropViewController/UIImage+CropRotate.h>
@import TOCropViewController;
@import GoogleMobileVision;

@interface OCRViewController ()<ItemScanDelegate>{
    NSMutableArray *listItemScan;
    TOCropView *cropView;
    CGRect lastFrame;
}
@property (nonatomic, weak) IBOutlet UIView *imageCroper;
@property (nonatomic, strong) GMVDetector *textDetector;
@end

@implementation OCRViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setupCropView];
    self.textDetector = [GMVDetector detectorOfType:GMVDetectorTypeText options:nil];
    // Do any additional setup after loading the view, typically from a nib.
    [self.merchantItemScan setType:TypeItemScanMerchant];
    [self.amountItemScan setType:TypeItemScanAmount];
    [self.dateItemScan setType:TypeItemScanDate];
    
    listItemScan = [[NSMutableArray alloc] init];
    [listItemScan addObject:self.merchantItemScan];
    [listItemScan addObject:self.amountItemScan];
    [listItemScan addObject:self.dateItemScan];
    
    for (ItemScan *item in listItemScan) {
        item.delegate = self;
    }
    [self.doneButton setImage:[UIImage imageNamed:@"icon_expense-1.svg"] forState:UIControlStateNormal];
    
}
- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [cropView performInitialSetup];
    self.navigationController.navigationBar.hidden = YES;
}
- (void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    [cropView moveCroppedContentToCenterAnimated:YES];
    [cropView layoutIfNeeded];
    [self touchEndMoving];
}
- (void)setupCropView{
    //    [self.imageCroper setImage:self.image];
    //    self.imageCroper.delegate = self;
    //    [self.imageCroper setCropRegionRect:CGRectMake(14, 120, 127, 77)];
    //    [self.imageCroper setHiddenCropView:YES];
    cropView = [[TOCropView alloc] initWithCroppingStyle:TOCropViewCroppingStyleDefault image:self.image];
    
    
    CGRect frame = _imageCroper.frame;
    frame.origin.x = 0;
    frame.origin.y = 0;
    [cropView setFrame:frame];
    
    cropView.simpleRenderMode = YES;
    cropView.internalLayoutDisabled = YES;
    [_imageCroper addSubview:cropView];
    
    NSLayoutConstraint *leadingConst = [NSLayoutConstraint
                                        constraintWithItem:cropView
                                        attribute:NSLayoutAttributeLeading
                                        relatedBy:NSLayoutRelationEqual
                                        toItem:_imageCroper
                                        attribute:NSLayoutAttributeLeading
                                        multiplier:1.0
                                        constant:0];
    NSLayoutConstraint *trailingConst = [NSLayoutConstraint
                                         constraintWithItem:cropView
                                         attribute:NSLayoutAttributeTrailing
                                         relatedBy:NSLayoutRelationEqual
                                         toItem:_imageCroper
                                         attribute:NSLayoutAttributeTrailing
                                         multiplier:1.0
                                         constant:0];
    NSLayoutConstraint *topConst = [NSLayoutConstraint
                                    constraintWithItem:cropView
                                    attribute:NSLayoutAttributeTop
                                    relatedBy:NSLayoutRelationEqual
                                    toItem:_imageCroper
                                    attribute:NSLayoutAttributeTop
                                    multiplier:1.0
                                    constant:0];
    NSLayoutConstraint *bottomConst = [NSLayoutConstraint
                                       constraintWithItem:cropView
                                       attribute:NSLayoutAttributeBottom
                                       relatedBy:NSLayoutRelationEqual
                                       toItem:_imageCroper
                                       attribute:NSLayoutAttributeBottom
                                       multiplier:1.0
                                       constant:0];
    [_imageCroper addConstraints:@[bottomConst,topConst,trailingConst,leadingConst]];
    
}

- (IBAction)touchToScan:(id)sender {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    for (ItemScan *item in listItemScan) {
        [dic addEntriesFromDictionary:[item data]];
    }
    [self.delegate completeWithImage:self.image dictionary:dic];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (void)touchEndMoving{
    [self performSelector:@selector(touchEndMoving) withObject:nil afterDelay:0.5];
    
    if (![NSStringFromCGRect(lastFrame) isEqualToString:NSStringFromCGRect(cropView.imageCropFrame)]) {
        lastFrame = cropView.imageCropFrame;
        NSString *textString = @"";
        UIImage *image = [self.image croppedImageWithFrame:cropView.imageCropFrame angle:cropView.angle circularClip:NO];
        NSArray<GMVTextBlockFeature *> *features = [self.textDetector featuresInImage:image options:nil];
        for (GMVTextBlockFeature *textBlock in features) {
            NSLog(@"Text Block: %@", NSStringFromCGRect(textBlock.bounds));
            NSLog(@"lang: %@ value: %@", textBlock.language, textBlock.value);
            if ([textString isEqualToString:@""]) {
                textString = textBlock.value;
            }else{
                textString = [NSString stringWithFormat:@"%@ %@",textString,textBlock.value];
            }
            
            //        // For each text block, iterate over each line.
            //        for (GMVTextLineFeature *textLine in textBlock.lines) {
            //            NSLog(@"Text Line: %@", NSStringFromCGRect(textLine.bounds));
            //            NSLog(@"lang: %@ value: %@", textLine.language, textLine.value);
            //
            //        }
        }
        [self updateTextToSelectedFeild:textString];
    }
    
    
}

- (void)updateTextToSelectedFeild:(NSString *)text{
    for (ItemScan *item in listItemScan) {
        if (item.isSelected) {
            [item setValueScaned:text];
        }
    }
}

- (void)didTouchToSelected:(ItemScan *)itemScan{
    for (ItemScan *item in listItemScan) {
        if (![item isEqual:itemScan]) {
            [item setSelected:NO];
        }
    }
    //    [self.imageCroper setHiddenCropView:NO];
}
- (void)didTouchToDeSelect:(ItemScan *)itemScan{
    
    for (ItemScan *item in listItemScan) {
        if (![item isEqual:itemScan]) {
            //            [item setSelected:YES];
        }
    }
    //    [self.imageCroper setHiddenCropView:YES];
}

@end

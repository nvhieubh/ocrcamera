/********* ocrcamera.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "OCRViewController.h"
#import "NSData+Base64.h"
@interface ocrcamera : CDVPlugin<OCRViewControllerDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate> {
  // Member variables go here.
   NSString *commandID;
     UIViewController *presentVC;
}

- (void)coolMethod:(CDVInvokedUrlCommand*)command;
@end

@implementation ocrcamera

- (void)openCameraOCR:(CDVInvokedUrlCommand*)command
{
    if (!commandID || [commandID isEqualToString:@""]) {
        commandID = command.callbackId;
        [self openCamera];
    }
    
    
}

// Implement
- (void)openCamera{
    UIImagePickerController *standardPicker = [[UIImagePickerController alloc] init];
    standardPicker.sourceType = UIImagePickerControllerSourceTypeCamera;
    standardPicker.allowsEditing = NO;
    standardPicker.delegate = self;
    [self.viewController presentViewController:standardPicker animated:YES completion:nil];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info
{
     OCRViewController *vc = [[OCRViewController alloc] initWithNibName:@"OCRViewController" bundle:nil];
     UIImage *image = info[UIImagePickerControllerOriginalImage];
     vc.image = image;
     vc.delegate = self;
     UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
    presentVC = navi;
     [picker dismissViewControllerAnimated:NO
                                completion:^{
                                    [self.viewController presentViewController:navi animated:YES completion:^{
                                       
                                    }];
                                }];
    
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
	commandID = @"";
    [picker dismissViewControllerAnimated:YES completion:nil];
}
- (void)completeWithImage:(UIImage *)img dictionary:(NSDictionary *)dic{
    if (commandID && ![commandID isEqualToString:@""]) {
        CDVPluginResult* pluginResult = nil;
        
        if (img != nil) {
            NSMutableDictionary *result = [NSMutableDictionary dictionaryWithDictionary:dic];
            UIImage *imageCompress = [self resizeImage:img percent:1];
            NSData* data = UIImageJPEGRepresentation(imageCompress, 0.4);
            NSString *img64=[data base64EncodedString];
            //NSLog(@"img64 : %@\n", img64);
            //long  lenImg =[img64 length];
            //NSLog(@"Length of img64 :  %d\n", lenImg );
            [result setObject:img64 forKey:@"image"];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }
        
        [self.commandDelegate sendPluginResult:pluginResult callbackId:commandID];
        [presentVC dismissViewControllerAnimated:YES completion:^{
            
        }];
        commandID = @"";
    }
    
}

-(UIImage *)resizeImage:(UIImage *)image percent:(CGFloat)percent
{
    
    NSInteger imageActualSize = UIImageJPEGRepresentation(image,1).length;
    
    NSLog(@"size of IMAGE before resizing: %@ ", [NSByteCountFormatter stringFromByteCount:imageActualSize countStyle:NSByteCountFormatterCountStyleFile]);
    
    float actualHeight = image.size.height;
    float actualWidth = image.size.width;
    float maxHeight = 400.0; // your custom  height
    float maxWidth = 350; // your custom  width
    float imgRatio = actualWidth/actualHeight;
    float maxRatio = maxWidth/maxHeight;
    float compressionQuality = percent;//50 percent compression
    
    if (actualHeight > maxHeight || actualWidth > maxWidth)
    {
        if(imgRatio < maxRatio)
        {
            //adjust width according to maxHeight
            imgRatio = maxHeight / actualHeight;
            actualWidth = imgRatio * actualWidth;
            actualHeight = maxHeight;
        }
        else if(imgRatio > maxRatio)
        {
            //adjust height according to maxWidth
            imgRatio = maxWidth / actualWidth;
            actualHeight = imgRatio * actualHeight;
            actualWidth = maxWidth;
        }
        else
        {
            actualHeight = maxHeight;
            actualWidth = maxWidth;
        }
    }
    
    CGRect rect = CGRectMake(0.0, 0.0, actualWidth, actualHeight);
    UIGraphicsBeginImageContext(rect.size);
    [image drawInRect:rect];
    UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
    NSData *imageData = UIImageJPEGRepresentation(img, compressionQuality);
    UIGraphicsEndImageContext();
    
    NSInteger imageReduceSize = imageData.length;
    
    NSLog(@"size of IMAGE after resizing: %@ ",[NSByteCountFormatter stringFromByteCount:imageReduceSize countStyle:NSByteCountFormatterCountStyleFile]);
    
    return [UIImage imageWithData:imageData];
    
}

@end
